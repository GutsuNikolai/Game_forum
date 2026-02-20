// Общие функции для хедера/футера, которые подгружаются через fetch.

function initializeMobileMenu() {
    const menuToggle = document.getElementById('menuToggle');
    const closeMenu = document.getElementById('closeMenu');
    const mobileMenu = document.getElementById('mobileMenu');
    const mobileMenuOverlay = document.getElementById('mobileMenuOverlay');

    if (!menuToggle || !mobileMenu || mobileMenu.dataset.bound === 'true') {
        return;
    }

    function closeMobileMenu() {
        mobileMenu.classList.remove('show');
        document.body.style.overflow = '';
    }

    menuToggle.addEventListener('click', () => {
        mobileMenu.classList.add('show');
        document.body.style.overflow = 'hidden';
    });

    if (closeMenu) {
        closeMenu.addEventListener('click', closeMobileMenu);
    }

    if (mobileMenuOverlay) {
        mobileMenuOverlay.addEventListener('click', closeMobileMenu);
    }

    // Делегирование на случай клика по иконке внутри кнопки закрытия.
    mobileMenu.addEventListener('click', (event) => {
        const clickedCloseButton = event.target.closest('#closeMenu');
        const clickedOverlay = event.target.id === 'mobileMenuOverlay';
        if (clickedCloseButton || clickedOverlay) {
            closeMobileMenu();
        }
    });

    document.addEventListener('keydown', (event) => {
        if (event.key === 'Escape' && mobileMenu.classList.contains('show')) {
            closeMobileMenu();
        }
    });

    mobileMenu.dataset.bound = 'true';
}

function updateHeaderLayout() {
    const headerContainer = document.querySelector('.header-container');
    if (!headerContainer) {
        return;
    }

    const isMobileViewport = window.innerWidth <= 992;
    headerContainer.classList.remove('compact');

    const logo = headerContainer.querySelector('.logo');
    const nav = headerContainer.querySelector('.nav');
    const authButtons = headerContainer.querySelector('.auth-buttons');
    const userProfile = headerContainer.querySelector('.user-profile');

    const styles = window.getComputedStyle(headerContainer);
    const paddingLeft = parseFloat(styles.paddingLeft) || 0;
    const paddingRight = parseFloat(styles.paddingRight) || 0;

    function visibleWidth(element) {
        if (!element) return 0;
        const elementStyles = window.getComputedStyle(element);
        if (elementStyles.display === 'none' || elementStyles.visibility === 'hidden') return 0;
        return element.offsetWidth;
    }

    const logoWidth = visibleWidth(logo);
    const navWidth = visibleWidth(nav);
    const profileWidth = visibleWidth(userProfile);
    const authWidth = visibleWidth(authButtons);
    const rightBlockWidth = profileWidth > 0 ? profileWidth : authWidth;
    const reserveSpace = 40;

    const requiredWidth = paddingLeft + paddingRight + logoWidth + navWidth + rightBlockWidth + reserveSpace;
    const isOverflowing = requiredWidth > headerContainer.clientWidth;

    const shouldCompact = isMobileViewport || isOverflowing;
    headerContainer.classList.toggle('compact', shouldCompact);

    if (!shouldCompact) {
        const mobileMenu = document.getElementById('mobileMenu');
        if (mobileMenu) {
            mobileMenu.classList.remove('show');
        }
        document.body.style.overflow = '';
    }
}

function initializeHeaderLayout() {
    if (!window.__headerLayoutBound) {
        window.addEventListener('resize', updateHeaderLayout);
        window.addEventListener('orientationchange', updateHeaderLayout);
        window.__headerLayoutBound = true;
    }

    updateHeaderLayout();
    window.requestAnimationFrame(updateHeaderLayout);
    window.setTimeout(updateHeaderLayout, 200);
}

function initializeScrollTop() {
    const scrollTopBtn = document.getElementById('scrollTopBtn');
    if (!scrollTopBtn || scrollTopBtn.dataset.bound === 'true') {
        return;
    }

    window.addEventListener('scroll', () => {
        if (window.scrollY > 300) {
            scrollTopBtn.classList.add('show');
        } else {
            scrollTopBtn.classList.remove('show');
        }
    });

    scrollTopBtn.addEventListener('click', () => {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    });

    scrollTopBtn.dataset.bound = 'true';
}

function resetClientAuthState() {
    localStorage.removeItem('token');
    localStorage.removeItem('isLoggedIn');
    localStorage.removeItem('username');
}

