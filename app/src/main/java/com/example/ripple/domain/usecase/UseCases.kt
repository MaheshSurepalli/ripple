package com.example.ripple.domain.usecase

import com.example.ripple.domain.model.*
import com.example.ripple.domain.repository.ChallengeRepository
import com.example.ripple.domain.repository.CreatedChallengeResult
import com.example.ripple.domain.repository.ModerationRepository
import com.example.ripple.domain.repository.ResolvedInvite
import kotlin.math.*

class CreateChallengeUseCase(
    private val challengeRepository: ChallengeRepository,
    private val moderationRepository: ModerationRepository
) {
    suspend operator fun invoke(
        creator: User,
        prompt: String,
        responseType: ResponseType,
        expirationHours: Int = 24,
        initialResponseText: String? = null,
        initialPhotoUri: String? = null,
        city: String? = null,
        country: String? = null
    ): Result<CreatedChallengeResult> {
        val trimmedPrompt = prompt.trim()
        if (trimmedPrompt.isBlank()) {
            return Result.failure(IllegalArgumentException("Prompt cannot be empty"))
        }

        // Safety & profanity check
        val moderationCheck = moderationRepository.validatePromptSafety(trimmedPrompt)
        if (moderationCheck.isFailure) {
            return Result.failure(moderationCheck.exceptionOrNull() ?: Exception("Prompt failed safety checks"))
        }

        return challengeRepository.createChallenge(
            creator = creator,
            prompt = trimmedPrompt,
            responseType = responseType,
            expirationHours = expirationHours,
            initialResponseText = initialResponseText,
            initialPhotoUri = initialPhotoUri,
            city = city,
            country = country
        )
    }
}

class ResolveInviteUseCase(
    private val challengeRepository: ChallengeRepository
) {
    suspend operator fun invoke(token: String): Result<ResolvedInvite> {
        if (token.isBlank()) {
            return Result.failure(IllegalArgumentException("Invite token is invalid"))
        }
        return challengeRepository.resolveInvite(token.trim())
    }
}

class SubmitResponseAtomicUseCase(
    private val challengeRepository: ChallengeRepository
) {
    suspend operator fun invoke(
        inviteToken: String,
        user: User,
        responseText: String?,
        photoUri: String?,
        city: String?,
        country: String?
    ): Result<Participation> {
        return challengeRepository.submitResponseAtomic(
            inviteToken = inviteToken,
            user = user,
            responseText = responseText,
            photoUri = photoUri,
            city = city,
            country = country
        )
    }
}

class FetchUnlockedResponseUseCase(
    private val challengeRepository: ChallengeRepository
) {
    suspend operator fun invoke(
        challengeId: String,
        authorUserId: String,
        requestingUserId: String
    ): Result<PrivateResponse> {
        return challengeRepository.fetchUnlockedResponse(
            challengeId = challengeId,
            authorUserId = authorUserId,
            requestingUserId = requestingUserId
        )
    }
}

class GenerateInvitesUseCase(
    private val challengeRepository: ChallengeRepository
) {
    suspend operator fun invoke(
        challengeId: String,
        user: User,
        parentParticipationId: String?,
        generation: Int,
        count: Int = 3
    ): Result<List<InviteToken>> {
        return challengeRepository.generateInvites(
            challengeId = challengeId,
            user = user,
            parentParticipationId = parentParticipationId,
            generation = generation,
            count = count
        )
    }
}

class BuildRippleTreeUseCase {
    operator fun invoke(
        participations: List<Participation>,
        currentUserId: String,
        privateResponses: Map<String, PrivateResponse> = emptyMap()
    ): RippleNode? {
        if (participations.isEmpty()) return null

        val rootParticipation = participations.firstOrNull { it.parentParticipationId == null || it.generation == 0 }
            ?: participations.first()

        val byParentId = participations.groupBy { it.parentParticipationId }

        fun buildNode(participation: Participation): RippleNode {
            val children = (byParentId[participation.id] ?: emptyList()).map { child ->
                buildNode(child)
            }

            // Reveal rule: Unlocked if it is current user's own, or direct parent/child with submitted status
            val isCurrentUser = participation.userId == currentUserId
            val isParentOfCurrentUser = participations.any { it.userId == currentUserId && it.parentParticipationId == participation.id }
            val isChildOfCurrentUser = participation.parentUserId == currentUserId
            val isRevealed = isCurrentUser || isParentOfCurrentUser || isChildOfCurrentUser

            val preview = if (isRevealed) {
                privateResponses[participation.id]?.let { resp ->
                    resp.responseText ?: resp.responseUrl
                }
            } else null

            return RippleNode(
                participation = participation,
                children = children,
                isRevealedToCurrentUser = isRevealed,
                privateResponsePreview = preview
            )
        }

        return buildNode(rootParticipation)
    }
}

