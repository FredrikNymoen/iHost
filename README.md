# iHost

An Android application for organizing and managing social events with friends.

## Technology Stack

### Backend
- **Kotlin** with Spring Boot
- **Firebase Firestore** - NoSQL database
- **Firebase Authentication** - User authentication
- **Cloudinary** - Cloud-based image storage

### Frontend (Android)
- **Kotlin** with Jetpack Compose
- **Material 3** - Design system
- **MVVM Architecture** - Architecture pattern
- **Retrofit** - HTTP client for API calls
- **Coil** - Image loading
- **Firebase Auth SDK** - Authentication

## Frontend Structure

```
frontend/
└── app/
    └── src/
        └── main/
            └── java/no/ntnu/prog2007/ihost/
                ├── data/
                │   ├── model/
                │   │   ├── domain/      # Domain models
                │   │   └── dto/          # Data Transfer Objects
                │   ├── remote/
                │   │   ├── api/          # Retrofit API interfaces
                │   │   └── config/       # Network configuration
                │   └── repository/       # Data repositories
                ├── ui/
                │   ├── components/
                │   │   ├── auth/         # Auth-related components
                │   │   ├── events/       # Event-related components
                │   │   ├── layout/       # Layout components
                │   │   ├── splash/       # Splash screen components
                │   │   └── states/       # State management components
                │   ├── navigation/
                │   │   ├── config/       # Navigation configuration
                │   │   ├── graph/        # Navigation graphs
                │   │   └── state/        # Navigation state
                │   ├── screens/
                │   │   ├── addevent/     # Add event screen
                │   │   ├── auth/         # Authentication screens
                │   │   │   ├── forgotpassword/
                │   │   │   ├── login/
                │   │   │   ├── personalinfo/
                │   │   │   ├── signup/
                │   │   │   └── welcome/
                │   │   ├── events/       # Event screens
                │   │   │   ├── editevent/
                │   │   │   ├── eventdetail/
                │   │   │   ├── inviteusers/
                │   │   │   └── main/
                │   │   └── profile/      # Profile screens
                │   │       ├── addfriend/
                │   │       ├── friendslist/
                │   │       └── main/
                │   └── theme/            # Material 3 theme
                ├── viewmodel/            # ViewModels
                └── MainActivity.kt       # Entry point
```

## Backend Structure

```
backend/
└── src/
    └── main/
        └── kotlin/no/ntnu/prog2007/ihostapi/
            ├── config/           # Firebase and Spring Security configuration
            ├── controller/       # REST API endpoints
            ├── exception/        # Custom exceptions and error handling
            ├── model/
            │   ├── dto/          # Data Transfer Objects
            │   └── entity/       # Domain entities
            ├── repository/       # Data access layer
            ├── security/
            │   └── filter/       # Authentication filters
            └── service/
                └── impl/         # Service implementations
```

## Firestore Database Structure

The application uses 5 main collections in Firebase Firestore:

### 1. `users` Collection

Stores user information. Document ID is used as `uid`.

```
users/
  └── {uid} (document ID)
      ├── email: String
      ├── username: String (4-12 characters)
      ├── firstName: String
      ├── lastName: String? (optional)
      ├── photoUrl: String? (Cloudinary URL)
      ├── createdAt: String (ISO-8601)
      └── updatedAt: String? (ISO-8601)
```

**Example:**
```json
{
  "email": "ola.nordmann@example.com",
  "username": "olanord",
  "firstName": "Ola",
  "lastName": "Nordmann",
  "photoUrl": "https://res.cloudinary.com/.../profile.jpg",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-03-20T14:45:00"
}
```

---

### 2. `events` Collection

Stores events created by users. Document ID is used as event ID.

```
events/
  └── {eventId} (document ID)
      ├── title: String
      ├── description: String?
      ├── eventDate: String (YYYY-MM-DD)
      ├── eventTime: String? (HH:mm)
      ├── location: String?
      ├── creatorUid: String (ref to users/{uid})
      ├── shareCode: String (unique share code)
      ├── createdAt: String (ISO-8601)
      └── updatedAt: String? (ISO-8601)
```

**Example:**
```json
{
  "title": "Sommerfest 2024",
  "description": "Grillkos i hagen",
  "eventDate": "2024-06-15",
  "eventTime": "18:00",
  "location": "Hjemme hos meg",
  "creatorUid": "abc123xyz",
  "shareCode": "SUMMER2024",
  "createdAt": "2024-01-20T09:00:00",
  "updatedAt": "2024-02-10T12:30:00"
}
```

---

### 3. `event_users` Collection

Junction table that connects users to events with status and role.

```
event_users/
  └── {Id} (document ID)
      ├── eventId: String (ref to events/{eventId})
      ├── userId: String (ref to users/{uid})
      ├── status: String (PENDING | ACCEPTED | DECLINED | CREATOR)
      ├── role: String (CREATOR | ATTENDEE)
      ├── invitedAt: String (ISO-8601)
      └── respondedAt: String? (ISO-8601)
```

**Statuses:**
- `PENDING` - Invited but not yet responded
- `ACCEPTED` - Accepted invitation
- `DECLINED` - Declined invitation
- `CREATOR` - Created the event

**Roles:**
- `CREATOR` - Event creator/host
- `ATTENDEE` - Regular attendee

**Example:**
```json
{
  "eventId": "event123",
  "userId": "user456",
  "status": "ACCEPTED",
  "role": "ATTENDEE",
  "invitedAt": "2024-02-01T10:00:00",
  "respondedAt": "2024-02-02T14:30:00"
}
```

---

### 4. `friendships` Collection

Stores friendship relationships and friend requests between users.

```
friendships/
  └── {friendshipId} (document ID)
      ├── user1Id: String (ref to users/{uid})
      ├── user2Id: String (ref to users/{uid})
      ├── status: String (PENDING | ACCEPTED | DECLINED)
      ├── requestedBy: String (ref to users/{uid})
      ├── requestedAt: String? (ISO-8601)
      └── respondedAt: String? (ISO-8601)
```

**Statuses:**
- `PENDING` - Friend request sent, waiting for response
- `ACCEPTED` - Friend request accepted
- `DECLINED` - Friend request declined

**Example:**
```json
{
  "user1Id": "user123",
  "user2Id": "user456",
  "status": "ACCEPTED",
  "requestedBy": "user123",
  "requestedAt": "2024-01-10T08:00:00",
  "respondedAt": "2024-01-11T10:30:00"
}
```

---

### 5. `event_images` Collection

Stores metadata for images associated with events. The actual images are stored in Cloudinary.

```
event_images/
  └── {imageId} (document ID)
      ├── path: String (Cloudinary URL)
      ├── eventId: String (ref to events/{eventId})
      └── createdAt: String (ISO-8601)
```

**Example:**
```json
{
  "path": "https://res.cloudinary.com/.../event_photo.jpg",
  "eventId": "event123",
  "createdAt": "2024-02-05T16:20:00"
}
```

## License

Project developed as part of PROG2007 - Mobile Programming at NTNU.
