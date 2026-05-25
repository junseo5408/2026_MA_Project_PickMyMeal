package com.nnine.pickmymeal.data

import com.nnine.pickmymeal.data.model.Restaurant
import com.nnine.pickmymeal.data.network.RetrofitClient

class RestaurantRepository {

    private val api = RetrofitClient.api
    private val apiKey = "KakaoAK bc6dcb74df1aabed59dbf5fbc45b90e1"

    suspend fun getNearbyRestaurants(
        latitude: Double,
        longitude: Double,
        radius: Int = 500,
        category: String = "무작위"
    ): List<Restaurant> {
        return if (category == "무작위") {
            api.searchByCategory(
                apiKey = apiKey,
                latitude = latitude,
                longitude = longitude,
                radius = radius
            ).documents
        } else {
            api.searchByKeyword(
                apiKey = apiKey,
                keyword = category,
                latitude = latitude,
                longitude = longitude,
                radius = radius
            ).documents
        }
    }
}
