package com.example.ripple.ui.main

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.ripple.domain.model.*
import com.example.ripple.navigation.RippleDestination
import com.example.ripple.theme.RippleCyan
import com.example.ripple.theme.RippleTheme
import com.example.ripple.ui.complete.CompleteChallengeScreen
import com.example.ripple.ui.create.CreateRippleScreen
import com.example.ripple.ui.home.HomeScreen
import com.example.ripple.ui.incoming.IncomingRippleScreen
import com.example.ripple.ui.invite.InviteShareScreen
import com.example.ripple.ui.milestone.ShareMilestoneScreen
import com.example.ripple.ui.moderation.ReportBlockDialog
import com.example.ripple.ui.notifications.NotificationsScreen
import com.example.ripple.ui.profile.ProfileScreen
import com.example.ripple.ui.reveal.RevealScreen
import com.example.ripple.ui.simulator.DemoSimulatorModal
import com.example.ripple.ui.stats.RippleStatsScreen
import com.example.ripple.ui.visualizer.RippleVisualizerScreen
import kotlinx.coroutines.launch

@Composable
fun RippleMainApp(
    viewModel: RippleViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val destination by viewModel.currentDestination.collectAsState()
    val waitingChallenges by viewModel.waitingChallenges.collectAsState()
    val createdChallenges by viewModel.createdChallenges.collectAsState()
    val joinedChallenges by viewModel.joinedChallenges.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val isSimulatorOpen by viewModel.isSimulatorOpen.collectAsState()
    val reportTarget by viewModel.reportTarget.collectAsState()

    RippleTheme {
        Box(modifier = modifier.fillMaxSize()) {
            Crossfade(targetState = destination, label = "screen_transition") { currentDest ->
                when (currentDest) {
                    is RippleDestination.Home -> {
                        HomeScreen(
                            currentUser = currentUser,
                            waitingChallenges = waitingChallenges,
                            createdChallenges = createdChallenges,
                            joinedChallenges = joinedChallenges,
                            onStartRippleClick = { viewModel.navigateTo(RippleDestination.CreateRipple) },
                            onIncomingChallengeClick = { token ->
                                viewModel.navigateTo(RippleDestination.IncomingRipple(token))
                            },
                            onViewRippleClick = { id ->
                                viewModel.navigateTo(RippleDestination.RippleVisualizer(id))
                            },
                            onStatsClick = { id ->
                                viewModel.navigateTo(RippleDestination.RippleStats(id))
                            },
                            onShareInviteClick = { id ->
                                viewModel.navigateTo(RippleDestination.InviteShare(id))
                            },
                            onNotificationsClick = { viewModel.navigateTo(RippleDestination.Notifications) },
                            onProfileClick = { viewModel.navigateTo(RippleDestination.Profile) },
                            onSimulatorClick = { viewModel.openSimulator() }
                        )
                    }

                    is RippleDestination.CreateRipple -> {
                        CreateRippleScreen(
                            currentUser = currentUser,
                            onBackClick = { viewModel.popBackStack() },
                            onRippleCreated = { challengeId ->
                                viewModel.navigateTo(RippleDestination.InviteShare(challengeId))
                            },
                            onCreateChallenge = { prompt, responseType, expHours, initText, initPhoto ->
                                viewModel.createChallenge(prompt, responseType, expHours, initText, initPhoto)
                            }
                        )
                    }

                    is RippleDestination.InviteShare -> {
                        var challenge by remember { mutableStateOf<Challenge?>(null) }
                        var tokens by remember { mutableStateOf<List<InviteToken>>(emptyList()) }

                        LaunchedEffect(currentDest.challengeId) {
                            challenge = viewModel.getChallenge(currentDest.challengeId)
                            tokens = viewModel.getInvitesForUser(currentDest.challengeId)
                        }

                        if (challenge != null) {
                            InviteShareScreen(
                                challenge = challenge!!,
                                inviteTokens = tokens,
                                onWatchRippleClick = { id ->
                                    viewModel.navigateTo(RippleDestination.RippleVisualizer(id))
                                },
                                onHomeClick = { viewModel.navigateTo(RippleDestination.Home) }
                            )
                        } else {
                            LoadingView()
                        }
                    }

                    is RippleDestination.IncomingRipple -> {
                        var resolved by remember { mutableStateOf<com.example.ripple.domain.repository.ResolvedInvite?>(null) }

                        LaunchedEffect(currentDest.inviteToken) {
                            val res = viewModel.resolveInvite(currentDest.inviteToken)
                            if (res.isSuccess) {
                                resolved = res.getOrNull()
                            }
                        }

                        if (resolved != null) {
                            IncomingRippleScreen(
                                challenge = resolved!!.challenge,
                                sender = resolved!!.sender,
                                inviteToken = resolved!!.inviteToken,
                                onCompleteChallengeClick = {
                                    viewModel.navigateTo(RippleDestination.CompleteChallenge(currentDest.inviteToken))
                                },
                                onReportClick = {
                                    viewModel.openReportDialog("Challenge", resolved!!.challenge.id)
                                },
                                onBackClick = { viewModel.popBackStack() }
                            )
                        } else {
                            LoadingView()
                        }
                    }

                    is RippleDestination.CompleteChallenge -> {
                        var resolved by remember { mutableStateOf<com.example.ripple.domain.repository.ResolvedInvite?>(null) }

                        LaunchedEffect(currentDest.inviteToken) {
                            val res = viewModel.resolveInvite(currentDest.inviteToken)
                            if (res.isSuccess) {
                                resolved = res.getOrNull()
                            }
                        }

                        if (resolved != null) {
                            CompleteChallengeScreen(
                                challenge = resolved!!.challenge,
                                sender = resolved!!.sender,
                                inviteToken = resolved!!.inviteToken,
                                currentUser = currentUser,
                                onSubmissionSuccess = { challengeId, senderUserId ->
                                    viewModel.navigateTo(
                                        RippleDestination.Reveal(
                                            challengeId = challengeId,
                                            inviterUserId = senderUserId,
                                            generation = resolved!!.inviteToken.generation + 1
                                        )
                                    )
                                },
                                onSubmitResponse = { token, text, photo, city, country ->
                                    viewModel.submitResponse(token, text, photo, city, country)
                                },
                                onBackClick = { viewModel.popBackStack() }
                            )
                        } else {
                            LoadingView()
                        }
                    }

                    is RippleDestination.Reveal -> {
                        var challenge by remember { mutableStateOf<Challenge?>(null) }
                        var inviterUser by remember { mutableStateOf<User?>(null) }
                        var unlockedResponse by remember { mutableStateOf<PrivateResponse?>(null) }

                        LaunchedEffect(currentDest.challengeId, currentDest.inviterUserId) {
                            challenge = viewModel.getChallenge(currentDest.challengeId)
                            inviterUser = viewModel.getAvailableDemoUsers().firstOrNull { it.id == currentDest.inviterUserId }
                                ?: User(currentDest.inviterUserId, "friend", "Friend")
                            val resp = viewModel.fetchUnlockedResponse(currentDest.challengeId, currentDest.inviterUserId)
                            unlockedResponse = resp.getOrNull()
                        }

                        if (challenge != null && inviterUser != null) {
                            RevealScreen(
                                challenge = challenge!!,
                                inviter = inviterUser!!,
                                inviterResponse = unlockedResponse,
                                generation = currentDest.generation,
                                onKeepItGoingClick = {
                                    viewModel.navigateTo(RippleDestination.InviteShare(currentDest.challengeId))
                                },
                                onWatchRippleClick = {
                                    viewModel.navigateTo(RippleDestination.RippleVisualizer(currentDest.challengeId))
                                },
                                onHomeClick = { viewModel.navigateTo(RippleDestination.Home) }
                            )
                        } else {
                            LoadingView()
                        }
                    }

                    is RippleDestination.RippleVisualizer -> {
                        var challenge by remember { mutableStateOf<Challenge?>(null) }
                        var participations by remember { mutableStateOf<List<Participation>>(emptyList()) }

                        LaunchedEffect(currentDest.challengeId) {
                            challenge = viewModel.getChallenge(currentDest.challengeId)
                            participations = viewModel.getParticipations(currentDest.challengeId)
                        }

                        if (challenge != null) {
                            RippleVisualizerScreen(
                                challenge = challenge!!,
                                participations = participations,
                                currentUser = currentUser,
                                onStatsClick = { id -> viewModel.navigateTo(RippleDestination.RippleStats(id)) },
                                onShareMilestoneClick = { id -> viewModel.navigateTo(RippleDestination.ShareMilestone(id)) },
                                onBackClick = { viewModel.popBackStack() }
                            )
                        } else {
                            LoadingView()
                        }
                    }

                    is RippleDestination.RippleStats -> {
                        var stats by remember { mutableStateOf<RippleStats?>(null) }

                        LaunchedEffect(currentDest.challengeId) {
                            stats = viewModel.getStats(currentDest.challengeId)
                        }

                        RippleStatsScreen(
                            stats = stats,
                            onShareMilestoneClick = { id -> viewModel.navigateTo(RippleDestination.ShareMilestone(id)) },
                            onBackClick = { viewModel.popBackStack() }
                        )
                    }

                    is RippleDestination.ShareMilestone -> {
                        var stats by remember { mutableStateOf<RippleStats?>(null) }
                        var cards by remember { mutableStateOf<List<MilestoneCard>>(emptyList()) }

                        LaunchedEffect(currentDest.challengeId) {
                            stats = viewModel.getStats(currentDest.challengeId)
                            if (stats != null) {
                                cards = viewModel.generateMilestoneCards(stats!!)
                            }
                        }

                        ShareMilestoneScreen(
                            milestoneCards = cards,
                            onBackClick = { viewModel.popBackStack() }
                        )
                    }

                    is RippleDestination.Profile -> {
                        ProfileScreen(
                            currentUser = currentUser,
                            onSignOut = { viewModel.switchUser("user_siva") },
                            onBackClick = { viewModel.popBackStack() }
                        )
                    }

                    is RippleDestination.Notifications -> {
                        NotificationsScreen(
                            notifications = notifications,
                            onNotificationClick = { notif ->
                                if (notif.inviteToken != null) {
                                    viewModel.navigateTo(RippleDestination.IncomingRipple(notif.inviteToken))
                                } else if (notif.challengeId != null) {
                                    viewModel.navigateTo(RippleDestination.RippleVisualizer(notif.challengeId))
                                }
                            },
                            onBackClick = { viewModel.popBackStack() }
                        )
                    }
                }
            }

            // Test Simulator Sheet
            if (isSimulatorOpen) {
                DemoSimulatorModal(
                    currentUser = currentUser,
                    availableDemoUsers = viewModel.getAvailableDemoUsers(),
                    onSwitchUser = { userId -> viewModel.switchUser(userId) },
                    onSimulateSpread = { challengeId -> viewModel.simulateSpread(challengeId) },
                    onDismiss = { viewModel.closeSimulator() }
                )
            }

            // Moderation Report / Block Dialog
            reportTarget?.let { target ->
                ReportBlockDialog(
                    targetType = target.first,
                    targetName = target.second,
                    onReportSubmitted = { reason, notes -> viewModel.submitReport(reason, notes) },
                    onBlockUser = { viewModel.blockUser() },
                    onDismiss = { viewModel.closeReportDialog() }
                )
            }
        }
    }
}

@Composable
fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = RippleCyan)
    }
}
