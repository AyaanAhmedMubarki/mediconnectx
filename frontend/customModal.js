/**
 * Custom Modal System for MediConnectX
 * Replaces native alert() and confirm() with themed custom modals
 * Auto-detects user role (Patient/Doctor/Admin) and applies appropriate colors
 */

// Color themes by role
const MODAL_THEMES = {
  PATIENT: {
    headerBg: '#003d3d',
    primaryBtn: '#006666',
    primaryBtnHover: '#008080',
    secondaryBtn: '#ccc',
    secondaryBtnHover: '#bbb',
    borderAccent: '#00b3b3',
    modalBg: '#fff'
  },
  DOCTOR: {
    headerBg: '#012b40',
    primaryBtn: '#01486b',
    primaryBtnHover: '#01556f',
    secondaryBtn: '#888',
    secondaryBtnHover: '#777',
    borderAccent: '#2980b9',
    modalBg: '#fff'
  },
  ADMIN: {
    headerBg: '#222',
    primaryBtn: '#333',
    primaryBtnHover: '#444',
    secondaryBtn: '#999',
    secondaryBtnHover: '#888',
    borderAccent: '#666',
    modalBg: '#fff'
  }
};

// Detect current user role from auth object
function getCurrentTheme() {
  try {
    const authStr = localStorage.getItem('auth');
    if (!authStr) return MODAL_THEMES.PATIENT; // Default to patient

    const auth = JSON.parse(authStr);
    if (auth.role === 'ADMIN') return MODAL_THEMES.ADMIN;
    if (auth.role === 'DOCTOR') return MODAL_THEMES.DOCTOR;
    return MODAL_THEMES.PATIENT;
  } catch (e) {
    return MODAL_THEMES.PATIENT;
  }
}

// Initialize modal HTML if not already present
function initializeModal() {
  if (document.getElementById('customModalOverlay')) {
    return; // Already initialized
  }

  const modalHTML = `
    <!-- Custom Modal System -->
    <div class="custom-modal-overlay" id="customModalOverlay">
      <div class="custom-modal" id="customModal">
        <div class="custom-modal-header" id="modalHeader">
          <h5 id="modalTitle"></h5>
          <button class="custom-modal-close" onclick="closeCustomModal()">×</button>
        </div>
        <div class="custom-modal-body">
          <p id="modalMessage"></p>
        </div>
        <div class="custom-modal-footer" id="modalFooter">
          <button class="custom-modal-btn custom-modal-btn-secondary" id="btnCancel">Cancel</button>
          <button class="custom-modal-btn custom-modal-btn-primary" id="btnConfirm">Confirm</button>
        </div>
      </div>
    </div>

    <style>
      .custom-modal-overlay {
        display: none;
        position: fixed;
        inset: 0;
        background: rgba(0, 0, 0, 0.55);
        z-index: 9999;
        align-items: center;
        justify-content: center;
        animation: fadeIn 0.2s ease;
      }

      .custom-modal-overlay.show {
        display: flex;
      }

      @keyframes fadeIn {
        from { opacity: 0; }
        to { opacity: 1; }
      }

      @keyframes slideUp {
        from {
          opacity: 0;
          transform: translateY(20px);
        }
        to {
          opacity: 1;
          transform: translateY(0);
        }
      }

      .custom-modal {
        background: #fff;
        border-radius: 14px;
        box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
        width: 90%;
        max-width: 420px;
        max-height: 85vh;
        display: flex;
        flex-direction: column;
        animation: slideUp 0.3s ease;
        overflow: hidden;
      }

      .custom-modal-header {
        padding: 20px 24px;
        border-bottom: 2px solid;
        display: flex;
        align-items: center;
        justify-content: space-between;
      }

      .custom-modal-header h5 {
        margin: 0;
        font-size: 1.1rem;
        font-weight: 700;
        color: #fff;
      }

      .custom-modal-close {
        background: none;
        border: none;
        color: #fff;
        font-size: 1.8rem;
        cursor: pointer;
        padding: 0;
        line-height: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        width: 32px;
        height: 32px;
        transition: transform 0.2s;
      }

      .custom-modal-close:hover {
        transform: scale(1.2);
      }

      .custom-modal-body {
        padding: 24px;
        overflow-y: auto;
        flex: 1;
        color: #333;
        line-height: 1.6;
        font-size: 0.95rem;
      }

      .custom-modal-body p {
        margin: 0;
        white-space: pre-wrap;
        word-wrap: break-word;
      }

      .custom-modal-footer {
        padding: 16px 24px;
        display: flex;
        gap: 12px;
        justify-content: flex-end;
        background: #f9f9f9;
        border-top: 1px solid #e8e8e8;
      }

      .custom-modal-btn {
        padding: 10px 20px;
        border: none;
        border-radius: 6px;
        font-size: 0.95rem;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.2s;
        min-width: 90px;
        text-align: center;
      }

      .custom-modal-btn:active {
        transform: scale(0.98);
      }

      .custom-modal-btn:disabled {
        opacity: 0.6;
        cursor: not-allowed;
        transform: none;
      }

      /* Hidden by default - button visibility controlled by JS */
      .custom-modal-btn-secondary {
        display: none;
      }

      .custom-modal-btn-primary {
        display: none;
      }

      /* Mobile responsiveness */
      @media (max-width: 480px) {
        .custom-modal {
          width: 95%;
          max-width: 100%;
        }

        .custom-modal-header {
          padding: 16px 20px;
        }

        .custom-modal-body {
          padding: 20px;
        }

        .custom-modal-footer {
          padding: 12px 20px;
          flex-direction: column;
        }

        .custom-modal-btn {
          width: 100%;
          min-width: auto;
        }
      }
    </style>
  `;

  document.body.insertAdjacentHTML('beforeend', modalHTML);
}

