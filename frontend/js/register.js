document.addEventListener('DOMContentLoaded', () => {
    const registerForm = document.getElementById('registerForm');
    const roleSelect = document.getElementById('regRole');
    const adminKeyGroup = document.getElementById('adminKeyGroup');

    if (roleSelect) {
        roleSelect.addEventListener('change', (e) => {
            if (e.target.value === 'ADMIN') {
                adminKeyGroup.style.display = 'block';
            } else {
                adminKeyGroup.style.display = 'none';
                document.getElementById('adminKey').value = ''; 
            }
        });
    }

    if (registerForm) {
        registerForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            const selectedRole = document.getElementById('regRole').value;
            const secretKey = document.getElementById('adminKey').value;

            const newUser = {
                fullName: document.getElementById('regFullName').value,
                city: document.getElementById('regCity').value,
                licenseCategory: document.getElementById('regLicense').value,
                email: document.getElementById('regEmail').value,
                password: document.getElementById('regPassword').value,
                role: selectedRole, 
                availability: "" 
            };

            Swal.fire({
                title: 'Реєстрація...',
                text: 'Будь ласка, зачекайте.',
                allowOutsideClick: false,
                didOpen: () => {
                    Swal.showLoading();
                }
            });

            try {
                const response = await fetch(`http://localhost:8080/api/users/register?secretKey=${secretKey}`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(newUser)
                });

                if (response.ok) {
                    await Swal.fire({
                        icon: 'success',
                        title: 'Успіх!',
                        text: 'Реєстрація пройшла успішно. Тепер ви можете увійти.',
                        confirmButtonColor: '#e85d04'
                    });
                    window.location.href = 'index.html'; 
                } else {
                    Swal.fire({
                        icon: 'error',
                        title: 'Помилка!',
                        text: 'Перевірте дані. Можливо, така пошта вже існує або введено неправильний секретний ключ.',
                        confirmButtonColor: '#e63946'
                    });
                }
            } catch (error) {
                console.error('Помилка:', error);
                Swal.fire({
                    icon: 'error',
                    title: 'Помилка з\'єднання',
                    text: 'Не вдалося підключитися до сервера.',
                    confirmButtonColor: '#e63946'
                });
            }
        });
    }
});