package com.example.ripple.ui.simulator

import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ripple.domain.model.User
import com.example.ripple.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoSimulatorModal(
    currentUser: User?,
    availableDemoUsers: List<User>,
    onSwitchUser: (String) -> Unit,
    onSimulateSpread: suspend (String) -> Result<Unit>,
    activeChallengeId: String = "chal_view_now",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isSimulating by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = OceanSurface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.BugReport, contentDescription = null, tint = RippleTeal)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ripple Multi-User Simulator",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = RippleTeal.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "MVP TEST HARNESS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = RippleTeal
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "Switch active user account instantly to test viral branch handoffs on a single device:",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )

            // User Switcher Chips
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                availableDemoUsers.forEach { user ->
                    val isActive = user.id == currentUser?.id
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isActive) RippleCyan.copy(alpha = 0.15f) else OceanSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isActive) RippleCyan else GlassBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSwitchUser(user.id)
                                Toast.makeText(context, "Switched to @${user.username}", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isActive) RippleCyan else OceanNight,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = user.displayName.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = if (isActive) OceanNight else TextPrimary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${user.displayName} (@${user.username})",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    )
                                    Text(
                                        text = "Reach: ${user.totalPeopleReached} people · ${user.challengesCompleted} completed",
                                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                                    )
                                }
                            }

                            if (isActive) {
                                Text(
                                    text = "ACTIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        color = RippleCyan
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Fast Forward Viral Spread Simulation Button
            Button(
                onClick = {
                    isSimulating = true
                    coroutineScope.launch {
                        val result = onSimulateSpread(activeChallengeId)
                        isSimulating = false
                        if (result.isSuccess) {
                            Toast.makeText(context, "🌊 New participant joined from another city!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Simulation: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = !isSimulating,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RippleTeal,
                    contentColor = OceanNight
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                if (isSimulating) {
                    CircularProgressIndicator(color = OceanNight, modifier = Modifier.size(20.dp))
                } else {
                    Icon(imageVector = Icons.Default.FastForward, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("⚡ Simulate Live Branch Join (Paris / Tokyo)", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
