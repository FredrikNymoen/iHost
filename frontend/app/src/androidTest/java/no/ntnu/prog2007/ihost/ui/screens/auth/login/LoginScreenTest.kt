package no.ntnu.prog2007.ihost.ui.screens.auth.login

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import no.ntnu.prog2007.ihost.viewmodel.AuthUiState
import no.ntnu.prog2007.ihost.viewmodel.AuthViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for LoginScreen
 *
 * AAA pattern: Arrange, Act, Assert
 *
 * These are UI tests that run on Android device/emulator
 * Testing user interactions and UI behavior
 */
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockViewModel: AuthViewModel
    private lateinit var fakeUiState: MutableStateFlow<AuthUiState>
    private var navigateBackCalled = false
    private var navigateToForgotPasswordCalled = false

    @Before
    fun setup() {
        // Arrange - Initialize fake UI state and mock ViewModel
        fakeUiState = MutableStateFlow(AuthUiState())
        mockViewModel = mockk(relaxed = true)
        
        // Mock the uiState to return our fake StateFlow
        every { mockViewModel.uiState } returns fakeUiState
        
        navigateBackCalled = false
        navigateToForgotPasswordCalled = false
    }

    @Test
    fun loginScreen_initialState_loginButtonIsDisabled() {
        // Arrange - Set up the LoginScreen
        composeTestRule.setContent {
            LoginScreen(
                viewModel = mockViewModel,
                onNavigateBack = { navigateBackCalled = true },
                onNavigateToForgotPassword = { navigateToForgotPasswordCalled = true }
            )
        }

        // Assert - Login button should be disabled when fields are empty
        composeTestRule
            .onNodeWithTag("log_in_button")
            .assertIsNotEnabled()
    }

    @Test
    fun loginScreen_enterEmail_emailFieldShowsText() {
        // Arrange
        composeTestRule.setContent {
            LoginScreen(
                viewModel = mockViewModel,
                onNavigateBack = { navigateBackCalled = true },
                onNavigateToForgotPassword = { navigateToForgotPasswordCalled = true }
            )
        }

        // Act - Enter text in email field
        composeTestRule
            .onNodeWithTag("emailField")
            .performTextInput("test@example.com")

        // Assert - Email field should display the entered text
        composeTestRule
            .onNodeWithTag("emailField")
            .assertTextContains("test@example.com")
    }

    @Test
    fun loginScreen_enterPassword_passwordFieldShowsText() {
        // Arrange
        composeTestRule.setContent {
            LoginScreen(
                viewModel = mockViewModel,
                onNavigateBack = { navigateBackCalled = true },
                onNavigateToForgotPassword = { navigateToForgotPasswordCalled = true }
            )
        }

        // Act - Enter text in password field
        composeTestRule
            .onNodeWithTag("passwordField")
            .performTextInput("password123")

        // Assert - Password field should display the entered text (as dots)
        composeTestRule
            .onNodeWithTag("passwordField")
            .assertTextContains("password123")
    }

    @Test
    fun loginScreen_enterBothFields_loginButtonIsEnabled() {
        // Arrange
        composeTestRule.setContent {
            LoginScreen(
                viewModel = mockViewModel,
                onNavigateBack = { navigateBackCalled = true },
                onNavigateToForgotPassword = { navigateToForgotPasswordCalled = true }
            )
        }

        // Act - Enter valid email and password
        composeTestRule
            .onNodeWithTag("emailField")
            .performTextInput("test@example.com")

        composeTestRule
            .onNodeWithTag("passwordField")
            .performTextInput("password123")

        // Assert - Login button should be enabled
        composeTestRule
            .onNodeWithTag("log_in_button")
            .assertIsEnabled()
    }

    @Test
    fun loginScreen_clickLoginButton_callsViewModelSignIn() {
        // Arrange
        composeTestRule.setContent {
            LoginScreen(
                viewModel = mockViewModel,
                onNavigateBack = { navigateBackCalled = true },
                onNavigateToForgotPassword = { navigateToForgotPasswordCalled = true }
            )
        }

        val testEmail = "test@example.com"
        val testPassword = "password123"

        // Act - Enter credentials and click login
        composeTestRule
            .onNodeWithTag("emailField")
            .performTextInput(testEmail)

        composeTestRule
            .onNodeWithTag("passwordField")
            .performTextInput(testPassword)

        composeTestRule
            .onNodeWithTag("log_in_button")
            .performClick()

        // Assert - Verify ViewModel's signIn method was called with correct parameters
        verify(exactly = 1) { mockViewModel.signIn(testEmail, testPassword) }
    }

    @Test
    fun loginScreen_clickReturnButton_navigatesBack() {
        // Arrange
        composeTestRule.setContent {
            LoginScreen(
                viewModel = mockViewModel,
                onNavigateBack = { navigateBackCalled = true },
                onNavigateToForgotPassword = { navigateToForgotPasswordCalled = true }
            )
        }

        // Act - Click the Return button (SecondaryButton with text "Return")
        composeTestRule
            .onNodeWithText("Return")
            .performClick()

        // Assert - Navigation callback should be called
        assert(navigateBackCalled) { "Navigate back should have been called" }
    }

    @Test
    fun loginScreen_screenDisplaysCorrectTitle() {
        // Arrange & Act
        composeTestRule.setContent {
            LoginScreen(
                viewModel = mockViewModel,
                onNavigateBack = { navigateBackCalled = true },
                onNavigateToForgotPassword = { navigateToForgotPasswordCalled = true }
            )
        }

        // Assert - Check that the screen displays the correct title
        composeTestRule
            .onNodeWithText("Welcome back")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Log in to continue")
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_emptyEmailFilledPassword_buttonIsDisabled() {
        // Arrange
        composeTestRule.setContent {
            LoginScreen(
                viewModel = mockViewModel,
                onNavigateBack = { navigateBackCalled = true },
                onNavigateToForgotPassword = { navigateToForgotPasswordCalled = true }
            )
        }

        // Act - Only fill password field, leave email empty
        composeTestRule
            .onNodeWithTag("passwordField")
            .performTextInput("password123")

        // Assert - Login button should still be disabled
        composeTestRule
            .onNodeWithTag("log_in_button")
            .assertIsNotEnabled()
    }

    @Test
    fun loginScreen_filledEmailEmptyPassword_buttonIsDisabled() {
        // Arrange
        composeTestRule.setContent {
            LoginScreen(
                viewModel = mockViewModel,
                onNavigateBack = { navigateBackCalled = true },
                onNavigateToForgotPassword = { navigateToForgotPasswordCalled = true }
            )
        }

        // Act - Only fill email field, leave password empty
        composeTestRule
            .onNodeWithTag("emailField")
            .performTextInput("test@example.com")

        // Assert - Login button should still be disabled
        composeTestRule
            .onNodeWithTag("log_in_button")
            .assertIsNotEnabled()
    }
}
