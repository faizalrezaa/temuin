package com.example.temuin.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage

// Warna tambahan dari desain profil
val ProfileOrange = Color(0xFFFF6B2C)
val ProfileBrown = Color(0xFFA83A00)
val ProfileBrownText = Color(0xFFA0522D)
val ProfileChipBg = Color(0xFFF3E9E5)
val ProfileFieldBorder = Color(0xFFE2CFC8)
val ProfileDanger = Color(0xFFC62828)
val ProfileDangerBg = Color(0xFFFFDAD6)

/** Avatar dari URL; ikon orang kalau belum ada foto. */
@Composable
fun ProfileAvatar(size: Dp, url: String?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFFE8D5CC)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Person,
            contentDescription = null,
            tint = Color(0xFF8D6E63),
            modifier = Modifier.size(size * 0.6f)
        )
        if (!url.isNullOrBlank()) {
            AsyncImage(
                model = url,
                contentDescription = "Foto profil",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size).clip(CircleShape)
            )
        }
    }
}
