# MediConnectX – Bugs Fixed Log

> **Safe to delete before submission.**
> This file is pure documentation. No code, config, or build tool references it.
> Deleting it has zero effect on running the project.

---

## SESSION 1 (Earlier session – reconstructed from summary)

---

### BUG-01 · Doctor slots completely invisible to patient after shift start time passes
**Files:** `SlotGenerationService.java`, `AppointmentSlotRepository.java`,
`DoctorShiftController.java`, `DoctorService.java`
**Problem:** If a doctor had a shift 21:18–23:35 and the current time was 21:37, the
entire shift became invisible to the patient. The idempotency guard used
`existsByDoctorIdAndSlotDate` (date-level), so a second shift on the same day was
silently skipped. Also `getAvailableDoctors()` hid doctors once ALL slots were past,
even if non-past slots still existed.
**Fix:**
- Changed idempotency guard to `existsByDoctorIdAndSlotDateAndStartTime` (per-shift).
- Added `generateSlotsForShift()` method in `SlotGenerationService`.
- Updated `DoctorShiftController.createShift()` to call `generateSlotsForShift()`.
- Added `findDoctorIdsWithAnySlotsOnDateFromTime()` query to show doctors with any
  non-past slots today.

---

### BUG-02 · "Find a Doctor" button linked to deleted page
**Files:** `patient.html`, `PatientSlots.html` (deleted)
**Problem:** A "Find a Doctor" quick-action button linked to `PatientSlots.html` which
was an incorrect/incomplete page.
**Fix:** Removed the button from `patient.html`. Deleted `PatientSlots.html`. Changed
the "Book a Consultation" fallback link to point to `PatientMeets.html`.

---

### BUG-03 · Event registration Cancel button showed for APPROVED/REJECTED status
**File:** `patient.html`
**Problem:** The Cancel button in "My Event Registrations" showed for all statuses,
including APPROVED and REJECTED, which makes no sense.
**Fix:** Updated `renderRegistrations()` to show Cancel only when `r.status === 'PENDING'`;
shows `—` for all other statuses.

---

### BUG-04 · Doctor could mark Complete / upload Rx before Join Call window opened
**File:** `DoctorMeets.html`
**Problem:** "Mark Complete" and "Upload Rx" buttons were enabled even when the
Join Call button was still showing "Opens in X time" (> 10 min before appointment).
**Fix:** Added `isCallPending` flag. Both buttons are rendered as disabled with a
tooltip until `isCallPending` is false (i.e., within 10 min of appointment time).

---

### BUG-05 · AdminMeets.html stats row wrapped onto multiple lines
**File:** `AdminMeets.html`
**Problem:** The 8 stat cards (Total, Pending, Confirmed, Completed, Cancelled,
Missed, Gross Revenue, Refunds Issued) wrapped to multiple rows below ~1280 px because
the grid used `repeat(auto-fit, minmax(160px, 1fr))`.
**Fix:** Changed to `repeat(8, 1fr)` with `overflow-x: auto` and reduced card padding
and font sizes so all 8 cards stay on one horizontal line.

---

### BUG-06 · pom.xml – Spring AI Ollama caused startup crash
**File:** `pom.xml`
**Problem:** `spring-ai-starter-model-ollama` auto-configured an Ollama client on
startup. Since Ollama was not running, the app crashed before accepting any requests.
Also, 4 non-existent test artifacts (`spring-boot-starter-data-jpa-test`, etc.) broke
`mvn clean install` on a fresh machine.
**Fix:** Removed `spring-ai-starter-model-ollama` and its BOM
(`spring-ai-bom` dependencyManagement). Removed the 4 fake test starters. Added the
real `spring-boot-starter-test` and `spring-security-test`.

---

### BUG-07 · Welcome.html – "Register" link used wrong case
**File:** `Welcome.html`
**Problem:** `href="register.html"` — file is actually `Register.html` (capital R).
On Windows this worked by accident; on Linux/macOS (and some servers) it 404s.
**Fix:** Changed to `href="Register.html"`.

---

### BUG-08 · Contact.html – missing api.js script tag
**File:** `Contact.html`
**Problem:** The contact form called `API_BASE` (from `api.js`) but `api.js` was never
loaded, so the form submission always failed with "API_BASE is not defined".
**Fix:** Added `<script src="api.js"></script>` before the inline script block.
Removed the redundant local `const API_BASE = 'http://localhost:8080'` declaration.

