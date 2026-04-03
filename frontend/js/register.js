document.addEventListener('DOMContentLoaded', () => {
    const registerForm = document.getElementById('registerForm');
    const errorMessage = document.getElementById('regErrorMessage');
    const roleSelect = document.getElementById('regRole');
    const adminKeyGroup = document.getElementById('adminKeyGroup');

    // Логіка показу/приховування поля для секретного ключа
    if (roleSelect) {
        roleSelect.addEventListener('change', (e) => {
            if (e.target.value === 'ADMIN') {
                adminKeyGroup.style.display = 'block';
            } else {
                adminKeyGroup.style.display = 'none';
                document.getElementById('adminKey').value = ''; // Очищаємо ключ, якщо передумали
            }
        });
    }

    if (registerForm) {
        registerForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            const selectedRole = document.getElementById('regRole').value;
            const secretKey = document.getElementById('adminKey').value;

            // Збираємо дані з форми
            const newUser = {
                fullName: document.getElementById('regFullName').value,
                city: document.getElementById('regCity').value,
                licenseCategory: document.getElementById('regLicense').value,
                email: document.getElementById('regEmail').value,
                password: document.getElementById('regPassword').value,
                role: selectedRole, 
                availability: "" 
            };

            try {
                // ЗМІНЕНО: Тепер ми відправляємо запит на /register
                const response = await fetch(`http://localhost:8080/api/users/register?secretKey=${secretKey}`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                        // Токен сюди НЕ додаємо, бо це відкритий маршрут
                    },
                    body: JSON.stringify(newUser)
                });

                if (response.ok) {
                    alert('Реєстрація успішна! Тепер ви можете увійти.');
                    window.location.href = 'index.html'; 
                } else {
                    errorMessage.textContent = 'Помилка реєстрації. Перевірте дані або секретний ключ!';
                    errorMessage.style.color = 'red';
                }
            } catch (error) {
                console.error('Помилка:', error);
                errorMessage.textContent = 'Помилка з\'єднання з сервером';
                errorMessage.style.color = 'red';
            }
        });
    }
});