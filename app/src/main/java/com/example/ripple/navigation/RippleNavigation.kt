package com.example.ripple.navigation

sealed interface RippleDestination {
    data object Home : RippleDestination
    data object CreateRipple : RippleDestination
    data class InviteShare(val challengeId: String) : RippleDestination
    data class IncomingRipple(val inviteToken: String) : RippleDestination
    data class CompleteChallenge(val inviteToken: String) : RippleDestination
    data class Reveal(val challengeId: String, val inviterUserId: String, val generation: Int) : RippleDestination
    data class RippleVisualizer(val challengeId: String) : RippleDestination
    data class RippleStats(val challengeId: String) : RippleDestination
    data class ShareMilestone(val challengeId: String) : RippleDestination
    data object Profile : RippleDestination
    data object Notifications : RippleDestination
}
