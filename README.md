# CreatorVerse

> A unified creator-first social platform and brand × creator collaboration marketplace.

CreatorVerse bridges the gap between creator audience building and brand campaign execution. Creators can publish visual work, grow their followers, and showcase engagement, while brands can launch campaigns, discover creators, evaluate applications, manage deliverables, and communicate inside dedicated collaboration workspaces.

[![Live Demo](https://img.shields.io/badge/Demo-creatorverse--beta.vercel.app-blue?style=flat-square)](https://creatorverse-beta.vercel.app/)
[![API Status](https://img.shields.io/badge/API-creatorverse--api--nk54.onrender.com-brightgreen?style=flat-square)](https://creatorverse-api-nk54.onrender.com/api/health)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-brightgreen?style=flat-square)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19.2-blue?style=flat-square)](https://react.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14%2B-blue?style=flat-square)](https://www.postgresql.org/)
[![Flyway](https://img.shields.io/badge/Flyway-9.22.3-red?style=flat-square)](https://flywaydb.org/)
[![Cloudinary](https://img.shields.io/badge/Cloudinary-Media%20Storage-blueviolet?style=flat-square)](https://cloudinary.com/)
[![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED?style=flat-square)](https://www.docker.com/)

---

## Project Links

- **Live Frontend**: [https://creatorverse-beta.vercel.app/](https://creatorverse-beta.vercel.app/)
- **Live Backend API**: [https://creatorverse-api-nk54.onrender.com](https://creatorverse-api-nk54.onrender.com/)
- **API Health Check**: [https://creatorverse-api-nk54.onrender.com/api/health](https://creatorverse-api-nk54.onrender.com/api/health)
- **Source Repository**: [https://github.com/Gameonixx/CreatorVerse](https://github.com/Gameonixx/CreatorVerse)

---

## Table of Contents

1. [Overview](#overview)
2. [Features](#features)
3. [End-to-End Workflow](#end-to-end-workflow)
4. [Technology Stack](#technology-stack)
5. [Architecture](#architecture)
6. [Database & Domain Model](#database--domain-model)
7. [Security & Engineering Decisions](#security--engineering-decisions)
8. [Screenshots / Demo](#screenshots--demo)
9. [Local Setup](#local-setup)
10. [Environment Variables](#environment-variables)
11. [API Overview](#api-overview)
12. [Deployment](#deployment)
13. [Current Scope & Roadmap](#current-scope--roadmap)
14. [Testing & Verification](#testing--verification)
15. [Limitations & Contribution](#limitations--contribution)

---

## Overview

### The Problem
The creator economy is frequently fragmented across disconnected tools:
- Creators build social followings on public platforms, but manage brand deals through scattered emails, direct messages, and spreadsheets.
- Brands struggle to identify creators with authentic engagement, verify audience metrics, and manage deliverable review cycles without lost context.
- Disconnecting creator social proof from brand partnership execution introduces operational friction, delayed approvals, and ambiguous commercial terms.

### The CreatorVerse Solution
CreatorVerse unifies social identity and marketplace collaboration on a shared platform:
- **Social Graph as Foundation**: The core `User` model powers public profiles, image publishing, discovery feeds, follows, likes, and comments.
- **Brand × Creator Marketplace**: Built on top of the social foundation, brands publish campaigns with specific niches and budgets, while creators submit pitches directly against campaigns.
- **Structured Deal Execution**: When a brand accepts an application, the platform automatically instantiates a binding `Collaboration` workspace equipped with deliverable tracking, revision feedback cycles, direct messaging, and notification events.

### Primary Users
- **Creators**: Publish visual content, grow a follower base, establish niche authority, browse open brand campaigns, submit pitches, track deliverables, and communicate directly with brand sponsors.
- **Brands**: Establish a company presence, launch targeted campaigns, filter creators by audience size and engagement metrics, review creator applications, inspect deliverable submissions, and manage partnerships to completion.

---

## Features

### Social Platform
- **User Authentication**: Secure registration and login issuing short-lived JWT access tokens and long-lived refresh tokens.
- **Public Profiles**: Universal `/user/:id` profile views displaying user bios, avatar images, follower/following counts, and published content grids.
- **Image Content Publishing**: Multi-part upload pipeline supporting image uploads up to 10 MB (12 MB maximum request size) hosted via Cloudinary.
- **Feed & Content Discovery**: Chronological paginated public feed (`/api/content/feed`), creator-specific content galleries, and private draft/published management.
- **Follow Graph**: Follow and unfollow users with server-synchronized follower and following counts, plus modal inspection of relationships.
- **Social Interactions**: Authenticated content likes with live toggle states and threaded comments with author deletion privileges.
- **In-App Social Notifications**: Automatic notifications triggered when users receive follows, likes, or comments.

### Creator Discovery & Analytics
- **Creator Discovery Hub**: Searchable directory (`/creators`) with real-time filters for niche categorization, keyword search, and follower ranges (`minFollowers`, `maxFollowers`).
- **Creator Profile Context**: Specialized creator extension (`CreatorProfile`) highlighting creator niche and engagement rating alongside social statistics.
- **Scheduled Engagement Analytics**: Automated hourly background job (`CreatorAnalyticsScheduler`) batch-processing published post counts, likes, and comments to compute engagement rates:
  $$\text{Engagement Rate} = \left(\frac{\text{Total Likes} + \text{Total Comments}}{\text{Total Published Posts} \times \max(1, \text{Followers})}\right) \times 100$$
- **Analytical Safeguards**: Calculates engagement metrics only for creators meeting threshold safeguards ($\ge 100$ followers, $\ge 5$ published posts, and $\ge 20$ interactions) to prevent skewed statistics on nascent profiles.

### Brand–Creator Marketplace
- **Brand Profiles**: Company identity extension (`BrandProfile`) capturing company name, description, industry, website, and logo.
- **Campaign Management**: Brands create, edit, pause, and close campaigns with defined niches, budgets, and application deadlines.
- **Commercial Term Protection**: Campaign budget, niche, and core terms become immutable once an active collaboration is underway.
- **Campaign Discovery**: Public marketplace (`/campaigns`) where creators filter open opportunities by niche and keywords.
- **Creator Applications**: Creators apply with personalized pitch messages; pending applications can be withdrawn by the creator.
- **Application Review & Deal Conversion**: Brands accept or reject applicants; accepting an application atomically instantiates a binding `Collaboration` in the `ACTIVE` state.

### Collaboration Execution Workspace
- **Dedicated Collaboration Workspace**: Contextual dashboard (`/collaborations/:id`) for brand and creator counterparts with live deal details.
- **Deliverable Management**: Brands create, edit, and delete deliverable requirements (e.g., photo post, story review) while in `PENDING` or `REJECTED` states.
- **Submission & Resubmission Cycle**: Creators submit deliverable URLs. Brands inspect the work and choose to approve or reject.
- **Mandatory Rejection Feedback**: If a deliverable is rejected, the brand must supply written feedback. The creator can revise the work and resubmit it against the same deliverable.
- **Brand-Controlled Completion**: Only the authorized brand owner can mark an active collaboration as `COMPLETED` once all obligations are fulfilled; either party can cancel if necessary.
- **Collaboration-Scoped Messaging**: Integrated chat stream scoped strictly to the collaboration deal, featuring message history, read timestamp tracking, and unread counters.
- **Transactional Notifications**: Event-driven notification engine alerting participants of application decisions, deliverable status changes, and incoming messages.

### Platform Engineering
- **Stateless JWT Security**: 15-minute access tokens paired with 7-day refresh tokens.
- **Refresh-Token Rotation**: Database-tracked refresh tokens hashed with SHA-256; previous tokens are revoked upon rotation to mitigate token theft.
- **Authorization & Ownership Verification**: Controller and service boundaries enforce resource ownership (e.g., only campaign owners can review applications; only collaboration participants can exchange messages).
- **API Rate Limiting**: Bucket4j token bucket rate limiting on authentication routes (`/api/auth/login`, `/api/auth/register`, and `/api/auth/refresh`), bounded to 10 requests per 60 seconds per client IP.
- **Versioned Migrations**: Flyway database migrations (`V1__init_schema.sql`, `V2__add_cascades.sql`) managing schema evolution with Hibernate schema validation (`ddl-auto: validate`).
- **Cloud Media Pipeline**: Direct Cloudinary integration with transactional synchronization hooks to clean up remote assets upon content deletion.

---

## End-to-End Workflow

```
[Brand] Creates Campaign (Niche, Budget, Deadline)
   │
   ▼
[Creator] Discovers Campaign & Submits Application (Pitch Message)
   │
   ▼
[Brand] Reviews Application
   ├── REJECTED ──► Application Closed
   └── ACCEPTED ──► Atomic Conversion to ACTIVE Collaboration
                       │
                       ▼
            Collaboration Workspace (/collaborations/:id)
                       ├── Deal-Specific Direct Messaging
                       │
                       ▼
            [Brand] Creates Deliverable Requirements (PENDING)
                       │
                       ▼
            [Creator] Submits Deliverable URL (SUBMITTED)
                       │
                       ▼
            [Brand] Reviews Deliverable Submission
                       ├── REJECTED (with Mandatory Feedback)
                       │      │
                       │      └──► [Creator] Revises & Resubmits URL (SUBMITTED)
                       │
                       └── APPROVED ──► Deliverable Satisfied
                                           │
                                           ▼
                                [Brand] Marks Collaboration COMPLETED
```

1. **Campaign Discovery**: The brand creates a campaign specifying commercial terms and deadlines. It appears on `/campaigns`.
2. **Application**: An interested creator applies with an introductory pitch.
3. **Review & Acceptance**: The brand reviews applicants in the Brand Dashboard. Accepting an applicant triggers an atomic transaction that creates an active `Collaboration` record.
4. **Deliverable Setup**: Inside the collaboration workspace, the brand outlines required deliverables.
5. **Submission**: The creator submits live URLs for each deliverable item.
6. **Review Loop**: The brand reviews submissions. Approvals advance the deal; rejections require descriptive feedback, returning the item to the creator for revision and resubmission.
7. **Messaging**: Both parties coordinate deliverable expectations and timeline updates inside the collaboration's integrated message feed.
8. **Completion**: When all deliverables are verified, the brand completes the collaboration.

---

## Technology Stack

### Backend
- **Language**: Java 17
- **Framework**: Spring Boot 3.2.4
- **Security**: Spring Security (Stateless JWT authentication, method security, BCrypt password hashing)
- **Persistence**: Spring Data JPA & Hibernate (`ddl-auto: validate`)
- **Database Migrations**: Flyway 9.22.3 (`flyway-core`, managed by Spring Boot starter parent)
- **Rate Limiting**: Bucket4j Core 8.9.0
- **JWT Provider**: Java JWT (`io.jsonwebtoken:jjwt` 0.11.5)
- **Media SDK**: Cloudinary Java SDK 1.36.0 (`cloudinary-http44`)
- **Monitoring**: Spring Boot Starter Actuator (health endpoints)
- **Build Tool**: Apache Maven 3.9+

### Frontend
- **Framework**: React 19 (`react` ^19.2.8, `react-dom` ^19.2.8)
- **Build Tool & Dev Server**: Vite 8 (`vite` ^8.2.0, `@vitejs/plugin-react` ^6.0.4)
- **Routing**: React Router DOM 7 (`react-router-dom` ^7.18.2)
- **Code Quality**: Oxlint (`oxlint` ^1.75.0)
- **Styling**: Vanilla CSS with custom design tokens (editorial / neo-brutalist aesthetic)
- **State Management**: React Context (`AuthContext`) and local state hooks

### Database & Storage
- **Primary Database**: PostgreSQL 14+ (hosted relational database)
- **Connection Pool**: HikariCP (configured pool sizes, timeouts, and lifetimes)
- **Media Storage & CDN**: Cloudinary (image asset hosting, thumbnail generation, and public ID tracking)

### Deployment & Tooling
- **Frontend Hosting**: Vercel (SPA deployment with single-page rewrite rules)
- **Backend Hosting**: Render (Linux container environment running Eclipse Temurin 17 JRE)
- **Containerization**: Multi-stage Docker build (`Dockerfile`)
- **Version Control**: Git & GitHub

---

## Architecture

CreatorVerse is architected as a **Modular Monolith**. The frontend Single Page Application communicates with the Spring Boot backend exclusively through RESTful JSON endpoints and multipart file uploads.

```mermaid
graph TD
    subgraph Client ["Frontend (React 19 + Vite)"]
        UI[Pages & Modals]
        AC[AuthContext]
        API[api.js Interceptor & Refresh Flow]
        UI --> AC
        UI --> API
    end

    subgraph Gateway ["Spring Boot Security & Filter Chain"]
        RLF[RateLimitingFilter (Bucket4j)]
        JWTF[JwtAuthenticationFilter]
        SEC[SecurityConfig Authorization Rules]
        EX[GlobalExceptionHandler]
        API --> RLF --> JWTF --> SEC
    end

    subgraph Domains ["Backend Domain Modules"]
        AUTH[Auth & User Module]
        SOC[Social & Feed Module]
        DISC[Creator Discovery & Analytics]
        MKT[Campaign & Marketplace Module]
        COL[Collaboration & Deliverable Module]
        MSG[Messaging Module]
        NOTIF[Notification Event Listener]
    end

    SEC --> AUTH
    SEC --> SOC
    SEC --> DISC
    SEC --> MKT
    SEC --> COL
    SEC --> MSG

    COL -.->|Publishes Event| NOTIF
    MSG -.->|Publishes Event| NOTIF
    SOC -.->|Publishes Event| NOTIF

    subgraph Infrastructure ["Persistence & Storage"]
        DB[(PostgreSQL Database)]
        MIG[Flyway Migrations]
        CLD[Cloudinary Media CDN]
    end

    AUTH --> DB
    SOC --> DB
    SOC --> CLD
    DISC --> DB
    MKT --> DB
    COL --> DB
    MSG --> DB
    NOTIF --> DB
    MIG --> DB
```

### Canonical User Identity Model
The root entity is `User`. A single registered account has:
- Universal social capabilities (followers, following, liking, commenting, content publishing).
- An optional `CreatorProfile` extension (niche, engagement rate).
- An optional `BrandProfile` extension (company name, description, industry, logo).

These roles are **not mutually exclusive**. The backend allows a user to maintain a creator profile and a brand profile under the same account. The frontend dynamically discovers which modes are active and surfaces creator or brand dashboard shortcuts accordingly.

### Asynchronous Event Architecture
State transitions in deliverables, applications, and messages publish Spring application events (`DeliverableStatusChangedEvent`, `ApplicationStatusChangedEvent`, `MessageCreatedEvent`, `SocialEvent`). The `NotificationEventListener` captures these events using `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` with `Propagation.REQUIRES_NEW`, ensuring that notification dispatch runs reliably without interfering with the parent transaction.

---

## Database & Domain Model

The relational schema is managed by Flyway migrations (`V1__init_schema.sql` and `V2__add_cascades.sql`) and validated by Hibernate on startup.

| Entity | Table Name | Purpose & Key Relationships |
| :--- | :--- | :--- |
| `User` | `users` | Primary identity; stores username, email, password hash, avatar URL, bio, and denormalized follower counts. |
| `UserProfile` | `user_profiles` | `1:1` with `users`; optional extended profile details (name, location, website). |
| `CreatorProfile` | `creator_profiles` | `1:1` with `users`; creator attributes including niche and calculated engagement rate. |
| `BrandProfile` | `brand_profiles` | `1:1` with `users`; brand attributes including company name, industry, and logo. |
| `Content` | `content` | Image posts authored by a `User`; tracks Cloudinary public IDs, caption, status (`DRAFT`, `PUBLISHED`), and visibility. |
| `ContentLike` | `content_likes` | Junction between `users` and `content`; unique constraint on `(user_id, content_id)`. |
| `Comment` | `comments` | User comments on content items; includes creation timestamps. |
| `Follow` | `follows` | Directed graph edge from `follower_id` to `following_id`; unique constraint prevents duplicate follows. |
| `Campaign` | `campaigns` | Brand marketing opportunities authored by a `User`; tracks title, description, niche, budget, status, and deadline. |
| `CampaignApplication` | `campaign_applications` | Pitch submitted by a creator for a campaign; unique constraint on `(campaign_id, creator_user_id)`. |
| `Collaboration` | `collaborations` | Binding deal created upon application acceptance; unique constraint on `originating_application_id`. |
| `Deliverable` | `deliverables` | Task item under a collaboration; tracks title, description, submission URL, status (`PENDING`, `SUBMITTED`, `APPROVED`, `REJECTED`), and feedback. |
| `Conversation` | `conversations` | `1:1` messaging channel between collaboration participants; unique constraint on `collaboration_id`. |
| `Message` | `messages` | Chat entries in a conversation; tracks sender, message text (up to 5,000 characters), and read timestamp. |
| `Notification` | `notifications` | User notifications for social, deal, message, and application events. |
| `RefreshToken` | `refresh_tokens` | Stores SHA-256 hashed refresh tokens with expiration and revocation flags. |

---

## Security & Engineering Decisions

- **Stateless Session Management**: Authentication relies entirely on signed JWTs; the backend preserves zero HTTP session state in memory, allowing container restarts without session loss.
- **Refresh Token Rotation & Hashing**: Refresh tokens are stored in the database exclusively as SHA-256 hashes (`tokenHash`). When a refresh token is exchanged, it is immediately revoked, and a newly generated token pair is issued.
- **Client Token Interceptor**: The frontend API client (`api.js`) intercepts outgoing requests to inject the `Authorization: Bearer <token>` header. Upon receiving a 401 or expired token response, it deduplicates concurrent requests, performs a refresh call, updates local storage, and replays the original request.
- **Rate Limiting**: To mitigate brute-force and credential-stuffing attempts, Bucket4j enforces an IP-based token bucket on `/api/auth/login`, `/api/auth/register`, and `/api/auth/refresh`. Client IPs are resolved through `X-Forwarded-For` to support proxied cloud infrastructure.
- **Granular Ownership Checks**: Sensitive operations (updating deliverables, reviewing applications, completing collaborations, viewing draft content) verify that the authenticated principal matches the resource owner.
- **Locking Commercial Campaign Terms**: Once a campaign has an active collaboration, mutations to `title`, `description`, `niche`, `budget`, and `applicationDeadline` are rejected to protect commercial terms.
- **Transactional Consistency**: Accepting an application atomically inserts the active collaboration record. Unique constraints on `originating_application_id` protect against race conditions and duplicate deals.
- **Bounded Batch Processing**: The hourly engagement rate calculator queries database profiles in fixed batches of 100 using `EntityManager.flush()` and `clear()`, avoiding memory spikes on larger datasets.
- **Remote Storage Cleanup Synchronization**: When a content item is deleted from the database, Spring's `TransactionSynchronizationManager` triggers Cloudinary asset destruction only *after* the database commit succeeds.

---

## Screenshots / Demo

> Visual assets and interface walkthrough screenshots can be stored in the `docs/screenshots/` directory.

```
┌────────────────────────────────────────────────────────────────────────┐
│                                                                        │
│                       CreatorVerse Interface Walkthrough                │
│                                                                        │
│   • Discovery Feed & Social Content Cards                              │
│   • Creator Discovery Hub (Niche & Follower Filtering)                 │
│   • Brand Campaign Creation & Public Discovery                         │
│   • Creator Application & Pitch Modal                                  │
│   • Brand Application Review & Instant Deal Acceptance                 │
│   • Collaboration Workspace (Deliverable Review & Resubmission Loop)   │
│   • Direct Collaboration Messaging & Notifications Dropdown            │
│                                                                        │
└────────────────────────────────────────────────────────────────────────┘
```

You can experience the interactive interface directly on the deployed [Live Demo](https://creatorverse-beta.vercel.app/).

---

## Local Setup

### Prerequisites
- **Java**: JDK 17 or higher
- **Maven**: Apache Maven 3.9+ (the repository does not bundle a `mvnw` wrapper)
- **Node.js**: Node 18+ or 20+
- **npm**: npm 9+
- **PostgreSQL**: PostgreSQL 14 or higher running locally or accessible via network
- **Cloudinary Account**: Cloud name, API key, and API secret for media handling

### 1. Clone the Repository
```bash
git clone https://github.com/Gameonixx/CreatorVerse.git
cd CreatorVerse
```

### 2. Configure the PostgreSQL Database
Connect to PostgreSQL and create a database:
```sql
CREATE DATABASE creatorverse;
```

### 3. Backend Setup

> **Important**: Spring Boot does not automatically read a local `.env` file unless explicitly configured with a loader. You should provide environment variables through your shell, IDE run configuration, or environment exports. Do not commit `.env` files containing secrets.

#### Linux / macOS
```bash
export PORT=8080
export DB_URL=jdbc:postgresql://localhost:5432/creatorverse
export DB_USERNAME=postgres
export DB_PASSWORD=your_postgres_password
export JWT_SECRET=your_base64_encoded_secret_minimum_256_bits_length
export JWT_ACCESS_EXPIRATION=900000
export JWT_REFRESH_EXPIRATION=604800000
export CORS_ALLOWED_ORIGINS=http://localhost:5173,http://127.0.0.1:5173
export CLOUDINARY_CLOUD_NAME=your_cloudinary_cloud_name
export CLOUDINARY_API_KEY=your_cloudinary_api_key
export CLOUDINARY_API_SECRET=your_cloudinary_api_secret
export ADMIN_USERNAME=admin
export ADMIN_PASSWORD=admin_secure_password
```

#### Windows (PowerShell)
```powershell
$env:PORT="8080"
$env:DB_URL="jdbc:postgresql://localhost:5432/creatorverse"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your_postgres_password"
$env:JWT_SECRET="your_base64_encoded_secret_minimum_256_bits_length"
$env:JWT_ACCESS_EXPIRATION="900000"
$env:JWT_REFRESH_EXPIRATION="604800000"
$env:CORS_ALLOWED_ORIGINS="http://localhost:5173,http://127.0.0.1:5173"
$env:CLOUDINARY_CLOUD_NAME="your_cloudinary_cloud_name"
$env:CLOUDINARY_API_KEY="your_cloudinary_api_key"
$env:CLOUDINARY_API_SECRET="your_cloudinary_api_secret"
$env:ADMIN_USERNAME="admin"
$env:ADMIN_PASSWORD="admin_secure_password"
```

#### Start the Spring Boot Backend
```bash
cd backend
mvn spring-boot:run
```
On startup:
- Flyway automatically applies migrations from `src/main/resources/db/migration/`.
- Hibernate validates the resulting schema (`ddl-auto: validate`).
- `AdminBootstrap` verifies or creates the initial administrator user.
- The server starts listening on `http://localhost:8080`.
- Verify the server health:
  ```bash
  curl http://localhost:8080/api/health
  ```

### 4. Frontend Setup

In a separate terminal:
```bash
cd frontend
npm install
```

#### Local Development Mode
For local development, leave `VITE_API_URL` unset. Vite's development server (`vite.config.js`) automatically proxies requests starting with `/api` to `http://localhost:8080`:

```bash
npm run dev
```
The application will be accessible at `http://localhost:5173`.

---

## Environment Variables

### Backend Configuration (`backend/.env.example`)

| Variable | Purpose | Example / Format | Required / Default |
| :--- | :--- | :--- | :--- |
| `PORT` | HTTP port for the Spring Boot application | `8080` | Default: `8080` |
| `DB_URL` | JDBC URL for PostgreSQL connection | `jdbc:postgresql://localhost:5432/creatorverse` | Default: `jdbc:postgresql://localhost:5432/creatorverse` |
| `DB_USERNAME` | Database username | `postgres` | Default: `postgres` |
| `DB_PASSWORD` | Database password | `your_secret_db_password` | **Required** in production |
| `DB_POOL_MAX` | Maximum HikariCP connection pool size | `10` | Default: `10` |
| `DB_POOL_MIN` | Minimum HikariCP idle connections | `2` | Default: `2` |
| `JWT_SECRET` | Base64-encoded secret key for signing HMAC-SHA256 JWTs | `dGhpc2lzYXNhbXBsZXNlY3JldGtleWZvcmp3dHRlc3Rpbmc=` | **Required** |
| `JWT_ACCESS_EXPIRATION` | Access token lifespan in milliseconds | `900000` (15 minutes) | Default: `900000` |
| `JWT_REFRESH_EXPIRATION` | Refresh token lifespan in milliseconds | `604800000` (7 days) | Default: `604800000` |
| `CORS_ALLOWED_ORIGINS` | Comma-separated list of allowed frontend origins | `http://localhost:5173,https://your-app.vercel.app` | Default: `http://localhost:5173,http://127.0.0.1:5173` |
| `CLOUDINARY_CLOUD_NAME` | Cloudinary account cloud name | `your_cloud_name` | **Required** for image uploads |
| `CLOUDINARY_API_KEY` | Cloudinary API key | `123456789012345` | **Required** for image uploads |
| `CLOUDINARY_API_SECRET` | Cloudinary API secret | `your_cloudinary_api_secret` | **Required** for image uploads |
| `ADMIN_USERNAME` | Initial system administrator username | `admin` | **Required** (enforced by `AdminBootstrap`) |
| `ADMIN_PASSWORD` | Initial system administrator password | `secure_admin_password` | **Required** (enforced by `AdminBootstrap`) |
| `RATE_LIMIT_AUTH_CAPACITY`| Maximum tokens in the Bucket4j auth bucket | `10` | Default: `10` |
| `RATE_LIMIT_AUTH_TOKENS` | Tokens refilled in the Bucket4j auth bucket | `10` | Default: `10` |
| `RATE_LIMIT_AUTH_SECONDS`| Refill period in seconds for the Bucket4j auth bucket | `60` | Default: `60` |

### Frontend Configuration (`frontend/.env.example`)

| Variable | Purpose | Example / Format | Required / Default |
| :--- | :--- | :--- | :--- |
| `VITE_API_URL` | Base URL for the backend API | `https://creatorverse-api-nk54.onrender.com/api` | Optional locally (uses Vite proxy). **Required** in production builds. |

---

## API Overview

Protected endpoints require a valid Bearer token in the `Authorization` header:
`Authorization: Bearer <accessToken>`

### Authentication (`/api/auth`)
| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Register a new user (`username`, `email`, `displayName`, `password`, `role`) | No |
| `POST` | `/api/auth/login` | Authenticate with credentials; returns access token, refresh token, and user ID | No |
| `POST` | `/api/auth/refresh` | Exchange a valid refresh token for a new access and refresh token pair | No |
| `POST` | `/api/auth/logout` | Revoke a refresh token | No |

### Users & Profiles (`/api/users`, `/api/creators/profile`, `/api/brands/profile`)
| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/users/me` | Fetch currently authenticated user profile | Yes |
| `GET` | `/api/users/public/{id}` | Fetch public user profile (strips private email) | No |
| `PUT` | `/api/users/{id}` | Update account details (bio, display name, avatar) | Yes (Owner) |
| `GET` | `/api/creators/profile/public/{userId}` | Fetch public creator profile (niche, engagement rate) | No |
| `POST` | `/api/creators/profile` | Create or enable creator profile mode | Yes (Owner) |
| `GET` | `/api/brands/profile/public/{userId}` | Fetch public brand profile (company name, industry, logo) | No |
| `POST` | `/api/brands/profile` | Create or enable brand profile mode | Yes (Owner) |

### Content & Feed (`/api/content`)
| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/content/feed` | Paginated public discovery feed | No |
| `GET` | `/api/content/user/{userId}` | Paginated public content published by a specific user | No |
| `GET` | `/api/content/{id}` | Get content details (includes like/comment totals, user liked state) | No (Drafts: Owner) |
| `POST` | `/api/content` | Multipart upload (JSON `metadata` + file + `publishNow` flag) | Yes (Creator) |
| `POST` | `/api/content/{id}/publish` | Publish an existing draft content item | Yes (Owner) |
| `DELETE`| `/api/content/{id}` | Delete content and trigger Cloudinary media cleanup | Yes (Owner/Admin) |

### Social Graph & Interactions (`/api/social`)
| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/social/follow/{userId}` | Follow target user | Yes |
| `DELETE`| `/api/social/unfollow/{userId}` | Unfollow target user | Yes |
| `GET` | `/api/social/followers/{userId}` | List followers of target user | No |
| `GET` | `/api/social/following/{userId}` | List accounts followed by target user | No |
| `POST` | `/api/social/content/{contentId}/like` | Like a content item | Yes |
| `DELETE`| `/api/social/content/{contentId}/unlike`| Remove like from a content item | Yes |
| `POST` | `/api/social/content/{contentId}/comments` | Post a comment on a content item | Yes |
| `GET` | `/api/social/content/{contentId}/comments` | List comments for a content item | No |

### Creator Discovery (`/api/discovery/creators`)
| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/discovery/creators` | Discover creators with filters (`search`, `niche`, `minFollowers`, `maxFollowers`, pagination) | No |

### Campaigns & Applications (`/api/campaigns`)
| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/campaigns` | Discover open brand campaigns (supports `search`, `niche`, pagination) | No |
| `GET` | `/api/campaigns/{id}` | View detailed campaign terms | No |
| `POST` | `/api/campaigns` | Create a new campaign (title, description, niche, budget, deadline) | Yes (Brand) |
| `PUT` | `/api/campaigns/{id}` | Update campaign (locked if active collaborations exist) | Yes (Owner) |
| `POST` | `/api/campaigns/{id}/applications` | Submit a creator pitch application | Yes (Creator) |
| `GET` | `/api/campaigns/{id}/applications` | View all applicants for a brand's campaign | Yes (Owner) |
| `PUT` | `/api/campaigns/{campaignId}/applications/{applicationId}/status` | Accept or reject application (acceptance instantiates a collaboration) | Yes (Owner) |
| `DELETE`| `/api/campaigns/applications/{applicationId}` | Withdraw a pending application | Yes (Owner) |

### Collaborations & Deliverables (`/api/collaborations`)
| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/collaborations/mine` | List active, completed, or cancelled collaborations for current user | Yes |
| `GET` | `/api/collaborations/{id}` | View detailed collaboration deal terms and counterpart profile | Yes (Participant) |
| `PUT` | `/api/collaborations/{id}/status` | Complete (`COMPLETED` by brand) or cancel (`CANCELLED` by either) | Yes (Participant) |
| `GET` | `/api/collaborations/{id}/deliverables` | List deliverables under this collaboration | Yes (Participant) |
| `POST` | `/api/collaborations/{id}/deliverables` | Create a deliverable item | Yes (Brand) |
| `PUT` | `/api/collaborations/{id}/deliverables/{deliverableId}/submission` | Submit deliverable URL | Yes (Creator) |
| `PUT` | `/api/collaborations/{id}/deliverables/{deliverableId}/review` | Approve or reject deliverable (rejection requires feedback) | Yes (Brand) |

### Messaging & Notifications (`/api/collaborations/{id}`, `/api/notifications`)
| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/collaborations/{id}/messages` | Paginated message history for a collaboration | Yes (Participant) |
| `POST` | `/api/collaborations/{id}/messages` | Send message within a collaboration | Yes (Participant) |
| `PUT` | `/api/collaborations/{id}/messages/read` | Mark collaboration messages as read | Yes (Participant) |
| `GET` | `/api/notifications` | Paginated notifications list | Yes |
| `GET` | `/api/notifications/unread-count` | Count of unread notifications | Yes |
| `POST` | `/api/notifications/{id}/read` | Mark individual notification as read | Yes |
| `POST` | `/api/notifications/read-all` | Mark all user notifications as read | Yes |

### Health & Monitoring (`/api/health`)
| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/health` | Public status endpoint returning `{"status": "UP", "message": "CreatorVerse API is running"}` | No |

---

## Deployment

The production environment is split across dedicated cloud providers:

```
┌───────────────────────────────────────┐
│           Vercel Frontend             │
│   https://creatorverse-beta.vercel.app │
└──────────────────┬────────────────────┘
                   │
                   │ HTTPS REST / Multipart
                   ▼
┌───────────────────────────────────────────┐
│            Render Backend API             │
│   https://creatorverse-api-nk54.onrender.com │
└─────────────┬───────────────────────────┬─┘
              │                           │
              ▼                           ▼
┌───────────────────────────┐   ┌───────────────────────────┐
│     Hosted PostgreSQL     │   │         Cloudinary        │
│   Relational Persistence  │   │     Media Storage & CDN   │
└───────────────────────────┘   └───────────────────────────┘
```

- **Frontend (Vercel)**:
  - Deployed as a Single Page Application.
  - `vercel.json` rewrite configuration redirects all incoming paths to `/index.html` to support client-side React Router navigation.
  - Built with the environment variable:
    ```
    VITE_API_URL=https://creatorverse-api-nk54.onrender.com/api
    ```
- **Backend (Render)**:
  - Built via a multi-stage `Dockerfile`:
    - Stage 1: `maven:3.9.6-eclipse-temurin-17-focal` downloads dependencies and builds the fat JAR.
    - Stage 2: `eclipse-temurin:17-jre-focal` runtime container executing the application.
  - Tuned with low-memory JVM flags suited for containerized runtimes:
    `-Xmx200m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC -Xss512k`
  - Connected to the hosted PostgreSQL instance and Cloudinary storage account.
  - Automatically executes Flyway migrations on container startup.
  - CORS is restricted via `CORS_ALLOWED_ORIGINS=https://creatorverse-beta.vercel.app`.
- **Free-Tier Operational Notice**:
  - The live backend is hosted on Render's free tier. After 15 minutes of inactivity, the container spins down.
  - If the application has been idle, the initial request may experience a **30 to 60-second cold-start delay** while the container initializes. Subsequent requests respond normally.

---

## Current Scope & Roadmap

### Current V1 Capabilities
- Full social graph (profiles, follows, likes, comments, chronological discovery feed).
- Image uploads up to 10 MB per file (12 MB request limit) hosted via Cloudinary.
- Dual-role profile architecture (users can maintain both creator and brand modes).
- Creator discovery directory with real-time filters and scheduled engagement rate analytics.
- Brand campaign creation, discovery, and creator application workflow.
- Atomic deal creation upon application acceptance.
- Deliverable submission, revision, and mandatory rejection feedback cycles.
- Deal-scoped REST messaging with read timestamp tracking and transactional notifications.
- API rate limiting, refresh token rotation, and Flyway schema migrations.

### Planned Capabilities (Future Roadmap)
- **Video Content**: Support for video uploads, chunked media streaming, and video transcoding pipelines.
- **Escrow & Payment Processing**: Integration with payment processors (e.g., Stripe Connect) for automated milestone funding and escrow payouts.
- **Contract Workflows**: Automated generation and digital signature tracking for commercial agreement terms.
- **Real-Time WebSockets**: Replacing HTTP polling with WebSocket or Server-Sent Events (SSE) connections for messaging and notifications.
- **Deeper Audience Analytics**: Demographic breakdown, impressions tracking, click-through rates, and historical campaign ROI reports.

---

## Testing & Verification

### Backend Automated Tests
The backend test suite includes **185 automated tests across 30 test classes** verifying controllers, services, repositories, and scheduling routines:
- **Authentication**: JWT generation, parsing, validation, and refresh token rotation with SHA-256 hash checks (`AuthServiceTest`, `RefreshTokenServiceTest`, `JwtUtilTest`, `AuthControllerTest`).
- **User & Social**: Follow relationships, like counters, and comment lifecycles (`UserServiceTest`, `LikeServiceTest`, `FollowServiceTest`, `CommentServiceTest`).
- **Creator Discovery**: Filter specifications for niche, follower ranges, and keywords (`CreatorDiscoveryServiceTest`).
- **Campaigns & Applications**: Campaign commercial protections, application submissions, and withdrawal rules (`CampaignServiceTest`, `CampaignApplicationServiceTest`, `CampaignApplicationServiceIntegrationTest`).
- **Collaborations & Deliverables**: Status transitions, deliverable review cycles, feedback validation, and brand completion permissions (`CollaborationServiceTest`, `DeliverableServiceTest`).
- **Analytics**: Hourly engagement rate math and eligibility safeguards (`CreatorAnalyticsServiceTest`, `CreatorAnalyticsSchedulerTest`).

To execute the backend test suite:
```bash
cd backend
mvn test
```

### Frontend Code Quality & Build Checks
The frontend leverages Oxlint for static linting and Vite for production bundle verification:
```bash
cd frontend

# Run linter
npm run lint

# Validate production build
npm run build
```

---

## Limitations & Contribution

### Known Limitations
- **Cold Starts**: Render free-tier container sleep causes latency on initial requests after inactivity.
- **Media Formats**: The current implementation handles image uploads; video media streaming is not yet supported.
- **Messaging Architecture**: Chat and notification refreshes are powered by HTTP requests and polling rather than persistent WebSockets.
- **Payment Handling**: Campaign budgets are currently recorded as commercial terms; financial escrow and direct payments are not processed in-app.

### Contribution Workflow
1. **Fork** the repository on GitHub.
2. **Create a branch** for your feature or fix:
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **Implement your changes**, ensuring clean documentation and adherence to existing conventions.
4. **Run tests and build verifications**:
   ```bash
   # Backend
   cd backend && mvn test

   # Frontend
   cd ../frontend && npm run lint && npm run build
   ```
5. **Commit your changes** with descriptive commit messages:
   ```bash
   git commit -m "feat: add feature description"
   ```
6. **Push to your fork** and submit a **Pull Request** targeting `main`.

### License
This repository does not currently include an open-source license. All rights are reserved by the repository owner.

---

## Closing

Explore the live application at [https://creatorverse-beta.vercel.app/](https://creatorverse-beta.vercel.app/) or inspect the source code on [GitHub](https://github.com/Gameonixx/CreatorVerse).
