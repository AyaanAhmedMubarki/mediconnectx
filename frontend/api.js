const API_BASE = 'http://localhost:8080';

function getAuth() {
  const raw = localStorage.getItem('auth');
  return raw ? JSON.parse(raw) : null;
}

function getToken() {
  const auth = getAuth();
  return auth ? auth.token : null;
}

function authHeaders() {
  const token = getToken();
  return token ? { 'Authorization': 'Bearer ' + token } : {};
}

function requireAuth(expectedRole) {
  const auth = getAuth();
  if (!auth) { window.location.href = 'Login.html'; return null; }
  if (expectedRole && auth.role !== expectedRole) {
    // Check if custom modal is available, otherwise use alert
    if (typeof showAlert === 'function') {
      showAlert('Access Denied', 'Please log in with the correct role.');
    } else {
      alert('Access denied. Please log in with the correct role.');
    }
    setTimeout(() => window.location.href = 'Login.html', 500);
    return null;
  }
  return auth;
}

function logout() {
  localStorage.removeItem('auth');
  window.location.href = 'Login.html';
}

function imgSrc(base64) {
  if (!base64) return 'https://via.placeholder.com/150';
  if (base64.startsWith('data:')) return base64;
  return 'data:image/jpeg;base64,' + base64;
}

// Central response guard — redirects to login on 401 (expired/invalid token)
function handleUnauthorized(res) {
  if (res.status === 401) {
    localStorage.removeItem('auth');
    window.location.href = 'Login.html';
    return true;
  }
  return false;
}

// Extracts a human-readable message from an error response body.
// Tries to parse JSON and pull out the "message" or "error" field;
// falls back to raw text if it is not JSON.
async function extractErrorMessage(res) {
  const text = await res.text();
  if (!text) return 'Something went wrong. Please try again.';
  try {
    const json = JSON.parse(text);
    return json.message || json.error || text;
  } catch {
    return text;
  }
}

async function apiGet(path) {
  const res = await fetch(API_BASE + path, {
    headers: { ...authHeaders() }
  });
  if (handleUnauthorized(res)) return;
  if (!res.ok) throw new Error(await extractErrorMessage(res));
  return res.json();
}

async function apiPostJSON(path, body) {
  const res = await fetch(API_BASE + path, {
    method: 'POST',
    headers: { ...authHeaders(), 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  if (handleUnauthorized(res)) return;
  if (!res.ok) throw new Error(await extractErrorMessage(res));
  return res.json();
}

async function apiFormPost(path, formData) {
  const res = await fetch(API_BASE + path, {
    method: 'POST',
    headers: { ...authHeaders() },
    body: formData
  });
  if (handleUnauthorized(res)) return;
  if (!res.ok) throw new Error(await extractErrorMessage(res));
  const text = await res.text();
  try { return JSON.parse(text); } catch { return text; }
}

async function apiFormPut(path, formData) {
  const res = await fetch(API_BASE + path, {
    method: 'PUT',
    headers: { ...authHeaders() },
    body: formData
  });
  if (handleUnauthorized(res)) return;
  if (!res.ok) throw new Error(await extractErrorMessage(res));
  return res.text();
}

async function apiDelete(path) {
  const res = await fetch(API_BASE + path, {
    method: 'DELETE',
    headers: { ...authHeaders() }
  });
  if (handleUnauthorized(res)) return;
  if (!res.ok) throw new Error(await extractErrorMessage(res));
  return res.text();
}

function today() {
  const d = new Date();
  return d.getFullYear() + '-' +
         String(d.getMonth() + 1).padStart(2, '0') + '-' +
         String(d.getDate()).padStart(2, '0');
}

// ========== DOCTOR LOCATIONS ==========
async function getDoctorLocations(doctorId) {
  return apiGet(`/api/doctor-locations/doctor/${doctorId}`);
}

async function addDoctorLocation(location) {
  return apiPostJSON('/api/doctor-locations/create', location);
}

async function deleteDoctorLocation(locationId) {
  return apiDelete(`/api/doctor-locations/${locationId}`);
}

// ========== MEDICAL RECORDS ==========
async function getPatientMedicalRecords(patientId) {
  return apiGet(`/api/medical-records/patient/${patientId}`);
}

async function getMedicalRecordByAppointment(appointmentId) {
  return apiGet(`/api/medical-records/appointment/${appointmentId}`);
}

async function createMedicalRecord(appointmentId, diagnosis, notes, observations) {
  return apiPostJSON('/api/medical-records/create', {
    appointment: { id: appointmentId },
    diagnosis,
    notes,
    observations
  });
}

// ========== PRESCRIPTIONS ==========
async function searchMedicines(query) {
  return apiGet(`/api/medicines/search/${query}`);
}

async function getAllMedicines() {
  return apiGet('/api/medicines/all');
}

async function getPatientPrescriptions(patientId) {
  return apiGet(`/api/prescriptions/patient/${patientId}`);
}

async function getPrescriptionsForAppointment(appointmentId) {
  return apiGet(`/api/prescriptions/appointment/${appointmentId}`);
}

async function createPrescription(appointmentId, medicineId, dosage, frequency, duration, quantity, instructions, warnings) {
  return apiPostJSON('/api/prescriptions/create', {
    appointment: { id: appointmentId },
    medicine: { id: medicineId },
    dosage,
    frequency,
    duration,
    quantity,
    instructions,
    warnings
  });
}

async function updatePrescriptionStatus(prescriptionId, status) {
  return apiPostJSON(`/api/prescriptions/${prescriptionId}`, { status });
}

// ========== MEDICAL TESTS ==========
async function prescribeTest(patientId, doctorId, testName, testType, testDescription) {
  return apiPostJSON('/api/tests/create', {
    patientId,
    doctorId,
    testName,
    testType,
    testDescription,
    prescribedDate: new Date().toISOString(),
    status: 'PRESCRIBED'
  });
}

async function getPatientTests(patientId) {
  return apiGet(`/api/tests/patient/${patientId}`);
}

async function getPendingTests(patientId) {
  return apiGet(`/api/tests/patient/${patientId}/pending`);
}

async function updateTestResults(testId, results, normalRange) {
  return apiPostJSON(`/api/tests/${testId}`, {
    results,
    normalRange,
    testDate: new Date().toISOString(),
    status: 'COMPLETED'
  });
}

async function getTestsByType(testType) {
  return apiGet(`/api/tests/type/${testType}`);
}

// ========== MEDICINES ==========
async function addMedicine(medicine) {
  return apiPostJSON('/api/medicines/create', medicine);
}

async function updateMedicine(medicineId, medicine) {
  return apiPostJSON(`/api/medicines/${medicineId}`, medicine);
}

async function deleteMedicine(medicineId) {
  return apiDelete(`/api/medicines/${medicineId}`);
}
