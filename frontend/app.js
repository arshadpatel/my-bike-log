// ═══════════════════════════════════════════════════════════
//  CONFIG
// ═══════════════════════════════════════════════════════════
// Point this at your running backend. No context path — controllers
// are mapped at the root (e.g. /bikes, /rides/{bikeId}), not /api/v1.
const API_BASE = 'http://localhost:8080';

// CustomAuthenticationSuccessHandler currently redirects back to a
// hardcoded http://127.0.0.1:5500/index.html after Google login, so
// serve this frontend from that exact origin/port for the OAuth
// round-trip to work (e.g. `npx live-server --port=5500`).

const JWT_KEY = 'bikelog_jwt';
const ACTIVE_BIKE_KEY = 'bikelog_active_bike';

// ═══════════════════════════════════════════════════════════
//  STATE
// ═══════════════════════════════════════════════════════════
let token = localStorage.getItem(JWT_KEY) || null;
let currentUser = null;
let bikes = [];
let currentBike = null;

let ridesState = { page: 0, size: 10, month: '', data: null };
let petrolState = { page: 0, size: 10, month: '', data: null };
let overallStatsCache = null;
let monthsCache = [];

let chartKm = null, chartPetrol = null, chartSpend = null;

// ═══════════════════════════════════════════════════════════
//  API CLIENT
// ═══════════════════════════════════════════════════════════
function authHeaders() {
  return token ? { 'Authorization': 'Bearer ' + token } : {};
}

async function apiFetch(path, options = {}) {
  let res;
  try {
    res = await fetch(API_BASE + path, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...authHeaders(),
        ...(options.headers || {})
      }
    });
  } catch (e) {
    throw new Error('Could not reach the backend. Is it running at ' + API_BASE + '?');
  }

  if (res.status === 401) {
    logout();
    throw new Error('Session expired. Please sign in again.');
  }

  if (res.status === 204) return null;

  let body = null;
  const text = await res.text();
  if (text) {
    try { body = JSON.parse(text); } catch (e) { body = null; }
  }

  if (!res.ok) {
    const msg = (body && body.message) ? body.message : `Request failed (${res.status})`;
    throw new Error(msg);
  }
  return body;
}

