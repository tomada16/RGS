package com.atpp.rgs.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.atpp.rgs.R
import com.atpp.rgs.RgsApplication
import com.atpp.rgs.ui.components.GradientButton
import com.atpp.rgs.ui.theme.BrandBlack
import com.atpp.rgs.ui.theme.BrandPanel
import com.atpp.rgs.ui.theme.BrandRedDark
import com.atpp.rgs.ui.theme.rgsColors

/**
 * Ekran logowania / rejestracji.
 * Dla rejestracji pokazujemy dodatkowe pole "CONFIRM PASSWORD" — walidacja
 * (zgodność haseł) jest w AuthViewModel.submit zanim cokolwiek poleci do repo.
 */
@Composable
fun AuthScreen(
    app: RgsApplication,
    viewModel: AuthViewModel = viewModel(factory = AuthViewModel.factory(app))
) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBlack)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            BrandLogoHeader()

            Spacer(Modifier.height(28.dp))

            FormPanel(
                modifier = Modifier.weight(1f),
                state = state,
                onUsername = viewModel::onUsernameChange,
                onPassword = viewModel::onPasswordChange,
                onConfirmPassword = viewModel::onConfirmPasswordChange,
                onSubmit = viewModel::submit,
                onToggleMode = viewModel::toggleMode
            )
        }
    }
}

/** Logo + tagline pod logo. */
@Composable
private fun BrandLogoHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = R.drawable.ic_logo),
            contentDescription = stringResource(R.string.auth_logo_cd),
            modifier = Modifier.size(140.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.auth_tagline),
            color = MaterialTheme.rgsColors.accentGold,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 4.sp
        )
        Spacer(Modifier.height(6.dp))
        // Złota kreska pod tagline z fade-outem po obu stronach
        // (poziomy gradient: przezroczysty -> złoto -> złoto -> przezroczysty).
        val gold = MaterialTheme.rgsColors.accentGold
        Box(
            modifier = Modifier
                .height(1.dp)
                .width(100.dp)
                .background(
                    Brush.horizontalGradient(
                        0.00f to Color.Transparent,
                        0.50f to gold,
                        1.00f to Color.Transparent
                    )
                )
        )
    }
}

@Composable
private fun FormPanel(
    modifier: Modifier = Modifier,
    state: AuthUiState,
    onUsername: (String) -> Unit,
    onPassword: (String) -> Unit,
    onConfirmPassword: (String) -> Unit,
    onSubmit: () -> Unit,
    onToggleMode: () -> Unit
) {
    val isLogin = state.mode == AuthMode.LOGIN

    // Burgundowy panel z gradientem (góra trochę jaśniej, dół ciemniej).
    val panelBg = Brush.verticalGradient(
        listOf(BrandRedDark.copy(alpha = 0.55f), BrandRedDark.copy(alpha = 0.85f))
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(panelBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 28.dp)
    ) {
        Text(
            text = stringResource(if (isLogin) R.string.auth_login_title else R.string.auth_register_title),
            color = MaterialTheme.rgsColors.textPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(if (isLogin) R.string.auth_login_subtitle else R.string.auth_register_subtitle),
            color = MaterialTheme.rgsColors.textMuted,
            fontSize = 13.sp
        )

        Spacer(Modifier.height(24.dp))

        // ---- Username ----
        FieldLabel(
            icon = { Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.rgsColors.accentGold, modifier = Modifier.size(16.dp)) },
            text = stringResource(R.string.auth_label_username)
        )
        Spacer(Modifier.height(6.dp))
        BrandTextField(
            value = state.username,
            onValueChange = onUsername,
            placeholder = stringResource(R.string.auth_placeholder_username),
            enabled = !state.isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        Spacer(Modifier.height(16.dp))

        // ---- Password ----
        FieldLabel(
            icon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.rgsColors.accentGold, modifier = Modifier.size(16.dp)) },
            text = stringResource(R.string.auth_label_password)
        )
        Spacer(Modifier.height(6.dp))
        BrandTextField(
            value = state.password,
            onValueChange = onPassword,
            placeholder = stringResource(R.string.auth_placeholder_password),
            enabled = !state.isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation()
        )

        // ---- Confirm password (tylko REGISTER) ----
        if (!isLogin) {
            Spacer(Modifier.height(16.dp))
            FieldLabel(
                icon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.rgsColors.accentGold, modifier = Modifier.size(16.dp)) },
                text = stringResource(R.string.auth_label_confirm_password)
            )
            Spacer(Modifier.height(6.dp))
            BrandTextField(
                value = state.confirmPassword,
                onValueChange = onConfirmPassword,
                placeholder = stringResource(R.string.auth_placeholder_confirm_password),
                enabled = !state.isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation()
            )
        }

        // ---- Komunikat błędu (z resourceId lub raw stringa z repo) ----
        val errorText: String? = when {
            state.errorRes != null -> stringResource(state.errorRes)
            state.errorMessage != null -> state.errorMessage
            else -> null
        }
        if (errorText != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp
            )
        }

        Spacer(Modifier.height(24.dp))

        GradientButton(
            text = stringResource(if (isLogin) R.string.auth_btn_login else R.string.auth_btn_register),
            onClick = onSubmit,
            enabled = !state.isLoading,
            isLoading = state.isLoading
        )

        Spacer(Modifier.height(12.dp))

        TextButton(
            onClick = onToggleMode,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(
                    if (isLogin) R.string.auth_toggle_to_register else R.string.auth_toggle_to_login
                ),
                color = MaterialTheme.rgsColors.accentGold,
                fontSize = 13.sp
            )
        }
    }
}

/** Etykieta pola: mała ikona (gold) + tekst caps. */
@Composable
private fun FieldLabel(
    icon: @Composable () -> Unit,
    text: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        icon()
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            color = MaterialTheme.rgsColors.accentGold,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.5.sp
        )
    }
}

/**
 * Stylizowany [OutlinedTextField] — ciemne wypełnienie, brand-owe kolory.
 * Wyciągnięty osobno bo używamy go w 2-3 miejscach z tymi samymi parametrami koloru.
 */
@Composable
private fun BrandTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation =
        androidx.compose.ui.text.input.VisualTransformation.None
) {
    // Stłumiona chłodna szarość domyślnie. Po wejściu w pole rozjaśnia się do złota,
    // żeby wizualnie sygnalizować focus (drobna afordancja, nie psuje brandu).
    val unfocusedBorder = MaterialTheme.rgsColors.inputBorder
    val focusedBorder = MaterialTheme.rgsColors.accentGold
    val fieldShape = RoundedCornerShape(16.dp)

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(fieldShape),
        enabled = enabled,
        singleLine = true,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        textStyle = TextStyle(color = MaterialTheme.rgsColors.textPrimary, fontSize = 15.sp),
        placeholder = {
            Text(
                text = placeholder,
                color = MaterialTheme.rgsColors.textMuted,
                fontSize = 14.sp
            )
        },
        shape = fieldShape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = BrandPanel,
            unfocusedContainerColor = BrandPanel,
            disabledContainerColor = BrandPanel,
            errorContainerColor = BrandPanel,
            focusedBorderColor = focusedBorder,
            unfocusedBorderColor = unfocusedBorder,
            disabledBorderColor = unfocusedBorder.copy(alpha = 0.4f),
            cursorColor = focusedBorder,
            focusedTextColor = MaterialTheme.rgsColors.textPrimary,
            unfocusedTextColor = MaterialTheme.rgsColors.textPrimary
        )
    )
}
