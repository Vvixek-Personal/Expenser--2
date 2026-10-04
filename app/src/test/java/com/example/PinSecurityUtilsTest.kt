package com.example

import com.example.data.PinSecurityUtils
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PinSecurityUtilsTest {

    @Test
    fun samePinHashesDifferentlyDueToRandomSalt() {
        val pin = "1234"
        val hash1 = PinSecurityUtils.hashPin(pin, iterations = 1000)
        val hash2 = PinSecurityUtils.hashPin(pin, iterations = 1000)

        assertNotEquals("Each hash should have a unique random salt", hash1, hash2)
        assertTrue("Hash format should start with v2: or v3:", hash1.startsWith("v2:") || hash1.startsWith("v3:"))
        assertTrue("Hash format should start with v2: or v3:", hash2.startsWith("v2:") || hash2.startsWith("v3:"))
    }

    @Test
    fun correctPinPassesVerification() {
        val pin = "5821"
        val storedHash = PinSecurityUtils.hashPin(pin, iterations = 2000)

        val isValid = PinSecurityUtils.verifyPin(pin, storedHash)
        assertTrue("Correct PIN must verify successfully", isValid)
    }

    @Test
    fun wrongPinFailsVerification() {
        val pin = "5821"
        val wrongPin = "0000"
        val storedHash = PinSecurityUtils.hashPin(pin, iterations = 2000)

        val isValid = PinSecurityUtils.verifyPin(wrongPin, storedHash)
        assertFalse("Wrong PIN must fail verification", isValid)
    }

    @Test
    fun blankOrEmptyPinFails() {
        val storedHash = PinSecurityUtils.hashPin("1234", iterations = 1000)
        assertFalse("Blank PIN should fail", PinSecurityUtils.verifyPin("", storedHash))
        assertFalse("Whitespace PIN should fail", PinSecurityUtils.verifyPin("   ", storedHash))
        assertFalse("Blank stored hash should fail", PinSecurityUtils.verifyPin("1234", ""))
    }

    @Test
    fun malformedStoredStringsDoNotCrashAndReturnFalse() {
        assertFalse(PinSecurityUtils.verifyPin("1234", "invalid_format_no_colons_mismatch"))
        assertFalse(PinSecurityUtils.verifyPin("1234", "v2:notanumber:salt:hash"))
        assertFalse(PinSecurityUtils.verifyPin("1234", "v3:notanumber:salt:hash"))
        assertFalse(PinSecurityUtils.verifyPin("1234", "v2:1000:badbase64:::"))
        assertFalse(PinSecurityUtils.verifyPin("1234", "::::"))
        assertFalse(PinSecurityUtils.verifyPin("1234", "v2:1000"))
        assertFalse(PinSecurityUtils.verifyPin("1234", "part1:part2:part3")) // 3 parts, invalid
    }

    @Test
    fun legacyPlaintextPinVerifiesAndNeedsRehash() {
        val plaintextPin = "9876"
        assertTrue("Plaintext exact match should verify", PinSecurityUtils.verifyPin("9876", plaintextPin))
        assertFalse("Plaintext mismatch should fail", PinSecurityUtils.verifyPin("1234", plaintextPin))
        assertTrue("Plaintext format needs rehash", PinSecurityUtils.needsRehash(plaintextPin))
    }

    @Test
    fun legacyV1HashVerifiesAndFlagsNeedsRehash() {
        // Construct a legacy 2-part hash (salt:hash) with 10,000 iterations
        val pin = "4321"
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        val spec = PBEKeySpec(pin.toCharArray(), salt, 10000, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val rawHash = factory.generateSecret(spec).encoded

        val legacyStored = "${PinSecurityUtils.encodeBase64(salt)}:${PinSecurityUtils.encodeBase64(rawHash)}"

        assertTrue("Legacy v1 salt:hash format must verify", PinSecurityUtils.verifyPin("4321", legacyStored))
        assertFalse("Wrong PIN against legacy format must fail", PinSecurityUtils.verifyPin("9999", legacyStored))
        assertTrue("Legacy hash must require rehash to modern standard", PinSecurityUtils.needsRehash(legacyStored))
    }

    @Test
    fun modernV2HashWithLowerIterationsNeedsRehash() {
        val lowerIterHash = "v2:10000:mockSalt:mockHash"
        assertTrue("v2 with lower iterations should require rehash", PinSecurityUtils.needsRehash(lowerIterHash))
    }

    @Test
    fun lockoutDelayMsMatchesEscalatingTiers() {
        assertEquals(0L, PinSecurityUtils.lockoutDelayMs(0))
        assertEquals(0L, PinSecurityUtils.lockoutDelayMs(1))
        assertEquals(0L, PinSecurityUtils.lockoutDelayMs(2))
        assertEquals(30_000L, PinSecurityUtils.lockoutDelayMs(3))
        assertEquals(60_000L, PinSecurityUtils.lockoutDelayMs(4))
        assertEquals(300_000L, PinSecurityUtils.lockoutDelayMs(5))
        assertEquals(300_000L, PinSecurityUtils.lockoutDelayMs(7))
        assertEquals(900_000L, PinSecurityUtils.lockoutDelayMs(8))
        assertEquals(900_000L, PinSecurityUtils.lockoutDelayMs(9))
        assertEquals(3_600_000L, PinSecurityUtils.lockoutDelayMs(10))
        assertEquals(3_600_000L, PinSecurityUtils.lockoutDelayMs(25))
    }

    @Test
    fun calculateLockoutStatus_whenNotLocked() {
        val status = PinSecurityUtils.calculateLockoutStatus(
            nowWall = 1_000_000L,
            nowElapsed = 50_000L,
            currentBootCount = 5,
            lockoutUntilWall = 0L,
            lockoutUntilElapsed = 0L,
            lastAttemptWall = 990_000L,
            savedBootCount = 5,
            lastLockoutDuration = 30_000L
        )
        assertFalse("Should not be locked", status.isLocked)
        assertEquals(0L, status.remainingMs)
        assertFalse(status.isClockTampered)
    }

    @Test
    fun calculateLockoutStatus_whenActivelyLocked() {
        val status = PinSecurityUtils.calculateLockoutStatus(
            nowWall = 1_000_000L,
            nowElapsed = 50_000L,
            currentBootCount = 5,
            lockoutUntilWall = 1_030_000L,
            lockoutUntilElapsed = 80_000L,
            lastAttemptWall = 1_000_000L,
            savedBootCount = 5,
            lastLockoutDuration = 30_000L
        )
        assertTrue("Should be locked", status.isLocked)
        assertEquals(30_000L, status.remainingMs)
        assertFalse(status.isClockTampered)
    }

    @Test
    fun calculateLockoutStatus_rebootScenario_prevents100HourLockout() {
        // Phone rebooted (boot count 5 -> 6), elapsed time reset to 1_000ms.
        // Wall clock deadline has expired (1_030_000 < 1_040_000).
        // Saved elapsed deadline is 360_000_000ms (100 hours from previous boot).
        val status = PinSecurityUtils.calculateLockoutStatus(
            nowWall = 1_040_000L,
            nowElapsed = 1_000L,
            currentBootCount = 6,
            lockoutUntilWall = 1_030_000L,
            lockoutUntilElapsed = 360_000_000L,
            lastAttemptWall = 1_000_000L,
            savedBootCount = 5, // Boot count mismatch!
            lastLockoutDuration = 30_000L
        )
        // Because boot count changed, elapsed time comparison is skipped and wall-time deadline has expired.
        assertFalse("Reboot after wall-time passed must NOT keep user locked for 100 hours", status.isLocked)
        assertEquals(0L, status.remainingMs)
    }

    @Test
    fun calculateLockoutStatus_clockRollbackScenario_grantsSinglePenaltyPeriod() {
        // Clock set backwards from 1_050_000 to 1_000_000 (nowWall < lastAttemptWall)
        val status = PinSecurityUtils.calculateLockoutStatus(
            nowWall = 1_000_000L,
            nowElapsed = 60_000L,
            currentBootCount = 5,
            lockoutUntilWall = 1_080_000L,
            lockoutUntilElapsed = 90_000L,
            lastAttemptWall = 1_050_000L, // clock jumped backwards!
            savedBootCount = 5,
            lastLockoutDuration = 60_000L
        )
        assertTrue("Clock rollback should be detected as locked", status.isLocked)
        assertTrue("Should be flagged as clock tampered", status.isClockTampered)
        assertEquals("Should grant exactly one lockout period penalty (60,000ms)", 60_000L, status.remainingMs)
    }

    @Test
    fun appSettingsManager_hashesPinOnSetPin() {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        val prefs = context.getSharedPreferences("app_settings_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        com.example.data.AppSettingsManager.resetInstanceForTesting()

        val manager = com.example.data.AppSettingsManager.getInstance(context)
        manager.dispatch(com.example.data.AppSettingsIntent.SetPin("4821"))

        val storedPinInPrefs = prefs.getString("app_pin", null)
        assertNotNull("Stored PIN in prefs must not be null", storedPinInPrefs)
        assertNotEquals("Stored PIN in prefs must NOT be plaintext '4821'", "4821", storedPinInPrefs)
        assertTrue("Stored PIN must start with v2: or v3:", storedPinInPrefs!!.startsWith("v2:") || storedPinInPrefs.startsWith("v3:"))
        assertTrue("Stored PIN must verify successfully with '4821'", PinSecurityUtils.verifyPin("4821", storedPinInPrefs))
        assertFalse("Stored PIN must fail verification with wrong PIN", PinSecurityUtils.verifyPin("0000", storedPinInPrefs))
    }

    @Test
    fun appSettingsManager_upgradesLegacyPlaintextPinOnInitialization() {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        val prefs = context.getSharedPreferences("app_settings_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putString("app_pin", "7392").commit()
        com.example.data.AppSettingsManager.resetInstanceForTesting()

        val manager = com.example.data.AppSettingsManager.getInstance(context)
        val statePin = manager.state.value.appPin
        val prefsPin = prefs.getString("app_pin", null)

        assertNotNull("State appPin should not be null", statePin)
        assertNotEquals("Legacy plaintext '7392' in SharedPreferences must be upgraded", "7392", prefsPin)
        assertTrue("Upgraded PIN in SharedPreferences must start with v2: or v3:", prefsPin!!.startsWith("v2:") || prefsPin.startsWith("v3:"))
        assertEquals("State appPin and prefsPin should match", prefsPin, statePin)
        assertTrue("Upgraded PIN must verify successfully with '7392'", PinSecurityUtils.verifyPin("7392", prefsPin))
    }
}
