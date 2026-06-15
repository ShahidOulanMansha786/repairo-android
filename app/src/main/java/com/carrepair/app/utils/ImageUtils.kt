package com.carrepair.app.utils

import com.carrepair.app.data.RetrofitClient

fun resolveImageUrl(url: String?): String? {
    if (url == null) return null
    if (url.startsWith("http")) return url
    
    // Remove leading slash if present to avoid double slashes
    val cleanUrl = if (url.startsWith("/")) url.substring(1) else url
    return "${RetrofitClient.BASE_URL}$cleanUrl"
}
