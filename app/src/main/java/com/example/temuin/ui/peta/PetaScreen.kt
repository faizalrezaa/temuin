package com.example.temuin.ui.peta

import android.content.Context
import android.content.Intent
import android.graphics.PointF
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.example.temuin.data.model.Item
import com.example.temuin.data.model.PickupPoint
import com.example.temuin.data.model.PickupPointIcon
import com.example.temuin.ui.home.StatusTag
import com.example.temuin.ui.theme.OrangePrimary
import com.example.temuin.ui.theme.TextPrimary
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

private val CardBorder = Color(0xFFF0ECE8)
private val SoftBackground = Color(0xFFF6F4F1)
private val MutedText = Color(0xFF89827D)

private const val OSM_STYLE_JSON = """
{
  "version": 8,
  "sources": {
    "osm": {
      "type": "raster",
      "tiles": [
        "https://tile.openstreetmap.org/{z}/{x}/{y}.png"
      ],
      "tileSize": 256,
      "attribution": "&copy; OpenStreetMap Contributors"
    }
  },
  "layers": [
    {
      "id": "osm-tiles",
      "type": "raster",
      "source": "osm",
      "minzoom": 0,
      "maxzoom": 19
    }
  ]
}
"""

@Composable
fun PetaScreen(
    viewModel: PetaViewModel,
    onNavigateToHomeWithFilter: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    PetaContent(
        uiState = uiState,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onSelectPickupPoint = viewModel::selectPickupPoint,
        onNavigateToHomeWithFilter = onNavigateToHomeWithFilter,
        onOpenDirections = { point ->
            openExternalGoogleMapsNavigation(context, point)
        }
    )
}

