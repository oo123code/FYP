package com.navigine.indoornavigationdemo.presentation.locations.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.navigine.indoornavigationdemo.presentation.ui.theme.ColorPrimary

@Composable
fun SubLocationListItem(
    name: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified
) {
    Card(
        modifier = modifier
            .size(48.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
//                indication = rememberRipple(color = ColorPrimary),
                onClick = onClick
            ),
        shape = RectangleShape,
        colors = CardDefaults.cardColors(containerColor = color),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = modifier.fillMaxSize()) {
            Text(
                text = name,
                modifier = modifier.padding(5.dp),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }
    }
}
