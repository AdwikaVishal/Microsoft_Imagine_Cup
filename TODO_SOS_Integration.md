# SOS Integration Plan

## Backend (Already Implemented ✅)
- `GET /api/admin/sos` - Returns paginated SOS alerts
- `GET /api/admin/stats/sos` - Returns active SOS count
- `GET /api/admin/map-data` - Returns incidents + SOS markers
- `create_sos_alert()` - Creates SOS + Message record

## Frontend Tasks

### 1. API Service (`sensesafe/src/services/api.js`)
- [ ] Add `getSOSStats()` - fetch active SOS count
- [ ] Update `getAllSOSAlerts()` - use admin endpoint
- [ ] Add `getMapData()` - fetch real map data

### 2. Dashboard (`pages/Dashboard.jsx`)
- [ ] Replace mock stats with real data from `/api/admin/stats/sos`
- [ ] Show real Active SOS count

### 3. New SOS Page (`pages/SOSAlerts.jsx`)
- [ ] Create dedicated SOS alerts page
- [ ] Table with: Time, User, Status, Ability, Battery, Location
- [ ] Actions: Resolve, View on Map

### 4. Map Component (`components/IncidentMap.jsx`)
- [ ] Fetch real data from `/api/admin/map-data`
- [ ] Render SOS markers (red) separately from incidents
- [ ] Popup: "SOS — Status: TRAPPED/INJURED/NEED_HELP"

### 5. Sidebar (`components/Sidebar.jsx`)
- [ ] Add "SOS Alerts" navigation item
- [ ] Badge for active SOS count

### 6. Messages Page (`pages/Messages.jsx`)
- [ ] SOS entries show "🚨 SOS" label clearly
- [ ] Status badges show SOS status

### 7. App Router (`App.jsx`)
- [ ] Add route for `/sos-alerts` page

---

## Implementation Order
1. Update API service with new functions
2. Create SOSAlerts page
3. Update Dashboard with real SOS stats
4. Update IncidentMap with real map data
5. Update Sidebar with SOS navigation
6. Update Messages page styling
7. Update App.jsx with new routes

