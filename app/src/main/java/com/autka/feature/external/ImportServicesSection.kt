package com.autka.feature.external

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autka.R
import com.autka.core.model.ImportService

/**
 * Detail-screen section listing import companies that can source a given offer into
 * Poland, shown directly under the landed-cost breakdown. [services] is already
 * region-filtered by the ViewModel (USA offer -> US importers, etc.); an empty list
 * hides the section (e.g. POLAND offers).
 */
@Composable
fun ImportServicesSection(
    services: List<ImportService>,
    modifier: Modifier = Modifier,
) {
    if (services.isEmpty()) return

    val context = LocalContext.current
    fun open(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, R.string.no_browser, Toast.LENGTH_SHORT).show()
        }
    }

    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                stringResource(R.string.import_via_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            services.forEach { svc ->
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(svc.displayName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                    svc.note?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { open(svc.url) }) {
                            Text(stringResource(R.string.import_open_site))
                        }
                        svc.calculatorUrl?.let { calc ->
                            TextButton(onClick = { open(calc) }) {
                                Text(stringResource(R.string.import_open_calculator))
                            }
                        }
                    }
                }
            }
            Text(
                stringResource(R.string.import_services_disclaimer),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
