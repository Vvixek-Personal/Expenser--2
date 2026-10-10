package com.example.data

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PinSecurityUtils {
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16
    private const val CURRENT_VERSION = "v3"
    private const val DEFAULT_ITERATIONS = 210000
    private const val KEYSTORE_ALIAS = "financer_pin_keystore_hmac"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"

    sealed class PinVerifyResult {
        object Success : PinVerifyResult()
        object Failed : PinVerifyResult()
        object KeystoreKeyCorrupted : PinVerifyResult()

        val isSuccess: Boolean get() = this is Success
    }

    private sealed class HmacVerificationResult {
        data class Success(val hmac: ByteArray) : HmacVerificationResult()
        object KeyMissingOrInvalid : HmacVerificationResult()
    }

    private fun computeKeystoreHmacForVerification(data: ByteArray): HmacVerificationResult {
        return try {
            val keyStore = java.security.KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)
            if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
                return HmacVerificationResult.KeyMissingOrInvalid
            }
            val secretKey = keyStore.getKey(KEYSTORE_ALIAS, null) as? javax.crypto.SecretKey
                ?: return HmacVerificationResult.KeyMissingOrInvalid
            val mac = javax.crypto.Mac.getInstance("HmacSHA256")
            mac.init(secretKey)
            HmacVerificationResult.Success(mac.doFinal(data))
        } catch (_: Throwable) {
            HmacVerificationResult.KeyMissingOrInvalid
        }
    }

    private fun signWithKeystoreHmac(data: ByteArray): ByteArray? {
        return try {
            val keyStore = java.security.KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)
            if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
                val keyGenerator = javax.crypto.KeyGenerator.getInstance(
                    android.security.keystore.KeyProperties.KEY_ALGORITHM_HMAC_SHA256,
                    ANDROID_KEYSTORE
                )
                val spec = android.security.keystore.KeyGenParameterSpec.Builder(
                    KEYSTORE_ALIAS,
                    android.security.keystore.KeyProperties.PURPOSE_SIGN
                ).build()
                keyGenerator.init(spec)
                keyGenerator.generateKey()
            }
            val secretKey = keyStore.getKey(KEYSTORE_ALIAS, null) as? javax.crypto.SecretKey
            if (secretKey != null) {
                val mac = javax.crypto.Mac.getInstance("HmacSHA256")
                mac.init(secretKey)
                mac.doFinal(data)
            } else {
                null
            }
        } catch (_: Throwable) {
            null
        }
    }

    fun hashPin(pin: String, iterations: Int = DEFAULT_ITERATIONS): String {
        if (pin.isBlank()) return ""
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)
        val pbkdf2Hash = pbkdf2(pin.toCharArray(), salt, iterations)
        val finalHash = signWithKeystoreHmac(pbkdf2Hash) ?: pbkdf2Hash
        return "$CURRENT_VERSION:$iterations:${encodeBase64(salt)}:${encodeBase64(finalHash)}"
    }

    fun verifyPin(pin: String, storedHash: String): PinVerifyResult {
        if (pin.isBlank() || storedHash.isBlank()) return PinVerifyResult.Failed
        
        // Handle legacy plaintext with constant-time MessageDigest comparison
        if (!storedHash.contains(":")) {
            val isMatch = MessageDigest.isEqual(
                pin.toByteArray(Charsets.UTF_8),
                storedHash.toByteArray(Charsets.UTF_8)
            )
            return if (isMatch) PinVerifyResult.Success else PinVerifyResult.Failed
        }
        
        val parts = storedHash.split(":")
        try {
            when (parts.size) {
                2 -> { // Legacy salt:hash
                    val salt = decodeBase64(parts[0])
                    val hash = decodeBase64(parts[1])
                    val testHash = pbkdf2(pin.toCharArray(), salt, 10000)
                    return if (MessageDigest.isEqual(hash, testHash)) {
                        PinVerifyResult.Success
                    } else {
                        PinVerifyResult.Failed
                    }
                }
                4 -> { // Modern version:iterations:salt:hash
                    val version = parts[0]
                    val iterations = parts[1].toInt()
                    val salt = decodeBase64(parts[2])
                    val expectedHash = decodeBase64(parts[3])
                    val testPbkdf2 = pbkdf2(pin.toCharArray(), salt, iterations)

                    if (version == "v3") {
                        when (val hmacResult = computeKeystoreHmacForVerification(testPbkdf2)) {
                            is HmacVerificationResult.Success -> {
                                if (MessageDigest.isEqual(expectedHash, hmacResult.hmac)) {
                                    return PinVerifyResult.Success
                                }
                                if (MessageDigest.isEqual(expectedHash, testPbkdf2)) {
                                    return PinVerifyResult.Success
                                }
                                return PinVerifyResult.Failed
                            }
                            is HmacVerificationResult.KeyMissingOrInvalid -> {
                                if (MessageDigest.isEqual(expectedHash, testPbkdf2)) {
                                    return PinVerifyResult.Success
                                }
                                // Keystore HMAC key is missing or invalid for a v3 hash - distinct result for recovery flow
                                return PinVerifyResult.KeystoreKeyCorrupted
                            }
                        }
                    } else {
                        // Modern v2 or other versions
                        return if (MessageDigest.isEqual(expectedHash, testPbkdf2)) {
                            PinVerifyResult.Success
                        } else {
                            PinVerifyResult.Failed
                        }
                    }
                }
                else -> return PinVerifyResult.Failed
            }
        } catch (_: Exception) {
            return PinVerifyResult.Failed
        }
    }

    fun needsRehash(storedHash: String): Boolean {
        if (!storedHash.contains(":")) return true
        val parts = storedHash.split(":")
        if (parts.size != 4) return true
        if (parts[0] != CURRENT_VERSION) return true
        if (parts[1].toInt() < DEFAULT_ITERATIONS) return true
        return false
    }

    private fun pbkdf2(password: CharArray, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(password, salt, iterations, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        return factory.generateSecret(spec).encoded
    }

    fun encodeBase64(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)
    fun decodeBase64(str: String): ByteArray = Base64.decode(str, Base64.NO_WRAP)

    fun lockoutDelayMs(attempts: Int): Long {
        return when {
            attempts < 3 -> 0L
            attempts == 3 -> 30_000L
            attempts == 4 -> 60_000L
            attempts in 5..7 -> 300_000L
            attempts in 8..9 -> 900_000L
            else -> 3_600_000L
        }
    }

    data class LockoutStatus(
        val isLocked: Boolean,
        val remainingMs: Long,
        val isClockTampered: Boolean = false
    )

    fun calculateLockoutStatus(
        nowWall: Long,
        nowElapsed: Long,
        currentBootCount: Int,
        lockoutUntilWall: Long,
        lockoutUntilElapsed: Long,
        lastAttemptWall: Long,
        savedBootCount: Int,
        lastLockoutDuration: Long
    ): LockoutStatus {
        if (lockoutUntilWall == 0L && lockoutUntilElapsed == 0L) {
            return LockoutStatus(false, 0L)
        }

        // Clock tampering check
        if (nowWall < lastAttemptWall) {
            return LockoutStatus(true, lastLockoutDuration, isClockTampered = true)
        }

        val wallRemaining = (lockoutUntilWall - nowWall).coerceAtLeast(0L)
        
        // If boot count changed, ignore elapsed time (prevents perpetual lockout if boot count resets)
        if (currentBootCount != savedBootCount) {
            return LockoutStatus(wallRemaining > 0, wallRemaining)
        }

        val elapsedRemaining = (lockoutUntilElapsed - nowElapsed).coerceAtLeast(0L)
        val actualRemaining = maxOf(wallRemaining, elapsedRemaining)
        
        return LockoutStatus(actualRemaining > 0, actualRemaining)
    }

    fun shouldLock(
        durationMillis: Long,
        backgroundedAtElapsed: Long,
        nowElapsed: Long,
        hasPin: Boolean,
        wasProcessRestarted: Boolean
    ): Boolean {
        if (!hasPin) return false
        if (durationMillis < 0) return false // NEVER
        if (wasProcessRestarted) return true
        
        val elapsedInBackground = nowElapsed - backgroundedAtElapsed
        return elapsedInBackground >= durationMillis
    }
}
