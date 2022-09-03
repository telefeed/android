package ru.tgfd.android.publication

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.skydoves.landscapist.glide.GlideImage
import ru.tgfd.android.toStringData
import ru.tgfd.ui.state.data.PublicationData

@Composable
fun PostHeader(postData: PublicationData) {
    Row(
        modifier = Modifier.padding(PaddingValues(top = 4.dp))
    ) {
        GlideImage(
            imageModel = postData.author.avatarUrl,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .border(1.dp, Color.Gray, CircleShape),
            contentDescription = "avatar",
            contentScale = ContentScale.Crop,
        )
        Column(modifier = Modifier.padding(PaddingValues(start = 10.dp))) {
            Text(text = postData.author.name)
            if (postData.author != postData.originalAuthor) {
                Text(text = "Переслано от ${postData.originalAuthor.name}")
            }
        }
        Text(
            color = Color.Gray,
            text = postData.timestamp.toStringData(),
            modifier = Modifier.padding(horizontal = 20.dp)
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(postData.text)
}
