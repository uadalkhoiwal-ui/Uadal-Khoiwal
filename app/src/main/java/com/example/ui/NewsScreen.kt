package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MarketCategory
import com.example.model.NewsImpact
import com.example.model.NewsItem
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GreenBullish
import com.example.ui.theme.RedBearish
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceDarkVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.TradingViewModel

@Composable
fun NewsScreen(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    val newsList by viewModel.news.collectAsState()
    val selectedCategory by viewModel.selectedNewsCategory.collectAsState()
    val activeNewsDetail by viewModel.activeNewsDetail.collectAsState()

    val filteredNews = if (selectedCategory == null) {
        newsList
    } else {
        newsList.filter { it.category == selectedCategory }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 14.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "📰 Market News",
                    color = CyanAccent,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Real-time global macro, crypto & earnings updates",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            IconButton(
                onClick = { viewModel.refreshNews() },
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceDark)
                    .border(BorderStroke(1.dp, CardBorder), RoundedCornerShape(8.dp))
                    .testTag("refresh_news_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh News",
                    tint = CyanAccent
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Category Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NewsFilterPill(
                label = "All News",
                isSelected = selectedCategory == null,
                onClick = { viewModel.selectNewsCategory(null) }
            )
            MarketCategory.entries.forEach { cat ->
                NewsFilterPill(
                    label = "${cat.icon} ${cat.label}",
                    isSelected = selectedCategory == cat,
                    onClick = { viewModel.selectNewsCategory(cat) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // News List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("news_feed_list"),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 90.dp)
        ) {
            items(filteredNews, key = { it.id }) { item ->
                NewsCardItem(
                    item = item,
                    onClick = { viewModel.showNewsDetail(item) }
                )
            }
        }
    }

    // Article Detail Modal
    activeNewsDetail?.let { article ->
        AlertDialog(
            onDismissRequest = { viewModel.showNewsDetail(null) },
            containerColor = SurfaceDark,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        ImpactBadge(impact = article.impact)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = article.title,
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 22.sp
                        )
                    }
                    IconButton(
                        onClick = { viewModel.showNewsDetail(null) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }
            },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${article.source} • ${article.timeAgo}",
                            color = CyanAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${article.category.icon} ${article.category.label}",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = article.summary,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.showNewsDetail(null) },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Text("Done", color = BackgroundDark, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun NewsFilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                BorderStroke(1.dp, if (isSelected) CyanAccent else CardBorder),
                RoundedCornerShape(8.dp)
            )
            .background(if (isSelected) CyanAccent.copy(alpha = 0.2f) else SurfaceDark)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) CyanAccent else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun NewsCardItem(
    item: NewsItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("news_item_${item.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.source} • ${item.timeAgo}",
                    color = TextMuted,
                    fontSize = 11.sp
                )
                ImpactBadge(impact = item.impact)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.summary,
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 2,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun ImpactBadge(impact: NewsImpact) {
    val (label, bg, textCol) = when (impact) {
        NewsImpact.BULLISH -> Triple("BULLISH", GreenBullish.copy(alpha = 0.2f), GreenBullish)
        NewsImpact.BEARISH -> Triple("BEARISH", RedBearish.copy(alpha = 0.2f), RedBearish)
        NewsImpact.NEUTRAL -> Triple("NEUTRAL", AmberWarning.copy(alpha = 0.2f), AmberWarning)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            color = textCol,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
