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



// Добавьте эту функцию в ваш script.js или создайте отдельный файл notifications.js

function showNotification(message, type = 'info') {
    // Удаляем предыдущее уведомление, если есть
    const existingNotification = document.querySelector('.notification');
    if (existingNotification) {
        existingNotification.remove();
    }
    
    // Создаем новое уведомление
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    
    // Иконка в зависимости от типа
    let icon = 'fa-info-circle';
    if (type === 'success') icon = 'fa-check-circle';
    if (type === 'error') icon = 'fa-exclamation-circle';
    if (type === 'warning') icon = 'fa-exclamation-triangle';
    
    notification.innerHTML = `
        <i class="fas ${icon}"></i>
        <span class="notification-message">${message}</span>
    `;
    
    // Добавляем стили если они не были добавлены через CSS
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
    
    // Проверяем, добавлены ли стили в документ
    if (!document.querySelector('#notification-styles')) {
        const styleElement = document.createElement('style');
        styleElement.id = 'notification-styles';
        styleElement.textContent = notificationStyles;
        document.head.appendChild(styleElement);
    }
    
    document.body.appendChild(notification);
    
    // Активируем анимацию появления
    setTimeout(() => {
        notification.classList.add('show');
    }, 10);
    
    // Автоматическое скрытие через 5 секунд
    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => {
            if (notification.parentNode) {
                notification.remove();
            }
        }, 400);
    }, 5000);
    
    // Закрытие при клике
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

// Делаем функцию доступной глобально
window.showNotification = showNotification;


// Экспортируем функции для использования в других файлах
window.openModal = openModal;
window.closeModal = closeModal;
window.showNotification = window.showNotification || function() {};

// Запускаем проверку авторизации при загрузке
setTimeout(checkAuthStatus, 100);