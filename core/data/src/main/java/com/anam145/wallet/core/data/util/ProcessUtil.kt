package com.anam145.wallet.core.data.util

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build

/**
 * 프로세스 관련 유틸리티 클래스
 * 
 * 멀티프로세스 환경에서 현재 프로세스를 식별하는 기능을 제공합니다.
 */
object ProcessUtil {
    
    /**
     * 현재 프로세스의 이름을 반환합니다.
     * 
     * Android P(API 28) 이상에서는 Application.getProcessName()을 사용하고,
     * 그 이하 버전에서는 ActivityManager를 통해 프로세스 이름을 가져옵니다.
     * 
     * @param context Application context
     * @return 현재 프로세스 이름 (예: "com.anam145.wallet", "com.anam145.wallet:blockchain")
     */
    fun currentProcessName(context: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Application.getProcessName()
        } else {
            // Android P 미만에서는 ActivityManager 사용
            val pid = android.os.Process.myPid()
            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            manager.runningAppProcesses?.find { it.pid == pid }?.processName ?: ""
        }
    }
    
    /**
     * 현재 프로세스가 메인 프로세스인지 확인합니다.
     * 
     * @param context Application context
     * @return 메인 프로세스이면 true
     */
    fun isMainProcess(context: Context): Boolean {
        return currentProcessName(context) == context.packageName
    }
    
    /**
     * 현재 프로세스가 특정 이름의 프로세스인지 확인합니다.
     * 
     * @param context Application context
     * @param processNameSuffix 프로세스 이름 접미사 (예: ":blockchain", ":app")
     * @return 해당 프로세스이면 true
     */
    fun isProcess(context: Context, processNameSuffix: String): Boolean {
        val currentProcess = currentProcessName(context)
        return currentProcess.endsWith(processNameSuffix)
    }
    
    /**
     * 현재 프로세스의 타입을 반환합니다.
     */
    sealed class ProcessType {
        object Main : ProcessType()
        object Blockchain : ProcessType()
        object WebApp : ProcessType()
        data class Unknown(val processName: String) : ProcessType()
    }
    
    /**
     * 현재 프로세스의 타입을 식별합니다.
     * 
     * @param context Application context
     * @return 프로세스 타입
     */
    fun getProcessType(context: Context): ProcessType {
        val processName = currentProcessName(context)
        val packageName = context.packageName
        
        return when {
            processName == packageName -> ProcessType.Main
            processName == "$packageName:blockchain" -> ProcessType.Blockchain
            processName == "$packageName:app" -> ProcessType.WebApp
            else -> ProcessType.Unknown(processName)
        }
    }
}