---

### BUG-09 · Stale SecurityConfig.java / CorsConfig.java in wrong location
**Files:** `src/main/resources/SecurityConfig.java`,
`src/main/resources/CorsConfig.java` (both deleted)
**Problem:** Java source files placed inside `src/main/resources/` are NOT compiled —
they are copied as plain text to the classpath. They were dead files that caused
confusion about which security/CORS config was actually active.
**Fix:** Deleted both stale files. The real, working configs remain in
`src/main/java/com/health/mediconnectx/config/`.

---

## SESSION 2 (Current session)

---

### BUG-10 · UTC date bug – "today" showed yesterday in IST after midnight
**Files:** `DoctorAvailability.html`, `AdminEvents.html`, `PatientMeets.html`,
`api.js`
**Problem:** All four files used `new Date().toISOString().split('T')[0]` to get
today's date. `toISOString()` returns UTC time. Between 00:00–05:29 IST (UTC+5:30),
UTC is still the previous day, so "today" was incorrectly set to yesterday. This
allowed adding shifts/events on past dates.
**Fix:** Replaced `toISOString().split('T')[0]` with local date arithmetic:
```js
const d = new Date();
return d.getFullYear() + '-' +
       String(d.getMonth() + 1).padStart(2, '0') + '-' +
       String(d.getDate()).padStart(2, '0');
```

---

### BUG-11 · DoctorAvailability.html – start/end time not auto-set when Today selected
**File:** `DoctorAvailability.html`
**Problem:** When the doctor selected today's date, the start time stayed at the
default "09:00" instead of auto-filling to the current clock time, and the end time
stayed at "13:00" instead of auto-filling to current time + 15 min.
**Fix:** In the `dateInput.addEventListener('change')` handler, when today is selected,
set `startInput.value = now` AND `endInput.value = addMinutes(now, 15)`.

---

### BUG-12 · Event registration – confirmation modal invisible (Bootstrap class conflict)
**Files:** `PatientEvents.html`, `DoctorEvents.html`
**Problem:** Clicking "Register Now" dimmed the page (overlay activated) but the white
confirmation dialog never appeared. Root cause: the inner dialog used
`<div class="modal">`. Bootstrap 5 defines `.modal { display: none; ... }`. The
custom `<style>` block styled `.modal` but never set `display`, so Bootstrap's
`display:none` was never overridden. The dialog was hidden behind the backdrop.
**Fix:** Renamed the inner dialog class from `modal` → `confirm-box` in both the
`<style>` CSS and the `<div>` HTML element in both files.

---

### BUG-13 · Event registration – backend DB schema issues caused silent 500 errors
**Files:** `DatabaseMigration.java`, `RegistrationController.java`,
`RegistrationService.java`
**Problem (a):** The `registrations.status` column may have been created as a MySQL
ENUM from an earlier schema version. Hibernate's `ddl-auto=update` does NOT convert
ENUM → VARCHAR. Trying to INSERT `"PENDING"` into an ENUM that didn't list it caused
a data truncation error → silent HTTP 500.
**Problem (b):** The `user_role` column was added to the entity later. If the app had
crashed before (Ollama bug) Hibernate never got to add the column, so INSERTs failed.
**Problem (c):** `RegistrationController.createRegistration()` had no try-catch around
the service call, so any DB exception produced a raw Spring 500 with no useful message.
**Problem (d):** `RegistrationService.createRegistration()` had no duplicate check.
A second registration attempt for the same event would hit a DB constraint.
**Fix:**
- Added two `DatabaseMigration` patches: convert `registrations.status` to
  `VARCHAR(30)` and `ADD COLUMN IF NOT EXISTS user_role VARCHAR(20)`.
- Wrapped service call in `RegistrationController` with try-catch returning a clear
  error message.
- Added `findByEventIdAndPatientId` duplicate check in `RegistrationService`; returns
  the existing registration instead of failing.

---

