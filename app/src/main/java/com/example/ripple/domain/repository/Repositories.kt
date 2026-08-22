package com.example.ripple.domain.repository

import com.example.ripple.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<User?>
    suspend fun signInDemoUser(userId: String): Result<User>
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signInWithPhone(phoneNumber: String, verificationCode: String): Result<User>
    suspend fun signOut()
    suspend fun updateProfile(displayName: String, username: String, avatarUrl: String?): Result<User>
    fun getAvailableDemoUsers(): List<User>
}

interface ChallengeRepository {
    fun observeWaitingChallenges(userId: String): Flow<List<Pair<Challenge, InviteToken>>>
    fun observeCreatedChallenges(userId: String): Flow<List<Challenge>>
    fun observeJoinedChallenges(userId: String): Flow<List<Challenge>>
    fun observeChallenge(challengeId: String): Flow<Challenge?>
    
    suspend fun getChallenge(challengeId: String): Challenge?
    suspend fun getInviteToken(token: String): InviteToken?
    suspend fun resolveInvite(token: String): Result<ResolvedInvite>
    
    suspend fun createChallenge(
        creator: User,
        prompt: String,
        responseType: ResponseType,
        expirationHours: Int,
        initialResponseText: String?,
        initialPhotoUri: String?,
        city: String?,
        country: String?
    ): Result<CreatedChallengeResult>

    suspend fun submitResponseAtomic(
        inviteToken: String,
        user: User,
        responseText: String?,
        photoUri: String?,
        city: String?,
        country: String?
    ): Result<Participation>

    suspend fun fetchUnlockedResponse(
        challengeId: String,
        authorUserId: String,
        requestingUserId: String
    ): Result<PrivateResponse>

    suspend fun generateInvites(
        challengeId: String,
        user: User,
        parentParticipationId: String?,
        generation: Int,
        count: Int = 3
    ): Result<List<InviteToken>>

    suspend fun getInvitesForUser(challengeId: String, userId: String): List<InviteToken>
    suspend fun getParticipations(challengeId: String): List<Participation>
    suspend fun getRippleStats(challengeId: String): RippleStats?
    
    // Testing & Simulation Helper
    suspend fun simulateBranchExpansion(challengeId: String, senderUserId: String): Result<Participation>
}

data class ResolvedInvite(
    val challenge: Challenge,
    val sender: User,
    val inviteToken: InviteToken,
    val isAlreadySubmitted: Boolean,
    val isExpired: Boolean
)

data class CreatedChallengeResult(
    val challenge: Challenge,
    val creatorParticipation: Participation,
    val inviteTokens: List<InviteToken>
)

interface NotificationRepository {
    fun observeNotifications(userId: String): Flow<List<NotificationItem>>
    suspend fun markAsRead(notificationId: String)
    suspend fun sendNotification(notification: NotificationItem)
}

interface ModerationRepository {
    suspend fun reportContent(ticket: ReportTicket): Result<Unit>
    suspend fun blockUser(currentUserId: String, blockedUserId: String): Result<Unit>
    suspend fun isUserBlocked(currentUserId: String, targetUserId: String): Boolean
    fun validatePromptSafety(prompt: String): Result<Unit>
}
