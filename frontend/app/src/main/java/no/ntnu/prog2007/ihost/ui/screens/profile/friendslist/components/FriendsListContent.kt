package no.ntnu.prog2007.ihost.ui.screens.profile.friendslist.components

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import no.ntnu.prog2007.ihost.data.model.domain.Friendship
import no.ntnu.prog2007.ihost.data.model.domain.User
import no.ntnu.prog2007.ihost.data.model.domain.getOtherUserId
import no.ntnu.prog2007.ihost.ui.components.UserCardWithIconAction
import no.ntnu.prog2007.ihost.viewmodel.FriendViewModel

@Composable
fun FriendsListContent(
    friends: List<Friendship>,
    userDetailsMap: Map<String, User>,
    currentUserId: String?,
    friendViewModel: FriendViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = friends,
            key = { it.id }
        ) { friendship ->
            val friendUserId = if (currentUserId != null)
                friendship.getOtherUserId(currentUserId)
            else null
            val friendUser = friendUserId?.let { userDetailsMap[it] }

            if (friendUser != null) {
                UserCardWithIconAction(
                    user = friendUser,
                    icon = Icons.Default.PersonRemove,
                    iconTint = MaterialTheme.colorScheme.error,
                    iconDescription = "Remove friend",
                    onIconClick = {
                        friendViewModel.removeFriend(
                            friendshipId = friendship.id,
                            onSuccess = {
                                Toast.makeText(
                                    context,
                                    "Removed ${friendUser.firstName} from friends",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onError = { error ->
                                Toast.makeText(
                                    context,
                                    "Error: $error",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                )
            }
        }
    }
}