function qs(params) {
  const entries = Object.entries(params).filter(([, v]) => v !== undefined && v !== null && v !== '');
  if (!entries.length) return '';
  return '?' + entries.map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`).join('&');
}

// ── Users ──
const getMe = () => apiFetch('/users/me');
const updateMe = (dto) => apiFetch('/users/me', { method: 'PATCH', body: JSON.stringify(dto) });

// ── Bikes ──
const getBikes = () => apiFetch('/bikes');
const addBikeApi = (dto) => apiFetch('/bikes', { method: 'POST', body: JSON.stringify(dto) });
const getBike = (bikeId) => apiFetch(`/bikes/${bikeId}`);
const deleteBikeApi = (bikeId) => apiFetch(`/bikes/${bikeId}`, { method: 'DELETE' });

// ── Rides ──
const getRides = (bikeId, { month, page, size }) => apiFetch(`/rides/${bikeId}${qs({ month, page, size })}`);
const addRideApi = (bikeId, dto) => apiFetch(`/rides/${bikeId}`, { method: 'POST', body: JSON.stringify(dto) });
const deleteRideApi = (bikeId, rideId) => apiFetch(`/rides/${bikeId}/${rideId}`, { method: 'DELETE' });

// ── Petrol ──
const getPetrolEntries = (bikeId, { month, page, size }) => apiFetch(`/petrol-entries/${bikeId}${qs({ month, page, size })}`);
const addPetrolApi = (bikeId, dto) => apiFetch(`/petrol-entries/${bikeId}`, { method: 'POST', body: JSON.stringify(dto) });
const deletePetrolApi = (bikeId, petrolEntryId) => apiFetch(`/petrol-entries/${bikeId}/${petrolEntryId}`, { method: 'DELETE' });

// ── Dashboard / Stats ──
const getMonths = (bikeId) => apiFetch(`/${bikeId}/months`);
const getMonthlyDashboard = (bikeId, month) => apiFetch(`/${bikeId}/dashboard${qs({ month })}`);
const getOverallStats = (bikeId) => apiFetch(`/${bikeId}/overall-stats`);
const getOilStatus = (bikeId) => apiFetch(`/${bikeId}/oil-status`);
const getTyreStatus = (bikeId) => apiFetch(`/${bikeId}/tyre-status`);

// ── Oil changes (backend not implemented yet — matches swagger contract) ──
const getOilChanges = (bikeId, { page, size }) => apiFetch(`/oil-changes/${bikeId}${qs({ page, size })}`);
const addOilChangeApi = (bikeId, dto) => apiFetch(`/oil-changes/${bikeId}`, { method: 'POST', body: JSON.stringify(dto) });
const deleteOilChangeApi = (bikeId, oilChangeId) => apiFetch(`/oil-changes/${bikeId}/${oilChangeId}`, { method: 'DELETE' });

// ── Tyre checks (backend not implemented yet) ──
const getTyreChecks = (bikeId, { page, size }) => apiFetch(`/tyre-checks/${bikeId}${qs({ page, size })}`);
const addTyreCheckApi = (bikeId, dto) => apiFetch(`/tyre-checks/${bikeId}`, { method: 'POST', body: JSON.stringify(dto) });
const deleteTyreCheckApi = (bikeId, tyreCheckId) => apiFetch(`/tyre-checks/${bikeId}/${tyreCheckId}`, { method: 'DELETE' });

// ── Services (backend not implemented yet) ──
const getServices = (bikeId, { page, size }) => apiFetch(`/services/${bikeId}${qs({ page, size })}`);
const addServiceApi = (bikeId, dto) => apiFetch(`/services/${bikeId}`, { method: 'POST', body: JSON.stringify(dto) });
const deleteServiceApi = (bikeId, serviceId) => apiFetch(`/services/${bikeId}/${serviceId}`, { method: 'DELETE' });

// ═══════════════════════════════════════════════════════════
//  FORMAT HELPERS
// ═══════════════════════════════════════════════════════════
function round2(n) {
  const num = Number(n);
  if (isNaN(num)) return 0;
  return Math.round((num + Number.EPSILON) * 100) / 100;
}
function fmt2(n) {
  const num = Number(n);
  if (n === null || n === undefined || isNaN(num)) return '0.00';
  return round2(num).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}
function fmtDate(d) {
  if (!d) return '—';
  const [y, m, day] = d.split('-');
  return `${day}/${m}/${y}`;
}
function fmtMonthLabel(m) {
  const [y, mo] = m.split('-');
  return new Date(y, parseInt(mo) - 1, 1).toLocaleString('default', { month: 'long', year: 'numeric' });
}

// ── Toast ──
function toast(msg, dur = 2600) {
  const t = document.getElementById('toast');
  t.textContent = msg;
  t.classList.add('show');
  setTimeout(() => t.classList.remove('show'), dur);
}

function errToast(e) {
  toast(e && e.message ? e.message : 'Something went wrong');
}

// ═══════════════════════════════════════════════════════════
//  AUTH
// ═══════════════════════════════════════════════════════════
function captureTokenFromUrl() {
  const params = new URLSearchParams(window.location.search);
  const t = params.get('token');
  if (t) {
    token = t;
    localStorage.setItem(JWT_KEY, t);
    params.delete('token');
    const newUrl = window.location.pathname + (params.toString() ? '?' + params.toString() : '');
    window.history.replaceState({}, '', newUrl);
  }
}

function showLogin(errorMsg) {
  document.getElementById('app').style.display = 'none';
  document.getElementById('login-screen').style.display = 'flex';
  document.getElementById('login-error').textContent = errorMsg || '';
}

function showApp() {
  document.getElementById('login-screen').style.display = 'none';
  document.getElementById('app').style.display = 'flex';
}

function logout() {
  token = null;
  currentUser = null;
  localStorage.removeItem(JWT_KEY);
  showLogin();
}

// ═══════════════════════════════════════════════════════════
//  INIT
// ═══════════════════════════════════════════════════════════
async function init() {
  captureTokenFromUrl();
  document.getElementById('google-login-btn').href = `${API_BASE}/oauth2/authorization/google`;

  if (!token) { showLogin(); return; }

  try {
    currentUser = await getMe();
  } catch (e) {
    showLogin(e.message);
    return;
  }

  document.getElementById('user-name').textContent = currentUser.name || currentUser.email;
  document.getElementById('user-email').textContent = currentUser.email || '';
  document.getElementById('user-avatar').src = currentUser.pictureUrl || '';

  try {
    bikes = await getBikes();
  } catch (e) {
    bikes = [];
    errToast(e);
  }

  showApp();
  populateBikeSelector();

  if (!bikes.length) {
    toast('Add your first bike to get started');
    return;
  }

  const preferredId = currentUser.activeBikeId || localStorage.getItem(ACTIVE_BIKE_KEY);
  const preferred = bikes.find(b => b.id === preferredId);
  await selectBike(preferred ? preferred.id : bikes[0].id);
}

// ═══════════════════════════════════════════════════════════
//  BIKES
// ═══════════════════════════════════════════════════════════
function populateBikeSelector() {
  const sel = document.getElementById('bikeSelector');
  sel.innerHTML = '';
  if (!bikes.length) {
    const opt = document.createElement('option');
    opt.textContent = 'No bikes yet';
    sel.appendChild(opt);
    return;
  }
  bikes.forEach(b => {
    const opt = document.createElement('option');
    opt.value = b.id;
    opt.textContent = b.name;
    sel.appendChild(opt);
  });
  if (currentBike) sel.value = currentBike.id;
}

async function selectBike(bikeId) {
  currentBike = bikes.find(b => b.id === bikeId) || null;
  if (!currentBike) return;

  localStorage.setItem(ACTIVE_BIKE_KEY, bikeId);
  document.getElementById('bikeSelector').value = bikeId;
  document.getElementById('sidebar-odo').textContent = fmt2(currentBike.currentOdo || 0) + ' km on clock';

  ridesState = { page: 0, size: 10, month: '', data: null };
  petrolState = { page: 0, size: 10, month: '', data: null };
  overallStatsCache = null;
  monthsCache = [];

  try {
    monthsCache = await getMonths(bikeId);
  } catch (e) {
    monthsCache = [];
  }

  populateMonthFilters();
  renderActivePanel();

  // Best-effort: persist as the user's active bike (non-blocking).
  if (currentUser && currentUser.activeBikeId !== bikeId) {
    updateMe({ activeBikeId: bikeId }).then(u => { currentUser = u; }).catch(() => {});
  }
}

function onBikeSwitch() {
  const bikeId = document.getElementById('bikeSelector').value;
  selectBike(bikeId);
}

function toggleAddBikeForm() {
  document.getElementById('add-bike-form').classList.toggle('open');
}

async function createBike() {
  const name = document.getElementById('new-bike-name').value.trim();
  const initialOdo = parseFloat(document.getElementById('new-bike-odo').value) || 0;
  const oilChangeIntervalKm = parseFloat(document.getElementById('new-bike-interval').value) || 2000;

  if (!name) { toast('Enter a bike name'); return; }

  try {
    const bike = await addBikeApi({ name, initialOdo, oilChangeIntervalKm });
    bikes.push(bike);
    populateBikeSelector();
    toggleAddBikeForm();
    document.getElementById('new-bike-name').value = '';
    document.getElementById('new-bike-odo').value = '0';
    document.getElementById('new-bike-interval').value = '2000';
    await selectBike(bike.id);
    toast('Bike added ✓');
  } catch (e) {
    errToast(e);
  }
}

async function deleteBike() {
  if (!currentBike) return;
  if (!confirm(`Delete "${currentBike.name}" and all its rides, petrol, oil, tyre, and service records? This can't be undone.`)) return;

  try {
    await deleteBikeApi(currentBike.id);
    bikes = bikes.filter(b => b.id !== currentBike.id);
    toast('Bike deleted ✓');
    populateBikeSelector();
    if (bikes.length) {
      await selectBike(bikes[0].id);
    } else {
      currentBike = null;
      document.getElementById('sidebar-odo').textContent = 'No bikes yet';
    }
  } catch (e) {
    errToast(e);
  }
}

