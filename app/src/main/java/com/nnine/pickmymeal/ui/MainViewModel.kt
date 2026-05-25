package com.nnine.pickmymeal.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nnine.pickmymeal.data.RestaurantRepository
import com.nnine.pickmymeal.data.model.Restaurant
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val repository = RestaurantRepository()

    private val _restaurants = MutableLiveData<List<Restaurant>>()
    val restaurants: LiveData<List<Restaurant>> = _restaurants

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    var selectedCategory: String = "무작위"
    var selectedRadius: Int = 500

    fun searchRestaurants(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val list = repository.getNearbyRestaurants(
                    latitude = latitude,
                    longitude = longitude,
                    radius = selectedRadius,
                    category = selectedCategory
                )
                if (list.isEmpty()) {
                    _errorMessage.value = "주변에 음식점이 없어요"
                }
                _restaurants.value = list
            } catch (e: Exception) {
                _errorMessage.value = "검색 실패: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
