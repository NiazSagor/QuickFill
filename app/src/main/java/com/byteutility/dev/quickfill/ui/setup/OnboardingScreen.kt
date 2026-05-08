package com.byteutility.dev.quickfill.ui.setup

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.byteutility.dev.quickfill.R
import com.byteutility.dev.quickfill.ui.theme.QuickFillTheme
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    QuickFillTheme {
        Scaffold { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) { page ->
                    val pageOffset = (
                            (pagerState.currentPage - page) + pagerState
                                .currentPageOffsetFraction
                            ).absoluteValue

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                val alpha = 1f - (pageOffset * 0.5f).coerceIn(0f, 1f)
                                val scale = 1f - (pageOffset * 0.2f).coerceIn(0f, 1f)

                                this.alpha = alpha
                                this.scaleX = scale
                                this.scaleY = scale
                            }
                    ) {
                        when (page) {
                            0 -> OnboardingPage(
                                title = stringResource(R.string.onboarding_welcome_title),
                                description = stringResource(R.string.onboarding_welcome_desc),
                                icon = Icons.Default.AutoAwesome
                            )

                            1 -> OnboardingPage(
                                title = stringResource(R.string.onboarding_privacy_title),
                                description = stringResource(R.string.onboarding_privacy_desc),
                                icon = Icons.Default.Lock
                            )

                            2 -> OnboardingPage(
                                title = stringResource(R.string.onboarding_setup_title),
                                description = stringResource(R.string.onboarding_setup_desc),
                                icon = Icons.Default.SettingsSuggest
                            )
                        }
                    }
                }

                PageIndicator(
                    pageCount = 3,
                    currentPage = pagerState.currentPage,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                OnboardingControls(
                    currentPage = pagerState.currentPage,
                    onNext = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                    onSkip = { scope.launch { pagerState.animateScrollToPage(2) } },
                    onComplete = onComplete
                )
            }
        }
    }
}

@Composable
fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage
            Box(
                modifier = Modifier
                    .size(if (isSelected) 12.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
            )
        }
    }
}

@Composable
fun OnboardingControls(
    currentPage: Int,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (currentPage < 2) {
            TextButton(onClick = onSkip) {
                Text(stringResource(R.string.onboarding_btn_skip))
            }
            Button(onClick = onNext) {
                Text(stringResource(R.string.onboarding_btn_next))
            }
        } else {
            Button(
                onClick = {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        val intent = Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE).apply {
                            data = Uri.parse("package:com.byteutility.dev.quickfill")
                        }
                        context.startActivity(intent)
                    }
                    onComplete()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.onboarding_btn_get_started))
            }
        }
    }
}

@Composable
fun OnboardingPage(title: String, description: String, icon: ImageVector) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingPreview() {
    QuickFillTheme {
        OnboardingScreen(onComplete = {})
    }
}

@Preview(showBackground = true, name = "Onboarding Controls - Page 1")
@Composable
fun OnboardingControlsFirstPagePreview() {
    QuickFillTheme {
        OnboardingControls(
            currentPage = 0,
            onNext = {},
            onSkip = {},
            onComplete = {}
        )
    }
}

@Preview(showBackground = true, name = "Onboarding Controls - Last Page")
@Composable
fun OnboardingControlsLastPagePreview() {
    QuickFillTheme {
        OnboardingControls(
            currentPage = 2,
            onNext = {},
            onSkip = {},
            onComplete = {}
        )
    }
}
