package com.example.weatherapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.weatherapp.ui.theme.WarningContainer
import com.example.weatherapp.ui.theme.OnWarningContainer
import com.example.weatherapp.ui.theme.WarningAmber

/**
 * Notice card displayed when an error occurs or when the API key requires configuration.
 */
@Composable
fun WeatherErrorBanner(
    message: String,
    onDismiss: () -> Unit,
    isApiKeyError: Boolean = false,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isApiKeyError) {
        WarningContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }

    val contentColor = if (isApiKeyError) {
        OnWarningContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }

    val iconVector = if (isApiKeyError) {
        Icons.Default.Key
    } else {
        Icons.Default.ErrorOutline
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = if (isApiKeyError) WarningAmber else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    if (isApiKeyError) {
                        Text(
                            text = "API Key Configuration Required",
                            style = MaterialTheme.typography.titleMedium,
                            color = contentColor
                        )
                    }
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor
                    )
                }
            }

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss error message",
                    tint = contentColor
                )
            }
        }
    }
}
