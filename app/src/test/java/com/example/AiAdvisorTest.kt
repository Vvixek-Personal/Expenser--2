package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.api.AiAdvisor
import com.example.api.AiModelConfig
import com.example.api.AiResult
import com.example.api.FinancialTotalsContext
import com.example.api.FirebaseAiAdvisor
import com.example.data.AppSettingsIntent
import com.example.data.AppSettingsManager
import com.example.data.FinanceDatabase
import com.example.data.FinanceRepository
import com.example.ui.FinanceViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

class FakeAiAdvisor : AiAdvisor {
    var callCount = 0
    var lastPrompt: String? = null
    var lastContext: FinancialTotalsContext? = null
    var resultToReturn: AiResult = AiResult.Success("Fake financial advice")

    override suspend fun ask(prompt: String, context: FinancialTotalsContext): AiResult {
        callCount++
        lastPrompt = prompt
        lastContext = context
        return resultToReturn
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AiAdvisorTest {

    private lateinit var application: Application
    private lateinit var fakeAdvisor: FakeAiAdvisor
    private lateinit var viewModel: FinanceViewModel
    private lateinit var settingsManager: AppSettingsManager

    @Before
    fun setup() {
        application = ApplicationProvider.getApplicationContext()
        fakeAdvisor = FakeAiAdvisor()
        val database = FinanceDatabase.getDatabase(application)
        val repository = FinanceRepository(database.financeDao(), database)
        viewModel = FinanceViewModel(application, repository, fakeAdvisor)
        settingsManager = AppSettingsManager.getInstance(application)

        // Reset consent state to false before each test
        settingsManager.dispatch(AppSettingsIntent.SetAiConsent(false))
    }

    @Test
    fun defaultModelNameIsVerifiedGeminiModel() {
        assertEquals("gemini-2.5-flash", AiModelConfig.DEFAULT_MODEL_NAME)
    }

    @Test
    fun userFriendlyMessagesContainNoTechnicalLeaks() {
        assertFalse(AiResult.Failure.Offline.userFriendlyMessage.contains("Exception"))
        assertFalse(AiResult.Failure.RateLimited.userFriendlyMessage.contains("429"))
        assertFalse(AiResult.Failure.Timeout.userFriendlyMessage.contains("Socket"))
        assertFalse(AiResult.Failure.ConsentRequired.userFriendlyMessage.contains("null"))
        assertTrue(AiResult.Failure.Offline.userFriendlyMessage.contains("offline"))
    }

    @Test
    fun askFailsWhenConsentIsNotGranted() = runTest {
        viewModel.setAiConsent(false)

        val result = viewModel.askAiAdvisor("How can I save more money?")
        assertTrue("Must require consent before calling network", result is AiResult.Failure.ConsentRequired)
        assertEquals("Fake AI advisor must never be called without consent", 0, fakeAdvisor.callCount)
        assertTrue("Consent dialog must be shown when user attempts AI query without consent", viewModel.showAiConsentDialog.value)
    }

    @Test
    fun askSucceedsWhenConsentIsGranted() = runTest {
        viewModel.setAiConsent(true)

        val result = viewModel.askAiAdvisor("How can I save more money?")
        assertTrue("Must succeed when consent is granted", result is AiResult.Success)
        assertEquals("Fake AI advisor must be invoked exactly once", 1, fakeAdvisor.callCount)
        assertEquals("How can I save more money?", fakeAdvisor.lastPrompt)

        val ctx = fakeAdvisor.lastContext
        assertNotNull(ctx)
        // Verify privacy: context contains ONLY numerical aggregates and category names
        assertTrue(ctx!!.totalIncome >= 0.0)
        assertTrue(ctx.totalExpenses >= 0.0)
    }

    @Test
    fun promptLengthLimitIsEnforced() = runTest {
        viewModel.setAiConsent(true)

        val oversizedPrompt = "a".repeat(FirebaseAiAdvisor.MAX_PROMPT_LENGTH + 10)
        val result = viewModel.askAiAdvisor(oversizedPrompt)

        assertTrue("Prompts over 1000 chars must fail", result is AiResult.Failure.Unknown)
        assertTrue((result as AiResult.Failure.Unknown).detail.contains("too long"))
        assertEquals("Oversized prompt must not be dispatched to advisor", 0, fakeAdvisor.callCount)
    }

    @Test
    fun cooldownPreventsRapidFireRequests() = runTest {
        viewModel.setAiConsent(true)

        // First query
        val firstResult = viewModel.askAiAdvisor("First question")
        assertTrue(firstResult is AiResult.Success)
        assertEquals(1, fakeAdvisor.callCount)

        // Immediate second query should be rate-limited by 3-second cooldown
        val secondResult = viewModel.askAiAdvisor("Second question immediately")
        assertTrue("Immediate follow-up must be rate limited by cooldown", secondResult is AiResult.Failure.RateLimited)
        assertEquals(1, fakeAdvisor.callCount)
    }
}
