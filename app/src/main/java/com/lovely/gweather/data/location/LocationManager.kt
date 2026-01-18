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
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        // If we get a cached location, use it immediately
                        Log.d("LocationManager", "Got location from cache: $location")
                        if (cont.isActive) cont.resume(location)
                    } else {
                        // If cached location is null, we must request the current location
                        Log.d("LocationManager", "Cached location is null. Requesting new location...")
                        val cancellationToken = CancellationTokenSource()

                        fusedLocationProviderClient.getCurrentLocation(
                            Priority.PRIORITY_HIGH_ACCURACY, // Request a fresh, accurate location
                            cancellationToken.token
                        ).addOnSuccessListener { newLocation: Location? ->
                            Log.d("LocationManager", "Got new location: $newLocation")
                            if (cont.isActive) cont.resume(newLocation)
                        }.addOnFailureListener { e ->
                            Log.e("LocationManager", "Failed to get new location", e)
                            if (cont.isActive) cont.resume(null)
                        }

                        // If the coroutine is cancelled, cancel the location request
                        cont.invokeOnCancellation {
                            cancellationToken.cancel()
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("LocationManager", "Failed to get location", e)
                    if (cont.isActive) cont.resume(null)
                }
        }
    }
}
