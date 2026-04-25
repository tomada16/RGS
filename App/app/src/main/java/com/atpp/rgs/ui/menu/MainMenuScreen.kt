package com.atpp.rgs.ui.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.atpp.rgs.RgsApplication

@Composable
fun MainMenuScreen(
    app: RgsApplication,
    viewModel: MainMenuViewModel = viewModel(factory = MainMenuViewModel.factory(app))
) {
    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "TBD", style = MaterialTheme.typography.displayLarge)
            Spacer(Modifier.height(24.dp))
            OutlinedButton(onClick = viewModel::logout) {
                Text("Wyloguj")
            }
        }
    }
}
