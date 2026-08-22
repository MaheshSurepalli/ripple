package com.example.ripple.ui.main

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ripple.RippleApplication
import com.example.ripple.data.deeplink.DeepLinkResolver
import com.example.ripple.domain.model.*
import com.example.ripple.navigation.RippleDestination
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class RippleViewModel(
    private val app: RippleApplication = RippleApplication.instance
) : ViewModel() {

    private val authRepo = app.authRepository
    private val challengeRepo = app.challengeRepository
    private val notifRepo = app.notificationRepository
    private val moderationRepo = app.moderationRepository

    val currentUser = authRepo.currentUser

    private val _currentDestination = MutableStateFlow<RippleDestination>(RippleDestination.Home)
    val currentDestination: StateFlow<RippleDestination> = _currentDestination.asStateFlow()

    private val _isSimulatorOpen = MutableStateFlow(false)
    val isSimulatorOpen: StateFlow<Boolean> = _isSimulatorOpen.asStateFlow()

    private val _reportTarget = MutableStateFlow<Pair<String, String>?>(null) // (type, id)
    val reportTarget: StateFlow<Pair<String, String>?> = _reportTarget.asStateFlow()

    // Real-time reactive challenge flows
    val waitingChallenges: StateFlow<List<Pair<Challenge, InviteToken>>> = currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else challengeRepo.observeWaitingChallenges(user.id)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val createdChallenges: StateFlow<List<Challenge>> = currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else challengeRepo.observeCreatedChallenges(user.id)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val joinedChallenges: StateFlow<List<Challenge>> = currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else challengeRepo.observeJoinedChallenges(user.id)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val notifications: StateFlow<List<NotificationItem>> = currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else notifRepo.observeNotifications(user.id)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun navigateTo(destination: RippleDestination) {
        _currentDestination.value = destination
    }

    fun popBackStack() {
        _currentDestination.value = RippleDestination.Home
    }

    fun openSimulator() {
        _isSimulatorOpen.value = true
    }

    fun closeSimulator() {
        _isSimulatorOpen.value = false
    }

    fun openReportDialog(type: String, id: String) {
        _reportTarget.value = Pair(type, id)
    }

    fun closeReportDialog() {
        _reportTarget.value = null
    }

    fun handleIntent(intent: Intent?) {
        val inviteToken = DeepLinkResolver.extractInviteToken(intent)
        if (inviteToken != null) {
            _currentDestination.value = RippleDestination.IncomingRipple(inviteToken)
        }
    }

    fun switchUser(userId: String) {
        viewModelScope.launch {
            authRepo.signInDemoUser(userId)
            _currentDestination.value = RippleDestination.Home
        }
    }

    fun getAvailableDemoUsers(): List<User> = authRepo.getAvailableDemoUsers()

    suspend fun createChallenge(
        prompt: String,
        responseType: ResponseType,
        expirationHours: Int,
        initialText: String?,
        initialPhotoUri: String?
    ): Result<String> {
        val user = currentUser.value ?: return Result.failure(IllegalStateException("Not logged in"))
        val result = app.createChallengeUseCase(
            creator = user,
            prompt = prompt,
            responseType = responseType,
            expirationHours = expirationHours,
            initialResponseText = initialText,
            initialPhotoUri = initialPhotoUri
        )
        return result.map { it.challenge.id }
    }

    suspend fun getChallenge(challengeId: String): Challenge? {
        return challengeRepo.getChallenge(challengeId)
    }

    suspend fun getInvitesForUser(challengeId: String): List<InviteToken> {
        val user = currentUser.value ?: return emptyList()
        return challengeRepo.getInvitesForUser(challengeId, user.id)
    }

    suspend fun resolveInvite(token: String) = app.resolveInviteUseCase(token)

    suspend fun submitResponse(
        inviteToken: String,
        responseText: String?,
        photoUri: String?,
        city: String?,
        country: String?
    ): Result<Unit> {
        val user = currentUser.value ?: return Result.failure(IllegalStateException("Not logged in"))
        val result = app.submitResponseAtomicUseCase(
            inviteToken = inviteToken,
            user = user,
            responseText = responseText,
            photoUri = photoUri,
            city = city,
            country = country
        )
        return result.map { }
    }

    suspend fun fetchUnlockedResponse(challengeId: String, authorUserId: String): Result<PrivateResponse> {
        val user = currentUser.value ?: return Result.failure(IllegalStateException("Not logged in"))
        return app.fetchUnlockedResponseUseCase(
            challengeId = challengeId,
            authorUserId = authorUserId,
            requestingUserId = user.id
        )
    }

    suspend fun getParticipations(challengeId: String): List<Participation> {
        return challengeRepo.getParticipations(challengeId)
    }

    suspend fun getStats(challengeId: String): RippleStats? {
        return challengeRepo.getRippleStats(challengeId)
    }

    fun generateMilestoneCards(stats: RippleStats): List<MilestoneCard> {
        return app.generateMilestoneUseCase(stats)
    }

    suspend fun simulateSpread(challengeId: String): Result<Unit> {
        val user = currentUser.value ?: return Result.failure(IllegalStateException("Not logged in"))
        val result = challengeRepo.simulateBranchExpansion(challengeId, user.id)
        return result.map { }
    }

    fun submitReport(reason: ReportReason, notes: String?) {
        val target = _reportTarget.value ?: return
        val user = currentUser.value ?: return
        viewModelScope.launch {
            moderationRepo.reportContent(
                ReportTicket(
                    id = "rep_${System.currentTimeMillis()}",
                    targetType = target.first,
                    targetId = target.second,
                    reporterUserId = user.id,
                    reason = reason,
                    additionalComments = notes
                )
            )
        }
    }

    fun blockUser() {
        val target = _reportTarget.value ?: return
        val user = currentUser.value ?: return
        viewModelScope.launch {
            moderationRepo.blockUser(user.id, target.second)
        }
    }
}
