package com.navigine.indoornavigationdemo.presentation.locations.composables

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import com.navigine.idl.java.Sublocation
import com.navigine.indoornavigationdemo.presentation.ui.theme.ColorPrimary
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.navigine.indoornavigationdemo.presentation.ui.theme.SublocationSelected


@Composable
fun SublocationsList(
    sublocations: List<Sublocation>,
    onSublocationClick: (Sublocation) -> Unit,
    modifier: Modifier = Modifier,
    maxVisibleItems: Int = 3

) {
    var selectedItem by rememberSaveable { mutableStateOf(0) }

    LazyColumn(
        modifier = modifier.height((48 * maxVisibleItems).dp)
    ) {
        items(sublocations.size) { index ->
            val item = sublocations[index]
            SubLocationListItem(
                modifier = Modifier.alpha(if (index == selectedItem) 0.9f else 0.75f),
                name = item.name,
                color = SublocationSelected.takeIf { index == selectedItem } ?: Color.White,
                onClick = {
                    selectedItem = index
                    onSublocationClick(item)
                }
            )
        }
    }
}

@Composable
@Preview
private fun SublocationsListPreview() {
    SublocationsList(
        sublocations = emptyList(),
        onSublocationClick = { _ -> Unit },
        modifier = Modifier
    )
}