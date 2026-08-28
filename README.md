# CreatorVerse

A creator-first social and brand collaboration platform.

CreatorVerse is a three-sided platform connecting Creators, Brands, and Users.

## Current Architecture

The project is built as a **Modular Monolith**. 

### Technologies
- **Frontend**: React + Vite
- **Backend**: Java 17 + Spring Boot 3 + Maven
- **Database**: PostgreSQL

### Current Phase: PHASE 3 (Content System)
This repository currently contains the Phase 3 implementation, which establishes the core persistence layer, users, profiles, JWT authentication, and the foundational content system for media uploads via Cloudinary.

## How to Run

### Requirements
- Node.js 18+
- Java 17+
- Maven
- PostgreSQL

### Database Setup
Ensure PostgreSQL is running. Create a database named `creatorverse`. 

Set the following environment variables (or rely on the defaults):
- `DB_URL` (default: `jdbc:postgresql://localhost:5432/creatorverse`)
- `DB_USERNAME` (default: `postgres`)
- `DB_PASSWORD` (default: `postgres`)

### Authentication Configuration
The backend uses JWT for authentication and requires an initial Admin account. You MUST configure the following environment variables (do not use hardcoded values in production):
- `ADMIN_USERNAME` (The username for the initial admin account)
- `ADMIN_PASSWORD` (The password for the initial admin account)
- `JWT_SECRET` (A secure base64-encoded secret key, at least 32 bytes for HS256)
- `JWT_ACCESS_EXPIRATION` (default: `900000` ms / 15 min)
- `JWT_REFRESH_EXPIRATION` (default: `604800000` ms / 7 days)

### Cloudinary Configuration (Phase 3)
The content system requires Cloudinary for media storage. You MUST provide:
- `CLOUDINARY_CLOUD_NAME`
- `CLOUDINARY_API_KEY`
- `CLOUDINARY_API_SECRET`

### Authentication Flow
1. **Register**: `POST /api/auth/register` (creates USER, CREATOR, or BRAND). ADMIN accounts cannot be created publicly; an initial ADMIN is bootstrapped on startup.
2. **Login**: `POST /api/auth/login` returns an access token and refresh token.
3. **Protected APIs**: Send `Authorization: Bearer <accessToken>`.
4. **Refresh Token**: `POST /api/auth/refresh` to get a new access token.
5. **Logout**: `POST /api/auth/logout` to revoke the refresh token.

### Running the Backend
1. Navigate to the backend directory:
   ```bash
   cd backend
   ```
2. Build and run the application:
   ```bash
   mvn spring-boot:run
   ```
   The backend runs on `http://localhost:8080`. You can check the health endpoint at `http://localhost:8080/api/health`.

### Running the Frontend
1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the development server:
   ```bash
   npm run dev
   ```

## Future Phase Roadmap
*(Note: These are planned modules. They are NOT implemented yet.)*
- Phase 1: Backend foundation + users + profiles
- Phase 2: Authentication + authorization
- Phase 3: Content system
- Phase 4: Social engine
- Phase 5: Feed + discovery
- Phase 6: Creator platform
- Phase 7: Brand marketplace
- Phase 8: Collaboration system
- Phase 9: Messaging + notifications
- Phase 10: Analytics engine
- Phase 11: Redis + caching
- Phase 12: Kafka + event-driven architecture
- Phase 13: AI capabilities
- Phase 14: Testing + security hardening
- Phase 15: Docker + deployment
- Phase 16: Production polish
