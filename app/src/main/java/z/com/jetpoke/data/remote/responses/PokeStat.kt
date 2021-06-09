package z.com.jetpoke.data.remote.responses


import com.google.gson.annotations.SerializedName

data class PokeStat(
    @SerializedName("base_stat")
    val baseStat: Int,
    @SerializedName("effort")
    val effort: Int,
    @SerializedName("stat")
    val stat: StatX
)