// ═══════════════════════════════════════════════════════════
//  NAVIGATION
// ═══════════════════════════════════════════════════════════
function nav(id, el) {
  document.querySelectorAll('.panel').forEach(p => p.classList.remove('active'));
  document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
  document.getElementById('panel-' + id).classList.add('active');
  el.classList.add('active');
  renderActivePanel();
}

function activePanelId() {
  const active = document.querySelector('.panel.active');
  return active ? active.id.replace('panel-', '') : 'dashboard';
}

function renderActivePanel() {
  if (!currentBike) return;
  const id = activePanelId();
  if (id === 'dashboard') renderDashboard();
  if (id === 'overall') renderOverall();
  if (id === 'rides') renderRides();
  if (id === 'petrol') renderPetrolLog();
  if (id === 'oil') renderOil();
  if (id === 'tyre') renderTyre();
  if (id === 'service') renderService();
}

function populateMonthFilters() {
  const populate = (sel) => {
    const current = sel.value;
    sel.innerHTML = '<option value="">All months</option>';
    monthsCache.forEach(m => {
      const opt = document.createElement('option');
      opt.value = m;
      opt.textContent = fmtMonthLabel(m);
      sel.appendChild(opt);
    });
    if (current && monthsCache.includes(current)) sel.value = current;
  };
  populate(document.getElementById('ridesMonthFilter'));
  populate(document.getElementById('petrolMonthFilter'));

  const dashSel = document.getElementById('monthSelector');
  const current = dashSel.value;
  dashSel.innerHTML = '';
  monthsCache.forEach(m => {
    const opt = document.createElement('option');
    opt.value = m;
    opt.textContent = fmtMonthLabel(m);
    dashSel.appendChild(opt);
  });
  if (current && monthsCache.includes(current)) dashSel.value = current;
}

// ═══════════════════════════════════════════════════════════
//  DASHBOARD
// ═══════════════════════════════════════════════════════════
async function loadOverallStats(force) {
  if (overallStatsCache && !force) return overallStatsCache;
  overallStatsCache = await getOverallStats(currentBike.id);
  return overallStatsCache;
}

async function renderDashboard() {
  const monthSel = document.getElementById('monthSelector');
  if (!monthSel.value && monthsCache.length) monthSel.value = monthsCache[0];
  const month = monthSel.value;
  if (!month) return;

  let dash;
  try {
    dash = await getMonthlyDashboard(currentBike.id, month);
  } catch (e) {
    errToast(e);
    return;
  }

  document.getElementById('mon-km').textContent = fmt2(dash.kmDrivenThisMonth);
  document.getElementById('mon-litres').textContent = fmt2(dash.litresThisMonth);
  document.getElementById('mon-current-mileage').textContent = dash.currentMileage == null ? '—' : fmt2(dash.currentMileage);
  document.getElementById('mon-max-mileage').textContent = dash.bestMileageThisMonth == null ? '—' : fmt2(dash.bestMileageThisMonth);
  document.getElementById('mon-spend').textContent = '₹' + fmt2(dash.spendThisMonth);
  document.getElementById('mon-days').textContent = dash.ridingDaysThisMonth || 0;
  document.getElementById('mon-rides').textContent = dash.totalRidesThisMonth || 0;
  document.getElementById('total-odo').textContent = fmt2(dash.totalOdo);
  document.getElementById('sidebar-odo').textContent = fmt2(dash.totalOdo) + ' km on clock';

  // Rings, scaled against the best month on record.
  try {
    const overall = await loadOverallStats();
    const circumference = 2 * Math.PI * 42;
    const maxKm = Math.max(...overall.monthlyBreakdown.map(b => b.km || 0), 1);
    const maxLitres = Math.max(...overall.monthlyBreakdown.map(b => b.litres || 0), 1);
    const kmDash = (dash.kmDrivenThisMonth / maxKm) * circumference;
    const lDash = (dash.litresThisMonth / maxLitres) * circumference;
    document.getElementById('ring-km-arc').style.strokeDasharray = `${kmDash.toFixed(2)} ${(circumference - kmDash).toFixed(2)}`;
    document.getElementById('ring-petrol-arc').style.strokeDasharray = `${lDash.toFixed(2)} ${(circumference - lDash).toFixed(2)}`;
  } catch (e) { /* rings are cosmetic — skip silently */ }

  renderOilBanner('oil-banner-dash');
}

