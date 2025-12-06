# iHost UML Diagrams

This directory contains comprehensive UML diagrams for the iHost event management application.

## Diagrams Overview

### 1. System Architecture (`system-architecture.puml`)
High-level component diagram showing:
- Frontend (Android) architecture layers (UI, ViewModel, Data)
- Backend (Spring Boot) architecture layers (Controllers, Services, Models)
- External services integration (Firebase, Stripe, Cloudinary, Google Maps)
- Communication flow between all components
- Security layer with Firebase token validation

**Key insights:**
- Shows MVVM pattern in frontend
- Shows layered architecture in backend
- Illustrates JWT token flow from Firebase through interceptor to backend
- Displays Firestore collections structure

### 2. Backend Class Diagram (`backend-class-diagram.puml`)
Detailed class diagram of backend components:
- **Entity Models**: User, Event, EventUser, Friendship with their properties
- **DTOs**: Request/Response objects for API communication
- **Controllers**: REST endpoints for all features
- **Services**: Business logic interfaces and implementations
- **Security**: Firebase token filter and security configuration
- **Configuration**: Firebase, Stripe, and Cloudinary setup

**Key insights:**
- Shows many-to-many relationship between Users and Events through EventUser junction table
- Demonstrates friendship bidirectional relationship structure
- Illustrates service layer abstraction
- Shows dependency injection patterns

### 3. Frontend Class Diagram (`frontend-class-diagram.puml`)
Detailed class diagram of Android frontend:
- **Domain Models**: User, Event, EventWithMetadata, Friendship
- **ViewModels**: AuthViewModel, EventViewModel, UserViewModel, FriendViewModel with UI state
- **Repositories**: Data access layer abstracting API calls
- **API Interfaces**: Retrofit service definitions
- **UI Screens**: Composable screens with ViewModel dependencies

**Key insights:**
- Shows MVVM architecture with clear separation of concerns
- Illustrates StateFlow usage for reactive UI updates
- Shows Repository pattern for data access
- Demonstrates Retrofit client configuration with Firebase auth interceptor

### 4. Authentication Sequence Diagram (`sequence-authentication.puml`)
Detailed flow for user registration and login:
- User registration with Firebase Auth + backend profile creation
- Token generation and storage
- Login flow
- Token validation on backend via FirebaseTokenFilter
- Automatic token refresh mechanism

**Key insights:**
- Firebase Auth creates user first, then backend stores profile
- JWT token automatically added to all requests via interceptor
- Backend verifies token using Firebase Admin SDK
- SecurityContext populated with user UID for authorization

### 5. Event Creation Sequence Diagram (`sequence-event-creation.puml`)
Complete flow for creating an event with image upload:
- Image upload to Cloudinary via backend
- Event creation with image URL
- Automatic creator assignment as CREATOR role
- Share code generation
- Error handling scenarios

**Key insights:**
- Image uploaded first, then URL included in event creation
- Share code format: "IH-XXXXX" (unique per event)
- Creator automatically added to event_users with CREATOR status
- Cloudinary transformations applied (1920x1080, auto quality)

### 6. Event Joining Sequence Diagram (`sequence-event-joining.puml`)
Flow for finding and joining events:
- Finding event by share code
- Creating PENDING event_user association
- Free event: Direct acceptance
- Paid event: Stripe payment flow with PaymentSheet
- Webhook handling for payment confirmation
- Accepting/declining invitations

**Key insights:**
- Finding event by code creates PENDING status automatically
- Payment flow creates Customer, Ephemeral Key, and Payment Intent
- Webhook validates payment before acceptance
- Currency: NOK (Norwegian Krone)

### 7. Friendship Sequence Diagram (`sequence-friendship.puml`)
Complete friend request and management flow:
- Loading all users for friend search
- Sending friend requests (creates PENDING friendship)
- Viewing pending requests (recipient perspective)
- Accepting/declining friend requests
- Removing friends (either party can remove)

**Key insights:**
- Friendships stored with user1Id and user2Id
- Status: PENDING (sent), ACCEPTED, DECLINED
- Only recipient (user2Id) can accept/decline
- Either party can remove accepted friendship
- Prevents duplicate friendships and self-friending

## How to View These Diagrams

### Option 1: PlantUML Online Server
1. Visit http://www.plantuml.com/plantuml/uml/
2. Copy the contents of any `.puml` file
3. Paste into the online editor

### Option 2: VS Code with PlantUML Extension
1. Install "PlantUML" extension by jebbs
2. Open any `.puml` file
3. Press `Alt+D` to preview

### Option 3: IntelliJ IDEA
1. Install "PlantUML integration" plugin
2. Open any `.puml` file
3. Preview pane will show diagram automatically

### Option 4: Generate PNG/SVG Files
Install PlantUML locally and run:
```bash
# Generate PNG
java -jar plantuml.jar system-architecture.puml

# Generate SVG (scalable)
java -jar plantuml.jar -tsvg system-architecture.puml
```

## Diagram Maintenance

When updating the codebase, please update these diagrams if:
- Adding/removing entity models
- Adding/removing API endpoints
- Changing authentication/authorization flow
- Adding new external services
- Modifying key business logic flows
- Changing data relationships

## Key Design Patterns Illustrated

1. **MVVM Pattern**: ViewModels manage UI state, Views observe state changes
2. **Repository Pattern**: Abstracts data sources from ViewModels
3. **Layered Architecture**: Clear separation between UI, business logic, and data
4. **Dependency Injection**: Services and repositories injected into consumers
5. **Interceptor Pattern**: Firebase auth interceptor adds tokens automatically
6. **Filter Chain Pattern**: Spring Security filter validates tokens
7. **DTO Pattern**: Separate request/response objects from domain models

## Database Schema (Firestore Collections)

```
users/
  {uid}/
    - email, username, firstName, lastName, photoUrl
    - createdAt, updatedAt

events/
  {eventId}/
    - title, description, eventDate, eventTime, location
    - creatorUid, free, price, shareCode
    - createdAt, updatedAt

event_users/
  {documentId}/
    - eventId, userId, status, role
    - invitedAt, respondedAt

friendships/
  {friendshipId}/
    - user1Id, user2Id, status
    - requestedBy, requestedAt, respondedAt

event_images/
  {documentId}/
    - eventId, imageUrl, publicId
    - createdAt
```

## External Service Integration Points

1. **Firebase Authentication**
   - Frontend: User signup/login
   - Backend: Token verification via Admin SDK

2. **Firestore Database**
   - All data storage (users, events, event_users, friendships, event_images)
   - Real-time querying with indexed fields

3. **Stripe Payment Processing**
   - Payment intents for event fees
   - Webhook validation for payment confirmation
   - Currency: NOK

4. **Cloudinary Image Storage**
   - Event images and profile photos
   - Automatic transformations and optimization

5. **Google Maps SDK**
   - Location selection for events
   - Map display in event details

## Security Model

1. **Authentication**: Firebase JWT tokens
2. **Authorization**: SecurityContext with user UID as principal
3. **API Protection**: All endpoints except registration and availability checks require authentication
4. **Resource Authorization**: Users can only modify their own resources (verified in service layer)
5. **Token Validation**: Every request validated via FirebaseTokenFilter
6. **Webhook Security**: Stripe webhook signature validation

---

For questions or clarifications about these diagrams, please refer to the main project documentation or contact the development team.
