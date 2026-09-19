# Phase 5.5: Collaboration / Deal Workflow

Phase 5.5 introduced the formal execution phase of a campaign by converting an `ACCEPTED` application into a binding `Collaboration`.

## Core Features Implemented

### 1. Collaboration Entity & State Management
- Created the `Collaboration` entity to serve as the join between a `Campaign` and a Creator.
- Created `CollaborationStatus` with states: `ACTIVE`, `COMPLETED`, `CANCELLED`.
- Enforced duplicate protection via a unique database constraint on `originating_application_id`.
- Handled the transactional side-effect of Campaign Application Acceptance: Accepting an application now atomically creates a `Collaboration` in the `ACTIVE` state.

### 2. Guardrails & Constraints
- Protected campaign commercial terms: `CampaignService.updateCampaign()` now rejects mutations to `title`, `description`, `niche`, `budget`, and `applicationDeadline` if the campaign has any `ACTIVE` collaborations.
- Re-used and extended dual-context logic to ensure users with both Creator and Brand roles see their corresponding collaborations from both perspectives.
- Ensured N+1 safety by using `@EntityGraph` for batch-loading users/campaigns, combined with manual batch queries for Profile information.

### 3. Frontend Integration
- **Creator Dashboard**: Added an "Active Collaborations" section that dynamically lists ongoing brand deals.
- **Brand Dashboard**: Added an "Active Collaborations" section for the brand to manage their ongoing partnerships. Accepting an application instantly creates and lists the new collaboration.
- **Collaboration Detail Page**: Created `/collaborations/:id` page to display the binding campaign terms, the brand partner, the creator partner, and status management actions (e.g., Mark Completed, Cancel).
- **Responsive Design**: Ensured all new dashboard components and the detail page are fully responsive for mobile viewports (320px+).

## Verification
- Backend logic was comprehensively verified using `mvn test` (136 tests passing).
- End-to-end workflows (login as brand, create campaign -> login as creator, apply -> login as brand, accept -> verify collaboration rendering) were verified in the browser.
