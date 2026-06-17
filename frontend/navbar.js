/**
 * navbar.js — MediConnectX Centralized Navigation Bar
 *
 * HOW TO USE:
 *   1. Place <div id="navbar-root"></div> as the FIRST element inside <body>
 *   2. Add <script src="navbar.js"></script> immediately after that div
 *
 * WHAT IT DOES:
 *   - Reads the logged-in user's role from localStorage ('auth' key)
 *   - Injects the correct themed <header> for PATIENT / DOCTOR / ADMIN
 *   - Auto-highlights the active link based on the current page filename
 *   - Injects all required CSS once (never duplicated)
 *   - Wires up the mobile hamburger toggle
 *
 * SAFE FALLBACK:
 *   - No auth in localStorage  → does nothing (public pages stay untouched)
 *   - Unknown role             → does nothing
 *   - #navbar-root missing     → does nothing
 *   - localStorage unavailable → does nothing (catches all exceptions)
 */
(function () {
  'use strict';

  /* ─────────────────────────────────────────────────────────────────
     1.  ROLE CONFIGURATION
     ───────────────────────────────────────────────────────────────── */
  var CONFIGS = {
    PATIENT: {
      bg:    '#003d3d',
      logo:  'MediConnectX – Patient',
      links: [
        { text: 'Home',                  href: 'PatientHome.html'           },
        { text: 'Dashboard',             href: 'Patient.html'              },
        { text: 'Events',                href: 'PatientEvents.html'        },
        { text: 'Online Consultation',   href: 'PatientMeets.html'         },
        { text: 'Offline Consultation',  href: 'PatientOfflineBooking.html' },
        { text: 'My Profile',            href: 'PatientProfile.html'       }
      ]
    },
    DOCTOR: {
      bg:    '#012b40',
      logo:  'MediConnectX – Doctor',
      links: [
        { text: 'Home',                  href: 'DoctorHome.html'         },
        { text: 'Dashboard',             href: 'Doctor.html'             },
        { text: 'Events',                href: 'DoctorEvents.html'       },
        { text: 'Availability',          href: 'DoctorAvailability.html' },
        { text: 'Online Consultations',  href: 'DoctorMeets.html'        },
        { text: 'Offline Consultations', href: 'DoctorOfflineConsults.html' },
        { text: 'My Profile',            href: 'DoctorProfile.html'      }
      ]
    },
    ADMIN: {
      bg:    '#111111',
      logo:  'MediConnectX – Admin',
      links: [
        { text: 'Home',                      href: 'AdminHome.html'      },
        { text: 'Dashboard',                 href: 'AdminDashboard.html' },
        { text: 'Manage Events',             href: 'AdminEvents.html'    },
        { text: 'Online Consultations',      href: 'AdminMeets.html'     },
        { text: 'Offline Consultations',     href: 'AdminOfflineConsults.html' },
        { text: 'My Profile',                href: 'AdminProfile.html'   }
      ]
    }
  };

  /* ─────────────────────────────────────────────────────────────────
     2.  SAFELY READ AUTH FROM localStorage
     ───────────────────────────────────────────────────────────────── */
  function getAuth() {
    try {
      var raw = localStorage.getItem('auth');
      return raw ? JSON.parse(raw) : null;
    } catch (e) {
      return null;
    }
  }

  /* ─────────────────────────────────────────────────────────────────
     3.  INJECT SHARED CSS (exactly once per page load)
     ───────────────────────────────────────────────────────────────── */
  function injectCSS() {
    if (document.getElementById('mcx-nav-style')) return; // already injected
    var s = document.createElement('style');
    s.id = 'mcx-nav-style';
    s.textContent =
      /* Reset the root placeholder */
      '#navbar-root{display:block;margin:0;padding:0}' +

      /* Main bar */
      '.mcx-navbar{' +
        'padding:1rem 1.5rem;' +
        'display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;' +
        'position:sticky;top:0;z-index:1000;' +
        'box-shadow:0 4px 8px rgba(0,0,0,.2);' +
        'font-family:"Segoe UI",sans-serif}' +

      /* Logo text */
      '.mcx-logo{color:#fff;font-size:1.4rem;font-weight:700;' +
        'white-space:nowrap;letter-spacing:.01em}' +

      /* Hamburger button (hidden on desktop) */
      '.mcx-hamburger{display:none;background:none;border:none;color:#fff;' +
        'font-size:1.8rem;cursor:pointer;padding:0;line-height:1}' +

      /* Link list */
      '.mcx-nav-links{list-style:none;display:flex;gap:.25rem;' +
        'align-items:center;margin:0;padding:0}' +

      /* Individual links */
      '.mcx-nav-links a{' +
        'color:rgba(255,255,255,.88);text-decoration:none;font-size:.95rem;' +
        'padding:6px 15px;border-radius:20px;' +
        'transition:background .2s,color .2s;' +
        'white-space:nowrap;letter-spacing:.02em;display:block}' +

      /* Hover */
      '.mcx-nav-links a:hover{background:rgba(255,255,255,.15);color:#fff}' +

      /* ── Active "white capsule" highlight ── */
      '.mcx-nav-links a.nav-active{' +
        'background:rgba(255,255,255,.28);color:#fff;font-weight:600}' +

      /* ── Mobile (≤768px) ── */
      '@media(max-width:768px){' +
        '.mcx-logo{font-size:1.1rem}' +
        '.mcx-hamburger{display:block}' +
        '.mcx-navbar nav{position:absolute;top:100%;left:0;width:100%;z-index:99}' +
        '.mcx-nav-links{' +
          'display:none;flex-direction:column;' +
          'padding:.8rem 1.5rem;gap:0;' +
          'box-shadow:0 4px 8px rgba(0,0,0,.3)}' +
        '.mcx-nav-links.mcx-open{display:flex}' +
        '.mcx-nav-links li{border-bottom:1px solid rgba(255,255,255,.1)}' +
        '.mcx-nav-links li:last-child{border-bottom:none}' +
        '.mcx-nav-links a{padding:.65rem 0}' +
      '}';
    document.head.appendChild(s);
  }

  /* ─────────────────────────────────────────────────────────────────
     4.  BUILD THE <header> HTML AND INJECT INTO #navbar-root
     ───────────────────────────────────────────────────────────────── */
  function buildAndInject(config) {
    /* Detect current page for active-link highlighting.
       Uses only the filename (last path segment), lowercase, so it works
       whether the page is served from file://, localhost, or a remote host. */
    var page = window.location.pathname.split('/').pop();
    if (!page) page = 'index.html';
    page = page.toLowerCase();

    /* Build <li> items */
    var items = '';
    for (var i = 0; i < config.links.length; i++) {
      var l      = config.links[i];
      var active = (l.href.toLowerCase() === page) ? ' class="nav-active"' : '';
      items += '<li><a href="' + l.href + '"' + active + '>' + l.text + '</a></li>';
    }

    /* Logout — fully self-contained, no dependency on api.js */
    items +=
      '<li><a href="#" onclick="' +
        '(function(){' +
          'try{localStorage.removeItem(\'auth\');}catch(e){}' +
          'window.location.href=\'Login.html\';' +
        '})();return false;">' +
      'Logout</a></li>';

    /* Assemble the full header string */
    var html =
      '<header class="mcx-navbar" style="background-color:' + config.bg + '">' +
        '<div class="mcx-logo">' + config.logo + '</div>' +
        '<button class="mcx-hamburger" id="mcx-hamburger" aria-label="Toggle navigation">&#9776;</button>' +
        '<nav><ul class="mcx-nav-links" id="mcx-nav-links">' + items + '</ul></nav>' +
      '</header>';

    /* Inject into placeholder */
    var root = document.getElementById('navbar-root');
    if (!root) return;
    root.innerHTML = html;

    /* Apply role colour to mobile dropdown background via inline style
       (CSS variables aren't always inherited through absolute positioning) */
    var navLinks = document.getElementById('mcx-nav-links');
    if (navLinks) {
      navLinks.style.backgroundColor = config.bg;

      /* Hamburger toggle */
      var btn = document.getElementById('mcx-hamburger');
      if (btn) {
        btn.addEventListener('click', function () {
          navLinks.classList.toggle('mcx-open');
        });
      }

      /* Close menu when any link is tapped (important for mobile UX) */
      navLinks.addEventListener('click', function (e) {
        if (e.target.tagName === 'A') {
          navLinks.classList.remove('mcx-open');
        }
      });
    }
  }

  /* ─────────────────────────────────────────────────────────────────
     5.  ENTRY POINT
     ───────────────────────────────────────────────────────────────── */
  function init() {
    var auth = getAuth();
    if (!auth || !auth.role) return;       // not logged in — leave page as-is

    var config = CONFIGS[auth.role];
    if (!config) return;                    // unknown role — leave page as-is

    if (!document.getElementById('navbar-root')) return;  // no placeholder

    injectCSS();
    buildAndInject(config);
  }

  /* Run immediately if the DOM is already available (script placed right
     after #navbar-root), otherwise wait for DOMContentLoaded. */
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }

}());