function setDesktopGuestActions() {
    const loginBtn = document.getElementById('loginBtn');
    const registerBtn = document.getElementById('registerBtn');
    const logoutBtn = document.getElementById('logoutBtn');

    if (loginBtn) {
        loginBtn.setAttribute('href', '/auth/login.html');
        loginBtn.style.removeProperty('display');
        loginBtn.innerHTML = `
            <i class="fas fa-sign-in-alt"></i>
            <span>Войти</span>
        `;
    }

    if (registerBtn) {
        registerBtn.setAttribute('href', '/auth/register.html');
        registerBtn.style.removeProperty('display');
        registerBtn.innerHTML = `
            <i class="fas fa-user-plus"></i>
            <span>Регистрация</span>
        `;
    }

    if (logoutBtn) {
        logoutBtn.style.setProperty('display', 'none', 'important');
    }
}

function setDesktopAuthenticatedActions() {
    const loginBtn = document.getElementById('loginBtn');
    const registerBtn = document.getElementById('registerBtn');
    const logoutBtn = document.getElementById('logoutBtn');

    if (loginBtn) {
        loginBtn.setAttribute('href', '/auth/profile.html');
        loginBtn.style.removeProperty('display');
        loginBtn.innerHTML = `
            <i class="fas fa-user"></i>
            <span>Профиль</span>
        `;
    }

    if (registerBtn) {
        registerBtn.style.setProperty('display', 'none', 'important');
    }

    if (logoutBtn) {
        logoutBtn.style.removeProperty('display');
    }
}

function setMobileGuestActions() {
    const mobileLoginBtn = document.getElementById('mobileLoginBtn');
    const mobileRegisterBtn = document.getElementById('mobileRegisterBtn');
    const mobileLogoutBtn = document.getElementById('mobileLogoutBtn');

    if (mobileLoginBtn) {
        mobileLoginBtn.setAttribute('href', '/auth/login.html');
        mobileLoginBtn.innerHTML = `
            <i class="fas fa-sign-in-alt"></i>
            <span>Войти</span>
        `;
    }

    if (mobileRegisterBtn) {
        mobileRegisterBtn.setAttribute('href', '/auth/register.html');
        mobileRegisterBtn.style.removeProperty('display');
        mobileRegisterBtn.innerHTML = `
            <i class="fas fa-user-plus"></i>
            <span>Регистрация</span>
        `;
    }

    if (mobileLogoutBtn) {
        mobileLogoutBtn.style.setProperty('display', 'none', 'important');
    }
}

function setMobileAuthenticatedActions() {
    const mobileLoginBtn = document.getElementById('mobileLoginBtn');
    const mobileRegisterBtn = document.getElementById('mobileRegisterBtn');
    const mobileLogoutBtn = document.getElementById('mobileLogoutBtn');

    if (mobileLoginBtn) {
        mobileLoginBtn.setAttribute('href', '/auth/profile.html');
        mobileLoginBtn.innerHTML = `
            <i class="fas fa-user"></i>
            <span>Профиль</span>
        `;
    }

    if (mobileRegisterBtn) {
        mobileRegisterBtn.style.setProperty('display', 'none', 'important');
    }

    if (mobileLogoutBtn) {
        mobileLogoutBtn.style.removeProperty('display');
    }
}

function setHeaderGuestState() {
    const authButtons = document.querySelector('.auth-buttons');
    const userProfile = document.getElementById('userProfile');

    if (authButtons) {
        authButtons.style.removeProperty('display');
    }

    if (userProfile) {
        userProfile.style.display = 'none';
    }

    setDesktopGuestActions();
    setMobileGuestActions();
}

function setHeaderAuthenticatedState(username, role) {
    const authButtons = document.querySelector('.auth-buttons');
    const userProfile = document.getElementById('userProfile');
    const profileName = document.querySelector('.header-profile-name');
    const profileLevel = document.querySelector('.header-profile-level');
    const profileInfo = document.querySelector('.header-profile-info');

    if (authButtons) {
        authButtons.style.removeProperty('display');
    }

    if (userProfile) {
        userProfile.style.display = 'none';
    }

    if (profileName) {
        profileName.textContent = username || localStorage.getItem('username') || 'Игрок';
    }

    if (profileLevel) {
        profileLevel.textContent = role ? `Роль: ${role}` : 'Участник';
    }

    if (profileInfo && profileInfo.dataset.bound !== 'true') {
        profileInfo.style.cursor = 'pointer';
        profileInfo.title = 'Открыть профиль';
        profileInfo.addEventListener('click', () => {
            window.location.href = '/auth/profile.html';
        });
        profileInfo.dataset.bound = 'true';
    }

    setDesktopAuthenticatedActions();
    setMobileAuthenticatedActions();
}

