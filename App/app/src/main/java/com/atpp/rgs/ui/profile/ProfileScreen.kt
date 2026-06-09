package com.atpp.rgs.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.atpp.rgs.R
import com.atpp.rgs.RgsApplication
import com.atpp.rgs.data.GameHistoryRow
import com.atpp.rgs.ui.components.GradientButton
import com.atpp.rgs.ui.theme.BrandDivider
import com.atpp.rgs.ui.theme.BrandMenuBackground
import com.atpp.rgs.ui.theme.BrandGold
import com.atpp.rgs.ui.theme.BrandPanel
import com.atpp.rgs.ui.theme.BrandRed
import com.atpp.rgs.ui.theme.BrandRedDark
import com.atpp.rgs.ui.theme.BrandRedLight
import com.atpp.rgs.ui.theme.BrandTextMuted
import com.atpp.rgs.ui.theme.BrandWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val WinGreen = Color(0xFF3FA34D)

/** Ile wpisów historii pokazujemy na start i o ile rozwijamy każdym kliknięciem. */
private const val HISTORY_PAGE = 4

@Composable
fun ProfileScreen(
    app: RgsApplication,
    userId: Int,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.factory(app, userId))
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BrandMenuBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeaderCard(
            username    = state.username,
            memberSince = state.memberSince,
            coins       = state.coins
        )
        StatsCard(state = state)
        HistoryCard(history = state.history)

        Spacer(Modifier.height(8.dp))
        GradientButton(
            text = stringResource(R.string.menu_btn_logout),
            onClick = onLogout,
            gradientColors = listOf(BrandRedLight, BrandRed, BrandRedDark),
            contentColor = BrandWhite,
            trailingIcon = {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint               = BrandWhite,
                    modifier           = Modifier.size(18.dp)
                )
            }
        )
    }
}

// ─── Nagłówek profilu ──────────────────────────────────────────────────────

@Composable
private fun HeaderCard(username: String, memberSince: Date?, coins: Int) {
    val dateFmt = remember { SimpleDateFormat("MMM yyyy", Locale.US) }

    Card {
        Text(
            text       = username.ifBlank { "—" },
            color      = BrandWhite,
            fontSize   = 26.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text          = stringResource(
                R.string.profile_member_since,
                memberSince?.let { dateFmt.format(it) } ?: "—"
            ),
            color         = BrandTextMuted,
            fontSize      = 11.sp,
            fontWeight    = FontWeight.Medium,
            letterSpacing = 2.sp
        )

        Spacer(Modifier.height(16.dp))
        Divider()
        Spacer(Modifier.height(16.dp))

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text          = stringResource(R.string.profile_balance),
                color         = BrandTextMuted,
                fontSize      = 12.sp,
                fontWeight    = FontWeight.Medium,
                letterSpacing = 1.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = Icons.Filled.MonetizationOn,
                    contentDescription = null,
                    tint               = BrandGold,
                    modifier           = Modifier.size(20.dp)
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text       = formatCoins(coins),
                    color      = BrandGold,
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ─── Statystyki ────────────────────────────────────────────────────────────

@Composable
private fun StatsCard(state: ProfileUiState) {
    Card {
        SectionHeader(icon = Icons.Filled.BarChart, label = stringResource(R.string.profile_section_stats))
        Spacer(Modifier.height(16.dp))
        Divider()
        Spacer(Modifier.height(12.dp))

        StatRow(stringResource(R.string.profile_stat_games), state.totalGames.toString(), BrandWhite)
        StatRow(stringResource(R.string.profile_stat_wins), state.totalWins.toString(), WinGreen)
        StatRow(stringResource(R.string.profile_stat_losses), state.totalLoses.toString(), BrandRedLight)
        StatRow(
            stringResource(R.string.profile_stat_winrate),
            stringResource(R.string.profile_winrate_format, state.winRate),
            BrandGold
        )
        StatRow(
            stringResource(R.string.profile_stat_net),
            formatSignedCoins(state.netResult),
            netColor(state.netResult)
        )
    }
}

@Composable
private fun StatRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(text = label, color = BrandTextMuted, fontSize = 13.sp)
        Text(text = value, color = valueColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

// ─── Historia gier ─────────────────────────────────────────────────────────

@Composable
private fun HistoryCard(history: List<GameHistoryRow>) {
    val dateFmt = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.US) }
    var visibleCount by remember { mutableStateOf(HISTORY_PAGE) }

    Card {
        SectionHeader(icon = Icons.Filled.History, label = stringResource(R.string.profile_section_history))
        Spacer(Modifier.height(16.dp))
        Divider()
        Spacer(Modifier.height(4.dp))

        if (history.isEmpty()) {
            Text(
                text = stringResource(R.string.profile_history_empty),
                color = BrandTextMuted,
                fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            val visible = history.take(visibleCount)
            visible.forEachIndexed { index, row ->
                HistoryRow(row = row, dateFmt = dateFmt)
                if (index < visible.lastIndex) Divider()
            }

            if (history.size > visibleCount) {
                Divider()
                TextButton(
                    onClick = { visibleCount += HISTORY_PAGE },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Text(
                        text          = stringResource(R.string.profile_show_more),
                        color         = BrandGold,
                        fontSize      = 12.sp,
                        fontWeight    = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(row: GameHistoryRow, dateFmt: SimpleDateFormat) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column {
            Text(text = row.gameName, color = BrandWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(2.dp))
            Text(
                text     = stringResource(R.string.profile_bet_format, row.betAmount),
                color    = BrandTextMuted,
                fontSize = 11.sp
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text       = resultLabel(row.resultName),
                color      = resultColor(row.resultName),
                fontSize   = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(2.dp))
            Text(text = dateFmt.format(row.playedAt), color = BrandTextMuted, fontSize = 11.sp)
        }
    }
}

// ─── Wspólne elementy ──────────────────────────────────────────────────────

@Composable
private fun Card(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BrandPanel)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        content = content
    )
}

@Composable
private fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = BrandGold, modifier = Modifier.size(20.dp))
        Text(
            text          = label,
            color         = BrandGold,
            fontSize      = 12.sp,
            fontWeight    = FontWeight.Bold,
            letterSpacing = 2.sp
        )
    }
}

@Composable
private fun Divider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(BrandDivider)
    )
}

// ─── Helpers ───────────────────────────────────────────────────────────────

private fun formatCoins(coins: Int): String = String.format(Locale.US, "%,d", coins)

private fun formatSignedCoins(amount: Int): String =
    if (amount >= 0) "+" + String.format(Locale.US, "%,d", amount)
    else "-" + String.format(Locale.US, "%,d", -amount)

private fun netColor(amount: Int): Color = when {
    amount > 0 -> WinGreen
    amount < 0 -> BrandRedLight
    else       -> BrandTextMuted
}

@Composable
private fun resultLabel(resultName: String?): String = when (resultName) {
    "WIN"  -> stringResource(R.string.profile_result_win)
    "LOSE" -> stringResource(R.string.profile_result_lose)
    "PUSH" -> stringResource(R.string.profile_result_push)
    else   -> resultName ?: "—"
}

private fun resultColor(resultName: String?): Color = when (resultName) {
    "WIN"  -> WinGreen
    "LOSE" -> BrandRedLight
    else   -> BrandTextMuted
}