// ═══════════════════════════════════════════════════════════
//  OVERALL STATS
// ═══════════════════════════════════════════════════════════
async function renderOverall() {
  let overall;
  try {
    overall = await loadOverallStats(true);
  } catch (e) {
    errToast(e);
    return;
  }

  document.getElementById('ov-km').textContent = fmt2(overall.totalKm) + ' km';
  document.getElementById('ov-litres').textContent = fmt2(overall.totalLitres) + ' L';
  document.getElementById('ov-spend').textContent = '₹' + fmt2(overall.totalSpend);
  document.getElementById('ov-avg').textContent = overall.overallAvgMileage == null ? '—' : fmt2(overall.overallAvgMileage) + ' km/L';
  document.getElementById('ov-max-mileage').textContent = overall.overallMaxMileage == null ? '—' : fmt2(overall.overallMaxMileage) + ' km/L';

  const breakdown = overall.monthlyBreakdown || [];
  const labels = breakdown.map(b => {
    const [y, mo] = b.month.split('-');
    return new Date(y, parseInt(mo) - 1, 1).toLocaleString('default', { month: 'short', year: '2-digit' });
  });
  const kmData = breakdown.map(b => round2(b.km));
  const petrolData = breakdown.map(b => round2(b.litres));
  const spendData = breakdown.map(b => round2(b.spend));

  const baseOpts = {
    responsive: true, maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: {
      x: { ticks: { color: '#5e5c58', font: { size: 11 }, autoSkip: false, maxRotation: 45 }, grid: { display: false }, border: { color: '#2a2a2a' } },
      y: { ticks: { color: '#5e5c58', font: { size: 11 } }, grid: { color: '#1e1e1e' }, border: { display: false } }
    }
  };

  if (chartKm) chartKm.destroy();
  chartKm = new Chart(document.getElementById('chart-km'), {
    type: 'bar',
    data: { labels, datasets: [{ data: kmData, backgroundColor: '#4a9eff', borderRadius: 4, borderSkipped: 'bottom' }] },
    options: { ...JSON.parse(JSON.stringify(baseOpts)), scales: { ...JSON.parse(JSON.stringify(baseOpts.scales)), y: { ...JSON.parse(JSON.stringify(baseOpts.scales.y)), ticks: { color: '#5e5c58', font: { size: 11 }, callback: v => fmt2(v) + ' km' } } } }
  });

  if (chartPetrol) chartPetrol.destroy();
  chartPetrol = new Chart(document.getElementById('chart-petrol'), {
    type: 'bar',
    data: { labels, datasets: [{ data: petrolData, backgroundColor: '#f0b429', borderRadius: 4, borderSkipped: 'bottom' }] },
    options: { ...JSON.parse(JSON.stringify(baseOpts)), scales: { ...JSON.parse(JSON.stringify(baseOpts.scales)), y: { ...JSON.parse(JSON.stringify(baseOpts.scales.y)), ticks: { color: '#5e5c58', font: { size: 11 }, callback: v => fmt2(v) + ' L' } } } }
  });

  if (chartSpend) chartSpend.destroy();
  chartSpend = new Chart(document.getElementById('chart-spend'), {
    type: 'bar',
    data: { labels, datasets: [{ data: spendData, backgroundColor: '#e86c2f', borderRadius: 4, borderSkipped: 'bottom' }] },
    options: { ...JSON.parse(JSON.stringify(baseOpts)), scales: { ...JSON.parse(JSON.stringify(baseOpts.scales)), y: { ...JSON.parse(JSON.stringify(baseOpts.scales.y)), ticks: { color: '#5e5c58', font: { size: 11 }, callback: v => '₹' + fmt2(v) } } } }
  });
}

