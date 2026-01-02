# TODO: Fix Resolve Button to Call Backend

## Task Summary
Make the "Resolve" button in admin dashboard send real requests to FastAPI backend.

## Files to Modify
1. `sensesafe/src/services/api.js` - Already has `resolveIncident()` ✅
2. `sensesafe/src/apps/admin-dashboard/src/pages/Alerts.jsx` - Updated ✅

## Steps
- [x] 1. ✅ `resolveIncident()` already exists in api.js (uses `PATCH /api/admin/incidents/{incident_id}/resolve`)
- [x] 2. Updated `Alerts.jsx` - handleResolve now:
    - Calls backend API for INCIDENTS only
    - Only removes from UI AFTER backend success
    - Logs detailed errors to console
    - Handles SOS/Messages gracefully (no backend endpoint)
- [ ] 3. Test the integration

## Backend Endpoints Available
- `PATCH /api/admin/incidents/{incident_id}/resolve` - For INCIDENTS ✅
- NOTE: No endpoint exists for SOS alerts or Messages - would need backend changes

