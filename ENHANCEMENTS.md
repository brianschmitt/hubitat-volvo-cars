# Project Enhancements and Future Work

## Core App & API

- [ ] Implement true oauth with Hubitat & Volvo - Need to create (and get approved) a Volvo Cars App
- [ ] Implement partial data refresh to reduce the number of API calls.
- [ ] Handling or not issuing the location and recharge status calls.
- [ ] Implement async calls where possible.
- [ ] Implement the 'Presence Sensor' capability.
- [ ] Implement calculation/mapping logic in the driver to populate the `Battery` attribute using fuel level data for non-electric vehicles.

## Testing / Verification

- [ ] Verify compatibility and data with Android-based Volvo vehicle models.
- [ ] Verify parsing and handling of `power` and `battery` values for all-electric vehicles.

## User Interface (UI)

- [ ] Display a clear error message on the Add Vehicle page if the call to list vehicles returns no results.
- [ ] Add a visual indicator (e.g., a spinner or loading text) on the Add Vehicle page.
- [ ] Improve on-boarding, the auth flow is a bit odd, and OTP can get stuck in a weird state.

## Code Quality & Testing

- [ ] Address any outstanding Groovy linting warnings or errors reported by the configured lint rules.
- [ ] Write unit tests for key logic in both the app and driver. May be difficult...
- [ ] Improve error handling throughout the app and driver.

## Documentation & Metadata

- [ ] Update the community thread ID placeholder in the `packageManifest.json`, the app.groovy file, and the README file with the actual link once created.
- [ ] Improve overall documentation.
