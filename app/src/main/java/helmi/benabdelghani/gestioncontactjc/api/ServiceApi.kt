package helmi.benabdelghani.gestioncontactjc.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

object ServerApi {
        private val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()

       // private const val BASE_URL = "http://10.0.2.2/servicephp"
        private const val BASE_URL = "http://192.168.59.22/servicephp"

        private fun errorJson(msg: String) = "{\"success\":0,\"message\":\"$msg\"}"

        suspend fun addPosition(pseudo: String,userId: Int, numero: String, longitude: Double, latitude: Double): String =
            withContext(Dispatchers.IO) {
                val body: RequestBody = FormBody.Builder()
                    .add("pseudo", pseudo)
                    .add("numero", numero)
                    .add("longitude", longitude.toString())
                    .add("latitude", latitude.toString())
                    .add("userId", userId.toString())
                    .build()
                val req = Request.Builder()
                    .url("$BASE_URL/add_position.php")
                    .post(body)
                    .build()

                try {
                    client.newCall(req).execute().use { resp ->
                        resp.body?.string() ?: errorJson("empty response")
                    }
                } catch (e: SocketTimeoutException) {
                    Log.e("ServerApi", "timeout", e)
                    errorJson("timeout")
                } catch (e: UnknownHostException) {
                    Log.e("ServerApi", "no network", e)
                    errorJson("no_network")
                } catch (e: IOException) {
                    Log.e("ServerApi", "io error", e)
                    errorJson("io_error")
                }
            }

        suspend fun fetchPositions(userId:Int): String = withContext(Dispatchers.IO) {
            val req = Request.Builder()
                .url("$BASE_URL/get_all.php/?userId=$userId")
                .get()
                .build()

            try {
                client.newCall(req).execute().use { resp ->
                    resp.body?.string() ?: errorJson("empty response")
                }
            } catch (e: SocketTimeoutException) {
                Log.e("ServerApi", "timeout", e)
                errorJson("timeout")
            } catch (e: UnknownHostException) {
                Log.e("ServerApi", "no network", e)
                errorJson("no_network")
            } catch (e: IOException) {
                Log.e("ServerApi", "io error", e)
                errorJson("io_error")
            }
        }

    suspend fun deletePosition(idposition: String): String = withContext(Dispatchers.IO) {
        val body: RequestBody = FormBody.Builder()
            .add("idposition", idposition)
            .build()
        val req = Request.Builder()
            .url("$BASE_URL/delete_position.php")
            .post(body)
            .build()

        try {
            client.newCall(req).execute().use { resp ->
                resp.body?.string() ?: errorJson("empty response")
            }
        } catch (e: SocketTimeoutException) {
            Log.e("ServerApi", "timeout", e)
            errorJson("timeout")
        } catch (e: UnknownHostException) {
            Log.e("ServerApi", "no network", e)
            errorJson("no_network")
        } catch (e: IOException) {
            Log.e("ServerApi", "io error", e)
            errorJson("io_error")
        }
    }

}