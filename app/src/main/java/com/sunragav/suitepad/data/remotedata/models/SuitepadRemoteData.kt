package com.sunragav.suitepad.data.remotedata.models

import com.google.gson.annotations.SerializedName


data class SuitepadRemoteData(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Int,
    @SerializedName("type") val type: String
)



