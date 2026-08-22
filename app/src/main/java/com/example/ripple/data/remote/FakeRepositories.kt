package com.example.ripple.data.remote

import com.example.ripple.domain.model.*
import com.example.ripple.domain.repository.*
import com.example.ripple.domain.usecase.CalculateStatsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

class FakeAuthRepository : AuthRepository {
    private val demoUsers = listOf(
        User(
            id = "user_siva",
            username = "siva",
            displayName = "Siva",
            avatarUrl = null,
            largestRipple = 327,
            totalPeopleReached = 842,
            challengesCreated = 3,
            challengesCompleted = 7,
            furthestKm = 8420.0
        ),
        User(
            id = "user_alex",
            username = "alex",
            displayName = "Alex",
            avatarUrl = null,
            largestRipple = 142,
            totalPeopleReached = 310,
            challengesCreated = 1,
            challengesCompleted = 5,
            furthestKm = 3200.0
        ),
        User(
            id = "user_john",
            username = "john",
            displayName = "John",
            avatarUrl = null,
            largestRipple = 89,
            totalPeopleReached = 120,
            challengesCreated = 1,
            challengesCompleted = 4,
            furthestKm = 1500.0
        ),
        User(
            id = "user_sarah",
            username = "sarah",
            displayName = "Sarah",
            avatarUrl = null,
            largestRipple = 640,
            totalPeopleReached = 1420,
            challengesCreated = 5,
            challengesCompleted = 12,
            furthestKm = 12500.0
        ),
        User(
            id = "user_elena",
            username = "elena",
            displayName = "Elena (Tokyo)",
            avatarUrl = null,
            largestRipple = 210,
            totalPeopleReached = 480,
            challengesCreated = 2,
            challengesCompleted = 6,
            furthestKm = 9800.0
        )
    )

    private val _currentUser = MutableStateFlow<User?>(demoUsers[0]) // Default to Siva
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    override suspend fun signInDemoUser(userId: String): Result<User> {
        val found = demoUsers.firstOrNull { it.id == userId }
            ?: demoUsers[0]
        _currentUser.value = found
        return Result.success(found)
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        val user = User(
            id = "user_google_${UUID.randomUUID().toString().take(6)}",
            username = "new_user",
            displayName = "Google User",
            avatarUrl = null
        )
        _currentUser.value = user
        return Result.success(user)
    }

    override suspend fun signInWithPhone(phoneNumber: String, verificationCode: String): Result<User> {
        val user = User(
            id = "user_phone_${UUID.randomUUID().toString().take(6)}",
            username = "user_${phoneNumber.takeLast(4)}",
            displayName = "User $phoneNumber",
            avatarUrl = null
        )
        _currentUser.value = user
        return Result.success(user)
    }

    override suspend fun signOut() {
        _currentUser.value = null
    }

    override suspend fun updateProfile(displayName: String, username: String, avatarUrl: String?): Result<User> {
        val current = _currentUser.value ?: return Result.failure(IllegalStateException("Not logged in"))
        val updated = current.copy(displayName = displayName, username = username, avatarUrl = avatarUrl)
        _currentUser.value = updated
        return Result.success(updated)
    }

    override fun getAvailableDemoUsers(): List<User> = demoUsers
}

