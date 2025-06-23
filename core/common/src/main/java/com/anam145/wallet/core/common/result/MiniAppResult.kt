package com.anam145.wallet.core.common.result

/**
 * MiniApp 도메인 전용 Result 타입
 * 
 * Kotlin의 기본 Result<T> 대신 사용하여 타입 안전한 에러 처리를 제공합니다.
 * 
 * @param T 성공 시 반환되는 데이터의 타입
 */
sealed interface MiniAppResult<out T> {
    
    /**
     * 성공 케이스
     * 
     * @property data 성공적으로 반환된 데이터
     */
    data class Success<T>(val data: T) : MiniAppResult<T>
    
    /**
     * 에러 케이스들
     * 
     * MiniApp 도메인에서 발생할 수 있는 모든 에러를 정의합니다.
     */
    sealed interface Error : MiniAppResult<Nothing> {
        
        /**
         * 설치 실패
         * 
         * @property appId 설치 실패한 앱의 ID
         * @property cause 실패 원인
         */
        data class InstallationFailed(
            val appId: String, 
            val cause: Throwable
        ) : Error
        
        /**
         * 매니페스트 파일을 찾을 수 없음
         * 
         * @property appId 매니페스트를 찾을 수 없는 앱의 ID
         */
        data class ManifestNotFound(val appId: String) : Error
        
        /**
         * 매니페스트 파일이 유효하지 않음
         * 
         * @property appId 유효하지 않은 매니페스트를 가진 앱의 ID
         * @property cause 파싱 실패 원인
         */
        data class InvalidManifest(
            val appId: String, 
            val cause: Throwable
        ) : Error
        
        /**
         * 설치된 앱이 없음
         */
        data object NoAppsInstalled : Error
        
        /**
         * 앱 스캔 실패
         * 
         * @property cause 스캔 실패 원인
         */
        data class ScanFailed(val cause: Throwable) : Error
        
        /**
         * 앱을 찾을 수 없음
         * 
         * @property appId 찾을 수 없는 앱의 ID
         */
        data class AppNotFound(val appId: String) : Error
        
        /**
         * 알 수 없는 에러
         * 
         * @property cause 에러 원인
         */
        data class Unknown(val cause: Throwable) : Error
    }
}

/**
 * 성공 케이스에 대한 처리
 * 
 * @param action 성공 시 실행할 액션
 * @return 원본 MiniAppResult (체이닝을 위해)
 */
fun <T> MiniAppResult<T>.onSuccess(
    action: (value: T) -> Unit
): MiniAppResult<T> {
    if (this is MiniAppResult.Success) {
        action(data)
    }
    return this
}

/**
 * 에러 케이스에 대한 처리
 * 
 * @param action 에러 시 실행할 액션
 * @return 원본 MiniAppResult (체이닝을 위해)
 */
fun <T> MiniAppResult<T>.onError(
    action: (error: MiniAppResult.Error) -> Unit
): MiniAppResult<T> {
    if (this is MiniAppResult.Error) {
        action(this)
    }
    return this
}

/**
 * 성공 값을 변환
 * 
 * @param transform 변환 함수
 * @return 변환된 MiniAppResult
 */
fun <T, R> MiniAppResult<T>.map(
    transform: (value: T) -> R
): MiniAppResult<R> = when (this) {
    is MiniAppResult.Success -> MiniAppResult.Success(transform(data))
    is MiniAppResult.Error -> this
}

/**
 * 성공 값을 다른 MiniAppResult로 변환 (flatMap)
 * 
 * @param transform 변환 함수
 * @return 변환된 MiniAppResult
 */
fun <T, R> MiniAppResult<T>.flatMap(
    transform: (value: T) -> MiniAppResult<R>
): MiniAppResult<R> = when (this) {
    is MiniAppResult.Success -> transform(data)
    is MiniAppResult.Error -> this
}

/**
 * 에러를 다른 에러로 변환
 * 
 * @param transform 변환 함수
 * @return 변환된 MiniAppResult
 */
fun <T> MiniAppResult<T>.mapError(
    transform: (error: MiniAppResult.Error) -> MiniAppResult.Error
): MiniAppResult<T> = when (this) {
    is MiniAppResult.Success -> this
    is MiniAppResult.Error -> transform(this)
}

/**
 * MiniAppResult를 Kotlin의 Result로 변환
 * 
 * @return Kotlin Result
 */
fun <T> MiniAppResult<T>.toKotlinResult(): Result<T> = when (this) {
    is MiniAppResult.Success -> Result.success(data)
    is MiniAppResult.Error -> Result.failure(
        MiniAppException(this)
    )
}

/**
 * Kotlin의 Result를 MiniAppResult로 변환
 * 
 * @param errorMapper 에러 매핑 함수
 * @return MiniAppResult
 */
fun <T> Result<T>.toMiniAppResult(
    errorMapper: (Throwable) -> MiniAppResult.Error = { MiniAppResult.Error.Unknown(it) }
): MiniAppResult<T> = fold(
    onSuccess = { MiniAppResult.Success(it) },
    onFailure = { errorMapper(it) }
)

/**
 * MiniAppResult.Error를 Exception으로 래핑
 * 
 * Kotlin Result와의 호환성을 위해 사용
 */
class MiniAppException(
    val error: MiniAppResult.Error
) : Exception(error.toString())

/**
 * 성공 값을 가져오거나 null 반환
 */
fun <T> MiniAppResult<T>.getOrNull(): T? = when (this) {
    is MiniAppResult.Success -> data
    is MiniAppResult.Error -> null
}

/**
 * 성공 값을 가져오거나 기본값 반환
 */
fun <T> MiniAppResult<T>.getOrDefault(default: T): T = when (this) {
    is MiniAppResult.Success -> data
    is MiniAppResult.Error -> default
}

/**
 * 성공 값을 가져오거나 예외 던지기
 */
fun <T> MiniAppResult<T>.getOrThrow(): T = when (this) {
    is MiniAppResult.Success -> data
    is MiniAppResult.Error -> throw MiniAppException(this)
}