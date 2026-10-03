package com.lovely.gweather.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log // Import Log for debugging
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.lovely.gweather.data.location.exceptions.GpsNotEnabledException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationManager(
    private val context: Context,
    private val fusedLocationProviderClient: FusedLocationProviderClient
) {

    @SuppressLint("MissingPermission")
    suspend fun getLocation(): Location? {
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val androidLocationManager = context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
        val isGpsEnabled = androidLocationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER) ||
                androidLocationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)

        // Guard clauses to ensure we can fetch a location
        if (!isGpsEnabled) {
            Log.w("LocationManager", "GPS is not enabled. Throwing GpsNotEnabledException.")
            // Instead of returning null, we throw an exception that the ViewModel can catch.
            throw GpsNotEnabledException()
        }
        if (!hasFineLocationPermission && !hasCoarseLocationPermission) {
            Log.e("LocationManager", "Location permissions are not granted.")
            return null
        }


        return suspendCancellableCoroutine { cont ->
            val cancellationToken = CancellationTokenSource()
            val priority = if (hasFineLocationPermission) {
                Priority.PRIORITY_HIGH_ACCURACY
            } else {
                Priority.PRIORITY_BALANCED_POWER_ACCURACY
            }

            fun useCachedLocation() {
                fusedLocationProviderClient.lastLocation
                    .addOnSuccessListener { cachedLocation ->
                        Log.d("LocationManager", "Using cached location as fallback: $cachedLocation")
                        if (cont.isActive) cont.resume(cachedLocation)
                    }
                    .addOnFailureListener { exception ->
                        Log.e("LocationManager", "Failed to get cached location", exception)
                        if (cont.isActive) cont.resume(null)
                    }
            }

            fusedLocationProviderClient.getCurrentLocation(priority, cancellationToken.token)
                .addOnSuccessListener { currentLocation ->
                    if (currentLocation != null) {
                        Log.d("LocationManager", "Got current location: $currentLocation")
                        if (cont.isActive) cont.resume(currentLocation)
                    } else {
                        useCachedLocation()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("LocationManager", "Fresh location unavailable; using cached location", exception)
                    useCachedLocation()
                }

            cont.invokeOnCancellation { cancellationToken.cancel() }
        }
    }
}
