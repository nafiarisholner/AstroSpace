package com.astro.space.apps.kshnzk

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.BatteryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Locale
import java.util.concurrent.TimeUnit

class ServerManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_data", Context.MODE_PRIVATE)
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
    
    private val TOKEN_KEY = "server_token"
    private val CONTENT_LINK_KEY = "content_link"
    
    fun getStoredToken(): String? {
        return prefs.getString(TOKEN_KEY, null)
    }
    
    fun hasStoredToken(): Boolean {
        return getStoredToken() != null
    }
    
    fun getStoredContentLink(): String? {
        return prefs.getString(CONTENT_LINK_KEY, null)
    }
    
    private fun getDeviceInfo(): Map<String, String> {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val batteryStatus = batteryManager?.let {
            val status = it.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
            when (status) {
                BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
                BatteryManager.BATTERY_STATUS_FULL -> "Full"
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "NotCharging"
                else -> "Unknown"
            }
        } ?: "Unknown"
        
        val batteryScale = batteryManager?.let {
            val level = it.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            val scale = level / 100f
            when {
                scale >= 1f -> "1"
                scale <= 0f -> "0"
                else -> {
                    val scaleStr = scale.toString()
                    scaleStr.trimEnd('0').trimEnd('.')
                }
            }
        } ?: "0"
        
        val androidVersion = Build.VERSION.RELEASE.split(".").firstOrNull() ?: Build.VERSION.RELEASE
        
        return mapOf(
            "os" to "Android $androidVersion",
            "lng" to Locale.getDefault().language,
            "loc" to Locale.getDefault().country,
            "devicemodel" to "${Build.MANUFACTURER} ${Build.MODEL}",
            "bs" to batteryStatus,
            "bl" to batteryScale
        )
    }
    
    suspend fun fetchServerData(): Pair<String?, String?> {
        return withContext(Dispatchers.IO) {
            try {
                val deviceInfo = getDeviceInfo()
                val baseAddress = "https://wallen-eatery.space/a-vdm-16/server.php"
                val addressBuilder = StringBuilder(baseAddress)
                    .append("?p=Jh675eYuunk85")
                    .append("&os=").append(java.net.URLEncoder.encode(deviceInfo["os"], "UTF-8"))
                    .append("&lng=").append(java.net.URLEncoder.encode(deviceInfo["lng"], "UTF-8"))
                    .append("&loc=").append(java.net.URLEncoder.encode(deviceInfo["loc"], "UTF-8"))
                    .append("&devicemodel=").append(java.net.URLEncoder.encode(deviceInfo["devicemodel"], "UTF-8"))
                    .append("&bs=").append(java.net.URLEncoder.encode(deviceInfo["bs"], "UTF-8"))
                    .append("&bl=").append(java.net.URLEncoder.encode(deviceInfo["bl"], "UTF-8"))

                val address = addressBuilder.toString()
                
                val request = Request.Builder()
                    .url(address)
                    .build()
                
                val response = client.newCall(request).execute()
                
                if (!response.isSuccessful) {
                    android.util.Log.e("ServerManager", "Response not successful: ${response.code}")
                    return@withContext Pair(null, null)
                }
                
                val responseBody = response.body?.string() ?: ""
                android.util.Log.d("ServerManager", "Response body: $responseBody")
                
                if (responseBody.contains("#")) {
                    val parts = responseBody.split("#", limit = 2)
                    val token = parts[0]
                    val contentLink = if (parts.size > 1) parts[1] else null
                    
                    prefs.edit()
                        .putString(TOKEN_KEY, token)
                        .putString(CONTENT_LINK_KEY, contentLink)
                        .apply()
                    
                    Pair(token, contentLink)
                } else {
                    android.util.Log.w("ServerManager", "Response does not contain # separator")
                    Pair(null, null)
                }
            } catch (e: Exception) {
                android.util.Log.e("ServerManager", "Error fetching server data", e)
                Pair(null, null)
            }
        }
    }
}

