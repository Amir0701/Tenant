package com.example.tenant.data.repository

import com.example.tenant.data.dao.Dao
import com.example.tenant.data.model.Category
import com.example.tenant.ioc.scope.CategoryRepositoryScope
import javax.inject.Inject

@CategoryRepositoryScope
class CategoryRepository @Inject constructor(private val dao: Dao) {
    suspend fun addCategory(category: Category) = dao.addCategory(category)
    suspend fun getAllCategories() = dao.getAllCategories()
}