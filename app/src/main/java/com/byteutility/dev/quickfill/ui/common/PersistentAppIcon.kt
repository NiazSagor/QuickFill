package com.byteutility.dev.quickfill.ui.common

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byteutility.dev.quickfill.ui.snippets.SnippetViewModel

/**
 * ARCHITECTURAL DECISION: Lazy loading app icons from the database.
 * This component handles the complexity of:
 * 1. Listening to the DB stream for metadata.
 * 2. Converting the BLOB (ByteArray) back to a Bitmap.
 * 3. Showing a placeholder while loading or if not found.
 */
@Composable
fun PersistentAppIcon(
    packageName: String?,
    category: String,
    viewModel: SnippetViewModel,
    modifier: Modifier = Modifier.size(48.dp)
) {
    val metadata by if (packageName != null) {
        viewModel.getAppMetadata(packageName).collectAsStateWithLifecycle(initialValue = null)
    } else {
        remember { androidx.compose.runtime.mutableStateOf(null) }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        val iconBlob = metadata?.iconBlob
        if (iconBlob != null) {
            // PERFORMANCE DECISION: Decode the bitmap only when needed.
            val bitmap = remember(iconBlob) {
                BitmapFactory.decodeByteArray(iconBlob, 0, iconBlob.size)
            }
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
        } else {
            // Placeholder based on category
            Icon(
                imageVector = when (category.uppercase()) {
                    "WORK" -> Icons.Default.Email
                    "SOCIAL" -> Icons.Default.Share
                    else -> Icons.Default.List
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
