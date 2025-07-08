package com.anam145.wallet.feature.hub.usecase

import android.content.Context
import com.anam145.wallet.core.common.model.MiniApp
import com.anam145.wallet.feature.hub.domain.repository.MiniAppRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import java.io.File

class UninstallMiniAppUseCase @Inject constructor(
    @ApplicationContext context: Context,
    private val miniAppRepository: MiniAppRepository,
) {
    private val _fileOutputDir = File(context.filesDir, "miniapps")

    suspend operator fun invoke(miniApp: MiniApp) {
        // DB에서 삭제
        miniAppRepository.deleteMiniApp(miniApp)

        // 디렉토리 삭제
        val miniAppDir = File(_fileOutputDir, miniApp.name.removeSuffix(".zip"))
        deleteRecursively(miniAppDir)
    }

    private fun deleteRecursively(fileOrDir: File) {
        if (fileOrDir.exists()) {
            if (fileOrDir.isDirectory) {
                fileOrDir.listFiles()?.forEach { child ->
                    deleteRecursively(child)
                }
            }
            fileOrDir.delete()
        }
    }
}