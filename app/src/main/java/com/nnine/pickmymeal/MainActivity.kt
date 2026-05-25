package com.nnine.pickmymeal

import android.Manifest
import android.util.Base64
import android.util.Log
import java.security.MessageDigest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.KakaoMapSdk
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.nnine.pickmymeal.databinding.ActivityMainBinding
import com.nnine.pickmymeal.ui.MainViewModel
import com.nnine.pickmymeal.ui.RestaurantAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var kakaoMap: KakaoMap? = null
    private var currentLat = 0.0
    private var currentLng = 0.0

    private val adapter = RestaurantAdapter { restaurant ->
        val lat = restaurant.latitude.toDoubleOrNull() ?: return@RestaurantAdapter
        val lng = restaurant.longitude.toDoubleOrNull() ?: return@RestaurantAdapter
        moveMapToRestaurant(lat, lng)
        Toast.makeText(this, restaurant.name, Toast.LENGTH_SHORT).show()
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            fetchLocationAndSearch()
        } else {
            Toast.makeText(this, "위치 권한이 필요해요", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        KakaoMapSdk.init(this, "d5a660e81bd9b8f7f27e5a6a50fb912a")
        logKeyHash()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupRecyclerView()
        setupMap()
        setupObservers()
        setupFilterButtons()

        binding.btnSearch.setOnClickListener {
            checkPermissionAndSearch()
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupMap() {
        binding.mapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {}
            override fun onMapError(e: Exception) {
                Log.e("KakaoMap", "지도 오류: ${e.message}")
                Toast.makeText(this@MainActivity, "지도 오류: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(map: KakaoMap) {
                kakaoMap = map
            }
        })
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnSearch.isEnabled = !loading
        }

        viewModel.errorMessage.observe(this) { msg ->
            msg?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        }

        viewModel.restaurants.observe(this) { list ->
            adapter.submitList(list)
        }
    }

    private fun setupFilterButtons() {
        binding.btnCategory.setOnClickListener {
            val categories = arrayOf("무작위", "한식", "중식", "일식", "패스트푸드")
            AlertDialog.Builder(this)
                .setTitle("음식 종류")
                .setItems(categories) { _, which ->
                    viewModel.selectedCategory = categories[which]
                    binding.btnCategory.text = categories[which]
                }
                .show()
        }

        binding.btnRadius.setOnClickListener {
            val labels = arrayOf("300m", "500m", "1km", "2km")
            val values = intArrayOf(300, 500, 1000, 2000)
            AlertDialog.Builder(this)
                .setTitle("검색 반경")
                .setItems(labels) { _, which ->
                    viewModel.selectedRadius = values[which]
                    binding.btnRadius.text = labels[which]
                    if (currentLat != 0.0) drawRadiusCircle(currentLat, currentLng)
                }
                .show()
        }

        binding.btnPrice.setOnClickListener {
            Toast.makeText(this, "가격 필터는 추후 지원 예정이에요", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermissionAndSearch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            fetchLocationAndSearch()
        } else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocationAndSearch() {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    currentLat = location.latitude
                    currentLng = location.longitude
                    moveMapToMyLocation(currentLat, currentLng)
                    drawRadiusCircle(currentLat, currentLng)
                    viewModel.searchRestaurants(currentLat, currentLng)
                } else {
                    Toast.makeText(this, "위치를 가져올 수 없어요. 잠시 후 다시 시도해주세요", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun moveMapToMyLocation(lat: Double, lng: Double) {
        kakaoMap?.let { map ->
            val position = LatLng.from(lat, lng)
            map.moveCamera(CameraUpdateFactory.newCenterPosition(position, 15))
            map.labelManager?.layer?.addLabel(
                LabelOptions.from(position)
                    .setStyles(LabelStyle.from(R.drawable.ic_launcher_foreground))
            )
        }
    }

    private fun moveMapToRestaurant(lat: Double, lng: Double) {
        kakaoMap?.moveCamera(
            CameraUpdateFactory.newCenterPosition(LatLng.from(lat, lng), 16)
        )
    }

    private fun drawRadiusCircle(lat: Double, lng: Double) {
        // 반경 원 표시 - 추후 SDK 버전 확인 후 구현
    }

    @Suppress("DEPRECATION")
    private fun logKeyHash() {
        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures ?: emptyArray()) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hash = Base64.encodeToString(md.digest(), Base64.DEFAULT)
                Log.d("KeyHash", "키 해시: $hash")
            }
        } catch (e: Exception) {
            Log.e("KeyHash", "키 해시 획득 실패: ${e.message}")
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.resume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.finish()
    }
}
