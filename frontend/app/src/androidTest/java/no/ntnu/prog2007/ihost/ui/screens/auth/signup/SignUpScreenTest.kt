package no.ntnu.prog2007.ihost.ui.screens.auth.signup

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import no.ntnu.prog2007.ihost.viewmodel.AuthUiState
import no.ntnu.prog2007.ihost.viewmodel.AuthViewModel
import no.ntnu.prog2007.ihost.viewmodel.RegistrationState
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for SignUpScreen
 *
 * AAA pattern: Arrange, Act, Assert
 *
 * Tests sign-up form validation including:
 * - Email and password field validation
 * - Password matching validation
 * - Button enable/disable states
 * - Loading states
 */
@RunWith(AndroidJUnit4::class)
class SignUpScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockViewModel: AuthViewModel
    private lateinit var fakeUiState: MutableStateFlow<AuthUiState>
    private lateinit var fakeRegistrationState: MutableStateFlow<RegistrationState>
    private var navigateBackCalled = false
    private var navigateToPersonalInfoCalled = false

    @Before
    fun setup() {
        // Arrange - Initialize fake states and mock ViewModel
        fakeUiState = MutableStateFlow(AuthUiState())
        fakeRegistrationState = MutableStateFlow(RegistrationState())
        mockViewModel = mockk(relaxed = true)

        // Mock the state flows to return our fake flows
        every { mockViewModel.uiState } returns fakeUiState
        every { mockViewModel.registrationState } returns fakeRegistrationState

        navigateBackCalled = false
        navigateToPersonalInfoCalled = false
    }

    @Test
    fun signUpScreen_initialState_nextButtonIsDisabled() {
        // Arrange - Set up SignUpScreen with empty fields
        composeTestRule.setContent {
            SignUpScreen(
                viewModel = mockViewModel,
                onNavigateBack = { navigateBackCalled = true },
                onNavigateToPersonalInfo = { navigateToPersonalInfoCalled = true }
            )
        }

        // Assert - Next button should be disabled when all fields are empty
        composeTestRule
            .onNodeWithTag("signUpNextButton")
            .assertIsNotEnabled()
    }

    @Test
    fun signUpScreen_enterEmail_emailFieldShowsText() {
        // Arrange
        composeTestRule.setContent {
            SignUpScreen(
                viewModel = mockViewModel,
                onNavigateBack = { navigateBackCalled = true },
                onNavigateToPersonalInfo = { navigateToPersonalInfoCalled = true }
            )
        }

        // Act - Enter text in email field
        val testEmail = "test@example.com"
        composeTestRule
            .onNodeWithTag("signUpEmailField")
            .performTextInput(testEmail)

        // Assert - Verify ViewModel's updateRegistrationField was called
        verify(atLeast = 1) {
            mockViewModel.updateRegistrationField("email", any())
        }
    }

    @Test
    fun signUpScreen_enterPassword_passwordFieldShowsText() {
        // Arrange
        composeTestRule.setContent {
            SignUpScreen(
                viewModel = mockViewModel,
                onNavigateBack = { navigateBackCalled = true },
                onNavigateToPersonalInfo = { navigateToPersonalInfoCalled = true }
            )
        }

        // Act - Enter text in password field
        composeTestRule
            .onNodeWithTag("signUpPasswordField")
            .performTextInput("password123")

        // Assert - Verify ViewModel's updateRegistrationField was called
        verify(atLeast = 1) {
            mockViewModel.updateRegistrationField("password", any())
        }
    }

    @Test
    fun signUpScreen_passwordsDontMatch_showsErrorAndButtonDisabled() {
        // Arrange - Set up with mismatched passwords in state
        fakeRegistrationState.value = RegistrationState(
            email = "test@example.com",
            password = "password123"
        )

        composeTestRule.setContent {
            SignUpScreen(
                viewModel = mockViewModel,
                onNavigateBack = { navigateBackCalled = true },
                onNavigateToPersonalInfo = { navigateToPersonalInfoCalled = true }
            )
        }

        // Act - Enter different password in confirm field
        composeTestRule
            .onNodeWithTag("signUpConfirmPasswordField")
            .performTextInput("differentPassword")

        composeTestRule.waitForIdle()

        // Assert - Error message should be displayed
        composeTestRule
            .onNodeWithText("Passwords do not match")
            .assertIsDisplayed()

        // Assert - Next button should be disabled
        composeTestRule
            .onNodeWithTag("signUpNextButton")
            .assertIsNotEnabled()
    }

    @Test
    fun signUpScreen_allFieldsValidAndMatch_nextButtonIsEnabled() {
        var password: String = "password123"
        // Arrange - Set up with valid matching passwords
        fakeRegistrationState.value = RegistrationState(
            email = "test@example.com",
            password = password
        )
        // Ensure not loading state
        fakeUiState.value = AuthUiState(isLoading = false)

        composeTestRule.setContent {
            SignUpScreen(
                viewModel = mockViewModel,
                onNavigateBack = { navigateBackCalled = true },
                onNavigateToPersonalInfo = { navigateToPersonalInfoCalled = true }
            )
        }

        // Act - Enter matching password in confirm field
        composeTestRule
            .onNodeWithTag("signUpConfirmPasswordField")
            .performTextClearance() // Clear the LaunchedEffect auto-filled value if any
        composeTestRule
            .onNodeWithTag("signUpConfirmPasswordField")
            .performTextInput(password) // Use the same password

        composeTestRule.waitForIdle()

        // Assert - Next button should be enabled
        composeTestRule
            .onNodeWithTag("signUpNextButton")
            .assertIsEnabled()
    }

    @Test
    fun signUpScreen_emptyEmail_nextButtonDisabled() {
        // Arrange - Set up with password but empty email
        fakeRegistrationState.value = RegistrationState(
            email = "",
            password = "password123"
        )

        composeTestRule.setContent {
            SignUpScreen(
                viewModel = mockViewModel,
                onNavigateBack = { navigateBackCalled = true },
                onNavigateToPersonalInfo = { navigateToPersonalInfoCalled = true }
            )
        }

        // Act - Enter matching password in confirm field
        composeTestRule
            .onNodeWithTag("signUpConfirmPasswordField")
            .performTextInput("password123")

        composeTestRule.waitForIdle()

        // Assert - Next button should be disabled (email is empty)
        composeTestRule
            .onNodeWithTag("signUpNextButton")
            .assertIsNotEnabled()
    }

    @Test
    fun signUpScreen_emptyPassword_nextButtonDisabled() {
        // Arrange - Set up with email but empty password
        fakeRegistrationState.value = RegistrationState(
            email = "test@example.com",
            password = ""
        )

        composeTestRule.setContent {
            SignUpScreen(
                viewModel = mockViewModel,
                onNavigateBack = { navigateBackCalled = true },
                onNavigateToPersonalInfo = { navigateToPersonalInfoCalled = true }
            )
        }

        composeTestRule.waitForIdle()

        // Assert - Next button should be disabled (password is empty)
        composeTestRule
            .onNodeWithTag("signUpNextButton")
            .assertIsNotEnabled()
    }

    @Test
    fun signUpScreen_fieldsDisabledWhileLoading() {
        // Arrange - Set loading state to true
        fakeUiState.value = AuthUiState(isLoading = true)

        composeTestRule.setContent {
            SignUpScreen(
                viewModel = mockViewModel,
                onNavigateBack = { navigateBackCalled = true },
                onNavigateToPersonalInfo = { navigateToPersonalInfoCalled = true }
            )
        }

        // Assert - All input fields should be disabled while loading
        composeTestRule
            .onNodeWithTag("signUpEmailField")
            .assertIsNotEnabled()

        composeTestRule
            .onNodeWithTag("signUpPasswordField")
            .assertIsNotEnabled()

        composeTestRule
            .onNodeWithTag("signUpConfirmPasswordField")
            .assertIsNotEnabled()
    }

    @Test
    fun signUpScreen_clickReturnButton_navigatesBack() {
        // Arrange
        composeTestRule.setContent {
            SignUpScreen(
                viewModel = mockViewModel,
                onNavigateBack = { navigateBackCalled = true },
                onNavigateToPersonalInfo = { navigateToPersonalInfoCalled = true }
            )
        }

        // Act - Click the Return button
        composeTestRule
            .onNodeWithTag("signUpReturnButton")
            .performClick()

        // Assert - Verify navigation callback was called
        assert(navigateBackCalled) { "onNavigateBack should have been called" }
        assert(!navigateToPersonalInfoCalled) { "onNavigateToPersonalInfo should not have been called" }
    }

    @Test
    fun signUpScreen_passwordMismatchError_hidesWhenPasswordsMatch() {
        // Arrange - Set up with email and password
        fakeRegistrationState.value = RegistrationState(
            email = "test@example.com",
            password = "password123"
        )

        composeTestRule.setContent {
            SignUpScreen(
                viewModel = mockViewModel,
                onNavigateBack = { navigateBackCalled = true },
                onNavigateToPersonalInfo = { navigateToPersonalInfoCalled = true }
            )
        }

        // Act - First enter mismatched password
        composeTestRule
            .onNodeWithTag("signUpConfirmPasswordField")
            .performTextInput("wrong")

        composeTestRule.waitForIdle()

        // Assert - Error should be visible
        composeTestRule
            .onNodeWithText("Passwords do not match")
            .assertIsDisplayed()

        // Act - Clear and enter matching password
        composeTestRule
            .onNodeWithTag("signUpConfirmPasswordField")
            .performTextClearance()

        composeTestRule
            .onNodeWithTag("signUpConfirmPasswordField")
            .performTextInput("password123")

        composeTestRule.waitForIdle()

        // Assert - Error should be hidden
        composeTestRule
            .onNodeWithText("Passwords do not match")
            .assertDoesNotExist()
    }
}
