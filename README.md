# CreatorVerse

A modern, full-stack social platform bridging the gap between content consumers and creators.

## Overview
CreatorVerse is a highly responsive web application designed to connect users through immersive content experiences and deep social interactions. The platform currently supports core social networking features including content feeds, interactive content cards, dynamic user profiles, and robust creator functionalities. 

## The Problem
Many social platforms heavily fragment the user base between passive "consumers" and active "creators," creating a disconnected social graph. Ordinary users often feel like second-class citizens, while creators struggle with disjointed monetization tools and analytics scattered across third-party apps. CreatorVerse aims to unify this ecosystem by offering a universal social engine paired with specialized creator extensions, bringing the community closer to the creators they love.

## Product Vision
CreatorVerse is evolving toward a **User-Centric Social Model**. The foundational social identity is the `User`. The social graph (following, likes, comments) is universal—everyone participates. Specialized creator functionalities (analytics, brand matching, monetization dashboards) operate as seamless extensions on top of the fundamental user profile, ensuring ordinary users are fully empowered social participants while providing creators the tools they need to thrive.

## Core Features
### Authentication
- JWT-based authentication
- Secure login and registration flows
- Protected routes using Bearer tokens
- Integrated frontend `AuthContext`
- Complete session management with automatic credential injection (including the `userId`)

### Content
- Algorithmic feed functionality
- Infinite scrolling/discovery content feeds
- Public content and detailed viewing pages
- Creator-specific content grids
- Secure media uploads powered by Cloudinary (currently restricted to creators)

### Social
- **Likes**: Authenticated liking/unliking with robust server state synchronization.
- **Comments**: Seamless commenting system with text handling and public viewing.
- **Followers/Following**: Dynamic user-to-user following capabilities.
- **Contextual Modals**: Clean, scroll-locked modals for discovering followers and following lists directly from profiles without harsh page navigations.

### Profile System
- Public Creator Profiles featuring niche and engagement metrics.
- Universal routing aliases preparing for the transition to unified `/user/:id` profiles.
- Protected edit interfaces and account settings.

## Current Development Status

CreatorVerse is actively under development. Core authentication, content, likes, comments, follows, and contextual follower/following interactions are fully implemented. The universal user-profile architecture and future creator ecosystem features remain in active progress.

| Phase | Feature | Status |
| :--- | :--- | :--- |
| **Phase 1-3** | Auth, Media Uploads, Content Feeds | ✅ Completed |
| **Phase 4.1** | Social Architecture Setup | ✅ Completed |
| **Phase 4.2** | Like System | ✅ Completed |
| **Phase 4.3** | Comment System | ✅ Completed |
| **Phase 4.4.1** | Follow Button Engine | ✅ Completed |
| **Phase 4.4.2** | Followers and Following Modals | ✅ Completed |
| **Phase 4.4.3** | Universal User Social Profiles | 🚧 Planned |

## Architecture
CreatorVerse utilizes a layered full-stack architecture separated cleanly between a Spring Boot REST API and a React Single Page Application (SPA).

* **Backend**: Spring Boot 3, Java 17, Spring Security
* **Frontend**: React 18, Vite, React Router DOM, Context API
* **Database**: PostgreSQL with Hibernate/JPA ORM
* **Media Storage**: Cloudinary integration
* **Authentication**: Stateless JWT access and refresh tokens

## Backend Structure
The backend strictly adheres to domain-driven packages:
- `com.creatorverse.auth`: Security, JWT filters, authentication controllers.
- `com.creatorverse.user`: Core `User` entity, user repositories, and role management.
- `com.creatorverse.creator`: Optional `CreatorProfile` extensions, monetization, and brand DTOs.
- `com.creatorverse.content`: Post/Reel entities, cloud storage services, and algorithmic fetching.
- `com.creatorverse.social`: Like, Comment, and Follow relationships mapping directly to the `User` graph.

