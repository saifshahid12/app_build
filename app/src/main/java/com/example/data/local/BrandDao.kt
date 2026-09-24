package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BrandDao {
    @Query("SELECT * FROM campaigns ORDER BY createdAt DESC")
    fun getAllCampaigns(): Flow<List<CampaignEntity>>

    @Query("SELECT * FROM campaigns WHERE id = :id")
    suspend fun getCampaignById(id: Long): CampaignEntity?

    @Query("SELECT * FROM campaigns WHERE id = :id")
    fun observeCampaignById(id: Long): Flow<CampaignEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaign(campaign: CampaignEntity): Long

    @Update
    suspend fun updateCampaign(campaign: CampaignEntity)

    @Query("DELETE FROM campaigns WHERE id = :id")
    suspend fun deleteCampaignById(id: Long)

    @Query("SELECT * FROM medium_shots WHERE campaignId = :campaignId ORDER BY createdAt ASC")
    fun getShotsForCampaign(campaignId: Long): Flow<List<MediumShotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShot(shot: MediumShotEntity): Long

    @Query("DELETE FROM medium_shots WHERE id = :id")
    suspend fun deleteShotById(id: Long)

    @Query("DELETE FROM medium_shots WHERE campaignId = :campaignId AND mediumType = :mediumType")
    suspend fun deleteShotsByMedium(campaignId: Long, mediumType: String)
}
