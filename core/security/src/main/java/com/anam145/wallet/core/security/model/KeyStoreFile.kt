package com.anam145.wallet.core.security.model

import com.google.gson.annotations.SerializedName

/**
 * 이더리움 키스토어 파일 구조 (V3)
 * Web3 Secret Storage Definition 표준을 따름
 */
data class KeyStoreFile(
    val address: String,
    @SerializedName("crypto", alternate = ["Crypto"])
    val crypto: Crypto,
    val id: String,
    val version: Int
)

data class Crypto(
    val cipher: String,
    val ciphertext: String,
    val cipherparams: CipherParams,
    val kdf: String,
    val kdfparams: KdfParams,
    val mac: String
)

data class CipherParams(
    val iv: String
)

/**
 * KDF (Key Derivation Function) 파라미터 인터페이스
 */
sealed class KdfParams {
    abstract val dklen: Int
    abstract val salt: String
}

/**
 * SCrypt KDF 파라미터
 */
data class ScryptKdfParams(
    override val dklen: Int,
    override val salt: String,
    val n: Int,
    val p: Int,
    val r: Int
) : KdfParams()

/**
 * PBKDF2 KDF 파라미터 (향후 확장용)
 */
data class Pbkdf2KdfParams(
    override val dklen: Int,
    override val salt: String,
    val c: Int,
    val prf: String
) : KdfParams()