// ═══════════════════════════════════════════════════════════
//  OIL STATUS BANNER (dashboard + oil panel)
// ═══════════════════════════════════════════════════════════
async function renderOilBanner(containerId) {
  const el = document.getElementById(containerId);
  if (!el) return;
  el.innerHTML = '<div class="spinner-row">Loading oil status…</div>';

  let status;
  try {
    status = await getOilStatus(currentBike.id);
  } catch (e) {
    el.innerHTML = `<div class="oil-banner warn" style="margin-bottom:1.5rem">
      <div class="oil-icon">🔧</div>
      <div class="oil-text">
        <div class="oil-title">Oil status isn't available yet</div>
        <div class="oil-sub">This endpoint hasn't been implemented on the backend yet.</div>
      </div>
    </div>`;
    return;
  }

  if (!status || status.status === 'NO_DATA') {
    el.innerHTML = `<div class="oil-banner warn" style="margin-bottom:1.5rem">
      <div class="oil-icon">🔧</div>
      <div class="oil-text">
        <div class="oil-title">No oil change recorded yet</div>
        <div class="oil-sub">Add your first oil change record to start tracking</div>
      </div>
    </div>`;
    return;
  }

  const clsMap = { OK: 'ok', DUE_SOON: 'warn', OVERDUE: 'due' };
  const iconMap = { OK: '✅', DUE_SOON: '⚠️', OVERDUE: '🚨' };
  const cls = clsMap[status.status] || 'ok';
  const icon = iconMap[status.status] || '✅';
  const barColor = cls === 'ok' ? 'var(--green)' : cls === 'warn' ? 'var(--yellow)' : 'var(--red)';
  const pct = status.percentUsed == null ? 0 : Math.min(100, status.percentUsed);

  el.innerHTML = `<div class="oil-banner ${cls}" style="margin-bottom:1.5rem">
    <div class="oil-icon">${icon}</div>
    <div class="oil-text" style="flex:1">
      <div class="oil-title">${status.message || ''}</div>
      <div class="oil-sub">Last changed at ${status.lastChangeOdo != null ? fmt2(status.lastChangeOdo) + ' km' : '—'} on ${fmtDate(status.lastChangeDate)}</div>
      <div class="oil-progress"><div class="oil-bar" style="width:${pct.toFixed(2)}%;background:${barColor}"></div></div>
    </div>
    <div style="text-align:right;min-width:60px">
      <div style="font-size:18px;font-weight:600">${status.kmSinceLastChange != null ? fmt2(status.kmSinceLastChange) : '—'}</div>
      <div style="font-size:10px;color:var(--text3)">km since</div>
    </div>
  </div>`;
}

// ═══════════════════════════════════════════════════════════
//  RIDES
// ═══════════════════════════════════════════════════════════
function onRidesMonthChange() {
  ridesState.month = document.getElementById('ridesMonthFilter').value;
  ridesState.page = 0;
  renderRides();
}

function ridesPage(delta) {
  ridesState.page += delta;
  renderRides();
}

async function renderRides() {
  const container = document.getElementById('rides-table');
  container.innerHTML = '<div class="spinner-row">Loading rides…</div>';
  document.getElementById('rides-pagination').innerHTML = '';

  let page;
  try {
    page = await getRides(currentBike.id, { month: ridesState.month, page: ridesState.page, size: ridesState.size });
  } catch (e) {
    container.innerHTML = `<div class="empty"><div class="e-icon">🛣️</div>Couldn't load rides — ${e.message}</div>`;
    return;
  }
  ridesState.data = page;

  if (!page.content.length) {
    container.innerHTML = '<div class="empty"><div class="e-icon">🛣️</div>No rides logged yet</div>';
    return;
  }

  let html = '<div class="tbl-wrap"><table><thead><tr><th>Date</th><th>Time</th><th>Odometer</th><th>Distance</th><th></th></tr></thead><tbody>';
  page.content.forEach(ride => {
    html += `<tr>
      <td class="primary">${fmtDate(ride.date)}</td>
      <td>${ride.time || '—'}</td>
      <td>${fmt2(ride.odo)} km</td>
      <td><span class="badge badge-ride">+${fmt2(ride.distanceKm)} km</span></td>
      <td><button class="del-btn" onclick="delRide('${ride.id}')">✕</button></td>
    </tr>`;
  });
  html += '</tbody></table></div>';
  container.innerHTML = html;

  renderPagination('rides-pagination', page, () => ridesPage(-1), () => ridesPage(1));
}

function renderPagination(containerId, page, onPrev, onNext) {
  const el = document.getElementById(containerId);
  if (page.totalPages <= 1) { el.innerHTML = ''; return; }
  el.innerHTML = `
    <button class="btn btn-sm" ${page.page <= 0 ? 'disabled' : ''} id="${containerId}-prev">‹ Prev</button>
    <span class="pg-info">Page ${page.page + 1} of ${page.totalPages}</span>
    <button class="btn btn-sm" ${page.page >= page.totalPages - 1 ? 'disabled' : ''} id="${containerId}-next">Next ›</button>
  `;
  document.getElementById(`${containerId}-prev`).onclick = onPrev;
  document.getElementById(`${containerId}-next`).onclick = onNext;
}

async function delRide(rideId) {
  if (!confirm('Delete this ride?')) return;
  try {
    await deleteRideApi(currentBike.id, rideId);
    toast('Ride deleted');
    overallStatsCache = null;
    await refreshCurrentBike();
    renderRides();
  } catch (e) {
    errToast(e);
  }
}

// ── Ride hint ──
function updateRideHint(odo) {
  const hint = document.getElementById('ride-hint');
  if (!odo || !currentBike) { hint.textContent = ''; return; }
  const lastOdo = currentBike.currentOdo || currentBike.initialOdo || 0;
  const diff = round2(odo - lastOdo);
  if (diff > 0) hint.textContent = `+${fmt2(diff)} km from current reading (${fmt2(lastOdo)} km)`;
  else if (diff === 0) hint.textContent = 'Same as current reading';
  else hint.textContent = `⚠ Less than current reading (${fmt2(lastOdo)} km)`;
}
document.addEventListener('input', (e) => {
  if (e.target && e.target.id === 'ride-odo') updateRideHint(parseFloat(e.target.value));
});

