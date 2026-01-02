# TODO: SOS API Fix

## Problem
The Android app was receiving errors when sending SOS alerts to `/api/sos`:
1. `422 Unprocessable Entity` - enum mismatch
2. `500 Internal Server Error` - null user handling

## Root Causes

### Issue 1: Enum mismatch (422 error)
- Android sends: `LOW_VISION`, `HARD_OF_HEARING`, `NORMAL`
- Backend only accepts: `BLIND`, `DEAF`, `NON_VERBAL`, `ELDERLY`, `OTHER`, `NONE`

### Issue 2: Null user handling (500 error)
- SOS endpoint is designed for anonymous users but `user_id` was `NOT NULL`
- When no auth token provided, `user` is `None` causing `AttributeError`

## Fixes Applied

### Fix 1: Updated enum values in `backend/app/db/models.py`
- Added `LOW_VISION = "LOW_VISION"`
- Added `HARD_OF_HEARING = "HARD_OF_HEARING"`

### Fix 2: Made `user_id` nullable in `backend/app/db/models.py`
- Changed `nullable=False` to `nullable=True`

### Fix 3: Handle null user in `backend/app/sos/service.py`
- Updated `create_sos_alert()` to use `user.id if user else None`

### Fix 4: Database migration (run this command)
```bash
python -c "from app.db.models import Base, engine; from sqlalchemy import text; 
with engine.connect() as conn: conn.execute(text('ALTER TABLE sos ALTER COLUMN user_id DROP NOT NULL')); conn.commit()"
```

## Status
✅ DONE - All fixes applied

## Notes
- Android `NORMAL` maps to backend `NONE` (semantic equivalent)

