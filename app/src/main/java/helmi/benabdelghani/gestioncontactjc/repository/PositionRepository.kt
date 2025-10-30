package helmi.benabdelghani.gestioncontactjc.repository

import android.util.Log
import helmi.benabdelghani.gestioncontactjc.api.ServerApi
import helmi.benabdelghani.gestioncontactjc.data.model.Position
import org.json.JSONArray
import org.json.JSONObject

object PositionRepository {
    suspend fun fetchPositions(userId:Int): List<Position> {
        val result = mutableListOf<Position>()
        return try {
            val json = ServerApi.fetchPositions(userId)
            val root = JSONObject(json)
            if (root.optInt("success", 0) == 1) {
                val arr: JSONArray = root.optJSONArray("positions") ?: JSONArray()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val id = o.optString("idposition")
                    val pseudo = o.optString("pseudo")
                    val numero = o.optString("numero")
                    val lon = o.optString("longitude").toDoubleOrNull() ?: 0.0
                    val lat = o.optString("latitude").toDoubleOrNull() ?: 0.0
                    val usId=o.optInt("userId",0)
                    result.add(Position(id, usId, pseudo, numero, lon, lat))
                }
            }
            result
        } catch (e: Exception) {
            Log.e("PositionRepository", "fetch/parse error", e)
            emptyList()
        }
    }

    suspend fun deletePosition(idposition: String): Boolean {
        return try {
            val response = ServerApi.deletePosition(idposition)
            val root = JSONObject(response)
            root.optInt("success", 0) == 1
        } catch (e: Exception) {
            Log.e("PositionRepository", "delete error", e)
            false
        }
    }

    suspend fun addPosition(pseudo: String,userId: Int, numero: String, longitude: Double, latitude: Double): Boolean {
        return try {
            val response = ServerApi.addPosition(pseudo,userId, numero, longitude, latitude)
            val root = JSONObject(response)
            root.optInt("success", 0) == 1
        } catch (e: Exception) {
            Log.e("PositionRepository", "add error", e)
            false
        }
    }
}