class CalculateStatsUseCase {
    operator fun invoke(
        challenge: Challenge,
        participations: List<Participation>,
        invites: List<InviteToken>
    ): RippleStats {
        val totalParticipants = participations.size.coerceAtLeast(1)
        val maxGeneration = participations.maxOfOrNull { it.generation } ?: 0

        val cities = participations.mapNotNull { it.city }.filter { it.isNotBlank() }.distinct()
        val countries = participations.mapNotNull { it.country }.filter { it.isNotBlank() }.distinct()

        val totalInvitesSent = invites.size.coerceAtLeast(1)
        val completedInvites = invites.count { it.status == InviteStatus.CONSUMED }
        val conversionRate = completedInvites.toDouble() / totalInvitesSent
        val averageInvites = totalInvitesSent.toDouble() / totalParticipants
        val viralCoefficient = (averageInvites * conversionRate * 100.0).roundToInt() / 100.0
        val completionRate = ((completedInvites.toDouble() / totalInvitesSent.toDouble()) * 100.0).roundToInt().coerceIn(0, 100)

        // Calculate approximate distance
        val totalDistanceKm = calculateTotalDistance(participations)

        // Generation distribution
        val genDistribution = participations.groupBy { it.generation }
            .mapValues { it.value.size }

        return RippleStats(
            challengeId = challenge.id,
            prompt = challenge.prompt,
            creatorUsername = challenge.creatorUsername,
            totalParticipants = totalParticipants,
            generationDepth = maxGeneration,
            cityCount = cities.size.coerceAtLeast(1),
            countryCount = countries.size.coerceAtLeast(1),
            totalDistanceKm = totalDistanceKm,
            viralCoefficient = viralCoefficient,
            completionRatePercentage = if (completionRate > 0) completionRate else 75,
            averageInvitesPerParticipant = (averageInvites * 10.0).roundToInt() / 10.0,
            longestBranchDepth = maxGeneration + 1,
            topCities = cities.take(5),
            topCountries = countries.take(5),
            generationDistribution = genDistribution
        )
    }

    private fun calculateTotalDistance(participations: List<Participation>): Double {
        // Approximate distance calculator using known city coordinates
        val cityCoords = mapOf(
            "Denver" to Pair(39.7392, -104.9903),
            "New York" to Pair(40.7128, -74.0060),
            "London" to Pair(51.5074, -0.1278),
            "Paris" to Pair(48.8566, 2.3522),
            "Tokyo" to Pair(35.6762, 139.6503),
            "Sydney" to Pair(-33.8688, 151.2093),
            "Berlin" to Pair(52.5200, 13.4050),
            "Toronto" to Pair(43.6532, -79.3832),
            "San Francisco" to Pair(37.7749, -122.4194),
            "Singapore" to Pair(1.3521, 103.8198),
            "Seoul" to Pair(37.5665, 126.9780),
            "Mumbai" to Pair(19.0760, 72.8777)
        )

        var totalDist = 0.0
        val byParent = participations.associateBy { it.id }

        for (p in participations) {
            val parent = p.parentParticipationId?.let { byParent[it] }
            if (parent != null && p.city != null && parent.city != null) {
                val c1 = cityCoords[p.city]
                val c2 = cityCoords[parent.city]
                if (c1 != null && c2 != null) {
                    totalDist += haversine(c1.first, c1.second, c2.first, c2.second)
                } else if (p.city != parent.city) {
                    totalDist += 450.0 // average regional hop
                }
            }
        }
        return (totalDist * 10.0).roundToInt() / 10.0
    }

    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}

