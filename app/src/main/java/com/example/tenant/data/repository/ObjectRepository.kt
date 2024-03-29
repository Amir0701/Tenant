package com.example.tenant.data.repository

import com.example.tenant.data.dao.Dao
import com.example.tenant.data.model.Obbject
import com.example.tenant.data.model.ObjectStatus
import com.example.tenant.ioc.scope.ObbjectRepositoryScope
import javax.inject.Inject

@ObbjectRepositoryScope
class ObjectRepository @Inject constructor(private val dao: Dao) {
    suspend fun add(obbject: Obbject) = dao.addObject(obbject)

    suspend fun getObjectById(id: Int) = dao.getObjectById(id)

    suspend fun getAllObjects() = dao.getAllObjects()

    suspend fun getObjectsWithCategory() = dao.getAllObjectsWithCategory()

    suspend fun deleteObject(id: Int): Int = dao.deleteObject(id)

    suspend fun updateStatusObject(id: Int, objectStatus: ObjectStatus) =
        dao.updateObjectStatus(id, objectStatus)

    suspend fun getObjectsWithHistoryPay() = dao.getObjectsWithHistoryPay()

    suspend fun getObjectsWithExploitations() = dao.getObjectsWithExploitations()
}