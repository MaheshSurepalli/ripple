package com.example.ripple.ui.incoming

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ripple.domain.model.Challenge
import com.example.ripple.domain.model.InviteToken
import com.example.ripple.domain.model.ResponseType
import com.example.ripple.domain.model.User
import com.example.ripple.theme.*
import com.example.ripple.ui.components.CountdownTimerBadge
import com.example.ripple.ui.components.GlowCard
import com.example.ripple.ui.components.LockedInviterCard
import com.example.ripple.ui.components.PulsingWaveBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomingRippleScreen(
    challenge: Challenge,
    sender: User,
    inviteToken: InviteToken,
    onCompleteChallengeClick: () -> Unit,
    onReportClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Incoming Ripple 🌊",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = onReportClick) {
                        Icon(imageVector = Icons.Default.Flag, contentDescription = "Report", tint = TextMuted)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = OceanNight,
        modifier = modifier
    ) { paddingValues ->
        PulsingWaveBackground(modifier = Modifier.padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Inviter Header Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = OceanSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = RippleCyan,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = sender.displayName.take(1).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = OceanNight,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "@${sender.username} challenged you 👀",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = RippleCyan
                            )
                        )
                    }
                }

                // Challenge Prompt Card
                GlowCard(
                    glowColor = RippleCyan,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "THE CHALLENGE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 1.sp
                            )
                        )
                        CountdownTimerBadge(expiresAtMillis = challenge.expiresAt)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = challenge.prompt,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (challenge.responseType == ResponseType.PHOTO) Icons.Default.CameraAlt else Icons.Default.TextFields,
                            contentDescription = null,
                            tint = RippleTeal,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (challenge.responseType == ResponseType.PHOTO) "Respond with Photo" else "Respond with Text",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = RippleTeal,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "• Gen ${inviteToken.generation + 1}",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                }

                // Mystery Locked Card
                LockedInviterCard(
                    inviterUsername = sender.username,
                    prompt = challenge.prompt
                )

                // Privacy Note Callout
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = OceanSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = RippleAqua,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Chain Privacy: Your submission will only be revealed to @${sender.username} and people you invite directly.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Complete Challenge Button
                Button(
                    onClick = onCompleteChallengeClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RippleCyan,
                        contentColor = OceanNight
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Icon(
                        imageVector = if (challenge.responseType == ResponseType.PHOTO) Icons.Default.CameraAlt else Icons.Default.Edit,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Complete Challenge",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
