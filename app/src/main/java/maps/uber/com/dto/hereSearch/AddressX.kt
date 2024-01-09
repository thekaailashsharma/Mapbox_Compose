package maps.uber.com.dto.hereSearch


import ai.travel.app.dto.hereSearch.Label
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class AddressX(
    @SerializedName("label")
    val label: List<Label?>?
)