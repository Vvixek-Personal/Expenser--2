package com.example.data

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PinSecurityUtils {
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16
    private const val CURRENT_VERSION = "v3"
    private const val DEFAULT_ITERATIONS = 20000

    fun hashPin(pin: String, iterations: Int = DEFAULT_ITERATIONS): String {
        if (pin.isBlank()) return ""
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)
        val hash = pbkdf2(pin.toCharArray(), salt, iterations)
        return "$CURRENT_VERSION:$iterations:${encodeBase64(salt)}:${encodeBase64(hash)}"
    }

    fun verifyPin(pin: String, storedHash: String): Boolean {
        if (pin.isBlank() || storedHash.isBlank()) return false
        
        // Handle legacy plaintext
        if (!storedHash.contains(":")) return pin == storedHash
        
        val parts = storedHash.split(":")
        try {
            when (parts.size) {
                2 -> { // Legacy salt:hash
                    val salt = decodeBase64(parts[0])
                    val hash = decodeBase64(parts[1])
                    val testHash = pbkdf2(pin.toCharArray(), salt, 10000)
                    return hash.contentEquals(testHash)
                }
                4 -> { // Modern version:iterations:salt:hash
                    val iterations = parts[1].toInt()
                    val salt = decodeBase64(parts[2])
                    val hash = decodeBase64(parts[3])
                    val testHash = pbkdf2(pin.toCharArray(), salt, iterations)
                    return hash.contentEquals(testHash)
                }
                else -> return false
            }
        } catch (e: Exception) {
            return false
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
