package com.example.temuin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.compose.AsyncImagePainter

@Composable
fun ReportThumbnail(
    url: String?,
    title: String,
    modifier: Modifier = Modifier,
) {
    SubcomposeAsyncImage(
        model = url?.takeIf(String::isNotBlank),
        contentDescription = title,
        modifier = modifier.clip(RoundedCornerShape(10.dp)),
        contentScale = ContentScale.Crop,
    ) {
        when (painter.state) {
            is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()
            else -> Box(
                Modifier.fillMaxSize().background(Color(0xFFF6F4F1)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFF9A8E87))
            }
        }
    }
}
