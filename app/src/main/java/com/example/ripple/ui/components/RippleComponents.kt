package com.example.ripple.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ripple.domain.model.User
import com.example.ripple.theme.*
import kotlinx.coroutines.delay

@Composable
fun PulsingWaveBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_transition")
    val waveProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_progress"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OceanNight)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height * 0.25f)
            val maxRadius = size.width * 0.9f

            // Draw 3 expanding concentric rings
            for (i in 0..2) {
                val offsetProgress = (waveProgress + i * 0.33f) % 1f
                val radius = maxRadius * offsetProgress
                val alpha = ((1f - offsetProgress) * 0.15f).coerceIn(0f, 1f)

                drawCircle(
                    color = RippleCyan.copy(alpha = alpha),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RippleTopBar(
    currentUser: User?,
    onAvatarClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSimulatorClick: () -> Unit,
    unreadNotifications: Int = 0,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Ripple",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        color = RippleCyan
                    )
                )
                Text(
                    text = " 🌊",
                    fontSize = 20.sp
                )
            }
        },
        actions = {
            // Simulator Switcher Button
            IconButton(onClick = onSimulatorClick) {
                Badge(containerColor = RippleTeal.copy(alpha = 0.2f)) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = "Test Simulator",
                        tint = RippleTeal
                    )
                }
            }

            // Notifications
            IconButton(onClick = onNotificationsClick) {
                BadgedBox(
                    badge = {
                        if (unreadNotifications > 0) {
                            Badge(containerColor = RippleCoral) {
                                Text("$unreadNotifications")
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = TextPrimary
                    )
                }
            }

            // User Avatar / Switcher
            currentUser?.let { user ->
                Box(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(RippleCyan, RippleBlue))
                        )
                        .border(1.5.dp, RippleCyan, CircleShape)
                        .clickable { onAvatarClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.displayName.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = OceanNight,
                        fontSize = 14.sp
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        ),
        modifier = modifier
    )
}

@Composable
fun GlowCard(
    modifier: Modifier = Modifier,
    glowColor: Color = RippleCyan,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Card(
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = OceanSurface),
        modifier = modifier
            .border(1.dp, glowColor.copy(alpha = 0.35f), shape)
            .shadow(elevation = 6.dp, shape = shape, spotColor = glowColor)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun CountdownTimerBadge(
    expiresAtMillis: Long,
    modifier: Modifier = Modifier
) {
    var remainingMillis by remember(expiresAtMillis) {
        mutableLongStateOf((expiresAtMillis - System.currentTimeMillis()).coerceAtLeast(0L))
    }

    LaunchedEffect(expiresAtMillis) {
        while (remainingMillis > 0) {
            delay(1000L)
            remainingMillis = (expiresAtMillis - System.currentTimeMillis()).coerceAtLeast(0L)
        }
    }

    val hours = remainingMillis / (1000 * 60 * 60)
    val minutes = (remainingMillis % (1000 * 60 * 60)) / (1000 * 60)
    val seconds = (remainingMillis % (1000 * 60)) / 1000

    val timeText = if (remainingMillis <= 0) {
        "EXPIRED"
    } else {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    val badgeColor = if (remainingMillis < 3600 * 1000L) RippleCoral else RippleAmber

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = badgeColor.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                tint = badgeColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = timeText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = badgeColor
                )
            )
        }
    }
}

@Composable
fun LockedInviterCard(
    inviterUsername: String,
    prompt: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "lock_glow")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    GlowCard(
        glowColor = RippleCyan.copy(alpha = pulseAlpha),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(OceanSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = OceanNight.copy(alpha = 0.8f),
                    border = androidx.compose.foundation.BorderStroke(2.dp, RippleCyan),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = RippleCyan,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "@$inviterUsername's response is locked",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Submit yours to reveal what they posted 👀",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = RippleCyan
                    )
                )
            }
        }
    }
}

@Composable
fun UnlockAnimationView(
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        expanded = true
        delay(1800L)
        onAnimationComplete()
    }

    val scale by animateFloatAsState(
        targetValue = if (expanded) 1.2f else 0.2f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "unlock_scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = tween(500),
        label = "unlock_alpha"
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = RippleTeal.copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(3.dp, RippleTeal),
                modifier = Modifier
                    .size(100.dp)
                    .shadow(elevation = 16.dp, shape = CircleShape, spotColor = RippleTeal)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = "Unlocked",
                        tint = RippleTeal,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "🔓 UNLOCKED",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = RippleTeal
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Revealing inviter's response...",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
            )
        }
    }
}
