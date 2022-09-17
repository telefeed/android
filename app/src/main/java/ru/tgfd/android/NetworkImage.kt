package ru.tgfd.android

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import ru.tgfd.core.model.AsyncImage

@Composable
fun NetworkImage(
    asyncImage: AsyncImage,
    contentDescription: String = "",
    modifier: Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {

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
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}
