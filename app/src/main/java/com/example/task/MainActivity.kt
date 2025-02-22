package com.example.task

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.example.task.database.UserTable
import com.example.task.databinding.ActivityMainBinding
import com.example.task.viewmodel.UserViewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: UserViewModel by viewModels()
    private lateinit var adapter: UserAdapter
    private var totalResults = 25 // Start with 25 results
    private val pageSize = 25
    private var isLoading = false
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private lateinit var networkReceiver: NetworkChangeReceiver
    private var isUserSearching = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up RecyclerView with StaggeredGridLayoutManager
        val staggeredGridLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.recyclerView.layoutManager = staggeredGridLayoutManager


        // Initialize Adapter with empty list
        adapter = UserAdapter(mutableListOf())
        binding.recyclerView.adapter = adapter
        binding.progressBar.visibility = View.VISIBLE


        // Initialize and register the receiver
        networkReceiver = NetworkChangeReceiver { isConnected ->
            runOnUiThread {
                if (isConnected) {
                    binding.networkTxt.visibility = View.GONE  // "Internet Available"
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.tempeatureLay.visibility = View.VISIBLE
                    binding.weatherIcon.visibility = View.VISIBLE
                    loadData()
                } else {
                    binding.networkTxt.visibility = View.VISIBLE  // "No Internet"
                    binding.recyclerView.visibility = View.GONE
                    binding.tempeatureLay.visibility = View.GONE
                    binding.weatherIcon.visibility = View.GONE
                }
            }
        }

        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkReceiver, intentFilter)


        // Add scroll listener for pagination
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)


                if (!isLoading && !isUserSearching) { // Prevent pagination during search
                    val layoutManager = recyclerView.layoutManager as StaggeredGridLayoutManager
                    val lastVisibleItemPositions = layoutManager.findLastVisibleItemPositions(null)
                    val lastVisibleItemPosition = lastVisibleItemPositions.maxOrNull() ?: 0

                    if (lastVisibleItemPosition >= adapter.itemCount - 5) { // Load more when 5 items remain
                        showLoadMoreProgress(true)
                        totalResults += pageSize // Increase limit
                        loadData()
                    }
                }
            }
        })


        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                isUserSearching = !charSequence.isNullOrEmpty()
                charSequence?.let {
                    viewModel.searchUsers(it.toString()) // Call the search function when text changes
                }

                binding.cancelIcon.visibility = if (charSequence.isNullOrEmpty()) View.GONE else View.VISIBLE
            }

            override fun afterTextChanged(editable: Editable) {}
        })

        binding.cancelIcon.setOnClickListener {
            binding.searchEditText.setText("")
        }


        /*binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    viewModel.searchUsers(it)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    viewModel.searchUsers(it)
                }
                return true
            }
        })*/

        viewModel.users.observe(this) { users ->
            adapter.setUsers(users)
            Log.d("TAG", "onCreate_users: "+users)
        }


        // Initialize the launcher
        locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getCurrentLocation()
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show()
            }
        }

        checkLocationPermission() //for Location Permission
    }

    private fun loadData() {
        isLoading = true
        val apiClient = ApiClient()

        apiClient.getUsers(totalResults) { users ->
            binding.progressBar.visibility = View.GONE
            showLoadMoreProgress(false)

            val userTables = users.map {
                UserTable(
                    firstName = it.name.first ?: "",
                    lastName = it.name.last ?: "",
                    imageUrl = it.picture.large ?: "",
                    email = it.email ?: "",
                    country = it.location.country ?: "",
                    state = it.location.state ?: "",
                    city = it.location.city ?: ""
                )
            }

            viewModel.insertUsers(userTables) // Store in Room
            viewModel.getPaginatedUsers(totalResults) // Fetch paginated data
            Log.d("TAG", "Users saved: $userTables")

            isLoading = false
        }
    }


    private fun showLoadMoreProgress(isLoading: Boolean) {
        binding.progressBarLoadMore.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        } else {
            locationPermissionLauncher.launch(ACCESS_FINE_LOCATION)
        }
    }

    private fun getCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        // Create a location request
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(2000)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location: Location? = locationResult.lastLocation
                location?.let {
                    latitude = it.latitude
                    longitude = it.longitude
                    getweatherApi()
                    Log.d("LocationLat&lng", "Lat: $latitude, Lng: $longitude")

                    // Stop updates after getting location
                    fusedLocationClient.removeLocationUpdates(this)
                } ?: Log.e("Location", "Failed to get location")
            }
        }

        // Request location updates
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    fun getweatherApi(){
        val apiClient = ApiClient()
        apiClient.getWeather(latitude, longitude,
            onSuccess = { weather ->
                var load_weather_image = "https://openweathermap.org/img/wn/"+weather.weather[0].icon+"@2x.png"
                Glide.with(this)
                    .load(load_weather_image)
                    .into(binding.weatherIcon)
                binding.tempTxt.text = "${weather.main.temp}°C ${weather.name}"
                binding.weatherTypeTxt.text = weather.weather[0].description
            },
            onFailure = { error ->
                Log.e("MainActivity", error)
                Toast.makeText(this@MainActivity , "Poor Network, After change the good network then only you get Weather details" , Toast.LENGTH_LONG).show()
            }
        )

    }

}
