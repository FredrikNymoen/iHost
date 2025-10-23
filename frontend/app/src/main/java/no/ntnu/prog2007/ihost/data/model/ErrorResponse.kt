package no.ntnu.prog2007.ihost.data.model

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("error")
    val error: String,

    @SerializedName("message")
    val message: String
)
