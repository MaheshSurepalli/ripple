package com.example.ripple.ui.reveal

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
import com.example.ripple.domain.model.PrivateResponse
import com.example.ripple.domain.model.ResponseType
import com.example.ripple.domain.model.User
import com.example.ripple.theme.*
import com.example.ripple.ui.components.GlowCard
import com.example.ripple.ui.components.PulsingWaveBackground
import com.example.ripple.ui.components.UnlockAnimationView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevealScreen(
    challenge: Challenge,
    inviter: User,
    inviterResponse: PrivateResponse?,
    generation: Int,
    onKeepItGoingClick: () -> Unit,
    onWatchRippleClick: () -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isAnimationComplete by remember { mutableStateOf(false) }

    if (!isAnimationComplete) {
        PulsingWaveBackground {
            UnlockAnimationView(onAnimationComplete = { isAnimationComplete = true })
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "🔓 Revealed!",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = RippleTeal
                            )
                        )
                    },
                    actions = {
                        TextButton(onClick = onHomeClick) {
                            Text("Done", color = TextPrimary)
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
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Success Banner
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = RippleTeal.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, RippleTeal)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(text = "🌊", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "You're Generation $generation of this Ripple!",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = RippleTeal
                                )
                            )
                        }
                    }

                    // Revealed Response Card
                    GlowCard(
                        glowColor = RippleTeal,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = RippleCyan,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = inviter.displayName.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = OceanNight,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Here's what @${inviter.username} submitted:",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = challenge.prompt,
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Photo or Text Content Container
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = OceanSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                if (challenge.responseType == ResponseType.PHOTO) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(OceanNight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = Icons.Default.Image,
                                                contentDescription = null,
                                                tint = RippleCyan,
                                                modifier = Modifier.size(48.dp)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "📸 Photo Unlocked",
                                                color = RippleCyan,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                }

                                Text(
                                    text = inviterResponse?.responseText ?: "Here is my response to the challenge! 🌊",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                )
                            }
                        }
                    }

                    // Keep It Going Callout
                    GlowCard(glowColor = RippleCyan) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Keep the Ripple Going!",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Send this challenge to 3 friends to expand your branch.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = onKeepItGoingClick,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RippleCyan,
                                    contentColor = OceanNight
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Send to 3 Friends 🌊",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Watch Ripple Tree CTA
                    OutlinedButton(
                        onClick = onWatchRippleClick,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RippleTeal),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RippleTeal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AccountTree, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Watch the Ripple Tree",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}
