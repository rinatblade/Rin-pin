package com.rinatblade.rinpin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun Pill(text: String, accent: Boolean = false) {
    val c = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(50),
        color = if (accent) c.primary else c.primary.copy(alpha = 0.12f),
        contentColor = if (accent) c.onPrimary else c.onSurface,
    ) {
        Text(
            text, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OfferCard(
    offer: Offer,
    rank: Int?,
    inCart: Boolean,
    onCart: () -> Unit,
) {
    val ctx = LocalContext.current
    val c = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = c.surface),
        border = BorderStroke(1.dp, c.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row {
                Box(
                    Modifier.size(100.dp).clip(RoundedCornerShape(16.dp)).background(c.surfaceVariant),
                ) {
                    AsyncImage(
                        model = offer.image, contentDescription = null,
                        contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize(),
                    )
                    if (rank != null) {
                        Surface(
                            color = c.primary, contentColor = c.onPrimary,
                            shape = RoundedCornerShape(bottomEnd = 12.dp),
                        ) {
                            Text(
                                "№$rank", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            )
                        }
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        offer.displayTitle, style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold, maxLines = 3, overflow = TextOverflow.Ellipsis,
                    )
                    if (offer.displayTitle != offer.title) {
                        Text(
                            offer.title, style = MaterialTheme.typography.bodySmall,
                            color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Магазин: " + offer.shop.ifBlank { "—" }, style = MaterialTheme.typography.bodySmall,
                        color = c.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (offer.price > 0) Pill(formatPrice(offer.price), accent = true)
                if (offer.rating > 0) Pill("★ " + String.format(java.util.Locale.US, "%.1f", offer.rating))
                Pill(formatSales(offer.salesNum, offer.salesPlus))
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                FilledTonalButton(
                    onClick = onCart, modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                ) {
                    Icon(if (inCart) Icons.Default.Delete else Icons.Default.Add, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (inCart) "Убрать" else "В корзину", fontSize = 13.sp, maxLines = 1)
                }
                Button(
                    onClick = { ctx.openLink(offer.appLink, offer.webLink) }, modifier = Modifier.weight(1.3f),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                ) {
                    Text("Открыть в Pinduoduo", fontSize = 13.sp, maxLines = 1)
                }
                IconButton(onClick = { ctx.copyText(offer.webLink) }) {
                    Icon(Icons.Default.Share, contentDescription = "Копировать ссылку", tint = c.primary)
                }
            }
        }
    }
}

@Composable
fun ScreenTitle(title: String, subtitle: String? = null) {
    Column(Modifier.padding(top = 8.dp, bottom = 4.dp)) {
        Text(
            title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        if (subtitle != null) {
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun InfoCard(content: @Composable () -> Unit) {
    val c = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = c.primaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { content() }
    }
}

@Composable
fun Dot(color: androidx.compose.ui.graphics.Color, size: Int = 14) {
    Box(Modifier.size(size.dp).clip(CircleShape).background(color))
}
