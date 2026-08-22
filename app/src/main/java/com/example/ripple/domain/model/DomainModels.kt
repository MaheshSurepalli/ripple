package com.example.ripple.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val largestRipple: Int = 0,
    val totalPeopleReached: Int = 0,
    val challengesCreated: Int = 0,
    val challengesCompleted: Int = 0,
    val furthestKm: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ResponseType {
    PHOTO,
    TEXT
}

enum class ChallengeStatus {
    ACTIVE,
    EXPIRED,
    CLOSED,
    MODERATED
}

@Serializable
data class Challenge(
    val id: String,
    val creatorId: String,
    val creatorUsername: String,
    val creatorAvatarUrl: String? = null,
    val prompt: String,
    val responseType: ResponseType = ResponseType.PHOTO,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 24 * 60 * 60 * 1000L,
    val status: ChallengeStatus = ChallengeStatus.ACTIVE,
    val participantCount: Int = 1,
    val generationCount: Int = 0,
    val cityCount: Int = 1,
    val countryCount: Int = 1,
    val totalDistanceKm: Double = 0.0,
    val maxInvitesPerPerson: Int = 3
) {
    val isExpired: Boolean get() = System.currentTimeMillis() >= expiresAt || status == ChallengeStatus.EXPIRED
    val remainingMillis: Long get() = (expiresAt - System.currentTimeMillis()).coerceAtLeast(0L)
}

enum class ParticipationStatus {
    INVITED,
    OPENED,
    SUBMITTED
}

@Serializable
data class Participation(
    val id: String, // format: "${challengeId}_${userId}"
    val challengeId: String,
    val userId: String,
    val username: String,
    val userAvatarUrl: String? = null,
    val parentUserId: String? = null,
    val parentParticipationId: String? = null,
    val inviteTokenUsed: String? = null,
    val generation: Int = 0,
    val status: ParticipationStatus = ParticipationStatus.SUBMITTED,
    val city: String? = null,
    val country: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val submittedAt: Long? = System.currentTimeMillis()
)

@Serializable
data class PrivateResponse(
    val participationId: String,
    val challengeId: String,
    val authorUserId: String,
    val responseType: ResponseType,
    val responseUrl: String? = null, // Photo uri / file url / cloud url
    val responseText: String? = null,
    val submittedAt: Long = System.currentTimeMillis()
)

enum class InviteStatus {
    PENDING,
    OPENED,
    CONSUMED,
    EXPIRED,
    REVOKED
}

@Serializable
data class InviteToken(
    val token: String,
    val challengeId: String,
    val senderUserId: String,
    val senderUsername: String,
    val parentParticipationId: String? = null,
    val generation: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 24 * 60 * 60 * 1000L,
    val status: InviteStatus = InviteStatus.PENDING,
    val consumedByUserId: String? = null,
    val consumedAt: Long? = null
) {
    val inviteUrl: String get() = "https://ripple.app/i/$token"
    val isAvailable: Boolean get() = status == InviteStatus.PENDING || status == InviteStatus.OPENED
}

@Serializable
data class RippleStats(
    val challengeId: String,
    val prompt: String,
    val creatorUsername: String,
    val totalParticipants: Int,
    val generationDepth: Int,
    val cityCount: Int,
    val countryCount: Int,
    val totalDistanceKm: Double,
    val viralCoefficient: Double, // average invites sent * conversion rate
    val completionRatePercentage: Int,
    val averageInvitesPerParticipant: Double,
    val longestBranchDepth: Int,
    val topCities: List<String> = emptyList(),
    val topCountries: List<String> = emptyList(),
    val generationDistribution: Map<Int, Int> = emptyMap() // generation -> count
)

@Serializable
data class RippleNode(
    val participation: Participation,
    val children: List<RippleNode> = emptyList(),
    val isRevealedToCurrentUser: Boolean = false,
    val privateResponsePreview: String? = null
)

@Serializable
data class MilestoneCard(
    val id: String,
    val challengeId: String,
    val title: String,
    val highlightMetric: String,
    val subtitle: String,
    val generationBadge: String,
    val inviteUrl: String,
    val prompt: String,
    val themeColorHex: Long = 0xFF00E5FF
)

enum class NotificationType {
    INVITE_RECEIVED,
    INVITE_ACCEPTED,
    MILESTONE_REACHED,
    GENERATION_REACHED,
    EXPIRATION_WARNING,
    BRANCH_GROWTH
}

@Serializable
data class NotificationItem(
    val id: String,
    val title: String,
    val body: String,
    val type: NotificationType,
    val challengeId: String? = null,
    val inviteToken: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

enum class ReportReason {
    HARASSMENT,
    INAPPROPRIATE_CONTENT,
    DANGEROUS_ACTIVITY,
    SPAM,
    OTHER
}

@Serializable
data class ReportTicket(
    val id: String,
    val targetType: String, // "CHALLENGE" or "RESPONSE" or "USER"
    val targetId: String,
    val reporterUserId: String,
    val reason: ReportReason,
    val additionalComments: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
