package com.example.ripple.ui.invite

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ripple.domain.model.Challenge
import com.example.ripple.domain.model.InviteStatus
import com.example.ripple.domain.model.InviteToken
import com.example.ripple.theme.*
import com.example.ripple.ui.components.GlowCard
import com.example.ripple.ui.components.PulsingWaveBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteShareScreen(
    challenge: Challenge,
    inviteTokens: List<InviteToken>,
    onWatchRippleClick: (String) -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedSlotIndex by remember { mutableIntStateOf(0) }
    val activeToken = inviteTokens.getOrNull(selectedSlotIndex) ?: inviteTokens.firstOrNull()

    fun getShareText(token: String): String {
        return "👀 I started a Ripple.\n\n\"${challenge.prompt}\"\n\nYou have to submit yours before you can see mine!\n\nhttps://ripple.app/i/$token"
    }

    fun shareExternal(text: String, packageName: String? = null) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
            if (packageName != null) {
                setPackage(packageName)
            }
        }
        try {
            val chooser = Intent.createChooser(sendIntent, "Send Ripple Invite")
            context.startActivity(if (packageName != null) sendIntent else chooser)
        } catch (e: Exception) {
            // Fallback to regular chooser
            context.startActivity(Intent.createChooser(sendIntent, "Send Ripple Invite"))
        }
    }

    fun copyToClipboard(token: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Ripple Invite Link", "https://ripple.app/i/$token")
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Invite link copied to clipboard! 📋", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pass It On 🌊",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                },
                actions = {
                    TextButton(onClick = onHomeClick) {
                        Text("Done", color = RippleCyan, fontWeight = FontWeight.Bold)
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
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header Callout
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "🌊", fontSize = 44.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Your Ripple is Active!",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = RippleCyan
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Send it to up to 3 people to start the chain.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                    )
                }

                // 3 Branching Slots
                Text(
                    text = "3 Branching Invite Slots",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = RippleTeal
                    ),
                    modifier = Modifier.padding(start = 4.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    (0..2).forEach { index ->
                        val token = inviteTokens.getOrNull(index)
                        val isSelected = selectedSlotIndex == index
                        val isConsumed = token?.status == InviteStatus.CONSUMED

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = when {
                                isSelected -> RippleCyan.copy(alpha = 0.2f)
                                isConsumed -> RippleGreen.copy(alpha = 0.15f)
                                else -> OceanSurfaceVariant
                            },
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                when {
                                    isSelected -> RippleCyan
                                    isConsumed -> RippleGreen
                                    else -> GlassBorder
                                }
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedSlotIndex = index }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isConsumed) RippleGreen else RippleCyan,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${index + 1}",
                                            fontWeight = FontWeight.Black,
                                            color = OceanNight,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (isConsumed) "Joined ✅" else "Ready",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isConsumed) RippleGreen else TextPrimary
                                    )
                                )
                            }
                        }
                    }
                }

                // Selected Token Live Preview Card
                activeToken?.let { token ->
                    GlowCard(glowColor = RippleCyan) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Invite Slot #${selectedSlotIndex + 1}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = RippleCyan
                                )
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = OceanSurfaceVariant
                            ) {
                                Text(
                                    text = "Unique Token: ${token.token}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = OceanNight.copy(alpha = 0.6f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = getShareText(token.token),
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary)
                                )
                            }
                        }
                    }

                    // Share Buttons
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Share Channel",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            ),
                            modifier = Modifier.padding(start = 4.dp)
                        )

                        // WhatsApp Button
                        Button(
                            onClick = { shareExternal(getShareText(token.token), "com.whatsapp") },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF25D366),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Send via WhatsApp", fontWeight = FontWeight.Bold)
                        }

                        // Native Share / More
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = { copyToClipboard(token.token) },
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, RippleCyan),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = RippleCyan),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy Link", fontSize = 13.sp)
                            }

                            Button(
                                onClick = { shareExternal(getShareText(token.token)) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RippleTeal,
                                    contentColor = OceanNight
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share Sheet", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Watch Ripple CTA
                Button(
                    onClick = { onWatchRippleClick(challenge.id) },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RippleCyan,
                        contentColor = OceanNight
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(imageVector = Icons.Default.AccountTree, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🌊 Watch the Ripple",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
