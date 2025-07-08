package com.example.letmecook.model

data class Recipe (
    var id: String = "",
    val title: String = "",
    val description: String = "",
    val process: String = "",
    val duration: String = "",
    val carbs: String = "",
    val proteins: String = "",
    val fats: String = "",
    val category: String = "",
    val cuisine: String = "",
    val halalStatus: String = "",
    val creatorId: String = "",
    var creatorName: String = "",
    val imageUrl: String = "",
    var averageRating: Float = 0.0f,
    var totalRatings: Int = 0,
    @set:JvmName("setBookmarked")
    var isBookmarked: Boolean = false
)