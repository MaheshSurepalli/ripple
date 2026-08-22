package com.example.ripple.ui.moderation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ripple.domain.model.ReportReason
import com.example.ripple.theme.*

@Composable
fun ReportBlockDialog(
    targetType: String, // "Challenge" or "User"
    targetName: String,
    onReportSubmitted: (ReportReason, String?) -> Unit,
    onBlockUser: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedReason by remember { mutableStateOf(ReportReason.INAPPROPRIATE_CONTENT) }
    var additionalNotes by remember { mutableStateOf("") }
    var isBlockChecked by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Flag, contentDescription = null, tint = RippleCoral)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Report $targetType",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Help keep Ripple safe. Why are you reporting this $targetName?",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )

                listOf(
                    Pair("Inappropriate or Explicit Content", ReportReason.INAPPROPRIATE_CONTENT),
                    Pair("Dangerous Stunt or Activity", ReportReason.DANGEROUS_ACTIVITY),
                    Pair("Harassment or Hate Speech", ReportReason.HARASSMENT),
                    Pair("Spam or Scam", ReportReason.SPAM)
                ).forEach { (label, reason) ->
                    val isSelected = selectedReason == reason
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) RippleCoral.copy(alpha = 0.15f) else OceanSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) RippleCoral else GlassBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = reason }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedReason = reason },
                                colors = RadioButtonDefaults.colors(selectedColor = RippleCoral)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isSelected) TextPrimary else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }

                // Block User Option
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Checkbox(
                        checked = isBlockChecked,
                        onCheckedChange = { isBlockChecked = it },
                        colors = CheckboxDefaults.colors(checkedColor = RippleCoral)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Also block this user completely",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isBlockChecked) {
                        onBlockUser()
                    }
                    onReportSubmitted(selectedReason, additionalNotes.ifBlank { null })
                    onDismiss()
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RippleCoral, contentColor = Color.White)
            ) {
                Text("Submit Report", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = OceanSurface,
        shape = RoundedCornerShape(16.dp)
    )
}
