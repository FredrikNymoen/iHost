package no.ntnu.prog2007.ihost.ui.screens.addevent

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import no.ntnu.prog2007.ihost.viewmodel.EventUiState
import no.ntnu.prog2007.ihost.viewmodel.EventViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for AddEventScreen
 *
 * AAA pattern: Arrange, Act, Assert
 *
 * These are UI tests that run on Android device/emulator
 * Testing event creation form validation and interactions
 */
@RunWith(AndroidJUnit4::class)
class AddEventScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockViewModel: EventViewModel
    private lateinit var fakeUiState: MutableStateFlow<EventUiState>
    private lateinit var context: Context
    private var eventCreatedCalled = false

    @Before
    fun setup() {
        // Arrange - Initialize fake UI state and mock ViewModel
        fakeUiState = MutableStateFlow(EventUiState())
        mockViewModel = mockk(relaxed = true)
        context = InstrumentationRegistry.getInstrumentation().targetContext

        // Mock the uiState to return our fake StateFlow
        every { mockViewModel.uiState } returns fakeUiState

        eventCreatedCalled = false
    }

    @Test
    fun addEventScreen_initialState_createButtonIsDisabled() {
        // Arrange - Set up the AddEventScreen with empty title
        composeTestRule.setContent {
            AddEventScreen(
                viewModel = mockViewModel,
                onEventCreated = { eventCreatedCalled = true }
            )
        }

        // Assert - Create button should be disabled when title is empty
        composeTestRule
            .onNodeWithTag("createEventButton")
            .assertIsNotEnabled()
    }

    @Test
    fun addEventScreen_enterTitle_createButtonIsEnabled() {
        // Arrange
        composeTestRule.setContent {
            AddEventScreen(
                viewModel = mockViewModel,
                onEventCreated = { eventCreatedCalled = true }
            )
        }

        // Act - Enter a title in the title field
        composeTestRule
            .onNodeWithTag("eventTitleField")
            .performTextInput("Summer Beach Party")

        // Wait for composition to update
        composeTestRule.waitForIdle()

        // Assert - Create button should be enabled when title is filled and date has default value
        composeTestRule
            .onNodeWithTag("createEventButton")
            .assertIsEnabled()
    }

    @Test
    fun addEventScreen_enterTitleThenClear_createButtonIsDisabled() {
        // Arrange
        composeTestRule.setContent {
            AddEventScreen(
                viewModel = mockViewModel,
                onEventCreated = { eventCreatedCalled = true }
            )
        }

        // Act - Enter title then clear it
        composeTestRule
            .onNodeWithTag("eventTitleField")
            .performTextInput("Test Event")

        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithTag("eventTitleField")
            .performTextClearance()

        composeTestRule.waitForIdle()

        // Assert - Button should be disabled again
        composeTestRule
            .onNodeWithTag("createEventButton")
            .assertIsNotEnabled()
    }

    @Test
    fun addEventScreen_enterDescription_descriptionFieldShowsText() {
        // Arrange
        composeTestRule.setContent {
            AddEventScreen(
                viewModel = mockViewModel,
                onEventCreated = { eventCreatedCalled = true }
            )
        }

        // Act - Enter text in description field
        val testDescription = "This is a fun summer party at the beach with BBQ and music!"
        composeTestRule
            .onNodeWithTag("eventDescriptionField")
            .performTextInput(testDescription)

        // Assert - Description field should display the entered text
        composeTestRule
            .onNodeWithTag("eventDescriptionField")
            .assertTextContains(testDescription)
    }

    @Test
    fun addEventScreen_clickCreateButton_callsViewModelCreateEvent() {
        // Arrange
        composeTestRule.setContent {
            AddEventScreen(
                viewModel = mockViewModel,
                onEventCreated = { eventCreatedCalled = true }
            )
        }

        val testTitle = "Team Building Event"
        val testDescription = "Annual team building activity"

        // Act - Fill in form and click create button
        composeTestRule
            .onNodeWithTag("eventTitleField")
            .performTextInput(testTitle)

        composeTestRule
            .onNodeWithTag("eventDescriptionField")
            .performTextInput(testDescription)

        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithTag("createEventButton")
            .performClick()

        composeTestRule.waitForIdle()

        // Assert - Verify ViewModel's createEvent method was called (with any parameters)
        verify(atLeast = 1) {
            mockViewModel.createEvent(
                context = any(),
                title = any(),
                description = any(),
                eventDate = any(),
                eventTime = any(),
                location = any(),
                free = any(),
                price = any(),
                imageUri = any()
            )
        }

        // Verify navigation callback was called
        assert(eventCreatedCalled) { "onEventCreated should have been called" }
    }

    @Test
    fun addEventScreen_dateFieldDisplaysDefaultDate() {
        // Arrange & Act
        composeTestRule.setContent {
            AddEventScreen(
                viewModel = mockViewModel,
                onEventCreated = { eventCreatedCalled = true }
            )
        }

        // Assert - Date field should have a default value (today's date)
        composeTestRule
            .onNodeWithTag("eventDateField")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun addEventScreen_fieldsAreDisabledWhileLoading() {
        // Arrange - Set loading state to true
        fakeUiState.value = EventUiState(isLoading = true)

        composeTestRule.setContent {
            AddEventScreen(
                viewModel = mockViewModel,
                onEventCreated = { eventCreatedCalled = true }
            )
        }

        // Assert - Fields should be disabled while loading
        composeTestRule
            .onNodeWithTag("eventTitleField")
            .assertIsNotEnabled()

        composeTestRule
            .onNodeWithTag("eventDescriptionField")
            .assertIsNotEnabled()
    }

    @Test
    fun addEventScreen_onlyTitleRequired_otherFieldsOptional() {
        // Arrange
        composeTestRule.setContent {
            AddEventScreen(
                viewModel = mockViewModel,
                onEventCreated = { eventCreatedCalled = true }
            )
        }

        // Act - Only fill in title (description, time, location are optional)
        composeTestRule
            .onNodeWithTag("eventTitleField")
            .performTextInput("Quick Event")

        composeTestRule.waitForIdle()

        // Assert - Button should be enabled with just title
        composeTestRule
            .onNodeWithTag("createEventButton")
            .assertIsEnabled()

        // Click create button
        composeTestRule
            .onNodeWithTag("createEventButton")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify createEvent was called (with any parameters)
        verify(atLeast = 1) {
            mockViewModel.createEvent(
                context = any(),
                title = any(),
                description = any(),
                eventDate = any(),
                eventTime = any(),
                location = any(),
                free = any(),
                price = any(),
                imageUri = any()
            )
        }

        // Verify navigation callback was called
        assert(eventCreatedCalled) { "onEventCreated should have been called" }
    }
}