function performClientLogout(event) {
    if (event) {
        event.preventDefault();
    }

    resetClientAuthState();
    setHeaderGuestState();
    updateHeaderLayout();

    const mobileMenu = document.getElementById('mobileMenu');
    if (mobileMenu) {
        mobileMenu.classList.remove('show');
    }
    document.body.style.overflow = '';

    window.location.href = '/home.html';
}

function bindLogoutButtons() {
    const logoutButtons = [
        document.getElementById('logoutBtn'),
        document.getElementById('mobileLogoutBtn'),
        document.querySelector('.header-profile-logout')
    ].filter(Boolean);

    logoutButtons.forEach((button) => {
        if (button.dataset.bound === 'true') {
            return;
        }
        button.addEventListener('click', performClientLogout);
        button.dataset.bound = 'true';
    });
}

async function initializeHeaderAuthState() {
    const headerContainer = document.querySelector('.header-container');
    if (!headerContainer) {
        return;
    }

    const token = localStorage.getItem('token');
    if (!token) {
        setHeaderGuestState();
        updateHeaderLayout();
        return;
    }

    try {
        const response = await fetch('/api/me', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) {
            throw new Error('Unauthorized');
        }

        const data = await response.json();
        if (!data.authenticated) {
            throw new Error('Unauthorized');
        }

        localStorage.setItem('isLoggedIn', 'true');
        if (data.username) {
            localStorage.setItem('username', data.username);
        }

        setHeaderAuthenticatedState(data.username, data.role);
    } catch (_) {
        resetClientAuthState();
        setHeaderGuestState();
    } finally {
        updateHeaderLayout();
    }
}

function initializeHeader() {
    initializeHeaderLayout();
    initializeMobileMenu();
    bindLogoutButtons();
    initializeHeaderAuthState();
}

function initializeFooter() {
    initializeScrollTop();
}

function showNotification(message, type = 'info') {
    const existingNotification = document.querySelector('.notification');
    if (existingNotification) {
        existingNotification.remove();
    }

    const notification = document.createElement('div');
    notification.className = `notification ${type}`;

    let icon = 'fa-info-circle';
    if (type === 'success') icon = 'fa-check-circle';
    if (type === 'error') icon = 'fa-exclamation-circle';
    if (type === 'warning') icon = 'fa-exclamation-triangle';

    notification.innerHTML = `
        <i class="fas ${icon}"></i>
        <span class="notification-message">${message}</span>
    `;

    const notificationStyles = `
        .notification {
            position: fixed;
            top: 20px;
            right: 20px;
            padding: 16px 24px;
            border-radius: 12px;
            display: flex;
            align-items: center;
            gap: 12px;
            z-index: 9999;
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
            backdrop-filter: blur(10px);
            border: 1px solid rgba(255, 255, 255, 0.2);
            transform: translateX(120%);
            transition: transform 0.4s cubic-bezier(0.68, -0.55, 0.265, 1.55);
            max-width: 400px;
            pointer-events: auto;
        }

        .notification.show {
            transform: translateX(0);
        }

        .notification i {
            font-size: 1.2rem;
            flex-shrink: 0;
        }

        .notification.success {
            background: rgba(107, 207, 127, 0.95);
            color: white;
        }

        .notification.error {
            background: rgba(255, 107, 107, 0.95);
            color: white;
        }

        .notification.info {
            background: rgba(100, 181, 246, 0.95);
            color: white;
        }

        .notification.warning {
            background: rgba(255, 193, 7, 0.95);
            color: white;
        }
    `;

    if (!document.querySelector('#notification-styles')) {
        const styleElement = document.createElement('style');
        styleElement.id = 'notification-styles';
        styleElement.textContent = notificationStyles;
        document.head.appendChild(styleElement);
    }

    document.body.appendChild(notification);

    setTimeout(() => {
        notification.classList.add('show');
    }, 10);

    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => {
            if (notification.parentNode) {
                notification.remove();
            }
        }, 400);
    }, 5000);

    notification.addEventListener('click', () => {
        notification.classList.remove('show');
        setTimeout(() => {
            if (notification.parentNode) {
                notification.remove();
            }
        }, 400);
    });

    return notification;
}

document.addEventListener('DOMContentLoaded', function() {
    initializeHeader();
    initializeFooter();
});

window.initializeHeader = initializeHeader;
window.initializeFooter = initializeFooter;
window.initializeMobileMenu = initializeMobileMenu;
window.initializeScrollTop = initializeScrollTop;
window.updateHeaderLayout = updateHeaderLayout;
window.initializeHeaderAuthState = initializeHeaderAuthState;
window.showNotification = showNotification;
