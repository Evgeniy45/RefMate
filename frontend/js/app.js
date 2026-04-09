document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');

    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault(); 

            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;

            Swal.fire({
                title: 'Вхід...',
                allowOutsideClick: false,
                didOpen: () => { Swal.showLoading(); }
            });

            try {
                const response = await fetch('http://localhost:8080/api/users/login', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email: email, password: password })
                });

                if (response.ok) {
                    const data = await response.json();
                    localStorage.setItem('jwtToken', data.token);
                    localStorage.setItem('currentUser', JSON.stringify(data.user));
                    
                    Swal.close(); 
                    window.location.href = 'dashboard.html';
                } else {
                    Swal.fire({
                        icon: 'error',
                        title: 'Доступ заборонено',
                        text: 'Неправильний email або пароль!',
                        confirmButtonColor: '#e63946'
                    });
                }
            } catch (error) {
                console.error('Помилка з\'єднання:', error);
                Swal.fire({
                    icon: 'error',
                    title: 'Помилка сервера',
                    text: 'Сервер недоступний.',
                    confirmButtonColor: '#e63946'
                });
            }
        });
    }
});