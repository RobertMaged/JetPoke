package z.com.jetpoke.data.remote.responses


import com.google.gson.annotations.SerializedName

data class PokeType(
    @SerializedName("slot")
    val slot: Int,
    @SerializedName("type")
    val type: TypeX
)