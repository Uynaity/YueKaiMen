package com.uynaity.opendoor

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

class PostClient {
    val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun getDoorInfo(context: Context, phone: String): List<DoorInfo>? {
        val result = CompletableDeferred<List<DoorInfo>?>()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: HttpResponse =
                    client.post("https://wxapi1210.grpu.com.cn/thirdParty/getExtEquipment") {
                        contentType(ContentType.Application.Json)
                        headers {
                            append(HttpHeaders.ContentType, "application/json;charset=UTF-8")
                        }
                        setBody(LoginRequestBody(phone = phone))
                    }
                val responseBody = response.bodyAsText()
                val doorInfoResponse = Json.decodeFromString<DoorInfoResponse>(responseBody)
                if (doorInfoResponse.code == 200) {
                    saveDoorInfo(context, doorInfoResponse.data)
                    result.complete(doorInfoResponse.data)
                } else {
                    result.complete(null)
                }
            } catch (e: Exception) {
                Log.e("LoginClient", "Error: ${e.message}")
                result.complete(null)
            }
        }
        return result.await()
    }

    private fun saveDoorInfo(context: Context, doorInfoList: List<DoorInfo>) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("door_info", Json.encodeToString(doorInfoList))
            apply()
        }
    }

    suspend fun sendPostRequest(context: Context, id: String, phone: String?): Pair<Int, String> {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val doorInfoJson = sharedPreferences.getString("door_info", null)
        val doorInfoList: List<DoorInfo> = if (doorInfoJson != null) {
            Json.decodeFromString(doorInfoJson)
        } else {
            emptyList()
        }

        val doorInfo = doorInfoList.find { it.equipmentId.toString() == id }
        val doorName = doorInfo?.equipmentName ?: "Unknown Door"

        val result = CompletableDeferred<Pair<Int, String>>()

        Toast.makeText(context, "$doorName 开门中...", Toast.LENGTH_SHORT).show()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: HttpResponse =
                    client.post("https://wxapi1210.grpu.com.cn/thirdParty/extOpenKey") {
                        contentType(ContentType.Application.Json)
                        headers {
                            append(HttpHeaders.ContentType, "application/json;charset=UTF-8")
                        }
                        setBody(RequestBody(phone = phone ?: "", equipmentId = id.toInt()))
                    }

                val responseBody = response.bodyAsText()
                val jsonResponse = Json.decodeFromString<JsonObject>(responseBody)
                val msg = jsonResponse["msg"]?.jsonPrimitive?.content ?: "Unknown error"
                result.complete(Pair(response.status.value, msg))
            } catch (e: Exception) {
                result.complete(Pair(-1, e.message ?: "Unknown error"))
            }
        }
        return result.await()
    }

    @SuppressLint("UnsafeOptInUsageError")
    @Serializable
    data class LoginRequestBody(val phone: String)

    @SuppressLint("UnsafeOptInUsageError")
    @Serializable
    data class RequestBody(val phone: String, val equipmentId: Int)
}
