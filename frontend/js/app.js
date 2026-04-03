// Чекаємо, поки весь HTML завантажиться
document.addEventListener('DOMContentLoaded', () => {
    
    const loginForm = document.getElementById('loginForm');
    const errorMessage = document.getElementById('errorMessage');

    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault(); // Зупиняємо стандартне перезавантаження сторінки

            // Збираємо дані з полів вводу
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;

            try {
                // Відправляємо запит на наш Java-сервер
                const response = await fetch('http://localhost:8080/api/users/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ email: email, password: password })
                });

                if (response.ok) {
                    // ЗМІНЕНО: Тепер сервер повертає { token: "...", user: {...} }
                    const data = await response.json();
                    
                    // 1. Зберігаємо цифровий ключ (JWT Token) у пам'ять браузера
                    localStorage.setItem('jwtToken', data.token);

                    // 2. Зберігаємо інфу про користувача
                    localStorage.setItem('currentUser', JSON.stringify(data.user));

                    // Перенаправляємо на головний кабінет
                    window.location.href = 'dashboard.html';
                } else {
                    // Якщо бекенд видав помилку (неправильний пароль або логін)
                    errorMessage.textContent = 'Неправильний email або пароль';
                    errorMessage.style.color = 'red';
                }
            } catch (error) {
                console.error('Помилка з\'єднання:', error);
                errorMessage.textContent = 'Помилка з\'єднання з сервером';
                errorMessage.style.color = 'red';
            }
        });
    }
});