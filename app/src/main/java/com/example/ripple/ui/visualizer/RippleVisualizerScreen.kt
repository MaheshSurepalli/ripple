package com.example.ripple.ui.visualizer

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
import com.example.ripple.domain.model.Challenge
import com.example.ripple.domain.model.Participation
import com.example.ripple.domain.model.RippleNode
import com.example.ripple.domain.model.User
import com.example.ripple.theme.*
import com.example.ripple.ui.components.GlowCard
import com.example.ripple.ui.components.PulsingWaveBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RippleVisualizerScreen(
    challenge: Challenge,
    participations: List<Participation>,
    currentUser: User?,
    onStatsClick: (String) -> Unit,
    onShareMilestoneClick: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedParticipation by remember { mutableStateOf<Participation?>(null) }

    // Group participations by generation
    val generationGroups = remember(participations) {
        participations.groupBy { it.generation }.toSortedMap()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Ripple Tree 🌊",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "${challenge.participantCount} participants · ${challenge.generationCount} generations",
                            style = MaterialTheme.typography.labelSmall.copy(color = RippleCyan)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { onStatsClick(challenge.id) }) {
                        Icon(imageVector = Icons.Default.BarChart, contentDescription = "Stats", tint = TextPrimary)
                    }
                    IconButton(onClick = { onShareMilestoneClick(challenge.id) }) {
                        Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = "Milestones", tint = RippleCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = OceanNight,
        modifier = modifier
    ) { paddingValues ->
        PulsingWaveBackground(modifier = Modifier.padding(paddingValues)) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Header Prompt Card
                item {
                    GlowCard(glowColor = RippleCyan) {
                        Text(
                            text = "CHALLENGE PROMPT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = RippleCyan,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = challenge.prompt,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }
                }

                // Propagation Metric Pill Summary
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = OceanSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "${challenge.participantCount}",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = RippleCyan
                                    )
                                )
                                Text(
                                    text = "Participants",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = OceanSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "${challenge.generationCount}",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = RippleTeal
                                    )
                                )
                                Text(
                                    text = "Generations",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = OceanSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "${challenge.countryCount}",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = RippleAqua
                                    )
                                )
                                Text(
                                    text = "Countries",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                                )
                            }
                        }
                    }
                }

                // Generation Breakdown Tree
                item {
                    Text(
                        text = "Propagation Tree",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        ),
                        modifier = Modifier.padding(start = 4.dp, top = 6.dp)
                    )
                }

                generationGroups.forEach { (gen, list) ->
                    item {
                        GenerationSection(
                            generation = gen,
                            participations = list,
                            currentUserId = currentUser?.id ?: "",
                            onNodeClick = { selectedParticipation = it }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }

        // Node Inspection Bottom Sheet
        selectedParticipation?.let { p ->
            ModalBottomSheet(
                onDismissRequest = { selectedParticipation = null },
                containerColor = OceanSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = RippleCyan,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = p.username.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = OceanNight,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "@${p.username}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = "Generation ${p.generation}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = RippleCyan)
                                )
                            }
                        }

                        p.city?.let { city ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = OceanSurfaceVariant
                            ) {
                                Text(
                                    text = "📍 $city, ${p.country ?: ""}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val isCurrentUser = p.userId == currentUser?.id
                    val isDirectParentOrChild = p.userId == currentUser?.id ||
                            participations.any { it.userId == currentUser?.id && it.parentUserId == p.userId } ||
                            participations.any { it.userId == p.userId && it.parentUserId == currentUser?.id }

                    if (isDirectParentOrChild) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = OceanSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "🔓 Unlocked Response",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = RippleTeal
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (isCurrentUser) "Your submission to this Ripple" else "Unlocked via direct invite chain",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = OceanSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(14.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = TextMuted)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Response hidden by Chain Privacy (only visible to direct inviter & invitees)",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun GenerationSection(
    generation: Int,
    participations: List<Participation>,
    currentUserId: String,
    onNodeClick: (Participation) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (generation == 0) RippleCyan else RippleTeal,
                modifier = Modifier.size(12.dp)
            ) {}
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (generation == 0) "Generation 0 (Creator)" else "Generation $generation",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (generation == 0) RippleCyan else RippleTeal
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "(${participations.size} branch nodes)",
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            participations.forEach { p ->
                val isYou = p.userId == currentUserId
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isYou) RippleCyan.copy(alpha = 0.15f) else OceanSurface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isYou) RippleCyan else CardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNodeClick(p) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = if (isYou) RippleCyan else OceanSurfaceVariant,
                                modifier = Modifier.size(30.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = p.username.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = if (isYou) OceanNight else TextPrimary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "@${p.username}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    )
                                    if (isYou) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "(You)",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = RippleCyan,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                                p.city?.let { city ->
                                    Text(
                                        text = "📍 $city, ${p.country ?: ""}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                                    )
                                }
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Details",
                            tint = TextMuted
                        )
                    }
                }
            }
        }
    }
}