async function addRide() {
  const date = document.getElementById('ride-date').value;
  const time = document.getElementById('ride-time').value;
  const odo = parseFloat(document.getElementById('ride-odo').value);
  if (!date) { toast('Select a date'); return; }
  if (!time) { toast('Enter the time'); return; }
  if (!odo || odo <= 0) { toast('Enter a valid odometer reading'); return; }

  try {
    await addRideApi(currentBike.id, { date, time, odo });
    document.getElementById('ride-odo').value = '';
    document.getElementById('ride-hint').textContent = '';
    overallStatsCache = null;
    await refreshCurrentBike();
    await refreshMonths();
    renderActivePanel();
    toast('Ride logged ✓');
  } catch (e) {
    errToast(e);
  }
}

// ═══════════════════════════════════════════════════════════
//  PETROL
// ═══════════════════════════════════════════════════════════
function onPetrolMonthChange() {
  petrolState.month = document.getElementById('petrolMonthFilter').value;
  petrolState.page = 0;
  renderPetrolLog();
}

function petrolPage(delta) {
  petrolState.page += delta;
  renderPetrolLog();
}

async function renderPetrolLog() {
  const container = document.getElementById('petrol-table');
  container.innerHTML = '<div class="spinner-row">Loading petrol log…</div>';
  document.getElementById('petrol-pagination').innerHTML = '';

  let page;
  try {
    page = await getPetrolEntries(currentBike.id, { month: petrolState.month, page: petrolState.page, size: petrolState.size });
  } catch (e) {
    container.innerHTML = `<div class="empty"><div class="e-icon">⛽</div>Couldn't load petrol entries — ${e.message}</div>`;
    return;
  }
  petrolState.data = page;

  if (!page.content.length) {
    container.innerHTML = '<div class="empty"><div class="e-icon">⛽</div>No petrol entries yet</div>';
    return;
  }

  const totalL = round2(page.content.reduce((s, p) => s + (p.litres || 0), 0));
  const totalSpend = round2(page.content.reduce((s, p) => s + (p.amount || 0), 0));

  let html = `<div style="display:flex;gap:1rem;margin-bottom:1rem;flex-wrap:wrap">
    <div class="stat-tile" style="flex:1;min-width:120px"><div class="num">${fmt2(totalL)} L</div><div class="desc">litres (this page)</div></div>
    <div class="stat-tile" style="flex:1;min-width:120px"><div class="num">₹${fmt2(totalSpend)}</div><div class="desc">spent (this page)</div></div>
  </div>
  <div class="tbl-wrap"><table><thead><tr><th>Date</th><th>Odometer</th><th>Amount paid</th><th>Price/L</th><th>Litres added</th><th>Total litres</th><th>Mileage</th><th></th></tr></thead><tbody>`;

  page.content.forEach(p => {
    html += `<tr>
      <td class="primary">${fmtDate(p.date)}</td>
      <td>${typeof p.odo === 'number' ? fmt2(p.odo) + ' km' : '—'}</td>
      <td>₹${fmt2(p.amount)}</td>
      <td>₹${fmt2(p.pricePerLitre)}/L</td>
      <td><span class="badge badge-petrol">${fmt2(p.litres)} L</span></td>
      <td style="color:var(--text2)">${fmt2(p.cumulativeLitres)} L</td>
      <td>${p.mileageKmPerLitre != null ? `<span class="badge badge-ride">${fmt2(p.mileageKmPerLitre)} km/L</span>` : '—'}</td>
      <td><button class="del-btn" onclick="delPetrol('${p.id}')">✕</button></td>
    </tr>`;
  });
  html += '</tbody></table></div>';
  container.innerHTML = html;

  renderPagination('petrol-pagination', page, () => petrolPage(-1), () => petrolPage(1));
}

async function delPetrol(petrolEntryId) {
  if (!confirm('Delete this petrol entry?')) return;
  try {
    await deletePetrolApi(currentBike.id, petrolEntryId);
    toast('Entry deleted');
    overallStatsCache = null;
    await refreshCurrentBike();
    renderPetrolLog();
  } catch (e) {
    errToast(e);
  }
}

function updatePetrolHint() {
  const a = parseFloat(document.getElementById('petrol-amount').value);
  const price = parseFloat(document.getElementById('petrol-price').value) || 0;
  const h = document.getElementById('petrol-calc-hint');
  if (a > 0 && price > 0) h.textContent = `= ${fmt2(a / price)} litres @ ₹${fmt2(price)}/L`;
  else h.textContent = '';
}

async function addPetrol() {
  const date = document.getElementById('petrol-date').value;
  const odo = parseFloat(document.getElementById('petrol-odo').value);
  const amount = parseFloat(document.getElementById('petrol-amount').value);
  const pricePerLitre = parseFloat(document.getElementById('petrol-price').value);

  if (!date) { toast('Select a date'); return; }
  if (!odo || odo <= 0) { toast('Enter a valid odometer reading'); return; }
  if (!amount || amount <= 0) { toast('Enter amount paid'); return; }
  if (!pricePerLitre || pricePerLitre <= 0) { toast('Enter price per litre'); return; }

  try {
    const saved = await addPetrolApi(currentBike.id, { date, odo, amount, pricePerLitre });
    document.getElementById('petrol-odo').value = '';
    document.getElementById('petrol-amount').value = '';
    document.getElementById('petrol-calc-hint').textContent = '';
    overallStatsCache = null;
    await refreshCurrentBike();
    await refreshMonths();
    renderActivePanel();
    toast(`Added ${fmt2(saved.litres)}L petrol ✓`);
  } catch (e) {
    errToast(e);
  }
}

