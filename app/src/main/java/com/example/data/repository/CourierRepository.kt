package com.example.data.repository

import com.example.data.db.EnvelopeDao
import com.example.data.db.TransactionDao
import com.example.data.model.EnvelopeEntity
import com.example.data.model.PredefinedEnvelopes
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

class CourierRepository(
    private val envelopeDao: EnvelopeDao,
    private val transactionDao: TransactionDao
) {
    val allEnvelopes: Flow<List<EnvelopeEntity>> = envelopeDao.getAllEnvelopes()
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val recentTransactions: Flow<List<TransactionEntity>> = transactionDao.getRecentTransactions(5)

    suspend fun checkAndSeedDefaultEnvelopes() {
        val count = envelopeDao.getEnvelopeCount()
        if (count == 0) {
            envelopeDao.insertEnvelopes(PredefinedEnvelopes.defaultEnvelopes)
        }
    }

    suspend fun insertTransaction(transaction: TransactionEntity): Long {
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun deleteTransaction(id: Int) {
        transactionDao.deleteTransaction(id)
    }

    suspend fun insertEnvelope(envelope: EnvelopeEntity): Long {
        return envelopeDao.insertEnvelope(envelope)
    }

    suspend fun updateEnvelope(envelope: EnvelopeEntity) {
        envelopeDao.updateEnvelope(envelope)
    }

    suspend fun deleteEnvelope(id: Int) {
        envelopeDao.deleteEnvelope(id)
    }

    suspend fun deleteAllData() {
        transactionDao.deleteAllTransactions()
        envelopeDao.deleteAllEnvelopes()
        checkAndSeedDefaultEnvelopes()
    }
}
