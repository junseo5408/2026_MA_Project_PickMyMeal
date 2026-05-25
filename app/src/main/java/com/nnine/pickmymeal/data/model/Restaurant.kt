package com.nnine.pickmymeal.data.model

import com.google.gson.annotations.SerializedName

data class KakaoSearchResponse(
    val documents: List<Restaurant>,
    val meta: Meta
)

data class Restaurant(
    @SerializedName("place_name") val name: String,
    @SerializedName("address_name") val address: String,
    @SerializedName("road_address_name") val roadAddress: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("distance") val distance: String,
    @SerializedName("place_url") val placeUrl: String,
    @SerializedName("x") val longitude: String,
    @SerializedName("y") val latitude: String,
    @SerializedName("category_name") val category: String
)

data class Meta(
    @SerializedName("total_count") val totalCount: Int,
    @SerializedName("is_end") val isEnd: Boolean
)
