package com.example.gestioncontactjc.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

object ServerApi {
    private val client = OkHttpClient()
    private const val BASE_URL = "http://10.0.2.2/servicephp"

    suspend fun postPosition(pseudo: String, numero: String, longitude: Double, latitude: Double): String =
        withContext(Dispatchers.IO) {
            val body: RequestBody = FormBody.Builder()
                .add("pseudo", pseudo)
                .add("numero", numero)
                .add("longitude", longitude.toString())
                .add("latitude", latitude.toString())
                .build()
            val req = Request.Builder()
                .url("$BASE_URL/add_position.php")
                .post(body)
                .build()
            client.newCall(req).execute().use { resp ->
                resp.body?.string() ?: "{\"success\":0,\"message\":\"empty response\"}"
            }
        }

    suspend fun fetchPositions(): String = withContext(Dispatchers.IO) {
        val req = Request.Builder()
            .url("$BASE_URL/get_all.php")
            .get()
            .build()
        client.newCall(req).execute().use { resp ->
            resp.body?.string() ?: "{\"success\":0,\"message\":\"empty response\"}"
        }
    }
}

// Example usage from an Activity/Fragment (for testing in Messages screen)
// lifecycleScope.launch {
//     val postResult = ServerApi.postPosition("alice", "12345", 1.23, 4.56)
//     Log.d("ServerTest", "POST result: $postResult")
//     val listResult = ServerApi.fetchPositions()
//     Log.d("ServerTest", "LIST result: $listResult")
// }