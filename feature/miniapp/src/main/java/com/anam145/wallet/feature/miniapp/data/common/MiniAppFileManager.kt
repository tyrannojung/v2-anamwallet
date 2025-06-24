package com.anam145.wallet.feature.miniapp.data.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.anam145.wallet.core.common.model.MiniAppManifest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton
import com.anam145.wallet.core.common.data.MiniAppConstants
import com.anam145.wallet.core.common.result.MiniAppResult

@Singleton
class MiniAppFileManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "MiniAppFileManager"
    }
    
    private val json = Json { 
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    suspend fun installAllFromAssets(): MiniAppResult<Unit> = withContext(Dispatchers.IO) {
        try {
            // Android의 AssetManager를 통해 assets 폴더에 접근
            val assetManager = context.assets

            // assets/miniapps 폴더 내의 모든 파일 목록을 가져옴
            // null일 경우 빈 배열 반환
            val zipFiles = assetManager.list(MiniAppConstants.MINIAPP_ASSETS_PATH) ?: emptyArray()
            
            Log.d(TAG, "Found ${zipFiles.size} miniapp zip files in assets")

            // 각 zip 파일에 대해 반복 처리
            zipFiles.forEach { zipFileName ->
                // 파일명이 .zip으로 끝나는 경우에만 처리
                if (zipFileName.endsWith(MiniAppConstants.ZIP_EXTENSION)) {
                    // zip 파일명에서 appId 추출
                    // 예: "bitcoin_v1.0.zip" → "bitcoin"
                    val appId = zipFileName.substringBeforeLast("_")
                    // 해당 앱을 assets에서 설치
                    installFromAssets(appId, zipFileName)
                }
            }

            // 설치 검증: 실제로 파일이 정상적으로 설치되었는지 확인
            val installedApps = getInstalledApps()
            Log.d(TAG, "Verification: Found ${installedApps.size} installed apps after installation")

            // 검증 실패 조건: zip 파일은 있었는데 설치된 앱이 하나도 없는 경우
            if (installedApps.isEmpty() && zipFiles.isNotEmpty()) {
                Log.e(TAG, "Installation verification failed - no apps found after installation")
                // 설치 실패 에러 반환
                return@withContext MiniAppResult.Error.InstallationFailed("verification", Exception("No apps found after installation"))
            }

            // 모든 설치가 성공적으로 완료됨
            MiniAppResult.Success(Unit)
        } catch (e: Exception) {
            // 설치 과정 중 예외 발생 시 에러 로그 출력
            Log.e(TAG, "Failed to install miniapps from assets", e)
            // 설치 실패 에러 반환
            MiniAppResult.Error.InstallationFailed("assets", e)
        }
    }
    
    private suspend fun installFromAssets(appId: String, zipFileName: String) = withContext(Dispatchers.IO) {
        try {
            val miniappsDir = File(context.filesDir, MiniAppConstants.MINIAPP_INSTALL_DIR)
            if (!miniappsDir.exists()) {
                miniappsDir.mkdirs()
            }
            
            val appDir = File(miniappsDir, appId)
            if (appDir.exists()) {
                Log.d(TAG, "MiniApp $appId already installed, skipping")
                return@withContext
            }
            
            appDir.mkdirs()
            
            context.assets.open("${MiniAppConstants.MINIAPP_ASSETS_PATH}/$zipFileName").use { inputStream ->
                ZipInputStream(inputStream).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        val file = File(appDir, entry.name)
                        
                        if (entry.isDirectory) {
                            file.mkdirs()
                            Log.d(TAG, "Created directory: ${file.absolutePath}")
                        } else {
                            file.parentFile?.mkdirs()
                            FileOutputStream(file).use { output ->
                                zip.copyTo(output)
                                output.flush()
                                output.fd.sync() // Force file system sync
                            }
                            Log.d(TAG, "Extracted file: ${file.absolutePath} (${file.length()} bytes)")
                        }
                        
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }
            }
            
            // Verify extraction
            val extractedFiles = appDir.walk().filter { it.isFile }.toList()
            Log.d(TAG, "Successfully installed miniapp: $appId")
            Log.d(TAG, "Extracted ${extractedFiles.size} files:")
            extractedFiles.forEach { file ->
                Log.d(TAG, "  - ${file.relativeTo(appDir).path} (${file.length()} bytes)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to install miniapp: $appId", e)
            throw e
        }
    }
    
    suspend fun getInstalledApps(): List<String> = withContext(Dispatchers.IO) {
        // 미니앱이 설치되는 기본 디렉토리 경로 생성
        // 예: /data/data/com.anam145.wallet/files/miniapps
        val miniappsDir = File(context.filesDir, MiniAppConstants.MINIAPP_INSTALL_DIR)

        // 미니앱 디렉토리가 아직 생성되지 않은 경우 (앱 최초 실행 등)
        if (!miniappsDir.exists()) {
            // 빈 리스트 반환 (설치된 앱이 없음)
            return@withContext emptyList()
        }

        // 디렉토리 내용 스캔 및 필터링
        miniappsDir.listFiles()                    // 모든 파일/디렉토리 목록 가져오기
            ?.filter { it.isDirectory }            // 디렉토리만 필터링 (파일 제외)
            ?.map { it.name }                      // 디렉토리 이름만 추출 (앱 ID)
            ?: emptyList()                         // null인 경우 빈 리스트 반환
    }
    
    suspend fun loadManifest(appId: String): MiniAppResult<MiniAppManifest> = withContext(Dispatchers.IO) {
        try {
            val manifestFile = File(context.filesDir, "${MiniAppConstants.MINIAPP_INSTALL_DIR}/$appId/${MiniAppConstants.MANIFEST_FILE_NAME}")
            if (!manifestFile.exists()) {
                Log.e(TAG, "Manifest file not found for $appId")
                return@withContext MiniAppResult.Error.ManifestNotFound(appId)
            }
            
            val manifestContent = manifestFile.readText()
            
            // anam-android와 동일한 방식으로 수동 파싱
            val jsonObject = org.json.JSONObject(manifestContent)
            val pagesArray = jsonObject.optJSONArray("pages") ?: org.json.JSONArray()
            val pages = mutableListOf<String>()
            for (i in 0 until pagesArray.length()) {
                pages.add(pagesArray.getString(i))
            }
            
            val manifest = MiniAppManifest(
                appId = jsonObject.getString("app_id"),  // manifest.json에는 app_id로 되어 있음
                name = jsonObject.getString("name"),
                version = jsonObject.getString("version"),
                type = jsonObject.getString("type"),
                mainPage = jsonObject.optString("main_page").takeIf { it.isNotEmpty() },
                pages = pages,
                permissions = emptyList()
            )
            
            MiniAppResult.Success(manifest)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load manifest for $appId", e)
            MiniAppResult.Error.InvalidManifest(appId, e)
        }
    }
    
    suspend fun loadAppIcon(appId: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val iconFile = File(context.filesDir, "${MiniAppConstants.MINIAPP_INSTALL_DIR}/$appId${MiniAppConstants.ICON_PATH}")
            if (!iconFile.exists()) {
                Log.d(TAG, "Icon file not found for $appId")
                return@withContext null
            }
            
            BitmapFactory.decodeFile(iconFile.absolutePath)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load icon for $appId", e)
            null
        }
    }
    
    fun getMiniAppBasePath(appId: String): String {
        val appDir = File(context.filesDir, "${MiniAppConstants.MINIAPP_INSTALL_DIR}/$appId")
        return "${appDir.absolutePath}/"  // anam-android와 동일하게 trailing slash 추가
    }
}