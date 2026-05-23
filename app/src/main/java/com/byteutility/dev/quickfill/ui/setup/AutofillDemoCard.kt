package com.byteutility.dev.quickfill.ui.setup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.byteutility.dev.quickfill.R
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kotlinx.coroutines.delay

private enum class DemoStep {
    Idle,
    FieldFocused,
    ContextMenuVisible,
    AutofillHighlighted,
    SuggestionsVisible,
    Filled
}

@Composable
fun AutofillDemoCard(modifier: Modifier = Modifier) {
    var step by remember { mutableStateOf(DemoStep.Idle) }

    LaunchedEffect(Unit) {
        while (true) {
            step = DemoStep.Idle
            delay(700)
            step = DemoStep.FieldFocused
            delay(1000)
            step = DemoStep.ContextMenuVisible
            delay(1100)
            step = DemoStep.AutofillHighlighted
            delay(1200)
            step = DemoStep.SuggestionsVisible
            delay(1300)
            step = DemoStep.Filled
            delay(1800)
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DemoStageLabel(step = step)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFF2F6FB))
                    .padding(16.dp)
            ) {
                FakeAppScreen(step = step)

                androidx.compose.animation.AnimatedVisibility(
                    visible = step.atLeast(DemoStep.ContextMenuVisible) &&
                            !step.atLeast(DemoStep.SuggestionsVisible),
                    enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 6 }),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 72.dp, end = 16.dp)
                    ) {
                        FakeContextMenu(highlightAutofill = step.atLeast(DemoStep.AutofillHighlighted))
                    }
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = step.atLeast(DemoStep.SuggestionsVisible),
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 3 })
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 8.dp, vertical = 12.dp)
                    ) {
                        FakeSuggestionPanel()
                    }
                }
            }
        }
    }
}

@Composable
private fun DemoStageLabel(step: DemoStep) {
    val label = when (step) {
        DemoStep.Idle,
        DemoStep.FieldFocused -> stringResource(R.string.demo_stage_open_app)
        DemoStep.ContextMenuVisible -> stringResource(R.string.demo_stage_long_press)
        DemoStep.AutofillHighlighted -> stringResource(R.string.demo_stage_autofill)
        DemoStep.SuggestionsVisible,
        DemoStep.Filled -> stringResource(R.string.demo_stage_pick_snippet)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun FakeAppScreen(step: DemoStep) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFCCE0FF))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Messenger Lite",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A2433)
                )
                Text(
                    text = "Compose a message",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF61758A)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White)
                .border(
                    width = if (step.atLeast(DemoStep.FieldFocused)) 2.dp else 1.dp,
                    color = if (step.atLeast(DemoStep.FieldFocused)) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color(0xFFD7E0EA)
                    },
                    shape = RoundedCornerShape(22.dp)
                )
                .padding(18.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.demo_mock_message_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF61758A)
                )
                Text(
                    text = if (step.atLeast(DemoStep.Filled)) {
                        stringResource(R.string.demo_mock_field_value)
                    } else {
                        " "
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF1A2433)
                )
                if (step.atLeast(DemoStep.FieldFocused) && !step.atLeast(DemoStep.Filled)) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(22.dp)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }

        repeat(2) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (it == 0) 0.72f else 0.54f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFFDCE6F1))
                    .alpha(0.8f)
            )
        }
    }
}

@Composable
private fun FakeContextMenu(highlightAutofill: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "autofill-highlight")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "autofill-alpha"
    )

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 10.dp
    ) {
        Column(
            modifier = Modifier
                .width(180.dp)
                .padding(vertical = 8.dp)
        ) {
            DemoMenuRow(
                icon = Icons.Default.ContentPaste,
                text = stringResource(R.string.demo_mock_context_paste)
            )
            DemoMenuRow(
                icon = Icons.Default.SelectAll,
                text = stringResource(R.string.demo_mock_context_select_all)
            )
            DemoMenuRow(
                icon = Icons.Default.AutoAwesome,
                text = stringResource(R.string.demo_mock_context_autofill),
                highlighted = highlightAutofill,
                highlightAlpha = pulse
            )
        }
    }
}

@Composable
private fun DemoMenuRow(
    icon: ImageVector,
    text: String,
    highlighted: Boolean = false,
    highlightAlpha: Float = 1f
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (highlighted) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f * highlightAlpha)
                else Color.Transparent
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (highlighted) MaterialTheme.colorScheme.primary else Color(0xFF526273),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (highlighted) MaterialTheme.colorScheme.primary else Color(0xFF1A2433),
            fontWeight = if (highlighted) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun FakeSuggestionPanel() {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 10.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.demo_mock_suggestion_header),
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFF1A2433),
                fontWeight = FontWeight.SemiBold
            )
            DemoAutofillItem(
                iconRes = android.R.drawable.ic_dialog_email,
                title = stringResource(R.string.demo_mock_snippet_work_email),
                subtitle = stringResource(R.string.demo_mock_field_value),
                emphasized = true
            )
            DemoAutofillItem(
                iconRes = android.R.drawable.ic_menu_mylocation,
                title = stringResource(R.string.demo_mock_snippet_home_address),
                subtitle = "Global - GENERAL"
            )
        }
    }
}

@Composable
private fun DemoAutofillItem(
    iconRes: Int,
    title: String,
    subtitle: String,
    emphasized: Boolean = false
) {
    val rowColor = if (emphasized) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
    } else {
        Color(0xFFF7F9FC)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(rowColor)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                LayoutInflater.from(context)
                    .inflate(R.layout.autofill_item, null, false)
                    .apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }
            },
            update = { view ->
                view.findViewById<ImageView>(R.id.autofill_icon).setImageResource(iconRes)
                view.findViewById<TextView>(R.id.autofill_title).text = title
                view.findViewById<TextView>(R.id.autofill_subtitle).text = subtitle
                view.alpha = if (emphasized) 1f else 0.92f
            }
        )
    }
}

private fun DemoStep.atLeast(other: DemoStep): Boolean = ordinal >= other.ordinal
