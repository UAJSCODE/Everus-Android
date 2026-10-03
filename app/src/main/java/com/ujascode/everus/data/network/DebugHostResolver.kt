package com.ujascode.everus.data.network

import android.os.Build
import java.util.Locale

object DebugHostResolver {
    /**
     * Returns the appropriate base URL for debug builds based on whether the device is an emulator or physical.
     * <p>
     * Emulator (e.g., Pixel API 33): 10.0.2.2 (Aliases to host's loopback)
     * Physical device (e.g., Xiaomi): 127.0.0.1 (via adb reverse tcp:8080 tcp:8080)
     */
    fun getBaseUrl(): String {
        return if (isEmulator()) "http://10.0.2.2:8080/" else "http://127.0.0.1:8080/"
    }

    private fun isEmulator(): Boolean {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.BRAND.startsWith("generic")
                || Build.DEVICE.startsWith("generic")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
    }
}