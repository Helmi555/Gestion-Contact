package com.example.gestioncontactjc.repository

import android.util.Log
import com.example.gestioncontactjc.api.ServerApi
import com.example.gestioncontactjc.data.model.Position
import org.json.JSONArray
import org.json.JSONObject

object PositionRepository {
    suspend fun fetchPositions(): List<Position> {
        val result = mutableListOf<Position>()
        return try {
            val json = ServerApi.fetchPositions() // uses existing ServerApi
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
                    result.add(Position(id, pseudo, numero, lon, lat))
                }
            }
            result
        } catch (e: Exception) {
            Log.e("PositionRepository", "fetch/parse error", e)
            emptyList()
        }
    }
}