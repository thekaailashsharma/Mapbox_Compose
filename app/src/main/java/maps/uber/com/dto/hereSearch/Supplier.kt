package maps.uber.com.dto.hereSearch


import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class Supplier(
    @SerializedName("id")
    val id: String?
)