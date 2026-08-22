package com.example.ripple.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ripple.domain.model.*
import com.example.ripple.theme.*
import com.example.ripple.ui.components.CountdownTimerBadge
import com.example.ripple.ui.components.GlowCard
import com.example.ripple.ui.components.PulsingWaveBackground
import com.example.ripple.ui.components.RippleTopBar

@Composable
fun HomeScreen(
    currentUser: User?,
    waitingChallenges: List<Pair<Challenge, InviteToken>>,
    createdChallenges: List<Challenge>,
    joinedChallenges: List<Challenge>,
    onStartRippleClick: () -> Unit,
    onIncomingChallengeClick: (String) -> Unit, // inviteToken
    onViewRippleClick: (String) -> Unit, // challengeId
    onStatsClick: (String) -> Unit,
    onShareInviteClick: (String) -> Unit, // challengeId
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSimulatorClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            RippleTopBar(
                currentUser = currentUser,
                onAvatarClick = onProfileClick,
                onNotificationsClick = onNotificationsClick,
                onSimulatorClick = onSimulatorClick,
                unreadNotifications = 1
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onStartRippleClick,
                containerColor = RippleCyan,
                contentColor = OceanNight,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Start Ripple")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Start Ripple",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        containerColor = OceanNight,
        modifier = modifier
    ) { paddingValues ->
        PulsingWaveBackground(modifier = Modifier.padding(paddingValues)) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // User Stats Summary Card
                item {
                    UserStatsHeader(currentUser = currentUser)
                }

                // Section 1: Waiting for You
                if (waitingChallenges.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Waiting for You",
                            badgeText = "${waitingChallenges.size}",
                            icon = Icons.Default.FlashOn,
                            tint = RippleCoral
                        )
                    }
                    items(waitingChallenges) { (challenge, inviteToken) ->
                        WaitingChallengeCard(
                            challenge = challenge,
                            inviteToken = inviteToken,
                            onClick = { onIncomingChallengeClick(inviteToken.token) }
                        )
                    }
                }

                // Section 2: Your Ripples
                item {
                    SectionHeader(
                        title = "Your Ripples",
                        badgeText = "${createdChallenges.size}",
                        icon = Icons.Default.WaterDrop,
                        tint = RippleCyan
                    )
                }
                if (createdChallenges.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "No Ripples Started Yet",
                            description = "Start a challenge in 30 seconds and watch it travel around the world!",
                            buttonText = "+ Start Your First Ripple",
                            onClick = onStartRippleClick
                        )
                    }
                } else {
                    items(createdChallenges) { challenge ->
                        CreatedRippleCard(
                            challenge = challenge,
                            onViewTree = { onViewRippleClick(challenge.id) },
                            onStats = { onStatsClick(challenge.id) },
                            onShare = { onShareInviteClick(challenge.id) }
                        )
                    }
                }

                // Section 3: Ripples You Joined
                if (joinedChallenges.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Ripples You Joined",
                            badgeText = "${joinedChallenges.size}",
                            icon = Icons.Default.Hub,
                            tint = RippleTeal
                        )
                    }
                    items(joinedChallenges) { challenge ->
                        JoinedRippleCard(
                            challenge = challenge,
                            onViewTree = { onViewRippleClick(challenge.id) },
                            onStats = { onStatsClick(challenge.id) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }
}

@Composable
fun UserStatsHeader(currentUser: User?) {
    GlowCard(
        glowColor = RippleCyan,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(
                    text = "Welcome back,",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
                Text(
                    text = "@${currentUser?.username ?: "friend"}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = OceanSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = "🌊", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "TOTAL REACH",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                color = TextSecondary
                            )
                        )
                        Text(
                            text = "${currentUser?.totalPeopleReached ?: 0} people",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = RippleCyan
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    badgeText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            shape = CircleShape,
            color = tint.copy(alpha = 0.2f),
            modifier = Modifier.size(20.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = tint,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
fun WaitingChallengeCard(
    challenge: Challenge,
    inviteToken: InviteToken,
    onClick: () -> Unit
) {
    GlowCard(
        glowColor = RippleCoral,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = RippleCoral.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, RippleCoral),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked challenge",
                            tint = RippleCoral,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "👀 @${inviteToken.senderUsername} challenged you",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = RippleCoral
                        )
                    )
                    Text(
                        text = challenge.prompt,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        ),
                        maxLines = 2
                    )
                }
            }
            CountdownTimerBadge(expiresAtMillis = challenge.expiresAt)
        }
    }
}

@Composable
fun CreatedRippleCard(
    challenge: Challenge,
    onViewTree: () -> Unit,
    onStats: () -> Unit,
    onShare: () -> Unit
) {
    GlowCard(
        glowColor = RippleCyan,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = challenge.prompt,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = RippleCyan.copy(alpha = 0.15f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "🌊 ${challenge.participantCount} people",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = RippleCyan
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = RippleTeal.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Gen ${challenge.generationCount}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = RippleTeal
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "↑ Growing",
                        style = MaterialTheme.typography.labelSmall.copy(color = RippleGreen)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = onViewTree,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, RippleCyan),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = RippleCyan),
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Default.AccountTree, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("View Tree", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = onStats,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Stats", fontSize = 12.sp)
            }

            Button(
                onClick = onShare,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RippleCyan, contentColor = OceanNight),
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Invite", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun JoinedRippleCard(
    challenge: Challenge,
    onViewTree: () -> Unit,
    onStats: () -> Unit
) {
    GlowCard(
        glowColor = RippleTeal,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "@${challenge.creatorUsername}'s Ripple",
                    style = MaterialTheme.typography.labelSmall.copy(color = RippleTeal)
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = RippleTeal.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Gen ${challenge.generationCount}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = RippleTeal
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = challenge.prompt,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🌊 ${challenge.participantCount} participants across ${challenge.countryCount} countries",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
                Row {
                    IconButton(onClick = onViewTree) {
                        Icon(imageVector = Icons.Default.AccountTree, contentDescription = "Tree", tint = RippleCyan)
                    }
                    IconButton(onClick = onStats) {
                        Icon(imageVector = Icons.Default.BarChart, contentDescription = "Stats", tint = TextPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    description: String,
    buttonText: String,
    onClick: () -> Unit
) {
    GlowCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "🌊", fontSize = 36.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RippleCyan, contentColor = OceanNight)
            ) {
                Text(buttonText, fontWeight = FontWeight.Bold)
            }
        }
    }
}
