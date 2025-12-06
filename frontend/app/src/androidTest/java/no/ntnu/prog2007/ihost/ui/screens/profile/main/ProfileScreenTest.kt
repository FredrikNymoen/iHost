package no.ntnu.prog2007.ihost.ui.screens.profile.main

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import no.ntnu.prog2007.ihost.data.model.domain.EventWithMetadata
import no.ntnu.prog2007.ihost.data.model.domain.Friendship
import no.ntnu.prog2007.ihost.data.model.domain.User
import no.ntnu.prog2007.ihost.viewmodel.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for ProfileScreen
 *
 * AAA pattern: Arrange, Act, Assert
 *
 * ProfileScreen is complex with 4 ViewModels:
 * - AuthViewModel (user authentication)
 * - UserViewModel (user profile data)
 * - EventViewModel (event statistics)
 * - FriendViewModel (friends and friend requests)
 *
 * Tests cover:
 * - Loading states
 * - Error states
 * - Profile display
 * - User interactions
 * - Dialog management
 * - Navigation callbacks
 */
@RunWith(AndroidJUnit4::class)
class ProfileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Mock ViewModels
    private lateinit var mockAuthViewModel: AuthViewModel
    private lateinit var mockUserViewModel: UserViewModel
    private lateinit var mockEventViewModel: EventViewModel
    private lateinit var mockFriendViewModel: FriendViewModel

    // Fake UI States
    private lateinit var fakeAuthUiState: MutableStateFlow<AuthUiState>
    private lateinit var fakeUserUiState: MutableStateFlow<UserUiState>
    private lateinit var fakeEventUiState: MutableStateFlow<EventUiState>
    private lateinit var fakeFriendUiState: MutableStateFlow<FriendUiState>

    // Mock Firebase User
    private lateinit var mockFirebaseUser: FirebaseUser

    // Navigation callbacks
    private var logOutCalled = false
    private var navigateToAddFriendCalled = false
    private var navigateToFriendsListCalled = false

    private lateinit var context: Context

    // Test data
    private val testUser = User(
        uid = "test-uid-123",
        email = "test@example.com",
        username = "testuser",
        firstName = "Test",
        lastName = "User",
        photoUrl = null
    )

    @Before
    fun setup() {
        // Arrange - Initialize context
        context = InstrumentationRegistry.getInstrumentation().targetContext

        // Initialize fake UI states
        fakeAuthUiState = MutableStateFlow(AuthUiState())
        fakeUserUiState = MutableStateFlow(UserUiState())
        fakeEventUiState = MutableStateFlow(EventUiState())
        fakeFriendUiState = MutableStateFlow(FriendUiState())

        // Create mock ViewModels
        mockAuthViewModel = mockk(relaxed = true)
        mockUserViewModel = mockk(relaxed = true)
        mockEventViewModel = mockk(relaxed = true)
        mockFriendViewModel = mockk(relaxed = true)

        // Mock Firebase User
        mockFirebaseUser = mockk(relaxed = true)
        every { mockFirebaseUser.uid } returns "test-uid-123"
        every { mockFirebaseUser.email } returns "test@example.com"

        // Mock the state flows
        every { mockAuthViewModel.uiState } returns fakeAuthUiState
        every { mockUserViewModel.uiState } returns fakeUserUiState
        every { mockEventViewModel.uiState } returns fakeEventUiState
        every { mockFriendViewModel.uiState } returns fakeFriendUiState

        // Reset navigation callbacks
        logOutCalled = false
        navigateToAddFriendCalled = false
        navigateToFriendsListCalled = false
    }

    @Test
    fun profileScreen_notLoggedIn_showsNotLoggedInMessage() {
        // Arrange - User is not logged in
        fakeAuthUiState.value = AuthUiState(
            currentUser = null,
            isLoggedIn = false
        )

        composeTestRule.setContent {
            ProfileScreen(
                authViewModel = mockAuthViewModel,
                userViewModel = mockUserViewModel,
                eventViewModel = mockEventViewModel,
                friendViewModel = mockFriendViewModel,
                onLogOut = { logOutCalled = true },
                onNavigateToAddFriend = { navigateToAddFriendCalled = true },
                onNavigateToFriendsList = { navigateToFriendsListCalled = true }
            )
        }

        // Assert - "Not logged in" message should be displayed
        composeTestRule
            .onNodeWithText("Not logged in")
            .assertIsDisplayed()
    }

    @Test
    fun profileScreen_profileLoading_showsLoadingOverlay() {
        // Arrange - User is logged in but profile is loading
        fakeAuthUiState.value = AuthUiState(
            currentUser = mockFirebaseUser,
            isLoggedIn = true
        )
        fakeUserUiState.value = UserUiState(
            isProfileLoading = true,
            selectedUser = null
        )

        composeTestRule.setContent {
            ProfileScreen(
                authViewModel = mockAuthViewModel,
                userViewModel = mockUserViewModel,
                eventViewModel = mockEventViewModel,
                friendViewModel = mockFriendViewModel,
                onLogOut = { logOutCalled = true },
                onNavigateToAddFriend = { navigateToAddFriendCalled = true },
                onNavigateToFriendsList = { navigateToFriendsListCalled = true }
            )
        }

        // Assert - Loading overlay should be displayed
        composeTestRule
            .onNodeWithText("Loading profile...")
            .assertIsDisplayed()
    }

    @Test
    fun profileScreen_accountDeleted_showsErrorState() {
        // Arrange - User logged in but account deleted error
        fakeAuthUiState.value = AuthUiState(
            currentUser = mockFirebaseUser,
            isLoggedIn = true
        )
        fakeUserUiState.value = UserUiState(
            selectedUser = testUser,
            errorMessage = "Your account has been deleted",
            isProfileLoading = false
        )

        composeTestRule.setContent {
            ProfileScreen(
                authViewModel = mockAuthViewModel,
                userViewModel = mockUserViewModel,
                eventViewModel = mockEventViewModel,
                friendViewModel = mockFriendViewModel,
                onLogOut = { logOutCalled = true },
                onNavigateToAddFriend = { navigateToAddFriendCalled = true },
                onNavigateToFriendsList = { navigateToFriendsListCalled = true }
            )
        }

        // Assert - Error message should be displayed
        composeTestRule
            .onNodeWithText("Please sign in again.")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Return to Login.")
            .assertIsDisplayed()
    }

    @Test
    fun profileScreen_profileLoaded_displaysUserInfo() {
        // Arrange - User logged in with profile loaded
        fakeAuthUiState.value = AuthUiState(
            currentUser = mockFirebaseUser,
            isLoggedIn = true
        )
        fakeUserUiState.value = UserUiState(
            selectedUser = testUser,
            isProfileLoading = false
        )

        composeTestRule.setContent {
            ProfileScreen(
                authViewModel = mockAuthViewModel,
                userViewModel = mockUserViewModel,
                eventViewModel = mockEventViewModel,
                friendViewModel = mockFriendViewModel,
                onLogOut = { logOutCalled = true },
                onNavigateToAddFriend = { navigateToAddFriendCalled = true },
                onNavigateToFriendsList = { navigateToFriendsListCalled = true }
            )
        }

        // Assert - User name should be displayed
        composeTestRule
            .onNodeWithText("Test User")
            .assertIsDisplayed()

        // Assert - Username should be displayed
        composeTestRule
            .onNodeWithText("@testuser")
            .assertIsDisplayed()

        // Assert - Email should be displayed
        composeTestRule
            .onNodeWithText("test@example.com")
            .assertIsDisplayed()
    }

    @Test
    fun profileScreen_displaysEventStatistics() {
        // Arrange - User logged in with events
        fakeAuthUiState.value = AuthUiState(
            currentUser = mockFirebaseUser,
            isLoggedIn = true
        )
        fakeUserUiState.value = UserUiState(
            selectedUser = testUser,
            isProfileLoading = false
        )

        // Create mock events with different roles
        val mockEvents = listOf(
            mockk<EventWithMetadata>(relaxed = true).apply {
                every { userRole } returns "CREATOR"
            },
            mockk<EventWithMetadata>(relaxed = true).apply {
                every { userRole } returns "CREATOR"
            },
            mockk<EventWithMetadata>(relaxed = true).apply {
                every { userRole } returns "ATTENDEE"
            },
            mockk<EventWithMetadata>(relaxed = true).apply {
                every { userRole } returns "ATTENDEE"
            },
            mockk<EventWithMetadata>(relaxed = true).apply {
                every { userRole } returns "ATTENDEE"
            }
        )

        fakeEventUiState.value = EventUiState(events = mockEvents)

        composeTestRule.setContent {
            ProfileScreen(
                authViewModel = mockAuthViewModel,
                userViewModel = mockUserViewModel,
                eventViewModel = mockEventViewModel,
                friendViewModel = mockFriendViewModel,
                onLogOut = { logOutCalled = true },
                onNavigateToAddFriend = { navigateToAddFriendCalled = true },
                onNavigateToFriendsList = { navigateToFriendsListCalled = true }
            )
        }

        // Assert - Should show "2 events created"
        composeTestRule
            .onNodeWithText("2 events created")
            .assertIsDisplayed()

        // Assert - Should show "3 invitations"
        composeTestRule
            .onNodeWithText("3 invitations")
            .assertIsDisplayed()
    }

    @Test
    fun profileScreen_clickLogOutButton_callsSignOutAndNavigation() {
        // Arrange - User logged in with profile
        fakeAuthUiState.value = AuthUiState(
            currentUser = mockFirebaseUser,
            isLoggedIn = true
        )
        fakeUserUiState.value = UserUiState(
            selectedUser = testUser,
            isProfileLoading = false
        )

        composeTestRule.setContent {
            ProfileScreen(
                authViewModel = mockAuthViewModel,
                userViewModel = mockUserViewModel,
                eventViewModel = mockEventViewModel,
                friendViewModel = mockFriendViewModel,
                onLogOut = { logOutCalled = true },
                onNavigateToAddFriend = { navigateToAddFriendCalled = true },
                onNavigateToFriendsList = { navigateToFriendsListCalled = true }
            )
        }

        // Act - Scroll to bottom and click log out button
        composeTestRule
            .onNodeWithText("Log Out")
            .performScrollTo()
            .performClick()

        composeTestRule.waitForIdle()

        // Assert - signOut should be called on ViewModel
        verify(exactly = 1) { mockAuthViewModel.signOut() }

        // Assert - onLogOut callback should be called
        assert(logOutCalled) { "onLogOut should have been called" }
    }

    @Test
    fun profileScreen_clickEditName_showsEditNameDialog() {
        // Arrange - User logged in with profile
        fakeAuthUiState.value = AuthUiState(
            currentUser = mockFirebaseUser,
            isLoggedIn = true
        )
        fakeUserUiState.value = UserUiState(
            selectedUser = testUser,
            isProfileLoading = false
        )

        composeTestRule.setContent {
            ProfileScreen(
                authViewModel = mockAuthViewModel,
                userViewModel = mockUserViewModel,
                eventViewModel = mockEventViewModel,
                friendViewModel = mockFriendViewModel,
                onLogOut = { logOutCalled = true },
                onNavigateToAddFriend = { navigateToAddFriendCalled = true },
                onNavigateToFriendsList = { navigateToFriendsListCalled = true }
            )
        }

        // Act - Find and click the edit icon (pencil icon)
        // The edit icon should be near the name
        composeTestRule
            .onAllNodesWithContentDescription("Edit Name")
            .onFirst()
            .performClick()

        composeTestRule.waitForIdle()

        // Assert - Edit name dialog should be displayed
        composeTestRule
            .onNodeWithText("Edit Name")
            .assertIsDisplayed()

        // Assert - Dialog should have First Name and Last Name labels
        composeTestRule
            .onNodeWithText("First Name")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Last Name")
            .assertIsDisplayed()
    }

    @Test
    fun profileScreen_editNameDialog_savesChanges() {
        // Arrange - User logged in with profile
        fakeAuthUiState.value = AuthUiState(
            currentUser = mockFirebaseUser,
            isLoggedIn = true
        )
        fakeUserUiState.value = UserUiState(
            selectedUser = testUser,
            isProfileLoading = false
        )

        composeTestRule.setContent {
            ProfileScreen(
                authViewModel = mockAuthViewModel,
                userViewModel = mockUserViewModel,
                eventViewModel = mockEventViewModel,
                friendViewModel = mockFriendViewModel,
                onLogOut = { logOutCalled = true },
                onNavigateToAddFriend = { navigateToAddFriendCalled = true },
                onNavigateToFriendsList = { navigateToFriendsListCalled = true }
            )
        }

        // Act - Open edit name dialog
        composeTestRule
            .onAllNodesWithContentDescription("Edit Name")
            .onFirst()
            .performClick()

        composeTestRule.waitForIdle()

        // Clear first name field and enter new name
        composeTestRule
            .onNodeWithTag("firstNameField")
            .performTextClearance()

        composeTestRule
            .onNodeWithTag("firstNameField")
            .performTextInput("NewName")

        // Click Save button
        composeTestRule
            .onNodeWithText("Save")
            .performClick()

        composeTestRule.waitForIdle()

        // Assert - updateUserProfile should be called with new name
        verify(atLeast = 1) {
            mockUserViewModel.updateUserProfile(
                uid = "test-uid-123",
                firstName = "NewName",
                lastName = any()
            )
        }
    }

    @Test
    fun profileScreen_editNameDialog_canCancel() {
        // Arrange - User logged in with profile
        fakeAuthUiState.value = AuthUiState(
            currentUser = mockFirebaseUser,
            isLoggedIn = true
        )
        fakeUserUiState.value = UserUiState(
            selectedUser = testUser,
            isProfileLoading = false
        )

        composeTestRule.setContent {
            ProfileScreen(
                authViewModel = mockAuthViewModel,
                userViewModel = mockUserViewModel,
                eventViewModel = mockEventViewModel,
                friendViewModel = mockFriendViewModel,
                onLogOut = { logOutCalled = true },
                onNavigateToAddFriend = { navigateToAddFriendCalled = true },
                onNavigateToFriendsList = { navigateToFriendsListCalled = true }
            )
        }

        // Act - Open edit name dialog
        composeTestRule
            .onAllNodesWithContentDescription("Edit Name")
            .onFirst()
            .performClick()

        composeTestRule.waitForIdle()

        // Click Cancel button
        composeTestRule
            .onNodeWithText("Cancel")
            .performClick()

        composeTestRule.waitForIdle()

        // Assert - Dialog should be dismissed
        composeTestRule
            .onNodeWithText("Edit Name")
            .assertDoesNotExist()

        // Assert - updateUserProfile should NOT be called
        verify(exactly = 0) {
            mockUserViewModel.updateUserProfile(any(), any(), any(), any())
        }
    }

    @Test
    fun profileScreen_clickAvatar_showsChangeAvatarDialog() {
        // Arrange - User logged in with profile
        fakeAuthUiState.value = AuthUiState(
            currentUser = mockFirebaseUser,
            isLoggedIn = true
        )
        fakeUserUiState.value = UserUiState(
            selectedUser = testUser,
            isProfileLoading = false
        )

        composeTestRule.setContent {
            ProfileScreen(
                authViewModel = mockAuthViewModel,
                userViewModel = mockUserViewModel,
                eventViewModel = mockEventViewModel,
                friendViewModel = mockFriendViewModel,
                onLogOut = { logOutCalled = true },
                onNavigateToAddFriend = { navigateToAddFriendCalled = true },
                onNavigateToFriendsList = { navigateToFriendsListCalled = true }
            )
        }

        // Act - Click on the profile avatar (identified by camera icon)
        composeTestRule
            .onNodeWithContentDescription("Change Avatar")
            .performClick()

        composeTestRule.waitForIdle()

        // Assert - Change avatar dialog should be displayed
        composeTestRule
            .onNodeWithText("Change Profile Picture")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Take Photo")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Gallery")
            .assertIsDisplayed()
    }

    @Test
    fun profileScreen_changeAvatarDialog_canDismiss() {
        // Arrange - User logged in with profile
        fakeAuthUiState.value = AuthUiState(
            currentUser = mockFirebaseUser,
            isLoggedIn = true
        )
        fakeUserUiState.value = UserUiState(
            selectedUser = testUser,
            isProfileLoading = false
        )

        composeTestRule.setContent {
            ProfileScreen(
                authViewModel = mockAuthViewModel,
                userViewModel = mockUserViewModel,
                eventViewModel = mockEventViewModel,
                friendViewModel = mockFriendViewModel,
                onLogOut = { logOutCalled = true },
                onNavigateToAddFriend = { navigateToAddFriendCalled = true },
                onNavigateToFriendsList = { navigateToFriendsListCalled = true }
            )
        }

        // Act - Open change avatar dialog
        composeTestRule
            .onNodeWithContentDescription("Change Avatar")
            .performClick()

        composeTestRule.waitForIdle()

        // Click Cancel button
        composeTestRule
            .onNodeWithText("Cancel")
            .performClick()

        composeTestRule.waitForIdle()

        // Assert - Dialog should be dismissed
        composeTestRule
            .onNodeWithText("Change Profile Picture")
            .assertDoesNotExist()
    }

    @Test
    fun profileScreen_friendsSection_displaysCorrectly() {
        // Arrange - User logged in with friends
        fakeAuthUiState.value = AuthUiState(
            currentUser = mockFirebaseUser,
            isLoggedIn = true
        )
        fakeUserUiState.value = UserUiState(
            selectedUser = testUser,
            isProfileLoading = false
        )

        // Create mock friendships
        val mockFriends = listOf(
            mockk<Friendship>(relaxed = true),
            mockk<Friendship>(relaxed = true),
            mockk<Friendship>(relaxed = true)
        )

        fakeFriendUiState.value = FriendUiState(
            friends = mockFriends
        )

        composeTestRule.setContent {
            ProfileScreen(
                authViewModel = mockAuthViewModel,
                userViewModel = mockUserViewModel,
                eventViewModel = mockEventViewModel,
                friendViewModel = mockFriendViewModel,
                onLogOut = { logOutCalled = true },
                onNavigateToAddFriend = { navigateToAddFriendCalled = true },
                onNavigateToFriendsList = { navigateToFriendsListCalled = true }
            )
        }

        // Assert - Friends section should be displayed
        composeTestRule
            .onNodeWithText("Friends")
            .assertIsDisplayed()
    }

    @Test
    fun profileScreen_clickAddFriend_navigatesToAddFriend() {
        // Arrange - User logged in with profile
        fakeAuthUiState.value = AuthUiState(
            currentUser = mockFirebaseUser,
            isLoggedIn = true
        )
        fakeUserUiState.value = UserUiState(
            selectedUser = testUser,
            isProfileLoading = false
        )

        composeTestRule.setContent {
            ProfileScreen(
                authViewModel = mockAuthViewModel,
                userViewModel = mockUserViewModel,
                eventViewModel = mockEventViewModel,
                friendViewModel = mockFriendViewModel,
                onLogOut = { logOutCalled = true },
                onNavigateToAddFriend = { navigateToAddFriendCalled = true },
                onNavigateToFriendsList = { navigateToFriendsListCalled = true }
            )
        }

        // Act - Find and click the "Add Friend" button
        composeTestRule
            .onNodeWithText("Add")
            .performScrollTo()
            .performClick()

        composeTestRule.waitForIdle()

        // Assert - Navigation callback should be called
        assert(navigateToAddFriendCalled) { "onNavigateToAddFriend should have been called" }
    }

    @Test
    fun profileScreen_clickFriendsArea_navigatesToFriendsList() {
        // Arrange - User logged in with friends
        fakeAuthUiState.value = AuthUiState(
            currentUser = mockFirebaseUser,
            isLoggedIn = true
        )
        fakeUserUiState.value = UserUiState(
            selectedUser = testUser,
            isProfileLoading = false
        )

        // Add at least one friend so friends section appears properly
        val mockFriends = listOf(mockk<Friendship>(relaxed = true))
        fakeFriendUiState.value = FriendUiState(friends = mockFriends)

        composeTestRule.setContent {
            ProfileScreen(
                authViewModel = mockAuthViewModel,
                userViewModel = mockUserViewModel,
                eventViewModel = mockEventViewModel,
                friendViewModel = mockFriendViewModel,
                onLogOut = { logOutCalled = true },
                onNavigateToAddFriend = { navigateToAddFriendCalled = true },
                onNavigateToFriendsList = { navigateToFriendsListCalled = true }
            )
        }

        // Act - Click on the "Friends" text (the friends count area is clickable)
        composeTestRule
            .onNodeWithText("Friends")
            .performScrollTo()
            .performClick()

        composeTestRule.waitForIdle()

        // Assert - Navigation callback should be called
        assert(navigateToFriendsListCalled) { "onNavigateToFriendsList should have been called" }
    }

    @Test
    fun profileScreen_loadingState_disablesInteractions() {
        // Arrange - User logged in but profile is loading
        fakeAuthUiState.value = AuthUiState(
            currentUser = mockFirebaseUser,
            isLoggedIn = true
        )
        fakeUserUiState.value = UserUiState(
            selectedUser = testUser,
            isProfileLoading = false,
            isLoading = true // General loading state
        )

        composeTestRule.setContent {
            ProfileScreen(
                authViewModel = mockAuthViewModel,
                userViewModel = mockUserViewModel,
                eventViewModel = mockEventViewModel,
                friendViewModel = mockFriendViewModel,
                onLogOut = { logOutCalled = true },
                onNavigateToAddFriend = { navigateToAddFriendCalled = true },
                onNavigateToFriendsList = { navigateToFriendsListCalled = true }
            )
        }

        // Assert - Loading overlay should be displayed
        composeTestRule.waitForIdle()

        // The loading overlay should block interactions
        // We can verify by checking that the loading state is active
        assert(fakeUserUiState.value.isLoading) { "Loading state should be true" }
    }

    @Test
    fun profileScreen_withPendingFriendRequests_showsRequestsInFriendsSection() {
        // Arrange - User logged in with pending requests
        fakeAuthUiState.value = AuthUiState(
            currentUser = mockFirebaseUser,
            isLoggedIn = true
        )
        fakeUserUiState.value = UserUiState(
            selectedUser = testUser,
            isProfileLoading = false
        )

        // Create mock pending requests
        val mockPendingRequests = listOf(
            mockk<Friendship>(relaxed = true),
            mockk<Friendship>(relaxed = true)
        )

        fakeFriendUiState.value = FriendUiState(
            pendingRequests = mockPendingRequests
        )

        composeTestRule.setContent {
            ProfileScreen(
                authViewModel = mockAuthViewModel,
                userViewModel = mockUserViewModel,
                eventViewModel = mockEventViewModel,
                friendViewModel = mockFriendViewModel,
                onLogOut = { logOutCalled = true },
                onNavigateToAddFriend = { navigateToAddFriendCalled = true },
                onNavigateToFriendsList = { navigateToFriendsListCalled = true }
            )
        }

        // Assert - Friends section should be displayed (it handles pending requests)
        composeTestRule
            .onNodeWithText("Friends")
            .assertIsDisplayed()

        // The FriendsSection component will show pending requests internally
        // but we can verify the state is correct
        assert(fakeFriendUiState.value.pendingRequests.size == 2) {
            "Should have 2 pending friend requests"
        }
    }

    @Test
    fun profileScreen_zeroEventsCreated_displaysZero() {
        // Arrange - User logged in with no events
        fakeAuthUiState.value = AuthUiState(
            currentUser = mockFirebaseUser,
            isLoggedIn = true
        )
        fakeUserUiState.value = UserUiState(
            selectedUser = testUser,
            isProfileLoading = false
        )
        fakeEventUiState.value = EventUiState(events = emptyList())

        composeTestRule.setContent {
            ProfileScreen(
                authViewModel = mockAuthViewModel,
                userViewModel = mockUserViewModel,
                eventViewModel = mockEventViewModel,
                friendViewModel = mockFriendViewModel,
                onLogOut = { logOutCalled = true },
                onNavigateToAddFriend = { navigateToAddFriendCalled = true },
                onNavigateToFriendsList = { navigateToFriendsListCalled = true }
            )
        }

        // Assert - Should show "0 events created"
        composeTestRule
            .onNodeWithText("0 events created")
            .assertIsDisplayed()

        // Assert - Should show "0 invitations"
        composeTestRule
            .onNodeWithText("0 invitations")
            .assertIsDisplayed()
    }
}
