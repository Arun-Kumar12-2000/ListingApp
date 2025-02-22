package com.example.task

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.task.databinding.ActivitySplashBinding
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationSettingsRequest: LocationSettingsRequest
    private lateinit var settingsClient: SettingsClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize GPS location request
        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 10000 // 10-second interval
        ).build()

        locationSettingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true) // Always show GPS enable popup
            .build()

        settingsClient = LocationServices.getSettingsClient(this)

        // Check GPS and prompt user
        checkGPSAndProceed()
    }

    private fun checkGPSAndProceed() {
        settingsClient.checkLocationSettings(locationSettingsRequest)
            .addOnSuccessListener {
                // GPS is already enabled, proceed to MainActivity
                navigateToMainActivity()
            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        // Show popup to enable GPS
                        gpsEnableLauncher.launch(
                            IntentSenderRequest.Builder(exception.resolution).build()
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {
                        sendEx.printStackTrace()
                    }
                } else {
                    // If GPS is not available at all, still move to MainActivity
                    navigateToMainActivity()
                }
            }
    }

    private val gpsEnableLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            navigateToMainActivity()
        } else {
            Toast.makeText(this, "GPS is Must required!..Because, Weathers working based on Location.", Toast.LENGTH_LONG).show()
            checkGPSAndProceed()
        }

    }

    private fun navigateToMainActivity() {
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2000) // Delay for 2 seconds before moving to MainActivity
    }
}
