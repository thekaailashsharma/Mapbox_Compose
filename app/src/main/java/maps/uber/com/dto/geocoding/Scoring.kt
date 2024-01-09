package maps.uber.com.dto.geocoding


import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable
import maps.uber.com.dto.geocoding.FieldScore

@Serializable
data class Scoring(
    @SerializedName("fieldScore")
    val fieldScore: FieldScore?,
    @SerializedName("queryScore")
    val queryScore: Double?
)