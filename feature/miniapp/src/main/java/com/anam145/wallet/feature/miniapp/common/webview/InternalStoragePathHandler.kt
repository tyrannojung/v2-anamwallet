package com.anam145.wallet.feature.miniapp.common.webview

import android.util.Log
import android.webkit.WebResourceResponse
import androidx.webkit.WebViewAssetLoader
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class InternalStoragePathHandler(
    private val basePath: File
) : WebViewAssetLoader.PathHandler {
    
    companion object {
        private const val TAG = "InternalStoragePathHandler"
    }
    
    override fun handle(path: String): WebResourceResponse? {
        try {
            val cleanPath = path.removePrefix("/")
            val file = File(basePath, cleanPath)
            
            Log.d(TAG, "Handling path: $path -> ${file.absolutePath}")
            
            if (!file.canonicalPath.startsWith(basePath.canonicalPath)) {
                Log.e(TAG, "Attempted to access file outside base path: $path")
                return null
            }
            
            if (!file.exists() || !file.isFile || !file.canRead()) {
                Log.e(TAG, "File not found or not readable: ${file.absolutePath}")
                Log.e(TAG, "File exists: ${file.exists()}, is file: ${file.isFile}, can read: ${file.canRead()}")
                
                // List directory contents for debugging
                if (file.parentFile?.exists() == true) {
                    Log.d(TAG, "Parent directory contents: ${file.parentFile.listFiles()?.map { it.name }?.joinToString(", ")}")
                }
                return null
            }
            
            val mimeType = getMimeType(file.name)
            val inputStream: InputStream = FileInputStream(file)
            
            Log.d(TAG, "Successfully serving file: ${file.absolutePath} with mime type: $mimeType")
            return WebResourceResponse(mimeType, "UTF-8", inputStream)
        } catch (e: Exception) {
            Log.e(TAG, "Error handling path: $path", e)
            return null
        }
    }
    
    private fun getMimeType(fileName: String): String {
        return when (fileName.substringAfterLast('.', "").lowercase()) {
            "html" -> "text/html"
            "js" -> "application/javascript"
            "css" -> "text/css"
            "json" -> "application/json"
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "gif" -> "image/gif"
            "svg" -> "image/svg+xml"
            "woff" -> "font/woff"
            "woff2" -> "font/woff2"
            "ttf" -> "font/ttf"
            "otf" -> "font/otf"
            else -> "application/octet-stream"
        }
    }
}