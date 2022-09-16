package ru.tgfd.android.publication

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.tgfd.android.NetworkImage
import ru.tgfd.android.toStringData
import ru.tgfd.ui.state.data.PublicationData

@Composable
fun PostHeader(postData: PublicationData) {
    Row {
        NetworkImage(
            asyncImage = postData.author.avatar,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
        )
        Column(modifier = Modifier.padding(PaddingValues(start = 12.dp))) {
            Text(
                text = postData.author.name,
                fontSize = 18.sp
            )
            if (postData.author != postData.originalAuthor) {
                Text(text = "Переслано от ${postData.originalAuthor.name}")
            }
            Text(
                color = Color.Gray,
                text = postData.timestamp.toStringData(),
                fontSize = 12.sp
            )
        }
    }
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = postData.text,
        fontSize = 16.sp,
        color = Color.DarkGray,
        lineHeight = 22.sp
    )
}
