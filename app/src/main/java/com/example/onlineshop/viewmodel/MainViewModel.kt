package com.example.onlineshop.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onlineshop.data.LocalDatabase
import com.example.onlineshop.model.CategoryModel
import com.example.onlineshop.model.ItemsModel
import com.example.onlineshop.model.SliderModel
import kotlinx.coroutines.launch
import com.google.gson.Gson

class MainViewModel(context: Context) : ViewModel() {
    private val database = LocalDatabase(context)
    
    private val _banner = MutableLiveData<List<SliderModel>>()
    val banners: LiveData<List<SliderModel>> = _banner

    private val _categories = MutableLiveData<List<CategoryModel>>()
    val categories: LiveData<List<CategoryModel>> = _categories

    private val _recommended = MutableLiveData<List<ItemsModel>>()
    val recommended: LiveData<List<ItemsModel>> = _recommended

    init {
        viewModelScope.launch {
            database.loadDatabase()
        }
    }

    fun loadBanners() {
        viewModelScope.launch {
            try {
                val result = database.query("Banner")
                if (result is List<*>) {
                    val bannerList = result.mapNotNull { item ->
                        convertToModel<SliderModel>(item)
                    }
                    _banner.value = bannerList
                }
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            try {
                val lists = mutableListOf<CategoryModel>()
                val result = database.query("Category")
                if (result is List<*>) {
                    val categoryList = result.mapNotNull { item ->
                        convertToModel<CategoryModel>(item)
                    }
                    lists.addAll(categoryList)
                }
                _categories.value = lists
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }

    fun loadRecommended() {
        viewModelScope.launch {
            try {
                val lists = mutableListOf<ItemsModel>()
                val result = database.query("Items")
                if (result is List<*>) {
                    val itemsList = result.mapNotNull { item ->
                        convertToModel<ItemsModel>(item)
                    }?.filter { it.showRecommended }
                    if (itemsList != null) {
                        lists.addAll(itemsList)
                    }
                }
                _recommended.value = lists
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }

    fun loadFiltered(id: String) {
        viewModelScope.launch {
            try {
                val lists = mutableListOf<ItemsModel>()
                val result = database.query("Items")
                if (result is List<*>) {
                    val itemsList = result.mapNotNull { item ->
                        convertToModel<ItemsModel>(item)
                    }?.filter { it.categoryId == id }
                    if (itemsList != null) {
                        lists.addAll(itemsList)
                    }
                }
                _recommended.value = lists
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }

    private inline fun <reified T> convertToModel(item: Any?): T? {
        return try {
            val gson = Gson()
            val json = gson.toJson(item)
            gson.fromJson(json, T::class.java)
        } catch (e: Exception) {
            null
        }
    }
}