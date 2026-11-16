
package com.example.temuin.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.temuin.data.model.Item
import com.example.temuin.data.model.ItemStatus
import com.example.temuin.data.source.fake.FakeItemRepository
import com.example.temuin.ui.theme.*

private val CardBorder = Color(0xFFF0ECE8)
private val SoftBackground = Color(0xFFF6F4F1)
private val MutedText = Color(0xFF89827D)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateToMap: () -> Unit = {}
) {
    val items by viewModel.items.collectAsState()
    val locationFilter by viewModel.locationFilter.collectAsState()
    var selectedFilter by remember { mutableStateOf("Semua") }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.refresh() }

    val filteredItems = remember(items, selectedFilter, searchQuery, locationFilter) {
        items.filter { item ->
            val matchesTab = when (selectedFilter) {
                "Barang Hilang" -> item.status == ItemStatus.DICARI
                "Ditemukan" -> item.status == ItemStatus.DITEMUKAN
                else -> true
            }

            val matchesSearch = searchQuery.isBlank() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.location.contains(searchQuery, ignoreCase = true)

            val matchesLocation = locationFilter.isBlank() ||
                    item.location.contains(locationFilter, ignoreCase = true) ||
                    item.titikPengambilan.contains(locationFilter, ignoreCase = true)

            matchesTab && matchesSearch && matchesLocation
        }
    }

    HomeContent(
        items = filteredItems,
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        selectedFilter = selectedFilter,
        onFilterSelected = { selectedFilter = it },
        locationFilter = locationFilter,
        onClearLocationFilter = { viewModel.clearLocationFilter() },
        onItemClick = { onNavigateToDetail(it.id) },
        onRadarClick = onNavigateToMap,
        onFilterClick = {}
    )
}

@Composable
fun HomeContent(
    items: List<Item>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    locationFilter: String,
    onClearLocationFilter: () -> Unit,
    onItemClick: (Item) -> Unit,
    onRadarClick: () -> Unit,
    onFilterClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight) // .statusBarsPadding() dihapus karena sudah ditangani Scaffold di AppNavigation
    ) {
        // TopBar kini ditangani secara global oleh AppNavigation via AppTopBar
        SearchAndFilterSection(
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            selectedFilter = selectedFilter,
            onFilterSelected = onFilterSelected,
            locationFilter = locationFilter,
            onClearLocationFilter = onClearLocationFilter,
            onFilterClick = onFilterClick
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 96.dp // Supaya item paling bawah tidak ketutup BottomNavigationBar
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                FeedHeader(
                    count = items.size,
                    onRadarClick = onRadarClick
                )
            }

            if (items.isEmpty()) {
                item {
                    EmptyFeed()
                }
            } else {
                items(items, key = { it.id }) { item ->
                    ItemCard(
                        item = item,
                        onClick = { onItemClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = FakeItemRepository.PROFILE_AVATAR_URL,
            contentDescription = "Foto profil",
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SoftBackground),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun SearchAndFilterSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    locationFilter: String = "",
    onClearLocationFilter: () -> Unit = {},
    onFilterClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 13.dp),
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
                        if (searchQuery.isEmpty()) {
                            Text(
                                "Cari barang atau lokasi...",
                                fontSize = 13.sp,
                                color = MutedText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = 13.sp,
                                color = TextPrimary
                            ),
                            cursorBrush = SolidColor(OrangePrimary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            val isFilterActive = locationFilter.isNotBlank()
            Surface(
                modifier = Modifier
                    .size(46.dp)
                    .clickable(onClick = onFilterClick),
                shape = RoundedCornerShape(12.dp),
                color = if (isFilterActive) OrangeLight else Color.White,
                border = BorderStroke(1.dp, if (isFilterActive) OrangePrimary else CardBorder)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.Tune,
                        contentDescription = "Filter",
                        tint = if (isFilterActive) OrangePrimary else TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SoftBackground, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("Semua", "Barang Hilang", "Ditemukan").forEach { filter ->
                val selected = selectedFilter == filter

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(
                            if (selected) Color.White else Color.Transparent
                        )
                        .clickable { onFilterSelected(filter) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filter,
                        fontSize = 12.sp,
                        fontWeight = if (selected) {
                            FontWeight.SemiBold
                        } else {
                            FontWeight.Medium
                        },
                        color = if (selected) OrangePrimary else MutedText,
                        maxLines = 1
                    )
                }
            }
        }

        if (locationFilter.isNotBlank()) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(OrangeLight)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = OrangePrimary,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "Lokasi: $locationFilter",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OrangePrimary
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Hapus filter lokasi",
                    tint = OrangePrimary,
                    modifier = Modifier
                        .size(15.dp)
                        .clickable(onClick = onClearLocationFilter)
                )
            }
        }
    }
}

@Composable
fun FeedHeader(
    count: Int,
    onRadarClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Laporan Terbaru",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = "$count laporan tersedia",
                fontSize = 11.sp,
                color = MutedText
            )
        }

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onRadarClick)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                Icons.Rounded.Explore,
                contentDescription = null,
                tint = OrangePrimary,
                modifier = Modifier.size(17.dp)
            )

            Text(
                "Lihat Peta",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = OrangePrimary
            )
        }
    }
}

@Composable
fun ItemCard(
    item: Item,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.title,
                modifier = Modifier
                    .size(width = 104.dp, height = 116.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SoftBackground),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(116.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StatusTag(item.status)

                    Text(
                        text = item.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 19.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MutedText,
                            modifier = Modifier.size(14.dp)
                        )

                        Text(
                            text = item.location,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MutedText,
                            modifier = Modifier.size(12.dp)
                        )

                        Text(
                            text = item.time,
                            fontSize = 10.sp,
                            color = MutedText,
                            maxLines = 1
                        )
                    }

                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Lihat detail",
                        tint = MutedText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun StatusTag(status: ItemStatus) {
    val isLost = status == ItemStatus.DICARI

    val bgColor = if (isLost) TagHilangBg else TagTemuanBg
    val textColor = if (isLost) TagHilangText else TagTemuanText
    val label = if (isLost) "DICARI" else "DITEMUKAN"

    Row(
        modifier = Modifier
            .background(bgColor, CircleShape)
            .padding(horizontal = 9.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(textColor, CircleShape)
        )

        Text(
            text = label,
            color = textColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.4.sp
        )
    }
}


@Composable
fun EmptyFeed() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            Icons.Default.Search,
            contentDescription = null,
            tint = MutedText.copy(alpha = 0.6f),
            modifier = Modifier.size(36.dp)
        )

        Text(
            "Tidak ada laporan ditemukan",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )

        Text(
            "Coba gunakan kata kunci atau filter lain.",
            fontSize = 12.sp,
            color = MutedText
        )
    }
}
