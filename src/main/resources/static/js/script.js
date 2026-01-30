// Основные функции для хедера и модальных окон

// Инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    console.log('GameForum инициализирован');
    
    // Инициализация всех компонентов
    initializeMobileMenu();
    initializeModals();
    initializeScrollTop();
    initializeNotifications();
    initializePasswordToggles();
    initializeForms();
    initializeScrollEffects();
});

// Мобильное меню
function initializeMobileMenu() {
    const menuToggle = document.getElementById('menuToggle');
    const closeMenu = document.getElementById('closeMenu');
    const mobileMenu = document.getElementById('mobileMenu');
    const mobileMenuOverlay = document.getElementById('mobileMenuOverlay');
    
    if (menuToggle && mobileMenu) {
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
        
        // Закрытие по ESC
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && mobileMenu.classList.contains('show')) {
                closeMobileMenu();
            }
        });
    }
    
    function closeMobileMenu() {
        const mobileMenu = document.getElementById('mobileMenu');
        if (mobileMenu) {
            mobileMenu.classList.remove('show');
            document.body.style.overflow = 'auto';
        }
    }
}

// Модальные окна
function initializeModals() {
    // Элементы
    const loginBtn = document.getElementById('loginBtn');
    const registerBtn = document.getElementById('registerBtn');
    const mobileLoginBtn = document.getElementById('mobileLoginBtn');
    const mobileRegisterBtn = document.getElementById('mobileRegisterBtn');
    const closeLoginModal = document.getElementById('closeLoginModal');
    const closeRegisterModal = document.getElementById('closeRegisterModal');
    const switchToRegister = document.getElementById('switchToRegister');
    const switchToLogin = document.getElementById('switchToLogin');
    const loginModal = document.getElementById('loginModal');
    const registerModal = document.getElementById('registerModal');
    
    // Открытие модальных окон
    function addModalListeners(button, modal) {
        if (button && modal) {
            button.addEventListener('click', () => {
                openModal(modal);
                closeMobileMenu();
            });
        }
    }
    
    // Кнопки входа
    addModalListeners(loginBtn, loginModal);
    addModalListeners(mobileLoginBtn, loginModal);
    
    // Кнопки регистрации
    addModalListeners(registerBtn, registerModal);
    addModalListeners(mobileRegisterBtn, registerModal);
    
    // Закрытие модальных окон
    if (closeLoginModal && loginModal) {
        closeLoginModal.addEventListener('click', () => closeModal(loginModal));
    }
    
    if (closeRegisterModal && registerModal) {
        closeRegisterModal.addEventListener('click', () => closeModal(registerModal));
    }
    
    // Переключение между модальными окнами
    if (switchToRegister) {
        switchToRegister.addEventListener('click', (e) => {
            e.preventDefault();
            if (loginModal && registerModal) {
                closeModal(loginModal);
                setTimeout(() => openModal(registerModal), 300);
            }
        });
    }
    
    if (switchToLogin) {
        switchToLogin.addEventListener('click', (e) => {
            e.preventDefault();
            if (registerModal && loginModal) {
                closeModal(registerModal);
                setTimeout(() => openModal(loginModal), 300);
            }
        });
    }
    
    // Закрытие при клике вне модального окна
    window.addEventListener('click', (e) => {
        if (e.target === loginModal) closeModal(loginModal);
        if (e.target === registerModal) closeModal(registerModal);
    });
    
    // Закрытие по ESC
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            if (loginModal && loginModal.classList.contains('show')) closeModal(loginModal);
            if (registerModal && registerModal.classList.contains('show')) closeModal(registerModal);
        }
    });
}

// Функции для работы с модальными окнами
function openModal(modal) {
    if (modal) {
        modal.style.display = 'flex';
        setTimeout(() => {
            modal.classList.add('show');
            document.body.style.overflow = 'hidden';
        }, 10);
    }
}

function closeModal(modal) {
    if (modal) {
        modal.classList.remove('show');
        setTimeout(() => {
            modal.style.display = 'none';
            document.body.style.overflow = 'auto';
        }, 300);
    }
}

// Кнопка "Наверх"
function initializeScrollTop() {
    const scrollTopBtn = document.getElementById('scrollTopBtn');
    
    if (scrollTopBtn) {
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
    }
}

// Показать/скрыть пароль
function initializePasswordToggles() {
    const showPasswordBtns = document.querySelectorAll('.show-password');
    
    showPasswordBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            const input = this.parentElement.querySelector('input');
            const icon = this.querySelector('i');
            
            if (input.type === 'password') {
                input.type = 'text';
                icon.classList.remove('fa-eye');
                icon.classList.add('fa-eye-slash');
            } else {
                input.type = 'password';
                icon.classList.remove('fa-eye-slash');
                icon.classList.add('fa-eye');
            }
        });
    });
}

// Уведомления
function initializeNotifications() {
    window.showNotification = function(message, type = 'info') {
        const notification = document.createElement('div');
        notification.className = `notification ${type}`;
        notification.innerHTML = `
            <i class="fas ${type === 'error' ? 'fa-exclamation-circle' : 'fa-check-circle'}"></i>
            <span>${message}</span>
        `;
        
        document.body.appendChild(notification);
        
        // Анимация появления
        setTimeout(() => {
            notification.classList.add('show');
        }, 10);
        
        // Автоматическое скрытие через 4 секунды
        setTimeout(() => {
            notification.classList.remove('show');
            setTimeout(() => {
                if (notification.parentNode) {
                    document.body.removeChild(notification);
                }
            }, 300);
        }, 4000);
        
        // Закрытие при клике
        notification.addEventListener('click', () => {
            notification.classList.remove('show');
            setTimeout(() => {
                if (notification.parentNode) {
                    document.body.removeChild(notification);
                }
            }, 300);
        });
    };
}

