package ru.tgfd.android.feed

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.FavoriteBorder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.tgfd.android.Settings

@Composable
fun DrawerContent(settings: Settings) {
    var experimentalFeedState by remember { mutableStateOf(settings.isExperimentalFacade()) }
    Column(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxHeight()
            .fillMaxWidth()
    ) {
        Row(
            Modifier
                .padding(4.dp)
                .height(54.dp)
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
        ) {
            Image(
                imageVector = Icons.Sharp.FavoriteBorder,
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.Red),
                modifier = Modifier.height(64.dp)
            )
            Spacer(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(12.dp)
            )
            Text(
                text = stringResource(id = ru.tgfd.android.R.string.favorites_title),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth(),
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp
            )
        }
        Spacer(
            modifier = Modifier.padding(4.dp)
                .height(1.dp).fillMaxWidth()
                .background(Color.LightGray)
        )

        Row(
            Modifier
                .padding(4.dp)
                .height(54.dp)
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .clickable {
                    settings.setExperimentalFacade(!experimentalFeedState)
                    experimentalFeedState = !experimentalFeedState
                }
        ) {
            Switch(
                checked = experimentalFeedState,
                onCheckedChange = null,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Spacer(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(12.dp)
            )
            Text(
                text = stringResource(id = ru.tgfd.android.R.string.experimental_feed_title),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth(),
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp
            )
        }

    }
}
