package com.example.ripple

import android.content.Intent
import android.net.Uri
import com.example.ripple.data.deeplink.DeepLinkResolver
import com.example.ripple.data.remote.FakeAuthRepository
import com.example.ripple.data.remote.FakeChallengeRepository
import com.example.ripple.domain.model.*
import com.example.ripple.domain.usecase.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RippleUnitTests {

    private lateinit var authRepo: FakeAuthRepository
    private lateinit var challengeRepo: FakeChallengeRepository
    private lateinit var createChallengeUseCase: CreateChallengeUseCase
    private lateinit var resolveInviteUseCase: ResolveInviteUseCase
    private lateinit var submitResponseAtomicUseCase: SubmitResponseAtomicUseCase
    private lateinit var fetchUnlockedResponseUseCase: FetchUnlockedResponseUseCase
    private lateinit var buildRippleTreeUseCase: BuildRippleTreeUseCase
    private lateinit var calculateStatsUseCase: CalculateStatsUseCase
    private lateinit var generateMilestoneUseCase: GenerateMilestoneUseCase
    private lateinit var moderationUseCase: ContentModerationUseCase

    @Before
    fun setup() {
        authRepo = FakeAuthRepository()
        calculateStatsUseCase = CalculateStatsUseCase()
        challengeRepo = FakeChallengeRepository(authRepo, calculateStatsUseCase)
        moderationUseCase = ContentModerationUseCase()

        createChallengeUseCase = CreateChallengeUseCase(challengeRepo, moderationUseCase)
        resolveInviteUseCase = ResolveInviteUseCase(challengeRepo)
        submitResponseAtomicUseCase = SubmitResponseAtomicUseCase(challengeRepo)
        fetchUnlockedResponseUseCase = FetchUnlockedResponseUseCase(challengeRepo)
        buildRippleTreeUseCase = BuildRippleTreeUseCase()
        generateMilestoneUseCase = GenerateMilestoneUseCase()
    }

    @Test
    fun testCreateChallenge_generatesThreeTokensAndGen0() = runBlocking {
        val siva = authRepo.currentUser.value!!
        val result = createChallengeUseCase(
            creator = siva,
            prompt = "Show the shoes you're wearing 👟",
            responseType = ResponseType.PHOTO,
            expirationHours = 24,
            initialResponseText = "White sneakers",
            initialPhotoUri = "shoes.jpg",
            city = "Denver",
            country = "United States"
        )

        assertTrue(result.isSuccess)
        val created = result.getOrThrow()
        assertEquals(siva.id, created.challenge.creatorId)
        assertEquals(0, created.creatorParticipation.generation)
        assertEquals(ParticipationStatus.SUBMITTED, created.creatorParticipation.status)
        assertEquals(3, created.inviteTokens.size)
        assertTrue(created.inviteTokens.all { it.status == InviteStatus.PENDING })
    }

    @Test
    fun testCreateChallenge_rejectsUnsafePrompt() = runBlocking {
        val siva = authRepo.currentUser.value!!
        val result = createChallengeUseCase(
            creator = siva,
            prompt = "Drive fast without looking at the road",
            responseType = ResponseType.PHOTO
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("disallowed safety phrase") == true)
    }

    @Test
    fun testResolveInviteToken_returnsChallengeAndSender() = runBlocking {
        val result = resolveInviteUseCase("tok_alex_pending")
        assertTrue(result.isSuccess)
        val resolved = result.getOrThrow()
        assertEquals("chal_view_now", resolved.challenge.id)
        assertEquals("user_siva", resolved.sender.id)
        assertEquals(InviteStatus.PENDING, resolved.inviteToken.status)
    }

    @Test
    fun testAtomicSubmission_incrementsGenerationAndMarksTokenConsumed() = runBlocking {
        val alex = User("user_alex", "alex", "Alex")
        val result = submitResponseAtomicUseCase(
            inviteToken = "tok_alex_pending",
            user = alex,
            responseText = "Coffee mug on my desk",
            photoUri = "alex_desk.jpg",
            city = "Denver",
            country = "United States"
        )

        assertTrue(result.isSuccess)
        val participation = result.getOrThrow()
        assertEquals(1, participation.generation) // Parent generation was 0 -> child is 1
        assertEquals("user_siva", participation.parentUserId)
        assertEquals(ParticipationStatus.SUBMITTED, participation.status)

        // Verify token is now CONSUMED
        val tokenAfter = challengeRepo.getInviteToken("tok_alex_pending")
        assertNotNull(tokenAfter)
        assertEquals(InviteStatus.CONSUMED, tokenAfter!!.status)
        assertEquals("user_alex", tokenAfter.consumedByUserId)

        // Verify challenge stats updated
        val challenge = challengeRepo.getChallenge("chal_view_now")
        assertNotNull(challenge)
        assertTrue(challenge!!.participantCount >= 5)
    }

    @Test
    fun testAntiCheating_rejectsDuplicateParticipationBySameUser() = runBlocking {
        val siva = authRepo.currentUser.value!!
        // Siva already participated in chal_view_now as creator
        val result = submitResponseAtomicUseCase(
            inviteToken = "tok_alex_pending",
            user = siva,
            responseText = "Trying to cheat",
            photoUri = "cheat.jpg",
            city = "Denver",
            country = "United States"
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("already participated") == true)
    }

    @Test
    fun testRevealPrivacy_authorizesDirectChainAndBlocksStrangers() = runBlocking {
        // Sarah participated as child of Siva (parentUserId = user_siva)
        val sivaResponseForSarah = fetchUnlockedResponseUseCase(
            challengeId = "chal_view_now",
            authorUserId = "user_siva",
            requestingUserId = "user_sarah"
        )
        assertTrue(sivaResponseForSarah.isSuccess)

        // Stranger (user_stranger) who hasn't participated cannot reveal Siva's response
        val strangerResponse = fetchUnlockedResponseUseCase(
            challengeId = "chal_view_now",
            authorUserId = "user_siva",
            requestingUserId = "user_stranger"
        )
        assertTrue(strangerResponse.isFailure)
    }

    @Test
    fun testRippleTreeReconstruction() = runBlocking {
        val participations = challengeRepo.getParticipations("chal_view_now")
        val tree = buildRippleTreeUseCase(participations, "user_siva")

        assertNotNull(tree)
        assertEquals("user_siva", tree!!.participation.userId)
        assertEquals(0, tree.participation.generation)
        assertTrue(tree.children.isNotEmpty())
    }

    @Test
    fun testStatsAndMilestoneCardGeneration() = runBlocking {
        val stats = challengeRepo.getRippleStats("chal_view_now")
        assertNotNull(stats)
        assertTrue(stats!!.totalParticipants >= 4)
        assertTrue(stats.generationDepth >= 2)
        assertTrue(stats.countryCount >= 2)

        val cards = generateMilestoneUseCase(stats)
        assertTrue(cards.isNotEmpty())
        assertTrue(cards.any { it.title.contains("RIPPLE") || it.title.contains("REACHED") })
    }
}
