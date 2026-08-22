package com.example.ripple

import android.app.Application
import com.example.ripple.data.remote.FakeAuthRepository
import com.example.ripple.data.remote.FakeChallengeRepository
import com.example.ripple.data.remote.FakeNotificationRepository
import com.example.ripple.domain.repository.AuthRepository
import com.example.ripple.domain.repository.ChallengeRepository
import com.example.ripple.domain.repository.ModerationRepository
import com.example.ripple.domain.repository.NotificationRepository
import com.example.ripple.domain.usecase.*

class RippleApplication : Application() {
    lateinit var authRepository: AuthRepository
    lateinit var challengeRepository: ChallengeRepository
    lateinit var notificationRepository: NotificationRepository
    lateinit var moderationRepository: ModerationRepository

    lateinit var createChallengeUseCase: CreateChallengeUseCase
    lateinit var resolveInviteUseCase: ResolveInviteUseCase
    lateinit var submitResponseAtomicUseCase: SubmitResponseAtomicUseCase
    lateinit var fetchUnlockedResponseUseCase: FetchUnlockedResponseUseCase
    lateinit var generateInvitesUseCase: GenerateInvitesUseCase
    lateinit var buildRippleTreeUseCase: BuildRippleTreeUseCase
    lateinit var calculateStatsUseCase: CalculateStatsUseCase
    lateinit var generateMilestoneUseCase: GenerateMilestoneUseCase

    override fun onCreate() {
        super.onCreate()
        instance = this

        authRepository = FakeAuthRepository()
        calculateStatsUseCase = CalculateStatsUseCase()
        challengeRepository = FakeChallengeRepository(authRepository, calculateStatsUseCase)
        notificationRepository = FakeNotificationRepository()
        moderationRepository = ContentModerationUseCase()

        createChallengeUseCase = CreateChallengeUseCase(challengeRepository, moderationRepository)
        resolveInviteUseCase = ResolveInviteUseCase(challengeRepository)
        submitResponseAtomicUseCase = SubmitResponseAtomicUseCase(challengeRepository)
        fetchUnlockedResponseUseCase = FetchUnlockedResponseUseCase(challengeRepository)
        generateInvitesUseCase = GenerateInvitesUseCase(challengeRepository)
        buildRippleTreeUseCase = BuildRippleTreeUseCase()
        generateMilestoneUseCase = GenerateMilestoneUseCase()
    }

    companion object {
        lateinit var instance: RippleApplication
            private set
    }
}
