# Jmoney Wear Reliability Regression Checklist

Run these checks only during the final physical-watch testing stage. No intermediate watch testing is required.

## Tile authentication and rendering

- [ ] Reboot the watch, do not open Jmoney first, then open each data tile. Verify the tile restores the existing saved session or uses the same existing development auto-login fallback as the app.
- [ ] Verify Remaining, Net Worth, Monthly Income, Monthly Expense, Budget, and Goal tiles show their actual data and do not randomly show a sign-in prompt while the saved/development session is valid.
- [ ] Open Jmoney after launching tiles and confirm the app and tiles use the same signed-in account.
- [ ] Verify configured Budget and Goal tiles continue to show the selected item after app restart and tile refresh.
- [ ] Verify action-only tiles still launch the correct app action.

## Settings full sync

- [ ] Open Settings and verify the Force full sync action is fully visible, readable, and easy to tap.
- [ ] Trigger a full sync and verify progress is visible.
- [ ] Verify a successful sync displays completion feedback and refreshes data tiles.
- [ ] Simulate a sync/network failure where practical. Verify Settings reports failure and tiles are not refreshed as if the sync succeeded.
- [ ] Verify Sign Out remains disabled while sync is running.

## Credential preservation

- [ ] Confirm existing development/production credential values and BuildConfig auto-login behavior remain unchanged.
- [ ] Confirm no credentials are added to source control, documentation, or CI logs.
