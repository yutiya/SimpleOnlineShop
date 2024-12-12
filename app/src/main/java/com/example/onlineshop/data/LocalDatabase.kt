package com.example.onlineshop.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class LocalDatabase(private val context: Context) {
    private var data: Map<String, Any>? = null
    
    // 使用协程在后台加载数据
    suspend fun loadDatabase() = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.assets.open("Database.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<Map<String, Any>>() {}.type
            data = Gson().fromJson(jsonString, type)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    
    // 异步查询方法
    suspend fun query(path: String): Any? = withContext(Dispatchers.Default) {
        if (data == null) {
            loadDatabase()
        }
        
        val keys = path.split("/").filter { it.isNotEmpty() }
        var current: Any? = data
        
        for (key in keys) {
            when (current) {
                is Map<*, *> -> current = (current as Map<*, *>)[key]
                is List<*> -> {
                    val index = key.toIntOrNull()
                    current = if (index != null && index >= 0 && index < (current as List<*>).size) {
                        (current as List<*>)[index]
                    } else {
                        null
                    }
                }
                else -> return@withContext null
            }
        }
        
        return@withContext current
    }
} 