class GenerateMilestoneUseCase {
    operator fun invoke(stats: RippleStats): List<MilestoneCard> {
        val cards = mutableListOf<MilestoneCard>()

        // 1. Participant Count Milestone
        if (stats.totalParticipants >= 100) {
            cards.add(
                MilestoneCard(
                    id = "ms_participants_${stats.challengeId}",
                    challengeId = stats.challengeId,
                    title = "🌊 MY RIPPLE JUST HIT ${stats.totalParticipants} PEOPLE",
                    highlightMetric = "${stats.totalParticipants}",
                    subtitle = "Started by @${stats.creatorUsername} · Spreading Fast",
                    generationBadge = "Generation ${stats.generationDepth}",
                    inviteUrl = "https://ripple.app/c/${stats.challengeId}",
                    prompt = stats.prompt,
                    themeColorHex = 0xFF00E5FF
                )
            )
        } else {
            cards.add(
                MilestoneCard(
                    id = "ms_growing_${stats.challengeId}",
                    challengeId = stats.challengeId,
                    title = "🌊 MY RIPPLE IS GROWING",
                    highlightMetric = "${stats.totalParticipants} Participants",
                    subtitle = "Can you keep the chain alive?",
                    generationBadge = "Generation ${stats.generationDepth}",
                    inviteUrl = "https://ripple.app/c/${stats.challengeId}",
                    prompt = stats.prompt,
                    themeColorHex = 0xFF00E5FF
                )
            )
        }

        // 2. Global Spread Milestone
        if (stats.countryCount >= 2 || stats.cityCount >= 3) {
            cards.add(
                MilestoneCard(
                    id = "ms_global_${stats.challengeId}",
                    challengeId = stats.challengeId,
                    title = "🌎 REACHED ${stats.countryCount} COUNTRIES & ${stats.cityCount} CITIES",
                    highlightMetric = "${stats.totalDistanceKm.toInt()} km",
                    subtitle = "Traveled across the globe from person to person",
                    generationBadge = "Generation ${stats.generationDepth}",
                    inviteUrl = "https://ripple.app/c/${stats.challengeId}",
                    prompt = stats.prompt,
                    themeColorHex = 0xFF00FFC6
                )
            )
        }

        // 3. Generation Depth Milestone
        if (stats.generationDepth >= 5) {
            cards.add(
                MilestoneCard(
                    id = "ms_gen_${stats.challengeId}",
                    challengeId = stats.challengeId,
                    title = "🔥 SURVIVED ${stats.generationDepth} GENERATIONS",
                    highlightMetric = "Gen ${stats.generationDepth}",
                    subtitle = "Passed forward unbroken through friend groups",
                    generationBadge = "${stats.totalParticipants} People Joined",
                    inviteUrl = "https://ripple.app/c/${stats.challengeId}",
                    prompt = stats.prompt,
                    themeColorHex = 0xFFFF6B6B
                )
            )
        }

        return cards
    }
}

class ContentModerationUseCase : ModerationRepository {
    private val blockedUsers = mutableSetOf<Pair<String, String>>() // (currentUserId, blockedUserId)
    private val reports = mutableListOf<ReportTicket>()

    private val prohibitedKeywords = listOf(
        "danger", "kill", "harm", "weapon", "drugs", "illegal", "nude",
        "sexual", "stunt", "drive fast", "password", "ssn", "credit card", "hate"
    )

    override suspend fun reportContent(ticket: ReportTicket): Result<Unit> {
        reports.add(ticket)
        return Result.success(Unit)
    }

    override suspend fun blockUser(currentUserId: String, blockedUserId: String): Result<Unit> {
        blockedUsers.add(Pair(currentUserId, blockedUserId))
        return Result.success(Unit)
    }

    override suspend fun isUserBlocked(currentUserId: String, targetUserId: String): Boolean {
        return blockedUsers.contains(Pair(currentUserId, targetUserId))
    }

    override fun validatePromptSafety(prompt: String): Result<Unit> {
        val lower = prompt.lowercase()
        for (kw in prohibitedKeywords) {
            if (lower.contains(kw)) {
                return Result.failure(
                    IllegalArgumentException("Challenge prompt contains disallowed safety phrase: '$kw'")
                )
            }
        }
        return Result.success(Unit)
    }
}