class FakeChallengeRepository(
    private val authRepository: AuthRepository,
    private val calculateStatsUseCase: CalculateStatsUseCase = CalculateStatsUseCase()
) : ChallengeRepository {

    private val challengesFlow = MutableStateFlow<Map<String, Challenge>>(emptyMap())
    private val participationsFlow = MutableStateFlow<Map<String, Participation>>(emptyMap())
    private val privateResponsesFlow = MutableStateFlow<Map<String, PrivateResponse>>(emptyMap())
    private val inviteTokensFlow = MutableStateFlow<Map<String, InviteToken>>(emptyMap())

    init {
        seedInitialDemoData()
    }

    private fun seedInitialDemoData() {
        val now = System.currentTimeMillis()
        val c1Id = "chal_view_now"

        // 1. Initial Challenge
        val chal1 = Challenge(
            id = c1Id,
            creatorId = "user_siva",
            creatorUsername = "siva",
            prompt = "Show what's directly in front of you right now 📸",
            responseType = ResponseType.PHOTO,
            createdAt = now - 6 * 3600 * 1000L,
            expiresAt = now + 18 * 3600 * 1000L,
            status = ChallengeStatus.ACTIVE,
            participantCount = 6,
            generationCount = 3,
            cityCount = 4,
            countryCount = 3,
            totalDistanceKm = 8420.0
        )

        // 2. Participations across generations
        val pSiva = Participation(
            id = "${c1Id}_user_siva",
            challengeId = c1Id,
            userId = "user_siva",
            username = "siva",
            generation = 0,
            status = ParticipationStatus.SUBMITTED,
            city = "Denver",
            country = "United States",
            createdAt = now - 6 * 3600 * 1000L,
            submittedAt = now - 6 * 3600 * 1000L
        )

        val pSarah = Participation(
            id = "${c1Id}_user_sarah",
            challengeId = c1Id,
            userId = "user_sarah",
            username = "sarah",
            parentUserId = "user_siva",
            parentParticipationId = pSiva.id,
            inviteTokenUsed = "tok_siva_1",
            generation = 1,
            status = ParticipationStatus.SUBMITTED,
            city = "London",
            country = "United Kingdom",
            createdAt = now - 4 * 3600 * 1000L,
            submittedAt = now - 4 * 3600 * 1000L
        )

        val pJohn = Participation(
            id = "${c1Id}_user_john",
            challengeId = c1Id,
            userId = "user_john",
            username = "john",
            parentUserId = "user_siva",
            parentParticipationId = pSiva.id,
            inviteTokenUsed = "tok_siva_2",
            generation = 1,
            status = ParticipationStatus.SUBMITTED,
            city = "New York",
            country = "United States",
            createdAt = now - 3 * 3600 * 1000L,
            submittedAt = now - 3 * 3600 * 1000L
        )

        val pElena = Participation(
            id = "${c1Id}_user_elena",
            challengeId = c1Id,
            userId = "user_elena",
            username = "elena",
            parentUserId = "user_sarah",
            parentParticipationId = pSarah.id,
            inviteTokenUsed = "tok_sarah_1",
            generation = 2,
            status = ParticipationStatus.SUBMITTED,
            city = "Tokyo",
            country = "Japan",
            createdAt = now - 2 * 3600 * 1000L,
            submittedAt = now - 2 * 3600 * 1000L
        )

        // 3. Private responses
        val rSiva = PrivateResponse(
            participationId = pSiva.id,
            challengeId = c1Id,
            authorUserId = "user_siva",
            responseType = ResponseType.PHOTO,
            responseUrl = "demo_desk_view.jpg",
            responseText = "Late night coding setup with iced coffee ☕"
        )
        val rSarah = PrivateResponse(
            participationId = pSarah.id,
            challengeId = c1Id,
            authorUserId = "user_sarah",
            responseType = ResponseType.PHOTO,
            responseUrl = "demo_london_rain.jpg",
            responseText = "Rainy window view over Big Ben 🌧️"
        )
        val rJohn = PrivateResponse(
            participationId = pJohn.id,
            challengeId = c1Id,
            authorUserId = "user_john",
            responseType = ResponseType.PHOTO,
            responseUrl = "demo_subway.jpg",
            responseText = "Packed morning NYC subway train 🚇"
        )
        val rElena = PrivateResponse(
            participationId = pElena.id,
            challengeId = c1Id,
            authorUserId = "user_elena",
            responseType = ResponseType.PHOTO,
            responseUrl = "demo_shibuya.jpg",
            responseText = "Neon lights of Shibuya crossing 🏮"
        )

        // 4. Pending invite for Alex from Siva
        val tokAlex = InviteToken(
            token = "tok_alex_pending",
            challengeId = c1Id,
            senderUserId = "user_siva",
            senderUsername = "siva",
            parentParticipationId = pSiva.id,
            generation = 0,
            createdAt = now - 1 * 3600 * 1000L,
            expiresAt = now + 23 * 3600 * 1000L,
            status = InviteStatus.PENDING
        )

        // Second challenge (Worst Selfie)
        val c2Id = "chal_worst_selfie"
        val chal2 = Challenge(
            id = c2Id,
            creatorId = "user_sarah",
            creatorUsername = "sarah",
            prompt = "Take the absolute worst angle selfie possible 😂",
            responseType = ResponseType.PHOTO,
            createdAt = now - 12 * 3600 * 1000L,
            expiresAt = now + 12 * 3600 * 1000L,
            status = ChallengeStatus.ACTIVE,
            participantCount = 4829,
            generationCount = 6,
            cityCount = 37,
            countryCount = 8,
            totalDistanceKm = 24800.0
        )

        challengesFlow.value = mapOf(c1Id to chal1, c2Id to chal2)
        participationsFlow.value = mapOf(
            pSiva.id to pSiva,
            pSarah.id to pSarah,
            pJohn.id to pJohn,
            pElena.id to pElena
        )
        privateResponsesFlow.value = mapOf(
            pSiva.id to rSiva,
            pSarah.id to rSarah,
            pJohn.id to rJohn,
            pElena.id to rElena
        )
        inviteTokensFlow.value = mapOf(
            tokAlex.token to tokAlex,
            "tok_siva_1" to InviteToken("tok_siva_1", c1Id, "user_siva", "siva", pSiva.id, 0, status = InviteStatus.CONSUMED, consumedByUserId = "user_sarah"),
            "tok_siva_2" to InviteToken("tok_siva_2", c1Id, "user_siva", "siva", pSiva.id, 0, status = InviteStatus.CONSUMED, consumedByUserId = "user_john"),
            "tok_siva_3" to tokAlex,
            "tok_sarah_1" to InviteToken("tok_sarah_1", c1Id, "user_sarah", "sarah", pSarah.id, 1, status = InviteStatus.CONSUMED, consumedByUserId = "user_elena")
        )
    }

    override fun observeWaitingChallenges(userId: String): Flow<List<Pair<Challenge, InviteToken>>> {
        return inviteTokensFlow.map { tokens ->
            val userParticipations = participationsFlow.value.values.filter { it.userId == userId }.map { it.challengeId }.toSet()
            tokens.values
                .filter { it.isAvailable && !userParticipations.contains(it.challengeId) && it.senderUserId != userId }
                .mapNotNull { token ->
                    challengesFlow.value[token.challengeId]?.let { challenge ->
                        Pair(challenge, token)
                    }
                }
        }
    }

    override fun observeCreatedChallenges(userId: String): Flow<List<Challenge>> {
        return challengesFlow.map { map ->
            map.values.filter { it.creatorId == userId }.sortedByDescending { it.createdAt }
        }
    }

    override fun observeJoinedChallenges(userId: String): Flow<List<Challenge>> {
        return participationsFlow.map { map ->
            val joinedChallengeIds = map.values.filter { it.userId == userId && it.parentUserId != null }.map { it.challengeId }.toSet()
            challengesFlow.value.values.filter { joinedChallengeIds.contains(it.id) }.sortedByDescending { it.createdAt }
        }
    }

    override fun observeChallenge(challengeId: String): Flow<Challenge?> {
        return challengesFlow.map { it[challengeId] }
    }

    override suspend fun getChallenge(challengeId: String): Challenge? {
        return challengesFlow.value[challengeId]
    }

    override suspend fun getInviteToken(token: String): InviteToken? {
        return inviteTokensFlow.value[token]
    }

    override suspend fun resolveInvite(token: String): Result<ResolvedInvite> {
        val invite = inviteTokensFlow.value[token]
            ?: return Result.failure(IllegalArgumentException("Invite token '$token' was not found"))

        val challenge = challengesFlow.value[invite.challengeId]
            ?: return Result.failure(IllegalStateException("Challenge for invite not found"))

        val sender = authRepository.getAvailableDemoUsers().firstOrNull { it.id == invite.senderUserId }
            ?: User(invite.senderUserId, invite.senderUsername, invite.senderUsername)

        val currentUserId = authRepository.currentUser.value?.id
        val alreadySubmitted = currentUserId != null && participationsFlow.value.containsKey("${challenge.id}_$currentUserId")
        val isExpired = invite.status == InviteStatus.EXPIRED || challenge.isExpired

        return Result.success(
            ResolvedInvite(
                challenge = challenge,
                sender = sender,
                inviteToken = invite,
                isAlreadySubmitted = alreadySubmitted,
                isExpired = isExpired
            )
        )
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
            cityCount = if (city.isNullOrBlank()) 1 else 1,
            countryCount = if (country.isNullOrBlank()) 1 else 1,
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

        val tokens = (1..3).map { index ->
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

        // Commit atomically to StateFlows
        challengesFlow.value = challengesFlow.value + (challengeId to challenge)
        participationsFlow.value = participationsFlow.value + (participationId to creatorParticipation)
        privateResponsesFlow.value = privateResponsesFlow.value + (participationId to privateResponse)
        inviteTokensFlow.value = inviteTokensFlow.value + tokens.associateBy { it.token }

        return Result.success(CreatedChallengeResult(challenge, creatorParticipation, tokens))
    }

    override suspend fun submitResponseAtomic(
        inviteToken: String,
        user: User,
        responseText: String?,
        photoUri: String?,
        city: String?,
        country: String?
    ): Result<Participation> {
        val token = inviteTokensFlow.value[inviteToken]
            ?: return Result.failure(IllegalArgumentException("Invite token '$inviteToken' is invalid"))

        if (!token.isAvailable) {
            return Result.failure(IllegalStateException("Invite token is already consumed or expired"))
        }

        val challenge = challengesFlow.value[token.challengeId]
            ?: return Result.failure(IllegalStateException("Challenge not found"))

        val participationId = "${challenge.id}_${user.id}"

        // Anti-cheating check: single participation per challenge
        if (participationsFlow.value.containsKey(participationId)) {
            return Result.failure(IllegalStateException("You have already participated in this Ripple!"))
        }

        val now = System.currentTimeMillis()
        val childGeneration = token.generation + 1

        val newParticipation = Participation(
            id = participationId,
            challengeId = challenge.id,
            userId = user.id,
            username = user.username,
            userAvatarUrl = user.avatarUrl,
            parentUserId = token.senderUserId,
            parentParticipationId = token.parentParticipationId,
            inviteTokenUsed = inviteToken,
            generation = childGeneration,
            status = ParticipationStatus.SUBMITTED,
            city = city ?: "San Francisco",
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

        val consumedToken = token.copy(
            status = InviteStatus.CONSUMED,
            consumedByUserId = user.id,
            consumedAt = now
        )

        // Recalculate stats atomically
        val updatedParticipations = participationsFlow.value + (participationId to newParticipation)
        val stats = calculateStatsUseCase(challenge, updatedParticipations.values.filter { it.challengeId == challenge.id }, inviteTokensFlow.value.values.filter { it.challengeId == challenge.id })

        val updatedChallenge = challenge.copy(
            participantCount = stats.totalParticipants,
            generationCount = stats.generationDepth,
            cityCount = stats.cityCount,
            countryCount = stats.countryCount,
            totalDistanceKm = stats.totalDistanceKm
        )

        // Apply transaction
        participationsFlow.value = updatedParticipations
        privateResponsesFlow.value = privateResponsesFlow.value + (participationId to privateResponse)
        inviteTokensFlow.value = inviteTokensFlow.value + (inviteToken to consumedToken)
        challengesFlow.value = challengesFlow.value + (challenge.id to updatedChallenge)

        return Result.success(newParticipation)
    }

    override suspend fun fetchUnlockedResponse(
        challengeId: String,
        authorUserId: String,
        requestingUserId: String
    ): Result<PrivateResponse> {
        val authorParticipationId = "${challengeId}_$authorUserId"
        val response = privateResponsesFlow.value[authorParticipationId]
            ?: return Result.failure(NoSuchElementException("Response not found for user $authorUserId"))

        // Security check: must be owner or direct parent/child with status == SUBMITTED
        val requesterParticipationId = "${challengeId}_$requestingUserId"
        val requesterParticipation = participationsFlow.value[requesterParticipationId]

        val isOwner = authorUserId == requestingUserId
        val isDirectParentOrChild = requesterParticipation != null && (
            requesterParticipation.parentUserId == authorUserId ||
            participationsFlow.value[authorParticipationId]?.parentUserId == requestingUserId
        )

        if (!isOwner && !isDirectParentOrChild) {
            return Result.failure(SecurityException("Access Denied: You must complete the challenge to reveal this response!"))
        }

        return Result.success(response)
    }

    override suspend fun generateInvites(
        challengeId: String,
        user: User,
        parentParticipationId: String?,
        generation: Int,
        count: Int
    ): Result<List<InviteToken>> {
        val challenge = challengesFlow.value[challengeId]
            ?: return Result.failure(IllegalStateException("Challenge not found"))

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

        inviteTokensFlow.value = inviteTokensFlow.value + tokens.associateBy { it.token }
        return Result.success(tokens)
    }

    override suspend fun getInvitesForUser(challengeId: String, userId: String): List<InviteToken> {
        return inviteTokensFlow.value.values.filter { it.challengeId == challengeId && it.senderUserId == userId }
    }

    override suspend fun getParticipations(challengeId: String): List<Participation> {
        return participationsFlow.value.values.filter { it.challengeId == challengeId }.sortedBy { it.generation }
    }

    override suspend fun getRippleStats(challengeId: String): RippleStats? {
        val challenge = challengesFlow.value[challengeId] ?: return null
        val participations = getParticipations(challengeId)
        val invites = inviteTokensFlow.value.values.filter { it.challengeId == challengeId }
        return calculateStatsUseCase(challenge, participations, invites)
    }

    override suspend fun simulateBranchExpansion(challengeId: String, senderUserId: String): Result<Participation> {
        val simulationPool = listOf(
            Triple("sam_sf", "Sam (San Francisco)", "San Francisco"),
            Triple("lucas_paris", "Lucas (Paris)", "Paris"),
            Triple("charlotte_berlin", "Charlotte (Berlin)", "Berlin"),
            Triple("takeshi_kyoto", "Takeshi (Kyoto)", "Kyoto"),
            Triple("mateo_buenos_aires", "Mateo (Buenos Aires)", "Buenos Aires"),
            Triple("priya_mumbai", "Priya (Mumbai)", "Mumbai")
        )

        val existingUserIds = participationsFlow.value.values.filter { it.challengeId == challengeId }.map { it.userId }.toSet()
        val candidate = simulationPool.firstOrNull { !existingUserIds.contains(it.first) }
            ?: Triple("guest_${UUID.randomUUID().toString().take(4)}", "Ripple Explorer", "Toronto")

        val senderParticipation = participationsFlow.value["${challengeId}_$senderUserId"]
            ?: return Result.failure(IllegalStateException("Sender participation not found"))

        // Create mock invite token
        val token = "sim_${UUID.randomUUID().toString().take(6)}"
        val inviteToken = InviteToken(
            token = token,
            challengeId = challengeId,
            senderUserId = senderUserId,
            senderUsername = senderParticipation.username,
            parentParticipationId = senderParticipation.id,
            generation = senderParticipation.generation,
            status = InviteStatus.PENDING
        )
        inviteTokensFlow.value = inviteTokensFlow.value + (token to inviteToken)

        val simulatedUser = User(
            id = candidate.first,
            username = candidate.first,
            displayName = candidate.second
        )

        return submitResponseAtomic(
            inviteToken = token,
            user = simulatedUser,
            responseText = "Joined from ${candidate.third}! 🌊",
            photoUri = "simulated_photo_${candidate.third}.jpg",
            city = candidate.third,
            country = "Global"
        )
    }
}

class FakeNotificationRepository : NotificationRepository {
    private val notificationsFlow = MutableStateFlow<List<NotificationItem>>(
        listOf(
            NotificationItem(
                id = "notif_1",
                title = "👀 Siva challenged you",
                body = "Show what's directly in front of you right now 📸",
                type = NotificationType.INVITE_RECEIVED,
                challengeId = "chal_view_now",
                inviteToken = "tok_alex_pending",
                timestamp = System.currentTimeMillis() - 15 * 60 * 1000L
            ),
            NotificationItem(
                id = "notif_2",
                title = "🌊 Your Ripple just crossed 100 people!",
                body = "Something you started reached Generation 6 across 4 countries.",
                type = NotificationType.MILESTONE_REACHED,
                challengeId = "chal_view_now",
                timestamp = System.currentTimeMillis() - 2 * 3600 * 1000L
            )
        )
    )

    override fun observeNotifications(userId: String): Flow<List<NotificationItem>> = notificationsFlow

    override suspend fun markAsRead(notificationId: String) {
        notificationsFlow.value = notificationsFlow.value.map {
            if (it.id == notificationId) it.copy(isRead = true) else it
        }
    }

    override suspend fun sendNotification(notification: NotificationItem) {
        notificationsFlow.value = listOf(notification) + notificationsFlow.value
    }
}