### BUG-14 · Event pages lost event name after register/unregister re-render
**Files:** `PatientEvents.html`, `DoctorEvents.html`
**Problem:** After `doRegister()` or `doUnregister()`, the action slot was re-rendered
by calling `actionHtml({ id: eventId })` — but this object had no `eventName`. If the
user then tried to unregister and re-register, the "Register Now" button's `onclick`
contained an empty event name.
**Fix:** Added `nameMap = {}` (eventId → eventName) populated when events are rendered.
`doRegister` and `doUnregister` now pass `{ id: eventId, eventName: nameMap[eventId] }`
to `actionHtml`.

---

## SESSION 3 (Critical security fixes)

---

### SEC-01 · Credentials protected from GitHub (already in .gitignore)
**File:** `.gitignore`
**Problem:** `application.properties` contains Gmail App Password, JWT secret, and Jitsi secret in plain text.
**Status:** Already protected — `.gitignore` at repo root lists `application.properties`. No code change needed. File is safe to include in the zip for laptop transfer (credentials stay local).

---

### SEC-02 · Stack traces exposed in HTTP error responses
**File:** `application.properties`
**Problem:** `server.error.include-exception=true` and `server.error.include-message=always` sent full Java stack traces to any caller on any 500 error.
**Fix:** Set `include-exception=false`, `include-message=never`, `include-binding-errors=never`. Also set `show-sql=false` to stop logging patient data in SQL output.

---

### SEC-03 · `/api/events/**` and `/api/payments/**` were fully public
**File:** `SecurityConfig.java`
**Problem:** Unauthenticated callers could create/delete events and confirm payments.
**Fix:** GET /api/events/** now requires authentication; write operations require ROLE_ADMIN. /api/payments/** requires authentication. /api/registration/** requires authentication.

---

### SEC-04 · JWT token had null authorities — roles never worked
**File:** `JwtAuthenticationFilter.java`
**Problem:** `new UsernamePasswordAuthenticationToken(userDetails, null, null)` — passing null authorities meant `@PreAuthorize` and `hasRole()` were broken.
**Fix:** Convert `user.getRoles()` to `SimpleGrantedAuthority("ROLE_" + name)` and pass the collection as the third argument. Also replaced `System.out.println` with SLF4J logger.

---

### SEC-05 · Any patient could read ALL appointments, mark any complete, overwrite any prescription
**File:** `AppointmentController.java`
**Problem:** No ownership check on any endpoint. Also removed stale `@CrossOrigin("*")` annotation.
**Fix:** Added `getCurrentUser()` + `isAdmin()` + `isOwner()` helpers. Each endpoint now verifies: admin can do everything; patient can only access their own patientId; doctor can only access their own doctorId; mark-complete and prescription upload require the calling doctor to be the assigned doctor.

---

### SEC-06 · patientId taken from request body — anyone could book under another patient's ID
**File:** `PaymentController.java`
**Problem:** `initiateBooking()` read `patientId` from `@RequestBody`, so a patient could forge any patientId. Also removed stale `@CrossOrigin("*")` annotation.
**Fix:** `patientId` is now always derived from `SecurityContextHolder` (the JWT). The request body value is ignored entirely. `mock-success` also checks the caller owns the appointment being confirmed.

---

### SEC-07 · Anyone could delete another user's registration; anyone could approve/reject
**File:** `RegistrationController.java`, `RegistrationService.java`
**Problem:** `DELETE /api/registration/delete/{id}` had no ownership check. `PUT /api/registration/update` had no role check.
**Fix:** Delete now fetches the registration, compares `registration.patientId` to the caller's profile ID, and returns 403 if they don't match. Update now requires admin role. Added `findById()` helper to `RegistrationService`. Also added status allowlist validation (APPROVED or REJECTED only).

---

### SEC-08 · CORS allowed any origin with credentials
**File:** `CorsConfig.java`
**Problem:** `allowedOriginPatterns("*")` + `allowCredentials(true)` — any website on any domain could make credentialed requests.
**Fix:** Changed to specific origins `http://localhost:5500` and `http://127.0.0.1:5500` (the two addresses VS Code Live Server uses). Removed `allowCredentials(true)` — not needed since authentication uses Authorization headers, not cookies.

---

## REMINDER – Sensitive file (never commit to GitHub)

`mediconnectx/src/main/resources/application.properties` contains a Gmail App
Password. Always run:
```
git restore --staged "mediconnectx/mediconnectx/src/main/resources/application.properties"
```
before committing the backend to GitHub.
