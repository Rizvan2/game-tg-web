// ====== МОДУЛЬ ОТПРАВКИ АТАКИ ======

const AttackSender = (function() {
    let selectedBodyPart = null;

    // Устанавливает выбранную часть тела
    function selectBodyPart(bodyPart) {
        selectedBodyPart = bodyPart;
        console.log(`🎯 Выбрана цель: ${bodyPart}`);
    }

    // Сброс выбранной части тела
    function resetSelectedBody() {
        selectedBodyPart = null;
        console.log('🔄 Выбор цели сброшен');
    }

    // Отправка атаки (использует window.sendAttack)
    function performAttack() {
        if (!selectedBodyPart) {
            return { success: false, error: 'Цель не выбрана' };
        }

        const success = window.sendAttack(selectedBodyPart);

        if (success) {
            console.log(`⚔️ Атака отправлена на ${selectedBodyPart}`);
            return { success: true };
        } else {
            return { success: false, error: 'Нет соединения с сервером' };
        }
    }

    // Проверка, уничтожена ли часть тела
    function isBodyPartDestroyed(bodyPart) {
        const hitZone = document.querySelector(`.hit-zone[data-part="${bodyPart}"]`);
        return hitZone ? hitZone.classList.contains('destroyed') : false;
    }

    // Отключает возможность выбора части тела
    function disableBodyPart(bodyPart) {
        const hitZone = document.querySelector(`.hit-zone[data-part="${bodyPart}"]`);
        if (hitZone) {
            hitZone.classList.add('destroyed');
            hitZone.style.pointerEvents = 'none';

            // Добавляем иконку черепа
            const skull = document.createElement('span');
            skull.className = 'skull-icon';
            skull.textContent = '💀';
            hitZone.appendChild(skull);

            console.log(`💀 Часть тела ${bodyPart} уничтожена`);
        }
    }

    // Инициализация обработчиков UI
    function initAttackUI() {
        const attackBtn = document.getElementById('attackBtn');
        const hitZones = document.querySelectorAll('.hit-zone');

        // Обработчики выбора части тела
        hitZones.forEach(zone => {
            zone.addEventListener('click', () => {
                if (isBodyPartDestroyed(zone.dataset.part)) {
                    return; // Нельзя выбрать уничтоженную часть
                }

                // Убираем выделение со всех
                hitZones.forEach(z => z.classList.remove('selected'));

                // Выделяем выбранную
                zone.classList.add('selected');
                selectBodyPart(zone.dataset.part);

                // Активируем кнопку атаки
                attackBtn.disabled = false;
            });
        });

        // Обработчик кнопки атаки
        attackBtn.addEventListener('click', () => {
            const result = performAttack();

            if (result.success) {
                attackBtn.disabled = true;

                // 🔄 СБРАСЫВАЕМ ВЫДЕЛЕНИЕ СРАЗУ ПОСЛЕ ОТПРАВКИ
                hitZones.forEach(z => z.classList.remove('selected'));
                resetSelectedBody();
            } else {
                alert(result.error);
            }
        });
    }

    // Публичный API
    return {
        selectBodyPart,
        resetSelectedBody,
        performAttack,
        isBodyPartDestroyed,
        disableBodyPart,
        initAttackUI
    };
})();

// Делаем доступным глобально
window.AttackSender = AttackSender;