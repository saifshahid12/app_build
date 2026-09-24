package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "campaigns")
data class CampaignEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String,
    val tagline: String,
    val description: String,
    val primaryColorHex: String,
    val accentColorHex: String,
    val materialFinish: String,
    val masterImagePath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
