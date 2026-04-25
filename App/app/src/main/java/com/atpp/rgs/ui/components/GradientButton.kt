package com.atpp.rgs.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atpp.rgs.ui.theme.BrandGold
import com.atpp.rgs.ui.theme.BrandGoldDark
import com.atpp.rgs.ui.theme.BrandGoldLight
import com.atpp.rgs.ui.theme.BrandRedDark
import com.atpp.rgs.ui.theme.rgsColors

/**
 * Główny przycisk akcji z poziomym gradientem (jaśniejszy ⇒ ciemniejszy).
 */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    /**
     * Kolory gradientu od lewej do prawej. Pierwszy = najjaśniejszy.
     */
    gradientColors: List<Color> = listOf(BrandGoldLight, BrandGold, BrandGoldDark),
    /** Kolor tekstu  */
    contentColor: Color = MaterialTheme.rgsColors.onAccentGold,
    /** Wysokość cienia (Material elevation). 0.dp = bez cienia. */
    elevation: Dp = 12.dp,
    /** Kolor cienia. */
    shadowColor: Color = Color.Black,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    val interactive = enabled && !isLoading
    val brush = Brush.horizontalGradient(gradientColors)
    val shape = RoundedCornerShape(8.dp)
    val effectiveElevation = if (interactive) elevation else 0.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            // Shadow MUSI być przed .clip()/.background() — inaczej zostałby przycięty
            // do kształtu razem z tłem.
            .shadow(
                elevation = effectiveElevation,
                shape = shape,
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(shape)
            .background(if (interactive) brush else Brush.horizontalGradient(
                listOf(BrandRedDark, BrandRedDark)
            ))
            .clickable(enabled = interactive) { onClick() }
            .padding(PaddingValues(horizontal = 24.dp, vertical = 14.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = text,
                    color = contentColor,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    fontSize = 16.sp
                )
                if (trailingIcon != null) {
                    Spacer(Modifier.width(10.dp))
                    trailingIcon()
                }
            }
        }
    }
}
