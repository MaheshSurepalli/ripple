package com.example.ripple.ui.complete

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ripple.domain.model.Challenge
import com.example.ripple.domain.model.InviteToken
import com.example.ripple.domain.model.ResponseType
import com.example.ripple.domain.model.User
import com.example.ripple.theme.*
import com.example.ripple.ui.components.GlowCard
import com.example.ripple.ui.components.PulsingWaveBackground
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompleteChallengeScreen(
    challenge: Challenge,
    sender: User,
    inviteToken: InviteToken,
    currentUser: User?,
    onSubmissionSuccess: (challengeId: String, senderUserId: String) -> Unit,
    onSubmitResponse: suspend (
        inviteToken: String,
        responseText: String?,
        photoUri: String?,
        city: String?,
        country: String?
    ) -> Result<Unit>,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textResponse by remember { mutableStateOf("") }
    var photoCaptured by remember { mutableStateOf(false) }
    var attachLocation by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Complete Challenge",
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
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Prompt Header
                GlowCard(glowColor = RippleCyan) {
                    Text(
                        text = "CHALLENGE PROMPT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = RippleCyan,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = challenge.prompt,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }

                // Photo Mode Capture Viewfinder / Preview
                if (challenge.responseType == ResponseType.PHOTO) {
                    GlowCard(glowColor = RippleTeal) {
                        Text(
                            text = "Camera Capture",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (photoCaptured) OceanSurfaceVariant else Color.Black
                                )
                                .border(1.5.dp, if (photoCaptured) RippleGreen else GlassBorder, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (photoCaptured) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Photo Captured",
                                        tint = RippleGreen,
                                        modifier = Modifier.size(54.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Photo Ready to Submit 📸",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedButton(
                                        onClick = { photoCaptured = false },
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, RippleCoral),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RippleCoral)
                                    ) {
                                        Text("Retake Photo")
                                    }
                                }
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Camera Viewfinder",
                                        tint = RippleCyan,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Take your photo response",
                                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { photoCaptured = true },
                                        shape = CircleShape,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = RippleCyan,
                                            contentColor = OceanNight
                                        ),
                                        modifier = Modifier.size(64.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PhotoCamera,
                                            contentDescription = "Capture",
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = textResponse,
                            onValueChange = { textResponse = it },
                            placeholder = { Text("Add an optional caption...", color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RippleCyan,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    // Text Mode Response
                    GlowCard(glowColor = RippleTeal) {
                        Text(
                            text = "Your Response",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = textResponse,
                            onValueChange = { textResponse = it },
                            placeholder = { Text("Type your answer here...", color = TextMuted) },
                            minLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RippleCyan,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Optional Approximate Location Tag
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = OceanSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = RippleTeal)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Show where Ripple traveled",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = "Shares approximate city (e.g. Denver)",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                                )
                            }
                        }
                        Switch(
                            checked = attachLocation,
                            onCheckedChange = { attachLocation = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = OceanNight,
                                checkedTrackColor = RippleTeal,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = OceanSurfaceVariant
                            )
                        )
                    }
                }

                // Error Message
                errorMessage?.let { error ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = RippleCoral.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RippleCoral)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Error, contentDescription = null, tint = RippleCoral)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = error, color = RippleCoral, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Submit Response Button
                Button(
                    onClick = {
                        if (challenge.responseType == ResponseType.PHOTO && !photoCaptured) {
                            errorMessage = "Please take a photo before submitting"
                            return@Button
                        }
                        if (challenge.responseType == ResponseType.TEXT && textResponse.isBlank()) {
                            errorMessage = "Please type your text response"
                            return@Button
                        }

                        isSubmitting = true
                        errorMessage = null
                        coroutineScope.launch {
                            val result = onSubmitResponse(
                                inviteToken.token,
                                textResponse.ifBlank { "Challenge completed! 📸" },
                                if (challenge.responseType == ResponseType.PHOTO) "captured_response.jpg" else null,
                                if (attachLocation) "Denver" else null,
                                if (attachLocation) "United States" else null
                            )
                            isSubmitting = false
                            if (result.isSuccess) {
                                onSubmissionSuccess(challenge.id, sender.id)
                            } else {
                                errorMessage = result.exceptionOrNull()?.message ?: "Failed to submit challenge"
                            }
                        }
                    },
                    enabled = !isSubmitting,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RippleTeal,
                        contentColor = OceanNight,
                        disabledContainerColor = OceanSurfaceVariant,
                        disabledContentColor = TextMuted
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = OceanNight, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = "Submit & Unlock @${sender.username}'s Response 🔓",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
