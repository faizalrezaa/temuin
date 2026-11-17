package com.example.temuin.data.match

/**
 * Domain model for POST /reports/{id}/matches (API Contract v1, Fungsi 4).
 * The server owns candidate selection and AI scoring; the client only presents the result.
 */
enum class MatchSource { AI, RULE_BASED }

enum class MatchLevel { HIGH, MEDIUM, LOW }

enum class MatchAttribute { CATEGORY, COLOR, LOCATION, TIME, PHOTO, KEYWORDS }

data class MatchReport(
    val id: String,
    val title: String,
    val category: String,
    val color: String?,
    val description: String,
    val occurredAt: String,
    val locationName: String,
    val ownerName: String?,
)

data class MatchItem(
    val report: MatchReport,
    val level: MatchLevel,
    val matchedAttributes: List<MatchAttribute> = emptyList(),
    val differentAttributes: List<MatchAttribute> = emptyList(),
    val notComparableAttributes: List<MatchAttribute> = emptyList(),
    val reason: String = "",
    val incompleteData: Boolean = false,
)

data class MatchResult(
    val source: MatchSource,
    val notices: List<String> = emptyList(),
    val candidatesFound: Int = 0,
    val items: List<MatchItem> = emptyList(),
)
