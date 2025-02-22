package com.example.task

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.task.databinding.ActivityDetailsScreenBinding
import com.example.task.databinding.ActivityMainBinding
import java.util.Locale

class DetailsScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailsScreenBinding

    var emailId: String = ""
    var cityName: String = ""
    var stateName: String = ""
    var countryName: String = ""
    var profile_img: String = ""
    var first_name: String = ""
    var last_name: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve data from intent
        emailId = intent.getStringExtra("email_id") ?: ""
        cityName = intent.getStringExtra("city_name") ?: ""
        stateName = intent.getStringExtra("state_name") ?:""
        countryName = intent.getStringExtra("country_name") ?:""
        profile_img = intent.getStringExtra("profile_img") ?:""
        first_name = intent.getStringExtra("first_name") ?:""
        last_name = intent.getStringExtra("last_name") ?:""

        binding.nameTxt.text = first_name +" "+last_name
        binding.emailTxt.text = emailId
        binding.cityTxt.text = cityName
        binding.stateTxt.text = stateName
        binding.countryTxt.text = countryName

        Glide.with(this)
            .load(profile_img)
            .into(binding.profileImage)

        val countryCode = getCountryCode(countryName)
        getweathersecondaryApi(countryCode!!)
    }


    fun getCountryCode(countryName: String): String? {
        val locales = Locale.getAvailableLocales()
        for (locale in locales) {
            if (locale.displayCountry.equals(countryName, ignoreCase = true)) {
                return locale.country
            }
        }
        return null
    }

    fun getweathersecondaryApi(countrycode : String){
        val apiClient = ApiClient()
        apiClient.getWeathersecondary(cityName, countrycode,
            onSuccess = { weather ->
                var load_weather_image = "https://openweathermap.org/img/wn/"+weather.weather[0].icon+"@2x.png"
                Glide.with(this)
                    .load(load_weather_image)
                    .into(binding.weatherIcon)
                binding.tempTxt.text = "${weather.main.temp}°C ${weather.name}"
                binding.weatherTypeTxt.text = weather.weather[0].description
            },
            onFailure = { error ->
                Log.e("DetailsScreenActivity", error)
            }
        )

    }
}