// ═══════════════════════════════════════════════════════════
//  ENGINE OIL
// ═══════════════════════════════════════════════════════════
async function renderOil() {
  document.getElementById('oil-sub').textContent =
    `Change reminder every ${fmt2(currentBike.oilChangeIntervalKm || 2000)} km`;
  renderOilBanner('oil-banner-main');

  const container = document.getElementById('oil-table');
  container.innerHTML = '<div class="spinner-row">Loading oil change history…</div>';
  try {
    const page = await getOilChanges(currentBike.id, { page: 0, size: 50 });
    const list = (page && page.content) || [];
    if (!list.length) {
      container.innerHTML = '<div class="empty"><div class="e-icon">🔧</div>No oil changes recorded yet</div>';
      return;
    }
    const sorted = [...list].sort((a, b) => new Date(b.date) - new Date(a.date));
    let html = '<div class="tbl-wrap"><table><thead><tr><th>Date</th><th>Odometer</th><th>Notes</th><th></th></tr></thead><tbody>';
    sorted.forEach((o, i) => {
      html += `<tr>
        <td class="primary">${fmtDate(o.date)}${i === 0 ? ' <span class="badge badge-oil">Latest</span>' : ''}</td>
        <td>${fmt2(o.odo)} km</td>
        <td style="color:var(--text2)">${o.notes || '—'}</td>
        <td><button class="del-btn" onclick="delOil('${o.id}')">✕</button></td>
      </tr>`;
    });
    html += '</tbody></table></div>';
    container.innerHTML = html;
  } catch (e) {
    container.innerHTML = `<div class="empty"><div class="e-icon">🔧</div>Oil change history isn't available yet — backend endpoint not implemented.</div>`;
  }
}

async function addOilChange() {
  const date = document.getElementById('oil-date').value;
  const odo = parseFloat(document.getElementById('oil-odo').value);
  const notes = document.getElementById('oil-notes').value.trim();
  if (!date || !odo) { toast('Enter date and odometer reading'); return; }

  try {
    await addOilChangeApi(currentBike.id, { date, odo, notes });
    document.getElementById('oil-date').value = '';
    document.getElementById('oil-odo').value = '';
    document.getElementById('oil-notes').value = '';
    renderOil();
    toast('Oil change recorded ✓');
  } catch (e) {
    errToast(e);
  }
}

async function delOil(oilChangeId) {
  if (!confirm('Delete this oil change record?')) return;
  try {
    await deleteOilChangeApi(currentBike.id, oilChangeId);
    toast('Oil record deleted');
    renderOil();
  } catch (e) {
    errToast(e);
  }
}

// ═══════════════════════════════════════════════════════════
//  TYRE PRESSURE
// ═══════════════════════════════════════════════════════════
async function renderTyre() {
  try {
    const status = await getTyreStatus(currentBike.id);
    fillTyreSide('front', status && status.front);
    fillTyreSide('rear', status && status.rear);
  } catch (e) {
    fillTyreSide('front', null, true);
    fillTyreSide('rear', null, true);
  }

  const container = document.getElementById('tyre-table');
  container.innerHTML = '<div class="spinner-row">Loading pressure log…</div>';
  try {
    const page = await getTyreChecks(currentBike.id, { page: 0, size: 50 });
    const list = (page && page.content) || [];
    if (!list.length) {
      container.innerHTML = '<div class="empty"><div class="e-icon">🔵</div>No tyre checks recorded yet</div>';
      return;
    }
    const sorted = [...list].sort((a, b) => new Date(b.date) - new Date(a.date));
    let html = '<div class="tbl-wrap"><table><thead><tr><th>Date</th><th>Tyre</th><th>Pressure</th><th>Notes</th><th></th></tr></thead><tbody>';
    sorted.forEach(t => {
      html += `<tr>
        <td class="primary">${fmtDate(t.date)}</td>
        <td><span class="badge badge-tyre">${t.tyre.charAt(0).toUpperCase() + t.tyre.slice(1)}</span></td>
        <td>${t.psi ? fmt2(t.psi) + ' PSI' : '—'}</td>
        <td style="color:var(--text2)">${t.notes || '—'}</td>
        <td><button class="del-btn" onclick="delTyre('${t.id}')">✕</button></td>
      </tr>`;
    });
    html += '</tbody></table></div>';
    container.innerHTML = html;
  } catch (e) {
    container.innerHTML = `<div class="empty"><div class="e-icon">🔵</div>Tyre check history isn't available yet — backend endpoint not implemented.</div>`;
  }
}

function fillTyreSide(side, sideStatus, unavailable) {
  const dateEl = document.getElementById(`tyre-${side}-date`);
  const noteEl = document.getElementById(`tyre-${side}-note`);
  if (unavailable) {
    dateEl.textContent = 'Status not available yet';
    noteEl.textContent = '';
    return;
  }
  if (!sideStatus || !sideStatus.lastCheckedDate) {
    dateEl.textContent = 'Never recorded';
    noteEl.textContent = '';
    return;
  }
  dateEl.textContent = `Last checked: ${fmtDate(sideStatus.lastCheckedDate)}`;
  noteEl.textContent = sideStatus.psi
    ? `${fmt2(sideStatus.psi)} PSI` + (sideStatus.notes ? ' · ' + sideStatus.notes : '')
    : (sideStatus.notes || '');
}

