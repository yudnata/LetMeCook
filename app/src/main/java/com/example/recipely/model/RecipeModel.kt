package com.example.recipely.model

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
    val creatorId: String = "",
    val imageUrl: String = "",
)