## Frontend Structure
The frontend architecture emphasizes reusability and centralized state:
- `/src/components`: UI primitives categorized by domain (`/social`, `/creator`, `/common`).
- `/src/pages`: Top-level route components (`CreatorProfilePage`, `FeedPage`, `LoginPage`).
- `/src/context`: Centralized global state management (e.g., `AuthContext`).
- `/src/services`: Singleton API client (`api.js`) configuring base URLs and intercepting JWT tokens.

## Social Engine
The social engine is entirely user-centric. `Follows`, `Likes`, and `Comments` map via `@ManyToOne` relationships directly to the primary `users` table. This allows highly performant graph queries and ensures that the platform can scale its social features universally to both consumers and creators without complex polymorphic database associations.

## Authentication & Security
CreatorVerse employs a stateless authentication mechanism. Upon successful login, the server issues an Access JWT (`HS256` signed) and a securely stored Refresh Token. The frontend's `api.js` client automatically intercepts outgoing REST requests and injects the Bearer token into the `Authorization` header. Passwords are securely hashed via BCrypt before reaching the database.

## Data Model (Conceptual)
* **User**: The fundamental identity (Username, Display Name, Role).
* **CreatorProfile**: A `1:1` extension of User holding business/metrics (Niche, Bio, Engagement).
* **Content**: The core media asset owned by a `User`.
* **ContentLike**: A highly unique junction between `User` and `Content`.
* **Comment**: A timestamped message linking a `User` to `Content`.
* **Follow**: A directed edge linking a `follower` (User) to a `following` (User).

## API Overview
*Protected endpoints require a valid Bearer JWT.*

| Method | Endpoint | Description | Protected |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/login` | Authenticate and retrieve JWT | ❌ |
| `GET` | `/api/content/feed` | Retrieve the public discovery feed | ❌ |
| `POST` | `/api/content` | Upload new media content | ✅ (Creator) |
| `GET` | `/api/creators/profile/public/{id}`| Fetch public creator metadata | ❌ |
| `POST` | `/api/social/follow/{userId}` | Follow or unfollow a user | ✅ |
| `GET` | `/api/social/followers/{userId}` | Retrieve list of followers | ❌ |
| `POST` | `/api/social/content/{contentId}/like`| Toggle like on content | ✅ |
| `POST` | `/api/social/content/{contentId}/comment`| Submit a comment | ✅ |

## Local Development
### Requirements
- Java 17+
- Node.js 18+
- PostgreSQL 14+

### Environment Variables
For the backend to boot, supply the following variables locally (e.g. in your IDE or shell):
```
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
JWT_ACCESS_EXPIRATION
JWT_REFRESH_EXPIRATION
CLOUDINARY_CLOUD_NAME
CLOUDINARY_API_KEY
CLOUDINARY_API_SECRET
```

### Running the Backend
```bash
cd backend
mvn spring-boot:run
```
The server runs on `http://localhost:8080`.

### Running the Frontend
```bash
cd frontend
npm install
npm run dev
```
The Vite development server runs on `http://localhost:5173`.

## Roadmap
CreatorVerse's next major evolution is **Phase 4.4.3: Universal User Social Profiles**. 
This phase will decouple public social presences from the `CreatorProfile` entity. By migrating bios and follower counts directly to the base `User` entity, standard users will achieve first-class social capabilities. Future milestones will include unified `/user/:id` routing, democratized content creation (removing strict role boundaries for uploads), and introducing the Creator Ecosystem Dashboard for monetization.

## Design Philosophy
CreatorVerse employs a **Neo-Brutalist / Editorial** design system:
- High contrast, deep blacks, and warm cream backgrounds.
- Tactile, sharp-cornered UI borders.
- Highly legible, bold typography.
- Avoidance of excessive gradients, glassmorphism, or rounded bubbles in favor of a striking, structured aesthetic.

## Testing & Verification
The platform undergoes rigorous build verifications. The React frontend is bundled efficiently utilizing Vite's optimized Rollup pipeline, and the Spring Boot backend reliably passes Maven lifecycle compilations. Strict component boundaries ensure modular stability as the application scales.