async function addTyreCheck() {
  const date = document.getElementById('tyre-date').value;
  const tyre = document.getElementById('tyre-which').value;
  const psi = document.getElementById('tyre-psi').value;
  const notes = document.getElementById('tyre-notes').value.trim();
  if (!date) { toast('Select a date'); return; }

  try {
    await addTyreCheckApi(currentBike.id, { date, tyre, psi: psi ? parseFloat(psi) : null, notes });
    document.getElementById('tyre-psi').value = '';
    document.getElementById('tyre-notes').value = '';
    renderTyre();
    toast('Tyre check saved ✓');
  } catch (e) {
    errToast(e);
  }
}

async function delTyre(tyreCheckId) {
  if (!confirm('Delete this tyre check?')) return;
  try {
    await deleteTyreCheckApi(currentBike.id, tyreCheckId);
    toast('Tyre record deleted');
    renderTyre();
  } catch (e) {
    errToast(e);
  }
}

// ═══════════════════════════════════════════════════════════
//  SERVICE HISTORY
// ═══════════════════════════════════════════════════════════
async function renderService() {
  const container = document.getElementById('service-list');
  container.innerHTML = '<div class="spinner-row">Loading service history…</div>';
  try {
    const page = await getServices(currentBike.id, { page: 0, size: 50 });
    const list = (page && page.content) || [];
    if (!list.length) {
      container.innerHTML = '<div class="empty"><div class="e-icon">🛠️</div>No service records yet</div>';
      return;
    }
    const sorted = [...list].sort((a, b) => new Date(b.date) - new Date(a.date));
    const totalCost = round2(sorted.reduce((s, sv) => s + (sv.cost || 0), 0));

    let html = '';
    if (totalCost > 0) {
      html += `<div style="display:flex;gap:1rem;margin-bottom:1rem">
        <div class="stat-tile" style="flex:1"><div class="num">₹${fmt2(totalCost)}</div><div class="desc">total service cost</div></div>
        <div class="stat-tile" style="flex:1"><div class="num">${sorted.length}</div><div class="desc">service records</div></div>
      </div>`;
    }

    sorted.forEach((sv, i) => {
      const isRecent = i === 0;
      html += `<div class="service-item">
        <div class="service-dot${isRecent ? '' : ' old'}"></div>
        <div class="service-info">
          <div class="service-title">${sv.title} ${isRecent ? '<span class="badge badge-service">Latest</span>' : ''}</div>
          <div class="service-meta">
            ${fmtDate(sv.date)}
            ${sv.odo ? `<span class="sep">·</span>${fmt2(sv.odo)} km` : ''}
            ${sv.cost ? `<span class="sep">·</span>₹${fmt2(sv.cost)}` : ''}
            ${sv.shop ? `<span class="sep">·</span>${sv.shop}` : ''}
          </div>
          ${sv.notes ? `<div class="service-desc">${sv.notes}</div>` : ''}
        </div>
        <button class="del-btn" onclick="delService('${sv.id}')">✕</button>
      </div>`;
    });
    container.innerHTML = html;
  } catch (e) {
    container.innerHTML = `<div class="empty"><div class="e-icon">🛠️</div>Service history isn't available yet — backend endpoint not implemented.</div>`;
  }
}

async function addService() {
  const date = document.getElementById('svc-date').value;
  const odo = parseFloat(document.getElementById('svc-odo').value) || null;
  const cost = parseFloat(document.getElementById('svc-cost').value) || null;
  const title = document.getElementById('svc-title').value.trim();
  const notes = document.getElementById('svc-notes').value.trim();
  const shop = document.getElementById('svc-shop').value.trim();
  if (!date || !title) { toast('Date and service type are required'); return; }

  try {
    await addServiceApi(currentBike.id, { date, odo, cost, title, notes, shop });
    document.getElementById('svc-date').value = '';
    document.getElementById('svc-odo').value = '';
    document.getElementById('svc-cost').value = '';
    document.getElementById('svc-title').value = '';
    document.getElementById('svc-notes').value = '';
    document.getElementById('svc-shop').value = '';
    renderService();
    toast('Service record saved ✓');
  } catch (e) {
    errToast(e);
  }
}

async function delService(serviceId) {
  if (!confirm('Delete this service record?')) return;
  try {
    await deleteServiceApi(currentBike.id, serviceId);
    toast('Service record deleted');
    renderService();
  } catch (e) {
    errToast(e);
  }
}

// ═══════════════════════════════════════════════════════════
//  MISC HELPERS
// ═══════════════════════════════════════════════════════════
async function refreshCurrentBike() {
  try {
    const fresh = await getBike(currentBike.id);
    currentBike = fresh;
    const idx = bikes.findIndex(b => b.id === fresh.id);
    if (idx >= 0) bikes[idx] = fresh;
    document.getElementById('sidebar-odo').textContent = fmt2(fresh.currentOdo || 0) + ' km on clock';
  } catch (e) { /* non-fatal */ }
}

async function refreshMonths() {
  try {
    monthsCache = await getMonths(currentBike.id);
    populateMonthFilters();
  } catch (e) { /* non-fatal */ }
}

// ─── Defaults ───
function setTodayDefaults() {
  const today = new Date().toISOString().slice(0, 10);
  const now = new Date().toTimeString().slice(0, 5);
  ['ride-date', 'petrol-date', 'oil-date', 'tyre-date', 'svc-date'].forEach(id => {
    const el = document.getElementById(id);
    if (el) el.value = today;
  });
  const rt = document.getElementById('ride-time');
  if (rt) rt.value = now;
}

setTodayDefaults();
init();
