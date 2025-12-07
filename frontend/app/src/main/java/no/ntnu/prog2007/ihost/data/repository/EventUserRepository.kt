package no.ntnu.prog2007.ihost.data.repository

import android.util.Log
import no.ntnu.prog2007.ihost.data.model.domain.Event
import no.ntnu.prog2007.ihost.data.model.domain.EventUser
import no.ntnu.prog2007.ihost.data.model.domain.EventWithMetadata
import no.ntnu.prog2007.ihost.data.model.dto.EventResponse
import no.ntnu.prog2007.ihost.data.model.dto.EventUserResponse
import no.ntnu.prog2007.ihost.data.model.dto.EventWithMetadataResponse
import no.ntnu.prog2007.ihost.data.model.dto.InviteUsersRequest
import no.ntnu.prog2007.ihost.data.model.dto.InviteUsersResponse
import no.ntnu.prog2007.ihost.data.remote.RetrofitClient
import no.ntnu.prog2007.ihost.data.remote.api.EventUserApi

class EventUserRepository(
    private val eventUserApi: EventUserApi = RetrofitClient.eventUserApi
) {

    /**
     * Invite users to an event
     */
    suspend fun inviteUsers(eventId: String, userIds: List<String>): Result<Unit> {
        return try {
            val request = InviteUsersRequest(eventId = eventId, userIds = userIds)
            val response = eventUserApi.inviteUsers(request)
            Log.d("EventUserRepository", "Invited ${response.invitedCount} users to event $eventId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("EventUserRepository", "Error inviting users to event", e)
            Result.failure(e)
        }
    }

    /**
     * Accept an event invitation
     */
    suspend fun acceptInvitation(eventUserId: String): Result<Unit> {
        return try {
            eventUserApi.acceptInvitation(eventUserId)
            Log.d("EventUserRepository", "Accepted invitation: $eventUserId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("EventUserRepository", "Error accepting invitation", e)
            Result.failure(e)
        }
    }

    /**
     * Decline an event invitation
     */
    suspend fun declineInvitation(eventUserId: String): Result<Unit> {
        return try {
            eventUserApi.declineInvitation(eventUserId)
            Log.d("EventUserRepository", "Declined invitation: $eventUserId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("EventUserRepository", "Error declining invitation", e)
            Result.failure(e)
        }
    }

    /**
     * Get all attendees for an event
     * @param status Optional filter by status (PENDING, ACCEPTED, DECLINED, CREATOR)
     */
    suspend fun getEventAttendees(eventId: String, status: String? = null): Result<List<EventUser>> {
        return try {
            val attendeesDto = eventUserApi.getEventAttendees(eventId, status)
            val attendees = attendeesDto.map { mapToEventUser(it) }
            Log.d("EventUserRepository", "Loaded ${attendees.size} attendees for event $eventId")
            Result.success(attendees)
        } catch (e: Exception) {
            Log.e("EventUserRepository", "Error loading event attendees", e)
            Result.failure(e)
        }
    }

    private fun mapToEventUser(dto: EventUserResponse): EventUser {
        return EventUser(
            id = dto.id,
            eventId = dto.eventId,
            userId = dto.userId,
            status = dto.status,
            role = dto.role,
            invitedAt = dto.invitedAt,
            respondedAt = dto.respondedAt
        )
    }

}
