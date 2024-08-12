package com.uynaity.opendoor

import android.annotation.SuppressLint
import android.content.Context
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

class PostClient {
    suspend fun sendPostRequest(context: Context, door: String, phone: String?): Pair<Int, String> {

        val client = HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }

        val equipmentId = if (door == "east") 15247 else 15248
        val doorName = if (equipmentId == 15247) "东门" else "西门"
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
                        setBody(RequestBody(phone = phone ?: "", equipmentId = equipmentId))
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
    data class RequestBody(val phone: String, val equipmentId: Int)
}
