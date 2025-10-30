package helmi.benabdelghani.gestioncontactjc.util

object MessageFormat {

    // Message format:
    private const val REQ_PREFIX = "LOCREQ:"
    private const val RESP_PREFIX = "LOCRESP:"

    fun locationRequest(humanText: String = "Please share your location"): String =
        "$REQ_PREFIX $humanText"

    fun locationResponse(lat: Double, lon: Double): String =
        "$RESP_PREFIX$lat,$lon"

    fun isLocationRequest(body: String?): Boolean =
        body?.trim()?.startsWith(REQ_PREFIX, ignoreCase = true) == true

    fun isLocationResponse(body: String?): Boolean =
        body?.trim()?.startsWith(RESP_PREFIX, ignoreCase = true) == true

    fun parseLocationResponse(body: String?): Pair<Double, Double>? {
        if (!isLocationResponse(body)) return null
        val payload = body!!.substringAfter(RESP_PREFIX).trim()
        val parts = payload.split(",")
        if (parts.size < 2) return null
        val lat = parts[0].toDoubleOrNull() ?: return null
        val lon = parts[1].toDoubleOrNull() ?: return null
        return Pair(lat, lon)
    }
}
