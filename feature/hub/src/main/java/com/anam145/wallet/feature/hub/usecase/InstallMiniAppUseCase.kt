package com.anam145.wallet.feature.hub.usecase

import android.content.Context
import android.util.Log
import com.anam145.wallet.core.common.model.MiniApp
import com.anam145.wallet.feature.hub.domain.repository.MiniAppRepository
import com.anam145.wallet.feature.hub.remote.api.HubServerApi
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipInputStream
import javax.inject.Inject
import java.io.File


class InstallMiniAppUseCase @Inject constructor(
    @ApplicationContext context: Context,
    private val miniAppRepository: MiniAppRepository,
    private val hubClient: HubServerApi
)  {
    val _fileOutputDir = File(context.filesDir, "miniapps")

    suspend operator fun invoke(miniApp: MiniApp) {
        try {
            val miniAppDir = File(_fileOutputDir, miniApp.name.removeSuffix(".zip"))
            val zipFile = File(miniAppDir, "${miniApp.name}.zip")

            // 디렉토리 생성
            miniAppDir.mkdirs()

            val response = hubClient.downloadMiniApp(miniApp.appId)

            if (response.isSuccessful) {
                response.body()?.byteStream()?.use { inputStream ->
                    FileOutputStream(zipFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                unzip(zipFile, miniAppDir)

                zipFile.delete()

                miniAppRepository.insertMiniApp(miniApp)
                Log.d(">>>", "MiniApp 다운로드 및 압축 해제 완료: ${miniAppDir.absolutePath}")

            } else {
                Log.e(">>>", "서버 응답 실패: ${response.code()}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(">>>", "MiniApp 다운로드 실패: ${e.message}")
        }
    }

    private fun unzip(zipFile: File, targetDirectory: File) {
        val buffer = ByteArray(1024)
        val zipInputStream = ZipInputStream(FileInputStream(zipFile))

        var zipEntry = zipInputStream.nextEntry
        while (zipEntry != null) {
            val file = File(targetDirectory, zipEntry.name)
            if (zipEntry.isDirectory) {
                file.mkdirs()
            } else {
                file.parentFile?.mkdirs()
                FileOutputStream(file).use { outputStream ->
                    var len: Int
                    while (zipInputStream.read(buffer).also { len = it } > 0) {
                        outputStream.write(buffer, 0, len)
                    }
                }
            }
            zipEntry = zipInputStream.nextEntry
        }

        zipInputStream.closeEntry()
        zipInputStream.close()
    }
}