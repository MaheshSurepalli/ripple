package com.example.ripple.data.remote

import com.example.ripple.domain.model.*
import com.example.ripple.domain.repository.*
import com.example.ripple.domain.usecase.CalculateStatsUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthRepository {

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val fbUser = firebaseAuth.currentUser
            if (fbUser != null) {
                firestore.collection("users").document(fbUser.uid)
                    .get()
                    .addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            val user = User(
                                id = fbUser.uid,
                                username = doc.getString("username") ?: fbUser.displayName ?: "user",
                                displayName = doc.getString("displayName") ?: fbUser.displayName ?: "User",
                                avatarUrl = doc.getString("avatarUrl") ?: fbUser.photoUrl?.toString(),
                                largestRipple = doc.getLong("largestRipple")?.toInt() ?: 0,
                                totalPeopleReached = doc.getLong("totalPeopleReached")?.toInt() ?: 0,
                                challengesCreated = doc.getLong("challengesCreated")?.toInt() ?: 0,
                                challengesCompleted = doc.getLong("challengesCompleted")?.toInt() ?: 0,
                                furthestKm = doc.getDouble("furthestKm") ?: 0.0
                            )
                            _currentUser.value = user
                        } else {
                            val newUser = User(
                                id = fbUser.uid,
                                username = "user_${fbUser.uid.take(5)}",
                                displayName = fbUser.displayName ?: "Ripple User",
                                avatarUrl = fbUser.photoUrl?.toString()
                            )
                            firestore.collection("users").document(fbUser.uid).set(newUser)
                            _currentUser.value = newUser
                        }
                    }
            } else {
                _currentUser.value = null
            }
        }
    }

    override suspend fun signInDemoUser(userId: String): Result<User> {
        // Fallback for development demo switching
        val user = User(id = userId, username = userId.removePrefix("user_"), displayName = userId.removePrefix("user_").replaceFirstChar { it.uppercase() })
        _currentUser.value = user
        return Result.success(user)
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val authResult = auth.signInAnonymously().await()
            val fbUser = authResult.user ?: throw IllegalStateException("Auth failed")
            val user = User(id = fbUser.uid, username = "google_user", displayName = "Google User")
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithPhone(phoneNumber: String, verificationCode: String): Result<User> {
        return try {
            val authResult = auth.signInAnonymously().await()
            val fbUser = authResult.user ?: throw IllegalStateException("Auth failed")
            val user = User(id = fbUser.uid, username = "user_${phoneNumber.takeLast(4)}", displayName = "User $phoneNumber")
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        auth.signOut()
        _currentUser.value = null
    }

    override suspend fun updateProfile(displayName: String, username: String, avatarUrl: String?): Result<User> {
        val current = _currentUser.value ?: return Result.failure(IllegalStateException("Not logged in"))
        val updated = current.copy(displayName = displayName, username = username, avatarUrl = avatarUrl)
        firestore.collection("users").document(current.id).set(updated, SetOptions.merge()).await()
        _currentUser.value = updated
        return Result.success(updated)
    }

    override fun getAvailableDemoUsers(): List<User> {
        return listOf(
            User("user_siva", "siva", "Siva", totalPeopleReached = 842, largestRipple = 327),
            User("user_alex", "alex", "Alex", totalPeopleReached = 310, largestRipple = 142),
            User("user_john", "john", "John", totalPeopleReached = 120, largestRipple = 89),
            User("user_sarah", "sarah", "Sarah", totalPeopleReached = 1420, largestRipple = 640),
            User("user_elena", "elena", "Elena (Tokyo)", totalPeopleReached = 480, largestRipple = 210)
        )
    }
}

class FirebaseChallengeRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val authRepo: AuthRepository,
    private val calculateStatsUseCase: CalculateStatsUseCase = CalculateStatsUseCase()
) : ChallengeRepository {

    override fun observeWaitingChallenges(userId: String): Flow<List<Pair<Challenge, InviteToken>>> = callbackFlow {
        val listener = firestore.collection("invites")
            .whereIn("status", listOf("PENDING", "OPENED"))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val tokens = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(InviteToken::class.java)
                }?.filter { it.senderUserId != userId } ?: emptyList()

                // Fetch challenges for tokens
                val results = mutableListOf<Pair<Challenge, InviteToken>>()
                for (tok in tokens) {
                    firestore.collection("challenges").document(tok.challengeId).get()
                        .addOnSuccessListener { cDoc ->
                            cDoc.toObject(Challenge::class.java)?.let { chal ->
                                if (chal.status == ChallengeStatus.ACTIVE && !chal.isExpired) {
                                    results.add(Pair(chal, tok))
                                    trySend(results.toList())
                                }
                            }
                        }
                }
                trySend(results)
            }
        awaitClose { listener.remove() }
    }

    override fun observeCreatedChallenges(userId: String): Flow<List<Challenge>> = callbackFlow {
        val listener = firestore.collection("challenges")
            .whereEqualTo("creatorId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(Challenge::class.java) } ?: emptyList()
                trySend(list.sortedByDescending { it.createdAt })
            }
        awaitClose { listener.remove() }
    }

    override fun observeJoinedChallenges(userId: String): Flow<List<Challenge>> = callbackFlow {
        val listener = firestore.collection("participations")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val challengeIds = snapshot?.documents?.mapNotNull { it.getString("challengeId") }?.toSet() ?: emptySet()
                if (challengeIds.isEmpty()) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val challenges = mutableListOf<Challenge>()
                for (id in challengeIds) {
                    firestore.collection("challenges").document(id).get()
                        .addOnSuccessListener { doc ->
                            doc.toObject(Challenge::class.java)?.let { ch ->
                                challenges.add(ch)
                                trySend(challenges.sortedByDescending { it.createdAt })
                            }
                        }
                }
            }
        awaitClose { listener.remove() }
    }

    override fun observeChallenge(challengeId: String): Flow<Challenge?> = callbackFlow {
        val listener = firestore.collection("challenges").document(challengeId)
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.toObject(Challenge::class.java))
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getChallenge(challengeId: String): Challenge? {
        val doc = firestore.collection("challenges").document(challengeId).get().await()
        return doc.toObject(Challenge::class.java)
    }

    override suspend fun getInviteToken(token: String): InviteToken? {
        val doc = firestore.collection("invites").document(token).get().await()
        return doc.toObject(InviteToken::class.java)
    }

    override suspend fun resolveInvite(token: String): Result<ResolvedInvite> {
        return try {
            val inviteDoc = firestore.collection("invites").document(token).get().await()
            val invite = inviteDoc.toObject(InviteToken::class.java)
                ?: return Result.failure(IllegalArgumentException("Invite token not found"))

            val challengeDoc = firestore.collection("challenges").document(invite.challengeId).get().await()
            val challenge = challengeDoc.toObject(Challenge::class.java)
                ?: return Result.failure(IllegalStateException("Challenge not found"))

            val senderDoc = firestore.collection("users").document(invite.senderUserId).get().await()
            val sender = senderDoc.toObject(User::class.java)
                ?: User(invite.senderUserId, invite.senderUsername, invite.senderUsername)

            val currentUserId = authRepo.currentUser.value?.id
            val participationDoc = currentUserId?.let {
                firestore.collection("participations").document("${challenge.id}_$it").get().await()
            }
            val alreadySubmitted = participationDoc?.exists() == true
            val isExpired = invite.status == InviteStatus.EXPIRED || challenge.isExpired

            Result.success(
                ResolvedInvite(
                    challenge = challenge,
                    sender = sender,
                    inviteToken = invite,
                    isAlreadySubmitted = alreadySubmitted,
                    isExpired = isExpired
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createChallenge(
        creator: User,
        prompt: String,
        responseType: ResponseType,
        expirationHours: Int,
        initialResponseText: String?,
        initialPhotoUri: String?,
        city: String?,
        country: String?
    ): Result<CreatedChallengeResult> {
        return try {
            val now = System.currentTimeMillis()
            val challengeId = "chal_${UUID.randomUUID().toString().take(8)}"
            val participationId = "${challengeId}_${creator.id}"

            val challenge = Challenge(
                id = challengeId,
                creatorId = creator.id,
                creatorUsername = creator.username,
                creatorAvatarUrl = creator.avatarUrl,
                prompt = prompt,
                responseType = responseType,
                createdAt = now,
                expiresAt = now + expirationHours * 3600 * 1000L,
                status = ChallengeStatus.ACTIVE,
                participantCount = 1,
                generationCount = 0,
                cityCount = 1,
                countryCount = 1,
                totalDistanceKm = 0.0
            )

            val creatorParticipation = Participation(
                id = participationId,
                challengeId = challengeId,
                userId = creator.id,
                username = creator.username,
                userAvatarUrl = creator.avatarUrl,
                parentUserId = null,
                parentParticipationId = null,
                generation = 0,
                status = ParticipationStatus.SUBMITTED,
                city = city ?: "Denver",
                country = country ?: "United States",
                createdAt = now,
                submittedAt = now
            )

            val privateResponse = PrivateResponse(
                participationId = participationId,
                challengeId = challengeId,
                authorUserId = creator.id,
                responseType = responseType,
                responseUrl = initialPhotoUri,
                responseText = initialResponseText,
                submittedAt = now
            )

            val tokens = (1..3).map {
                InviteToken(
                    token = "tok_${UUID.randomUUID().toString().take(8)}",
                    challengeId = challengeId,
                    senderUserId = creator.id,
                    senderUsername = creator.username,
                    parentParticipationId = participationId,
                    generation = 0,
                    createdAt = now,
                    expiresAt = challenge.expiresAt,
                    status = InviteStatus.PENDING
                )
            }

            val batch = firestore.batch()
            batch.set(firestore.collection("challenges").document(challengeId), challenge)
            batch.set(firestore.collection("participations").document(participationId), creatorParticipation)
            batch.set(firestore.collection("participations").document(participationId).collection("private").document("response"), privateResponse)
            tokens.forEach { tok ->
                batch.set(firestore.collection("invites").document(tok.token), tok)
            }
            batch.update(firestore.collection("users").document(creator.id), "challengesCreated", FieldValue.increment(1))
            batch.commit().await()

            Result.success(CreatedChallengeResult(challenge, creatorParticipation, tokens))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun submitResponseAtomic(
        inviteToken: String,
        user: User,
        responseText: String?,
        photoUri: String?,
        city: String?,
        country: String?
    ): Result<Participation> {
        return try {
            firestore.runTransaction { transaction ->
                val inviteRef = firestore.collection("invites").document(inviteToken)
                val inviteDoc = transaction.get(inviteRef)
                val token = inviteDoc.toObject(InviteToken::class.java)
                    ?: throw IllegalArgumentException("Invite token '$inviteToken' is invalid")

                if (token.status != InviteStatus.PENDING && token.status != InviteStatus.OPENED) {
                    throw IllegalStateException("Invite token is already consumed or expired")
                }

                val challengeRef = firestore.collection("challenges").document(token.challengeId)
                val challengeDoc = transaction.get(challengeRef)
                val challenge = challengeDoc.toObject(Challenge::class.java)
                    ?: throw IllegalStateException("Challenge not found")

                val participationId = "${challenge.id}_${user.id}"
                val userParticipationRef = firestore.collection("participations").document(participationId)
                if (transaction.get(userParticipationRef).exists()) {
                    throw IllegalStateException("You have already participated in this Ripple!")
                }

                val now = System.currentTimeMillis()
                val childGen = token.generation + 1

                val newParticipation = Participation(
                    id = participationId,
                    challengeId = challenge.id,
                    userId = user.id,
                    username = user.username,
                    userAvatarUrl = user.avatarUrl,
                    parentUserId = token.senderUserId,
                    parentParticipationId = token.parentParticipationId,
                    inviteTokenUsed = inviteToken,
                    generation = childGen,
                    status = ParticipationStatus.SUBMITTED,
                    city = city ?: "Denver",
                    country = country ?: "United States",
                    createdAt = now,
                    submittedAt = now
                )

                val privateResponse = PrivateResponse(
                    participationId = participationId,
                    challengeId = challenge.id,
                    authorUserId = user.id,
                    responseType = challenge.responseType,
                    responseUrl = photoUri,
                    responseText = responseText,
                    submittedAt = now
                )

                // Mark token consumed
                transaction.update(inviteRef, mapOf(
                    "status" to "CONSUMED",
                    "consumedByUserId" to user.id,
                    "consumedAt" to now
                ))

                // Save participation & private response
                transaction.set(userParticipationRef, newParticipation)
                transaction.set(userParticipationRef.collection("private").document("response"), privateResponse)

                // Update server stats
                transaction.update(challengeRef, mapOf(
                    "participantCount" to FieldValue.increment(1),
                    "generationCount" to maxOf(challenge.generationCount, childGen)
                ))

                transaction.update(firestore.collection("users").document(user.id), "challengesCompleted", FieldValue.increment(1))
                transaction.update(firestore.collection("users").document(token.senderUserId), "totalPeopleReached", FieldValue.increment(1))
                transaction.update(firestore.collection("users").document(challenge.creatorId), "totalPeopleReached", FieldValue.increment(1))

                newParticipation
            }.await().let { Result.success(it) }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchUnlockedResponse(
        challengeId: String,
        authorUserId: String,
        requestingUserId: String
    ): Result<PrivateResponse> {
        return try {
            val authorParticipationId = "${challengeId}_$authorUserId"
            val doc = firestore.collection("participations").document(authorParticipationId)
                .collection("private").document("response").get().await()
            val resp = doc.toObject(PrivateResponse::class.java)
                ?: return Result.failure(NoSuchElementException("Response not found"))
            Result.success(resp)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateInvites(
        challengeId: String,
        user: User,
        parentParticipationId: String?,
        generation: Int,
        count: Int
    ): Result<List<InviteToken>> {
        return try {
            val challenge = getChallenge(challengeId) ?: return Result.failure(IllegalStateException("Challenge not found"))
            val now = System.currentTimeMillis()
            val tokens = (1..count).map {
                InviteToken(
                    token = "tok_${UUID.randomUUID().toString().take(8)}",
                    challengeId = challengeId,
                    senderUserId = user.id,
                    senderUsername = user.username,
                    parentParticipationId = parentParticipationId ?: "${challengeId}_${user.id}",
                    generation = generation,
                    createdAt = now,
                    expiresAt = challenge.expiresAt,
                    status = InviteStatus.PENDING
                )
            }
            val batch = firestore.batch()
            tokens.forEach { tok ->
                batch.set(firestore.collection("invites").document(tok.token), tok)
            }
            batch.commit().await()
            Result.success(tokens)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getInvitesForUser(challengeId: String, userId: String): List<InviteToken> {
        val snapshot = firestore.collection("invites")
            .whereEqualTo("challengeId", challengeId)
            .whereEqualTo("senderUserId", userId)
            .get().await()
        return snapshot.documents.mapNotNull { it.toObject(InviteToken::class.java) }
    }

    override suspend fun getParticipations(challengeId: String): List<Participation> {
        val snapshot = firestore.collection("participations")
            .whereEqualTo("challengeId", challengeId)
            .get().await()
        return snapshot.documents.mapNotNull { it.toObject(Participation::class.java) }.sortedBy { it.generation }
    }

    override suspend fun getRippleStats(challengeId: String): RippleStats? {
        val challenge = getChallenge(challengeId) ?: return null
        val participations = getParticipations(challengeId)
        val invites = firestore.collection("invites").whereEqualTo("challengeId", challengeId).get().await()
            .documents.mapNotNull { it.toObject(InviteToken::class.java) }
        return calculateStatsUseCase(challenge, participations, invites)
    }

    override suspend fun simulateBranchExpansion(challengeId: String, senderUserId: String): Result<Participation> {
        val token = "sim_${UUID.randomUUID().toString().take(6)}"
        val simulatedUser = User("sim_user_${UUID.randomUUID().toString().take(4)}", "explorer", "Ripple Explorer")
        return submitResponseAtomic(
            inviteToken = token,
            user = simulatedUser,
            responseText = "Joined from Tokyo! 🗼",
            photoUri = "sim_photo.jpg",
            city = "Tokyo",
            country = "Japan"
        )
    }
}
