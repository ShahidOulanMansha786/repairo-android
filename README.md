---

# Repairo — Android App

Repairo Android app is built for two roles — Car Owner and Shop Owner. Car owners post repair leads, track quotes, chat with shops, and make payments. Shop owners browse nearby leads, submit quotes, and manage their jobs.

---

## Tech Stack

- Kotlin, Jetpack Compose
- Retrofit + OkHttp (REST API)
- Firebase FCM (push notifications)
- Firebase Firestore (real-time chat)
- Firebase Storage (chat images)
- STOMP over SockJS (real-time quotes via WebSocket)
- EncryptedSharedPreferences (secure local storage)
- Coil (image loading)

---

## Roles

- CAR_OWNER — posts leads, views quotes, makes payments, chats with shops
- SHOP_OWNER — views nearby leads, submits quotes, manages job progress, buys credits

---

## System Architecture

```mermaid
graph LR
    Android[Android App]
    Backend[Spring Boot Backend]
    FCM[Firebase FCM]
    Firestore[Firebase Firestore]
    S3[AWS S3]

    Android -->|REST API| Backend
    Android -->|WebSocket STOMP| Backend
    Backend -->|notifications| FCM
    FCM -->|deliver| Android
    Android -->|chat| Firestore
    Android -->|file upload| S3
```
---

## App Startup Flow

```mermaid
flowchart TD
    A[App Launch] --> B[SplashScreen]
    B --> C{JWT token exists?}
    C -->|No| D[RoleSelectionScreen]
    C -->|Yes| E{Check role}
    E -->|CAR_OWNER| F[HomeScreen]
    E -->|SHOP_OWNER| G[ShopHomeScreen]
    E -->|Token expired| H[Refresh token]
    H -->|Success| E
    H -->|Fail| D
    F --> I{isBlocked?}
    G --> I
    I -->|Yes| J[BlockedScreen]
```

---

## Car Owner Auth Flow

```mermaid
sequenceDiagram
    participant CO as Car Owner
    participant App
    participant BE as Backend
    participant Gmail

    CO->>App: Enter email, name, phone
    App->>BE: POST /auth/signup
    BE->>Gmail: Send OTP

    CO->>App: Enter OTP
    App->>BE: POST /auth/verify-otp
    BE->>App: JWT + Refresh Token
    App->>App: Save tokens in EncryptedSharedPreferences

    App->>BE: POST /users/fcm-token
    BE->>App: FCM token saved
```

---

## Lead Posting Flow

```mermaid
sequenceDiagram
    participant CO as Car Owner
    participant App
    participant BE as Backend
    participant S3

    CO->>App: CarDetailsScreen (make, model, year)
    CO->>App: IssueDescriptionScreen (title, description, photos)
    App->>BE: POST /media/presigned-url
    BE->>App: S3 presigned URL
    App->>S3: Upload images directly

    CO->>App: LeadLocationScreen (pick location on map)
    App->>BE: POST /leads (all details + location + image URLs)
    BE->>App: Lead created
    App->>App: LeadSuccessScreen
```

---

## Quote and Payment Flow

```mermaid
sequenceDiagram
    participant CO as Car Owner
    participant App
    participant BE as Backend
    participant WS as WebSocket

    App->>WS: Subscribe /topic/leads/{leadId}/quotes
    BE-->>WS: New quote event (real-time)
    WS-->>App: Quote received

    CO->>App: QuotesScreen — view all quotes
    CO->>App: Select quote → accept
    App->>BE: POST /leads/{leadId}/quotes/{quoteId}/accept

    CO->>App: PaymentConfirmScreen
    App->>BE: POST /api/payments/initiate
    BE->>App: Mock payment URL

    App->>App: MockCheckoutScreen
    App->>BE: GET /api/payments/{paymentId}/mock-pay
    BE->>App: escrowStatus = FUNDS_RECEIVED
    App->>App: PaymentSuccessScreen

    CO->>App: EscrowStatusScreen
    CO->>App: Release payment
    App->>BE: POST /api/payments/{paymentId}/release-immediately
    BE->>App: escrowStatus = RELEASED_TO_SHOP
```

---

## Shop Owner Auth Flow

```mermaid
sequenceDiagram
    participant SO as Shop Owner
    participant App
    participant BE as Backend

    SO->>App: ShopOwnerDetailsScreen (name, phone)
    SO->>App: ShopDetailsScreen (shop name, address, description)
    SO->>App: ShopDocumentsScreen (CNIC, business doc)
    App->>BE: POST /shops/documents
    BE->>App: Documents uploaded

    SO->>App: ShopOtpScreen
    App->>BE: POST /auth/shop/verify-otp
    BE->>App: JWT + Refresh Token
    App->>App: Navigate to PendingApprovalScreen

    Note over App,BE: Admin approves shop
    BE-->>App: FCM — SHOP_APPROVED
    App->>App: Navigate to ShopHomeScreen
```

