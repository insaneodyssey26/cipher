package com.masum.cipher.core.domain.usecase

import com.masum.cipher.core.data.local.dao.SubscriptionDao
import com.masum.cipher.core.data.local.entity.SubscriptionEntity
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.data.repository.TransactionRepository
import com.masum.cipher.core.domain.SubscriptionSchedule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class ApproveSubscriptionUseCase @Inject constructor(
    private val subscriptionDao: SubscriptionDao,
    private val transactionRepository: TransactionRepository,
    private val userPreferences: UserPreferences
) {
    suspend operator fun invoke(subscription: SubscriptionEntity) {
        val now = System.currentTimeMillis()
        val newTransaction = TransactionEntity(
            merchant = subscription.merchant,
            amount = subscription.amount,
            currency = userPreferences.settingsFlow.first().currencyCode,
            rawSms = null,
            category = subscription.category,
            timestamp = now,
            isIncome = false,
            note = "Approved subscription"
        )
        transactionRepository.insertTransaction(newTransaction)
        subscriptionDao.update(SubscriptionSchedule.resolved(subscription, now))
    }
}

class SkipSubscriptionUseCase @Inject constructor(
    private val subscriptionDao: SubscriptionDao
) {
    suspend operator fun invoke(subscription: SubscriptionEntity) {
        subscriptionDao.update(SubscriptionSchedule.resolved(subscription, System.currentTimeMillis()))
    }
}

class SaveSubscriptionUseCase @Inject constructor(
    private val subscriptionDao: SubscriptionDao
) {
    suspend operator fun invoke(
        merchant: String,
        amount: Double,
        category: String,
        frequencyDays: Int,
        nextExpectedDate: Long
    ) {
        val existing = subscriptionDao.getAllSubscriptions().firstOrNull()
            ?.find { it.merchant.equals(merchant, ignoreCase = true) }
        val entity = SubscriptionEntity(
            id = existing?.id ?: 0,
            merchant = merchant,
            amount = amount,
            category = category,
            frequencyDays = frequencyDays,
            nextExpectedDate = nextExpectedDate
        )
        subscriptionDao.insert(entity)
    }
}

class DeleteSubscriptionUseCase @Inject constructor(
    private val subscriptionDao: SubscriptionDao
) {
    suspend operator fun invoke(merchant: String): SubscriptionEntity? {
        val existing = subscriptionDao.getAllSubscriptions().firstOrNull()
            ?.find { it.merchant.equals(merchant, ignoreCase = true) }
        if (existing != null) {
            subscriptionDao.delete(existing)
        }
        return existing
    }
}

class RestoreSubscriptionUseCase @Inject constructor(
    private val subscriptionDao: SubscriptionDao
) {
    suspend operator fun invoke(subscription: SubscriptionEntity) {
        subscriptionDao.insert(subscription)
    }
}
