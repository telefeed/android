package ru.tgfd.android.publication

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.tgfd.ui.state.Publication

@Composable
internal fun PublicationScreen(state: Publication) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        border = BorderStroke(1.dp, Color.Black),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = state.data.author.name)
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = state.data.text)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Timestamp: ${state.data.timestamp}")
        }
    }
}
