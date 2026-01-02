# Frontend Fix Plan - Admin Dashboard Alerts

## Objective
Fix the admin dashboard React frontend to properly display SOS and Incident alerts from the backend.

## Backend Response Formats (Confirmed)
- **GET `/api/sos/user`**: `{ sos_alerts: [{ id, user_id, ability, lat, lng, battery, status, created_at }] }`
- **GET `/api/incidents/user`**: `{ incidents: [{ id, user_id, type, description, lat, lng, status, image_url, risk_score, risk_level, created_at }] }`
- **GET `/api/messages/admin/all`**: `{ messages: [{ id, user_id, user_name, message_type, title, content, ... }] }`

## Tasks Completed

### 1. Fix `sensesafe/src/services/api.js`
- [x] Update `getAllAlertsForAdmin()` to handle exact backend response format
- [x] Add defensive null checks for all fields
- [x] Map fields correctly: `type` → `category`, `description` → `content`
- [x] Add fallback values for missing user data
- [x] Return empty data structure on error instead of throwing
- [x] **PRIORITY FIX**: Prioritize `/api/messages/admin/all` endpoint (has real UUIDs)
- [x] **PRIORITY FIX**: Fall back to SOS/incidents only when messages endpoint is empty
- [x] **PRIORITY FIX**: Add `_isFallbackData` flag for data from fallback endpoints

### 2. Fix `sensesafe/src/apps/admin-dashboard/src/pages/Alerts.jsx`
- [x] Update `handleRefresh()` to properly convert API response
- [x] Add error handling for empty/null data
- [x] Ensure proper loading states
- [x] Add `Number()` wrapper for lat/lng toFixed()

### 3. Fix `sensesafe/src/apps/admin-dashboard/src/pages/Messages.jsx`
- [x] Add defensive `?.` operators for all data access
- [x] Improve loading and empty state handling
- [x] Ensure stats calculation handles edge cases
- [x] **PRIORITY FIX**: Handle 404 errors gracefully in `handleMarkAsRead()`
- [x] **PRIORITY FIX**: Disable "Mark as read" button for fallback data

### 4. Fix `sensesafe/src/apps/admin-dashboard/src/App.jsx`
- [x] Add defensive checks for data?.messages and data?.stats
- [x] Handle missing fields in alert conversion
- [x] Set empty arrays on error to prevent crashes

### 5. Testing (Pending)
- [ ] Start admin dashboard: `cd sensesafe && npm run dev:admin`
- [ ] Check browser console for API call logs
- [ ] Verify alerts appear in both Alerts and Messages pages
- [ ] Test with empty data scenarios

## Implementation Notes
- DO NOT modify backend code
- DO NOT modify Android app code
- Only fix frontend to match backend response format

## Changes Made Summary

### api.js
- Added detailed comments documenting backend response format
- Added defensive `Array.isArray()` checks for data arrays
- Added fallback ID generation for entries without IDs
- Fixed field mapping: `type` → `category`, `description` → `content`
- Added `Number()` wrapper for lat/lng toFixed() calls
- Return empty data structure instead of throwing on error
- **PRIORITY FIX**: Changed fetch priority - use `/api/messages/admin/all` first (has real UUIDs)
- **PRIORITY FIX**: Only fall back to SOS/incidents when messages endpoint returns empty
- **PRIORITY FIX**: Added `_isFallbackData` flag for fallback data

### Alerts.jsx
- Added `Array.isArray(data?.messages)` check
- Added fallback values for all fields
- Added error handling that sets empty array on error
- Fixed lat/lng formatting with `Number()` wrapper

### Messages.jsx
- Extracted `statsData = data?.stats || {}` for safer access
- Added error handling that sets empty data on error
- **PRIORITY FIX**: Added 404 error handling in `handleMarkAsRead()` - marks read locally on 404
- **PRIORITY FIX**: Button shows "Read-only" and is disabled for fallback data

### App.jsx
- Added defensive checks for `data?.messages` and `data?.stats`
- Added fallback ID generation
- Fixed lat/lng formatting
- Set empty arrays on error to prevent crashes

## Bug Fix: "Error marking message as read" 404

### Root Cause
1. Frontend `getAllAlertsForAdmin()` fetched from multiple endpoints in parallel
2. Data from SOS/incidents endpoints has IDs that are **not real UUIDs** (e.g., `sos-timestamp-random`)
3. When "Mark as read" was clicked, it called `/api/messages/admin/{fakeId}/read`
4. Backend returned 404 because the message didn't exist in the `messages` table

### Solution
1. **Priority-based fetching**: Always try `/api/messages/admin/all` first (has real UUIDs)
2. **Fallback only when empty**: Only use SOS/incidents endpoints if messages is empty
3. **Graceful 404 handling**: When 404 occurs, mark as read locally instead of crashing
4. **UI feedback**: Disable "Mark as read" button for fallback data with "Read-only" label

## How to Test

1. **Start the backend:**
   ```bash
   cd backend && python -m uvicorn app.main:app --host 0.0.0.0 --port 8000
   ```

2. **Start the admin dashboard:**
   ```bash
   cd sensesafe && npm run dev:admin
   ```

3. **Open browser at:** http://localhost:3001

4. **Login with:**
   - Email: `admin@sensesafe.com`
   - Password: `admin123`

5. **Check browser console (F12) for:**
   - `🔄 Fetching all alerts from backend...`
   - `📨 Received X messages from messages endpoint`
   - `📖 Marking message as read...`

6. **Expected behavior:**
   - Alerts sent from Android app should appear in the Messages page
   - SOS alerts should appear with "SOS Alert" type
   - Incident reports should appear with "Incident" type
   - "Mark as read" button works without 404 errors
   - Empty data should not crash the dashboard