---

## Shop Lead and Quote Flow

```mermaid
sequenceDiagram
    participant SO as Shop Owner
    participant App
    participant BE as Backend
    participant FCM

    App->>BE: GET /repair-shop/leads/nearby (lat, lng, radius)
    BE->>App: Nearby open leads list

    SO->>App: ShopLeadDetailScreen
    SO->>App: SubmitQuoteScreen (price, message)
    App->>BE: POST /quotes
    BE->>FCM: Notify car owner

    Note over App,BE: Car owner accepts quote

    BE-->>App: FCM — quote accepted
    App->>App: Navigate to ShopHomeScreen

    SO->>App: ShopAcceptedLeadDetailScreen
    Note over App: Customer contact info visible here

    SO->>App: Mark work done
    App->>BE: POST /leads/{leadId}/mark-work-done
    BE->>App: Job progress updated
```

---

## FCM Notification Handling

```mermaid
flowchart TD
    A[FCM Notification received] --> B{Notification type}
    B -->|SHOP_APPROVED| C[Navigate to ShopHomeScreen]
    B -->|SHOP_REJECTED| D[Navigate to RejectedScreen]
    B -->|NEW_LEAD| E[Navigate to ShopHomeScreen]
    B -->|QUOTE_ACCEPTED| F[Navigate to ShopHomeScreen]
    B -->|NEW_MESSAGE| G[Navigate to chat/channelId]
    B -->|ACCOUNT_BLOCKED| H[Set AppState.isBlocked = true]
    H --> I[Immediately redirect to BlockedScreen]
    B -->|ACCOUNT_UNBLOCKED| J[Navigate to RoleSelectionScreen]
```

---

## Block/Unblock Flow

```mermaid
sequenceDiagram
    participant BE as Backend
    participant FCM
    participant App

    BE->>FCM: Send ACCOUNT_BLOCKED notification
    FCM->>App: CarRepairMessagingService receives it
    App->>App: AppState.isBlocked = true

    Note over App: AppNavigation observes isBlocked StateFlow
    App->>App: Immediately redirect to BlockedScreen
    Note over App: Works from any screen in real-time

    BE->>FCM: Send ACCOUNT_UNBLOCKED notification
    FCM->>App: CarRepairMessagingService receives it
    App->>App: AppState.shouldNavigateToRoleSelection = true
    App->>App: Navigate to RoleSelectionScreen
```

---

## Chat Flow

```mermaid
sequenceDiagram
    participant CO as Car Owner
    participant App
    participant BE as Backend
    participant Firestore

    CO->>App: Open chat from LeadDetailScreen
    App->>BE: GET /leads/{leadId}/chat-channel
    BE->>App: channelId

    App->>Firestore: Listen to messages (getMessagesFlow)
    CO->>App: Type message
    App->>Firestore: sendTextMessage()

    CO->>App: Send image
    App->>Firestore: sendImageMessage() via Firebase Storage

    App->>Firestore: markMessagesAsRead()
    App->>App: ChannelListScreen shows all active chats
```

---

## Prerequisites

- Android Studio Hedgehog or newer
- Java 17
- `google-services.json` from Firebase project
- Running Repairo Spring Boot backend

---

## Environment Setup

In `RetrofitClient.kt` base URL set karo:

```kotlin
// Emulator ke liye
private const val BASE_URL = "http://10.0.2.2:8080/"

// Physical device ke liye apna machine ka local IP use karo
private const val BASE_URL = "http://192.168.x.x:8080/"
```

---

## Key Decisions

- Firebase Firestore used for chat instead of third-party SDK (Stream Chat had integration issues)
- FCM token saved after login, also cached in EncryptedSharedPreferences if user was not logged in at token refresh time
- BlockCheckInterceptor handles 403 ACCOUNT_BLOCKED globally across all API calls
- AppState uses MutableStateFlow so block/unblock works from any screen instantly
- Chat navigation handled via LaunchedEffect and resetChannelId() to avoid re-triggering
- Bottom navigation hidden on detail, chat, and payment screens
- S3 upload done directly from app using presigned URLs — backend never handles file bytes
- STOMP WebSocket used only for real-time quote delivery on lead detail screen

---
