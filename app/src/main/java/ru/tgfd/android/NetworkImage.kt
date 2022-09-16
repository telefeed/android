package ru.tgfd.android

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import ru.tgfd.core.AsyncImage

@Composable
fun NetworkImage(asyncImage: AsyncImage, modifier: Modifier) {

    var image by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(asyncImage) {
        val byteArray = asyncImage.bytes()

        if (byteArray.isNotEmpty()) {
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            image = bitmap.asImageBitmap()
        }
    }

    val imageBitmap = image

    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap,
            "",
            modifier = modifier
        )
    }
}
