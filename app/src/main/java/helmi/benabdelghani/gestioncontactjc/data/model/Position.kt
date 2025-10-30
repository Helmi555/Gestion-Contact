package helmi.benabdelghani.gestioncontactjc.data.model

data class Position(
    val idposition: String,
    val userId:Int,
    val pseudo: String,
    val numero: String,
    val longitude: Double,
    val latitude: Double
)