// Формы
function initializeForms() {
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');
    
    // Форма входа
    if (loginForm) {
        loginForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const email = document.getElementById('loginEmail').value;
            const password = document.getElementById('loginPassword').value;
            
            // Валидация
            if (!email || !password) {
                showNotification('Заполните все поля', 'error');
                return;
            }
            
            // Имитация запроса к серверу
            simulateRequest(() => {
                showNotification('Вход выполнен успешно!', 'success');
                closeModal(document.getElementById('loginModal'));
                
                // Имитация успешного входа
                const userProfile = document.getElementById('userProfile');
                const authButtons = document.querySelector('.auth-buttons');
                
                if (userProfile) userProfile.style.display = 'block';
                if (authButtons) authButtons.style.display = 'none';
                
                // Сохраняем в localStorage
                localStorage.setItem('isLoggedIn', 'true');
                localStorage.setItem('username', email.split('@')[0]);
                
                // Очистка формы
                this.reset();
            });
        });
    }
    
    // Форма регистрации
    if (registerForm) {
        registerForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const username = document.getElementById('regUsername').value;
            const email = document.getElementById('regEmail').value;
            const password = document.getElementById('regPassword').value;
            const confirmPassword = document.getElementById('regConfirmPassword').value;
            
            // Валидация
            if (!username || !email || !password || !confirmPassword) {
                showNotification('Заполните все поля', 'error');
                return;
            }
            
            if (password !== confirmPassword) {
                showNotification('Пароли не совпадают', 'error');
                return;
            }
            
            if (password.length < 6) {
                showNotification('Пароль должен содержать минимум 6 символов', 'error');
                return;
            }
            
            if (!email.includes('@') || !email.includes('.')) {
                showNotification('Введите корректный email', 'error');
                return;
            }
            
            // Имитация запроса к серверу
            simulateRequest(() => {
                showNotification(`Регистрация успешна! Добро пожаловать, ${username}!`, 'success');
                closeModal(document.getElementById('registerModal'));
                
                // Очистка формы
                this.reset();
            });
        });
    }
}

// Имитация запроса к серверу
function simulateRequest(callback) {
    const submitBtns = document.querySelectorAll('.submit-btn');
    
    submitBtns.forEach(btn => {
        const originalText = btn.innerHTML;
        btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i><span>Загрузка...</span>';
        btn.disabled = true;
        
        setTimeout(() => {
            callback();
            btn.innerHTML = originalText;
            btn.disabled = false;
        }, 1500);
    });
}

// Эффекты при скролле
function initializeScrollEffects() {
    // Эффект для хедера при скролле
    window.addEventListener('scroll', () => {
        const header = document.querySelector('.glass-header');
        if (header) {
            if (window.scrollY > 50) {
                header.style.transform = 'translateX(-50%) scale(0.98)';
                header.style.boxShadow = '0 4px 20px rgba(31, 38, 135, 0.15)';
            } else {
                header.style.transform = 'translateX(-50%) scale(1)';
                header.style.boxShadow = '0 8px 32px 0 rgba(31, 38, 135, 0.2)';
            }
        }
    });
    
    // Плавная прокрутка для якорных ссылок
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            const href = this.getAttribute('href');
            if (href !== '#' && href !== '#!') {
                e.preventDefault();
                const targetElement = document.querySelector(href);
                if (targetElement) {
                    targetElement.scrollIntoView({
                        behavior: 'smooth',
                        block: 'start'
                    });
                }
            }
        });
    });
}

// Проверка авторизации при загрузке
function checkAuthStatus() {
    const isLoggedIn = localStorage.getItem('isLoggedIn');
    if (isLoggedIn === 'true') {
        const userProfile = document.getElementById('userProfile');
        const authButtons = document.querySelector('.auth-buttons');
        
        if (userProfile) {
            userProfile.style.display = 'block';
            
            // Устанавливаем имя пользователя
            const username = localStorage.getItem('username') || 'Игрок';
            const profileName = userProfile.querySelector('.profile-name');
            if (profileName) {
                profileName.textContent = username;
            }
            
            // Кнопка выхода
            const logoutBtn = userProfile.querySelector('.profile-logout');
            if (logoutBtn) {
                logoutBtn.addEventListener('click', function() {
                    localStorage.removeItem('isLoggedIn');
                    localStorage.removeItem('username');
                    userProfile.style.display = 'none';
                    if (authButtons) authButtons.style.display = 'flex';
                    showNotification('Вы вышли из аккаунта', 'success');
                });
            }
        }
        
        if (authButtons) {
            authButtons.style.display = 'none';
        }
    }
}

// Экспортируем функции для использования в других файлах
window.openModal = openModal;
window.closeModal = closeModal;
window.showNotification = window.showNotification || function() {};

// Запускаем проверку авторизации при загрузке
setTimeout(checkAuthStatus, 100);