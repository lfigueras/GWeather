package com.lovely.gweather.data.location.exceptions

/**
 * A custom exception to clearly signal that GPS is disabled on the device.
 */
class GpsNotEnabledException : Exception("GPS is not enabled on the device.")