// Close modal
function closeCustomModal() {
  const overlay = document.getElementById('customModalOverlay');
  if (overlay) {
    overlay.classList.remove('show');
  }
}

// Apply theme colors to modal
function applyTheme(theme) {
  const header = document.getElementById('modalHeader');
  const btnPrimary = document.getElementById('btnConfirm');
  const btnSecondary = document.getElementById('btnCancel');

  if (header) {
    header.style.backgroundColor = theme.headerBg;
    header.style.borderBottomColor = theme.borderAccent;
  }

  if (btnPrimary) {
    btnPrimary.style.backgroundColor = theme.primaryBtn;
    btnPrimary.style.color = '#fff';
    btnPrimary.onmouseenter = () => btnPrimary.style.backgroundColor = theme.primaryBtnHover;
    btnPrimary.onmouseleave = () => btnPrimary.style.backgroundColor = theme.primaryBtn;
  }

  if (btnSecondary) {
    btnSecondary.style.backgroundColor = theme.secondaryBtn;
    btnSecondary.style.color = '#fff';
    btnSecondary.onmouseenter = () => btnSecondary.style.backgroundColor = theme.secondaryBtnHover;
    btnSecondary.onmouseleave = () => btnSecondary.style.backgroundColor = theme.secondaryBtn;
  }
}

/**
 * Show confirmation modal (replaces window.confirm())
 * @param {string} title - Modal title
 * @param {string} message - Modal message
 * @param {function} onConfirm - Callback when user clicks Confirm
 * @param {function} onCancel - Callback when user clicks Cancel (optional)
 */
function showConfirmation(title, message, onConfirm, onCancel) {
  initializeModal();
  const theme = getCurrentTheme();
  applyTheme(theme);

  const overlay = document.getElementById('customModalOverlay');
  const modalTitle = document.getElementById('modalTitle');
  const modalMessage = document.getElementById('modalMessage');
  const btnConfirm = document.getElementById('btnConfirm');
  const btnCancel = document.getElementById('btnCancel');

  modalTitle.textContent = title;
  modalMessage.textContent = message;

  // Show both buttons
  btnConfirm.style.display = 'block';
  btnCancel.style.display = 'block';

  // Reset button text
  btnConfirm.textContent = 'Confirm';
  btnCancel.textContent = 'Cancel';

  // Clear previous event listeners
  btnConfirm.onclick = null;
  btnCancel.onclick = null;

  btnConfirm.onclick = () => {
    closeCustomModal();
    if (onConfirm) onConfirm();
  };

  btnCancel.onclick = () => {
    closeCustomModal();
    if (onCancel) onCancel();
  };

  overlay.classList.add('show');
}

/**
 * Show alert modal (replaces window.alert())
 * @param {string} title - Modal title
 * @param {string} message - Modal message
 * @param {function} onClose - Callback when modal closes (optional)
 */
function showAlert(title, message, onClose) {
  initializeModal();
  const theme = getCurrentTheme();
  applyTheme(theme);

  const overlay = document.getElementById('customModalOverlay');
  const modalTitle = document.getElementById('modalTitle');
  const modalMessage = document.getElementById('modalMessage');
  const btnConfirm = document.getElementById('btnConfirm');
  const btnCancel = document.getElementById('btnCancel');

  modalTitle.textContent = title;
  modalMessage.textContent = message;

  // Show only confirm button
  btnConfirm.style.display = 'block';
  btnCancel.style.display = 'none';

  btnConfirm.textContent = 'OK';

  // Clear previous event listeners
  btnConfirm.onclick = null;
  btnCancel.onclick = null;

  btnConfirm.onclick = () => {
    closeCustomModal();
    if (onClose) onClose();
  };

  overlay.classList.add('show');
}

/**
 * Show success modal
 * @param {string} title - Modal title
 * @param {string} message - Modal message
 * @param {function} onClose - Callback when modal closes (optional)
 */
function showSuccess(title, message, onClose) {
  showAlert(title, message, onClose);
}

/**
 * Show error modal
 * @param {string} title - Modal title
 * @param {string} message - Modal message
 * @param {function} onClose - Callback when modal closes (optional)
 */
function showError(title, message, onClose) {
  showAlert(title, message, onClose);
}

// Allow closing modal by clicking outside
document.addEventListener('click', function(event) {
  const overlay = document.getElementById('customModalOverlay');
  if (overlay && event.target === overlay) {
    closeCustomModal();
  }
});

// Allow closing modal with Escape key
document.addEventListener('keydown', function(event) {
  if (event.key === 'Escape') {
    closeCustomModal();
  }
});
