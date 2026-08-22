package com.example.ripple.ui.create

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.ripple.domain.model.ResponseType
import com.example.ripple.domain.model.User
import com.example.ripple.theme.*
import com.example.ripple.ui.components.GlowCard
import com.example.ripple.ui.components.PulsingWaveBackground
import kotlinx.coroutines.launch

data class TemplateCategory(
    val name: String,
    val prompts: List<String>
)

val InspirationTemplates = listOf(
    TemplateCategory(
        name = "📸 Right Now",
        prompts = listOf(
            "Show what's directly in front of you right now",
            "Show what you're eating or drinking right now",
            "Show the shoes you're wearing today",
            "Show your current view out of the nearest window"
        )
    ),
    TemplateCategory(
        name = "😂 Funny",
        prompts = listOf(
            "Take the absolute worst angle selfie possible",
            "Show your most useless possession",
            "Send your funniest facial expression"
        )
    ),
    TemplateCategory(
        name = "👥 Friends",
        prompts = listOf(
            "Describe our friendship in one word",
            "What's the first memory you have of me?",
            "Send a photo that reminds you of me"
        )
    ),
    TemplateCategory(
        name = "🎲 Random",
        prompts = listOf(
            "Find and photograph something blue near you",
            "Take a picture without moving from your seat",
            "Photograph something older than you are"
        )
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRippleScreen(
    currentUser: User?,
    onBackClick: () -> Unit,
    onRippleCreated: (challengeId: String) -> Unit,
    onCreateChallenge: suspend (
        prompt: String,
        responseType: ResponseType,
        expirationHours: Int,
        initialText: String?,
        initialPhotoUri: String?
    ) -> Result<String>,
    modifier: Modifier = Modifier
) {
    var prompt by remember { mutableStateOf("") }
    var selectedResponseType by remember { mutableStateOf(ResponseType.PHOTO) }
    var selectedExpirationHours by remember { mutableIntStateOf(24) }
    var initialResponseText by remember { mutableStateOf("") }
    var initialPhotoUri by remember { mutableStateOf<String?>("demo_creator_response.jpg") }
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Start a Ripple 🌊",
                        style = MaterialTheme.typography.titleLarge.copy(
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
                // Prompt Input Box
                GlowCard(glowColor = RippleCyan) {
                    Text(
                        text = "What should everyone do?",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = prompt,
                        onValueChange = {
                            prompt = it
                            errorMessage = null
                        },
                        placeholder = {
                            Text(
                                text = "e.g. Show what's in front of you right now...",
                                color = TextMuted
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RippleCyan,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = RippleCyan
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Inspiration Templates
                Column {
                    Text(
                        text = "💡 Inspiration Ideas",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = RippleTeal
                        ),
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )

                    // Category Tabs
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(InspirationTemplates.indices.toList()) { index ->
                            val category = InspirationTemplates[index]
                            val isSelected = selectedCategoryIndex == index
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) RippleCyan else OceanSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) RippleCyan else GlassBorder
                                ),
                                modifier = Modifier.clickable { selectedCategoryIndex = index }
                            ) {
                                Text(
                                    text = category.name,
                                    color = if (isSelected) OceanNight else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Prompts in Selected Category
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        InspirationTemplates[selectedCategoryIndex].prompts.forEach { itemPrompt ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = OceanSurface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        prompt = itemPrompt
                                        errorMessage = null
                                    }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = "+",
                                        color = RippleCyan,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = itemPrompt,
                                        style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                                    )
                                }
                            }
                        }
                    }
                }

                // Response Type Selector
                GlowCard(glowColor = RippleTeal) {
                    Text(
                        text = "Answer with",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedResponseType == ResponseType.PHOTO) RippleTeal.copy(alpha = 0.2f) else OceanSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (selectedResponseType == ResponseType.PHOTO) RippleTeal else GlassBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedResponseType = ResponseType.PHOTO }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = if (selectedResponseType == ResponseType.PHOTO) RippleTeal else TextSecondary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Photo",
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedResponseType == ResponseType.PHOTO) RippleTeal else TextSecondary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedResponseType == ResponseType.TEXT) RippleTeal.copy(alpha = 0.2f) else OceanSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (selectedResponseType == ResponseType.TEXT) RippleTeal else GlassBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedResponseType = ResponseType.TEXT }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TextFields,
                                    contentDescription = null,
                                    tint = if (selectedResponseType == ResponseType.TEXT) RippleTeal else TextSecondary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Text",
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedResponseType == ResponseType.TEXT) RippleTeal else TextSecondary
                                )
                            }
                        }
                    }
                }

                // Expiration Picker
                GlowCard(glowColor = RippleAqua) {
                    Text(
                        text = "Challenge expires in",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(
                            Pair("1h", 1),
                            Pair("6h", 6),
                            Pair("24h", 24),
                            Pair("3d", 72)
                        ).forEach { (label, hours) ->
                            val isSelected = selectedExpirationHours == hours
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) RippleAqua else OceanSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) RippleAqua else GlassBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedExpirationHours = hours }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) OceanNight else TextSecondary,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Creator Initial Response Capture Preview
                GlowCard(glowColor = RippleCyan) {
                    Text(
                        text = "Your response (required to start)",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Friends must submit theirs before they can see this.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (selectedResponseType == ResponseType.PHOTO) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = OceanSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = RippleTeal,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Photo Snapshot Attached 📸",
                                        color = RippleTeal,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = initialResponseText,
                        onValueChange = { initialResponseText = it },
                        placeholder = { Text("Add caption or message (optional)...", color = TextMuted) },
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

                // Submit Button
                Button(
                    onClick = {
                        if (prompt.isBlank()) {
                            errorMessage = "Please enter a challenge prompt"
                            return@Button
                        }
                        isSubmitting = true
                        errorMessage = null
                        coroutineScope.launch {
                            val result = onCreateChallenge(
                                prompt.trim(),
                                selectedResponseType,
                                selectedExpirationHours,
                                initialResponseText.ifBlank { "Here is my response!" },
                                initialPhotoUri
                            )
                            isSubmitting = false
                            if (result.isSuccess) {
                                onRippleCreated(result.getOrThrow())
                            } else {
                                errorMessage = result.exceptionOrNull()?.message ?: "Failed to create Ripple"
                            }
                        }
                    },
                    enabled = !isSubmitting,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RippleCyan,
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
                            text = "🌊 Start Ripple",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