@Composable
fun PetaContent(
    uiState: PetaUiState,
    onSearchQueryChange: (String) -> Unit,
    onSelectPickupPoint: (PickupPoint) -> Unit,
    onNavigateToHomeWithFilter: (String) -> Unit,
    onOpenDirections: (PickupPoint) -> Unit
) {
    val context = LocalContext.current
    val ubCenter = remember { LatLng(-7.9529, 112.6138) }

    var mapInstance by remember { mutableStateOf<MapLibreMap?>(null) }
    var cameraUpdateTrigger by remember { mutableIntStateOf(0) }

    val density = LocalDensity.current

    val mapView = rememberMapViewWithLifecycle(context = context)

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. MapLibre MapView with OpenStreetMap Tiles
        AndroidView(
            factory = {
                mapView.apply {
                    getMapAsync { map ->
                        mapInstance = map
                        map.setStyle(Style.Builder().fromJson(OSM_STYLE_JSON)) {
                            map.uiSettings.isAttributionEnabled = false
                            map.uiSettings.isLogoEnabled = false
                            map.uiSettings.isCompassEnabled = false
                            map.cameraPosition = CameraPosition.Builder()
                                .target(ubCenter)
                                .zoom(16.5)
                                .build()
                            cameraUpdateTrigger++
                        }

                        map.addOnCameraMoveListener {
                            cameraUpdateTrigger++
                        }
                        map.addOnCameraIdleListener {
                            cameraUpdateTrigger++
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Custom Compose Markers Overlay (Preserves Radar Pulse & Floating Pills)
        key(cameraUpdateTrigger) {
            val currentMap = mapInstance
            if (currentMap != null) {
                uiState.filteredPoints.forEach { point ->
                    val isSelected = uiState.selectedPoint?.id == point.id
                    val screenLocation: PointF = currentMap.projection.toScreenLocation(
                        LatLng(point.latitude, point.longitude)
                    )

                    // Render marker only when within visible viewport
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    x = screenLocation.x.toInt(),
                                    y = screenLocation.y.toInt()
                                )
                            }
                            .wrapContentSize(unbounded = true)
                    ) {
                        Box(
                            modifier = Modifier
                                .offset(
                                    x = if (isSelected) (-54).dp else (-46).dp,
                                    y = if (isSelected) (-88).dp else (-72).dp
                                )
                                .clickable {
                                    onSelectPickupPoint(point)
                                    currentMap.animateCamera(
                                        CameraUpdateFactory.newLatLngZoom(
                                            LatLng(point.latitude, point.longitude),
                                            16.8
                                        )
                                    )
                                }
                        ) {
                            PickupPointMarker(
                                point = point,
                                isSelected = isSelected
                            )
                        }
                    }
                }
            }
        }

        // 3. Interactive Top Search Bar Overlay & Dropdown Results
        var isDropdownVisible by remember { mutableStateOf(false) }
        val focusManager = LocalFocusManager.current

        LaunchedEffect(uiState.searchQuery) {
            if (uiState.searchQuery.isNotBlank()) {
                isDropdownVisible = true
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .shadow(elevation = 6.dp, shape = RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = MutedText,
                        modifier = Modifier.size(20.dp)
                    )

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (uiState.searchQuery.isEmpty()) {
                            Text(
                                "Cari pos pickup atau lokasi UB...",
                                fontSize = 13.sp,
                                color = MutedText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        BasicTextField(
                            value = uiState.searchQuery,
                            onValueChange = {
                                onSearchQueryChange(it)
                                isDropdownVisible = it.isNotBlank()
                            },
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = 13.sp,
                                color = TextPrimary
                            ),
                            cursorBrush = SolidColor(OrangePrimary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (uiState.searchQuery.isNotEmpty()) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Hapus pencarian",
                            tint = MutedText,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable {
                                    onSearchQueryChange("")
                                    isDropdownVisible = false
                                }
                        )
                    }
                }
            }

            // Dropdown List Hasil Pencarian Lokasi
            if (isDropdownVisible && uiState.searchQuery.isNotBlank()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 8.dp, shape = RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        if (uiState.filteredPoints.isEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MutedText,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Lokasi tidak ditemukan",
                                    fontSize = 13.sp,
                                    color = MutedText
                                )
                            }
                        } else {
                            uiState.filteredPoints.forEachIndexed { index, point ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            isDropdownVisible = false
                                            focusManager.clearFocus()
                                            onSelectPickupPoint(point)
                                            mapInstance?.animateCamera(
                                                CameraUpdateFactory.newLatLngZoom(
                                                    LatLng(point.latitude, point.longitude),
                                                    17.0
                                                )
                                            )
                                        }
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(OrangePrimary.copy(alpha = 0.12f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = OrangePrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = point.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = point.locationDetail,
                                            fontSize = 11.sp,
                                            color = MutedText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                if (index < uiState.filteredPoints.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 14.dp),
                                        thickness = 0.5.dp,
                                        color = CardBorder
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Quick Location & Campus Layer Indicator (Pojok Kanan Atas)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 74.dp, end = 16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier
                    .size(42.dp)
                    .clickable {
                        mapInstance?.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(ubCenter, 16.5)
                        )
                    },
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.MyLocation,
                        contentDescription = "Kembali ke lokasi UB",
                        tint = OrangePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.94f),
                shadowElevation = 2.dp
            ) {
                Text(
                    text = "Kampus UB (OSM)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // 5. Persistent Bottom Sheet Details
        AnimatedVisibility(
            visible = uiState.selectedPoint != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(durationMillis = 200)
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            uiState.selectedPoint?.let { selectedPoint ->
                PickupPointDetailCard(
                    pickupPoint = selectedPoint,
                    items = uiState.itemsAtSelectedPoint,
                    onNavigateToHome = {
                        onNavigateToHomeWithFilter(selectedPoint.shortName)
                    },
                    onDirectionsClick = {
                        onOpenDirections(selectedPoint)
                    }
                )
            }
        }
    }
}

@Composable
fun PickupPointMarker(
    point: PickupPoint,
    isSelected: Boolean
) {
    val icon = when (point.iconType) {
        PickupPointIcon.LIBRARY -> Icons.Default.LocalLibrary
        PickupPointIcon.SECURITY -> Icons.Default.Security
        PickupPointIcon.FACULTY -> Icons.Default.School
        PickupPointIcon.STUDENT_CENTER -> Icons.Default.Groups
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Label Pill
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) OrangePrimary else Color.White,
            shadowElevation = 4.dp,
            border = if (!isSelected) BorderStroke(1.dp, CardBorder) else null
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else OrangePrimary,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = point.shortName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else TextPrimary
                )
            }
        }

        // Pin Circle with Radar Pulse Animation
        Box(
            modifier = Modifier.size(if (isSelected) 48.dp else 40.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                val infiniteTransition = rememberInfiniteTransition(label = "RadarPulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.45f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "PulseScale"
                )
                val pulseAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.5f,
                    targetValue = 0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "PulseAlpha"
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(pulseScale)
                        .background(OrangePrimary.copy(alpha = pulseAlpha), CircleShape)
                )
            }

            Surface(
                modifier = Modifier.size(if (isSelected) 40.dp else 34.dp),
                shape = CircleShape,
                color = if (isSelected) OrangePrimary else Color.White,
                shadowElevation = 6.dp,
                border = BorderStroke(2.dp, Color.White)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = point.name,
                        tint = if (isSelected) Color.White else OrangePrimary,
                        modifier = Modifier.size(if (isSelected) 20.dp else 18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PickupPointDetailCard(
    pickupPoint: PickupPoint,
    items: List<Item>,
    onNavigateToHome: () -> Unit,
    onDirectionsClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0))
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Nama Pos
            Text(
                text = pickupPoint.name,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Detail Posisi Ruangan
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SoftBackground, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = OrangePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = "Posisi Ruangan",
                        fontSize = 11.sp,
                        color = MutedText,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = pickupPoint.locationDetail,
                        fontSize = 12.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Section Header Carousel Mini
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Barang Diamankan (${items.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                // Tombol Lihat Produk 1 (Text Link)
                Text(
                    text = "Lihat Semua",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OrangePrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onNavigateToHome)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Horizontal Carousel Mini Items
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(SoftBackground, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada barang di pos ini",
                        fontSize = 12.sp,
                        color = MutedText
                    )
                }
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        MiniItemCard(item = item, onClick = onNavigateToHome)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dual Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tombol Petunjuk Arah / Rute (Membuka Google Maps Eksternal)
                OutlinedButton(
                    onClick = onDirectionsClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CardBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Icon(
                        Icons.Default.Directions,
                        contentDescription = null,
                        tint = OrangePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Rute",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }

                // Tombol Lihat Produk 2 (Primary Filled Button)
                Button(
                    onClick = onNavigateToHome,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangePrimary,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Lihat Produk",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun MiniItemCard(
    item: Item,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(170.dp)
            .height(52.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = SoftBackground,
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.title,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = item.title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                StatusTag(status = item.status)
            }
        }
    }
}

@Composable
fun rememberMapViewWithLifecycle(context: Context): MapView {
    val mapView = remember {
        MapLibre.getInstance(context)
        MapView(context)
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    return mapView
}

/**
 * Membuka aplikasi Google Maps eksternal secara langsung untuk memandu rute/navigasi ke lokasi pos.
 */
private fun openExternalGoogleMapsNavigation(context: Context, point: PickupPoint) {
    try {
        // 1. Coba buka navigasi Google Maps App langsung
        val navigationUri = Uri.parse("google.navigation:q=${point.latitude},${point.longitude}&mode=d")
        val mapIntent = Intent(Intent.ACTION_VIEW, navigationUri).apply {
            setPackage("com.google.android.apps.maps")
        }
        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            // 2. Fallback jika Google Maps app belum terinstal atau tidak merespons action tersebut
            val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${point.latitude},${point.longitude}")
            val fallbackIntent = Intent(Intent.ACTION_VIEW, webUri)
            context.startActivity(fallbackIntent)
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Tidak dapat membuka Google Maps: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}
