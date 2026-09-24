package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medium_shots",
    foreignKeys = [
        ForeignKey(
            entity = CampaignEntity::class,
            parentColumns = ["id"],
            childColumns = ["campaignId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["campaignId"])]
)
data class MediumShotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val campaignId: Long,
    val mediumType: String,
    val mediumTitle: String,
    val prompt: String,
    val imagePath: String,
    val aspectRatio: String = "1:1",
    val isConsistentWithMaster: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
