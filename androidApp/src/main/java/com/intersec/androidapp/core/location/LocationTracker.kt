package com.intersec.androidapp.core.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.intersec.androidapp.core.storage.SecuritySettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Gerencia o rastreamento de localização em tempo real e persistência exata.
 */
class LocationTracker(
    private val context: Context,
    private val securitySettings: SecuritySettingsManager
) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation.let { location ->
                _currentLocation.value = location
                persistLocation(location)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startTracking() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000
            fastestInterval = 5000
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun stopTracking() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun persistLocation(location: Location) {
        CoroutineScope(Dispatchers.IO).launch {
            securitySettings.saveLocation(location.latitude, location.longitude)
        }
    }
}
