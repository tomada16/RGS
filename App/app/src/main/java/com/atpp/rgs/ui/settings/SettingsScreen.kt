package com.atpp.rgs.ui.settings

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.atpp.rgs.R
import com.atpp.rgs.RgsApplication
import com.atpp.rgs.ui.theme.BrandBlack
import com.atpp.rgs.ui.theme.BrandDivider
import com.atpp.rgs.ui.theme.BrandGold
import com.atpp.rgs.ui.theme.BrandGoldDark
import com.atpp.rgs.ui.theme.BrandPanel
import com.atpp.rgs.ui.theme.BrandTextMuted
import com.atpp.rgs.ui.theme.BrandWhite

@Composable
fun SettingsScreen(
    app: RgsApplication,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(app))
) {
    val masterVolume by viewModel.masterVolume.collectAsState()
    val musicVolume  by viewModel.musicVolume.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BrandBlack)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AudioSection(
            masterVolume = masterVolume,
            musicVolume  = musicVolume,
            onMasterChange = viewModel::setMasterVolume,
            onMusicChange  = viewModel::setMusicVolume
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AudioSection(
    masterVolume: Float,
    musicVolume: Float,
    onMasterChange: (Float) -> Unit,
    onMusicChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BrandPanel)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Nagłówek sekcji
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector        = Icons.Filled.VolumeUp,
                contentDescription = null,
                tint               = BrandGold,
                modifier           = Modifier.size(20.dp)
            )
            Text(
                text          = stringResource(R.string.settings_section_audio),
                color         = BrandGold,
                fontSize      = 12.sp,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }

        Spacer(Modifier.height(16.dp))
        Divider()
        Spacer(Modifier.height(16.dp))

        VolumeRow(
            label    = stringResource(R.string.settings_master_volume),
            value    = masterVolume,
            onChange = onMasterChange
        )

        Spacer(Modifier.height(4.dp))
        Divider()
        Spacer(Modifier.height(4.dp))

        VolumeRow(
            label    = stringResource(R.string.settings_music_volume),
            value    = musicVolume,
            onChange = onMusicChange
        )
    }
}

@Composable
private fun VolumeRow(
    label: String,
    value: Float,
    onChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text          = label,
                color         = BrandTextMuted,
                fontSize      = 12.sp,
                fontWeight    = FontWeight.Medium,
                letterSpacing = 1.sp
            )
            Text(
                text       = "${(value * 100).toInt()}%",
                color      = BrandGold,
                fontSize   = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value         = value,
            onValueChange = onChange,
            modifier      = Modifier.fillMaxWidth(),
            colors        = SliderDefaults.colors(
                thumbColor           = BrandGold,
                activeTrackColor     = BrandGold,
                inactiveTrackColor   = BrandGoldDark.copy(alpha = 0.3f)
            )
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
