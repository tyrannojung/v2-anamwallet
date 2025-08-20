package com.anam145.wallet.feature.miniapp.common.data.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.anam145.wallet.core.common.model.MiniAppManifest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
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
                    // 예: "com.anam.bitcoin.zip" → "com.anam.bitcoin"
                    val appId = zipFileName.removeSuffix(MiniAppConstants.ZIP_EXTENSION)
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
            
            // Bridge 설정 파싱 (선택적)
            val bridgeConfig = jsonObject.optJSONObject("bridge")?.let { bridgeJson ->
                val scriptPath = bridgeJson.optString("script")
                if (scriptPath.isNotEmpty()) {
                    com.anam145.wallet.core.common.model.BridgeConfig(script = scriptPath)
                } else null
            }
            
            val manifest = MiniAppManifest(
                appId = jsonObject.getString("app_id"),  // manifest.json에는 app_id로 되어 있음
                name = jsonObject.getString("name"),
                version = jsonObject.getString("version"),
                type = jsonObject.getString("type"),
                mainPage = jsonObject.optString("main_page").takeIf { it.isNotEmpty() },
                pages = pages,
                permissions = emptyList(),
                bridge = bridgeConfig
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
    
    /**
     * 브릿지 스크립트 파일 로드
     * @param appId 미니앱 ID
     * @param scriptPath 스크립트 파일 경로 (manifest.json의 bridge.script 값)
     * @return 스크립트 내용 또는 에러
     */
    suspend fun loadBridgeScript(appId: String, scriptPath: String): MiniAppResult<String> = withContext(Dispatchers.IO) {
        try {
            val scriptFile = File(context.filesDir, "${MiniAppConstants.MINIAPP_INSTALL_DIR}/$appId/$scriptPath")
            if (!scriptFile.exists()) {
                Log.e(TAG, "Bridge script file not found: $scriptPath for $appId")
                return@withContext MiniAppResult.Error.FileNotFound("$appId/$scriptPath")
            }
            
            val scriptContent = scriptFile.readText()
            Log.d(TAG, "Bridge script loaded successfully for $appId: ${scriptContent.length} bytes")
            MiniAppResult.Success(scriptContent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load bridge script for $appId", e)
            MiniAppResult.Error.FileLoadFailed("$appId/$scriptPath", e)
        }
    }
    
    fun getMiniAppBasePath(appId: String): String {
        val appDir = File(context.filesDir, "${MiniAppConstants.MINIAPP_INSTALL_DIR}/$appId")
        return "${appDir.absolutePath}/"  // anam-android와 동일하게 trailing slash 추가
    }
    
    /**
     * 미니앱 제거
     * @param appId 제거할 앱 ID
     * @return 제거 결과
     */
    suspend fun uninstallMiniApp(appId: String): MiniAppResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val appDir = File(context.filesDir, "${MiniAppConstants.MINIAPP_INSTALL_DIR}/$appId")
            
            if (!appDir.exists()) {
                Log.w(TAG, "App directory not found for uninstall: $appId")
                return@withContext MiniAppResult.Error.MiniAppNotFound(appId)
            }
            
            // 재귀적으로 디렉토리 삭제
            val deleted = appDir.deleteRecursively()
            
            if (deleted) {
                Log.d(TAG, "Successfully uninstalled miniapp: $appId")
                return@withContext MiniAppResult.Success(Unit)
            } else {
                Log.e(TAG, "Failed to delete app directory: $appId")
                return@withContext MiniAppResult.Error.UninstallFailed(appId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error uninstalling miniapp: $appId", e)
            return@withContext MiniAppResult.Error.UnknownError(e.message ?: "Uninstall failed")
        }
    }
    
    /**
     * InputStream으로부터 미니앱 설치
     * @param appId 설치할 앱 ID
     * @param inputStream ZIP 파일 스트림
     * @return 설치 결과
     */
    suspend fun installFromInputStream(
        appId: String, 
        inputStream: InputStream
    ): MiniAppResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val appDir = File(context.filesDir, "${MiniAppConstants.MINIAPP_INSTALL_DIR}/$appId")
            appDir.mkdirs()
            
            // 임시 ZIP 파일 생성
            val tempZip = File(context.cacheDir, "$appId.zip")
            inputStream.use { input ->
                FileOutputStream(tempZip).use { output ->
                    input.copyTo(output)
                }
            }
            
            // ZIP 압축 해제
            ZipInputStream(FileInputStream(tempZip)).use { zip ->
                var entry = zip.nextEntry
                
                while (entry != null) {
                    // Mac 관련 파일 필터링
                    if (entry.name.startsWith("__MACOSX/") || entry.name.contains("/.DS_Store") || entry.name == ".DS_Store") {
                        Log.d(TAG, "Skipping Mac file: ${entry.name}")
                        zip.closeEntry()
                        entry = zip.nextEntry
                        continue
                    }
                    
                    val file = File(appDir, entry.name)
                    
                    if (entry.isDirectory) {
                        file.mkdirs()
                    } else {
                        file.parentFile?.mkdirs()
                        FileOutputStream(file).use { output ->
                            zip.copyTo(output)
                            output.fd.sync()
                        }
                    }
                    
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
            
            
            // 임시 파일 삭제
            tempZip.delete()
            
            Log.d(TAG, "Successfully installed miniapp from stream: $appId")
            return@withContext MiniAppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to install from stream: $appId", e)
            return@withContext MiniAppResult.Error.InstallationFailed(appId, e)
        }
    }
}