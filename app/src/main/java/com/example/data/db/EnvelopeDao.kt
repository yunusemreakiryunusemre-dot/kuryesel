package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.EnvelopeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EnvelopeDao {
    @Query("SELECT * FROM envelopes ORDER BY type ASC, id ASC")
    fun getAllEnvelopes(): Flow<List<EnvelopeEntity>>

    @Query("SELECT * FROM envelopes WHERE id = :id LIMIT 1")
    suspend fun getEnvelopeById(id: Int): EnvelopeEntity?

    @Query("SELECT COUNT(*) FROM envelopes")
    suspend fun getEnvelopeCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnvelope(envelope: EnvelopeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnvelopes(envelopes: List<EnvelopeEntity>)

    @Update
    suspend fun updateEnvelope(envelope: EnvelopeEntity)

    @Query("DELETE FROM envelopes WHERE id = :id")
    suspend fun deleteEnvelope(id: Int)

    @Query("DELETE FROM envelopes")
    suspend fun deleteAllEnvelopes()
}
