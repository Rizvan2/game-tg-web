// ====== WEBSOCKET ПОДКЛЮЧЕНИЕ ======
(function() {
    const params = new URLSearchParams(window.location.search);
    const gameCode = params.get('gameCode') || params.get('id');
    let playerName = null;
    let myUnitName = null; // переменная для хранения имени нашего юнита

    const wsProtocol = location.protocol === 'https:' ? 'wss' : 'ws';
    let ws = null;
    const DEFLECTION_MAX_BY_SLOT = {};

    let wsConnected = false;

    try {
        ws = new WebSocket(
            `${wsProtocol}://${location.host}/ws/duel?gameCode=${encodeURIComponent(gameCode)}&player=${encodeURIComponent(playerName)}`
        );
    } catch (e) {
        log("❌ Ошибка: WebSocket не может быть создан.");
    }

    // ====== ИНИЦИАЛИЗАЦИЯ UI АТАКИ ======
    AttackSender.initAttackUI();

    // ====== КНОПКА АТАКИ ======
    const attackBtn = document.getElementById('attackBtn');

    // ====== СОБЫТИЯ WEBSOCKET ======
    if (ws) {

        ws.onopen = () => {
            wsConnected = true;
            log(`✅ Подключено к комнате "${gameCode}" как ${playerName}`);
            ws.send(JSON.stringify({ type: 'join', playerName }));
        };

        ws.onclose = () => {
            wsConnected = false;
            log("🔒 Соединение закрыто");
        };

        ws.onerror = () => {
            wsConnected = false;
            log("⚠️ Ошибка соединения");
        };

        ws.onmessage = (event) => {
            console.log("🛰️ RAW MESSAGE:", event.data); // <- прямо в начале

            const msg = JSON.parse(event.data);

            if (msg.type === 'INIT') {
                console.log("🚀 INIT получен:", msg);
                playerName = msg.playerName
                localStorage.setItem('playerName', playerName);
                myUnitName = msg.playerUnitName;

                console.log(`📌 Мой юнит зафиксирован: ${myUnitName}`);
                return;
            }
            if (msg.type === 'join') {
                log(`👤 ${msg.message}`);
                return;
            }
            if (msg.type === 'reconnect') {
                log(`🔄 ${msg.message}`);
                attackBtn.disabled = false;
                return;
            }
            if (msg.type === 'info') {
                log(`ℹ️ ${msg.message}`);
                return;
            }
            if (msg.type === 'error') {
                log(`❌ ${msg.message}`);
                attackBtn.disabled = false;
                return;
            }
            if (msg.type === 'bothSelected') {
                log("⏳ Оба игрока выбрали цели, идёт расчёт атаки...");
                return;
            }
            if (msg.type === 'duelResult') {
                console.log("🏁 DUEL RESULT EVENT RECEIVED");
                console.log("➡️ resultText:", msg.resultText);
                console.log("➡️ targetPlayer:", msg.targetPlayer);
                console.log("➡️ full payload:", msg);

                showDuelResult(msg.resultText);
                return;
            }
            if (msg.type === 'BODY_PART_DESTROYED') {
                console.log("💀 BODY PART DESTROYED:", msg);
                handleBodyPartDestroyed(msg);
                return;
            }

            if (msg.type === 'UNITS_STATE') {
                if (!Array.isArray(msg.units)) return;

                const slots = [null, null];

                msg.units.forEach(u => {
                    if (slots[0] && slots[0].playerId === u.playerId) {
                        slots[0] = u;
                        return;
                    }
                    if (slots[1] && slots[1].playerId === u.playerId) {
                        slots[1] = u;
                        return;
                    }

                    if (!slots[0]) slots[0] = u;
                    else if (!slots[1]) slots[1] = u;
                });

                slots.forEach((unit, idx) => {
                    const slotNum = idx + 1;
                    if (unit) {
                        setUnitToSlot(slotNum, unit);
                    } else {
                        clearSlot(slotNum);
                    }
                });
            }

            function setUnitToSlot(slot, unit) {
                const img = document.getElementById(`player${slot}Img`);
                const name = document.getElementById(`player${slot}Name`);
                const health = document.getElementById(`player${slot}Health`);
                const deflectionContainer = document.getElementById(`player${slot}Deflection`);

                // ⬇️ фиксируем максимум ОДИН РАЗ ДЛЯ СЛОТА
                if (DEFLECTION_MAX_BY_SLOT[slot] === undefined) {
                    DEFLECTION_MAX_BY_SLOT[slot] = unit.deflectionCurrent;
                }

                const max = DEFLECTION_MAX_BY_SLOT[slot];
                const current = Math.max(unit.deflectionCurrent, 0);

                if (name.textContent === unit.player) {
                    console.log(`ℹ️ Слот ${slot}: обновление состояния (${current}/${max} deflection)`);
                } else {
                    console.log(`✅ Слот ${slot}: новый юнит ${unit.player}`);
                }

                // Базовые данные
                img.src = unit.imagePath;
                name.textContent = unit.player;
                health.style.width = (unit.hp / unit.hpMax * 100) + '%';

                // 🛡️ Рендер ячеек дефлекта
                renderDeflectionCharges(deflectionContainer, current, max);
            }

            function renderDeflectionCharges(container, current, max) {
                if (!container) return;

                // очищаем старые ячейки
                container.innerHTML = '';

                for (let i = 0; i < max; i++) {
                    const charge = document.createElement('span');
                    charge.classList.add('charge');

                    if (i < current) {
                        charge.classList.add('active');
                    }

                    container.appendChild(charge);
                }
            }

            function clearSlot(slot) {
                const img = document.getElementById(`player${slot}Img`);
                const name = document.getElementById(`player${slot}Name`);
                const health = document.getElementById(`player${slot}Health`);

                img.src = '/img/waiting.png';
                name.textContent = slot === 1 ? 'Ожидание вашего юнита…' : 'Ожидание соперника…';
                health.style.width = '0%';

                delete DEFLECTION_MAX_BY_SLOT[slot];

                console.log(`ℹ️ Слот ${slot} очищен`);
            }

            // --- ЧАТ ---
            if (msg.type === 'chat') {
                let inner = null;
                try { inner = JSON.parse(msg.message); } catch {}

                if (inner && inner.turnMessages) {
                    chatMsg("💥 Результат раунда:");
                    inner.turnMessages.forEach(m => chatMsg(`→ ${m}`));
                    chatMsg(`❤️ HP Плеер 1: ${inner.attackerHp}, Плеер 2: ${inner.defenderHp}`);

                    attackBtn.disabled = false;

                    // Сброс через модуль
                    AttackSender.resetSelectedBody();
                    document.querySelectorAll('.hit-zone').forEach(z => z.classList.remove('selected'));

                } else {
                    // 👉 обычное сообщение игрока
                    const sender = msg.playerName ?? msg.sender;
                    const text = msg.message ?? msg.text;

                    chatMsg(`${sender}: ${text}`);

                    // 💬 показываем пузырь над моделькой
                    showBubble(sender, text);
                }
            }
        };
    }

    // ====== ФУНКЦИИ ДЛЯ ВНЕШНЕГО ИСПОЛЬЗОВАНИЯ ======
    window.sendChat = function(text) {
        if (!wsConnected) return false;
        ws.send(JSON.stringify({ type: "chat", message: text }));
        return true;
    };

    window.sendAttack = function(body) {
        if (!wsConnected) return false;
        ws.send(JSON.stringify({ type: "attack", body }));
        return true;
    };

    function showDuelResult(text) {
        console.log("🏆 Вызов showDuelResult:", text); // <-- логируем событие

        const modal = document.getElementById('duelResultModal');
        const title = document.getElementById('duelResultTitle');

        title.textContent = text;
        modal.style.display = 'flex';
    }

    // Функция обработки уничтожения части тела
    function handleBodyPartDestroyed(data) {
        // data = {
        //   type: "BODY_PART_DESTROYED",
        //   player: "Вася",
        //   bodyPart: "HEAD",
        //   message: "Вася потерял голову!"
        // }

        console.log("🔥 BODY_PART_DESTROYED:", data);
        console.log("🔥 мой юнит:", myUnitName);

        log(`💀 ${data.message}`);
        chatMsg(`💀 ${data.message}`);
        showNotification(data.message);

        if (!myUnitName) {
            console.warn("⚠️ INIT ещё не получен — пропускаем UI");
            return;
        }

        // если пострадал НЕ мой юнит — отключаем часть тела
        if (data.playerUnitName !== myUnitName) {
            console.log(`💀 Отключаем часть тела у врага: ${data.bodyPart}`);
            AttackSender.disableBodyPart(data.bodyPart);
        } else {
            console.log("💡 Это мой юнит — UI не трогаем");
        }
    }


    function showNotification(message) {
        const notification = document.createElement('div');
        notification.className = 'notification';
        notification.textContent = message;
        document.body.appendChild(notification);

        setTimeout(() => {
            notification.classList.add('show');
        }, 100);

        setTimeout(() => {
            notification.classList.remove('show');
            setTimeout(() => notification.remove(), 300);
        }, 3000);
    }

    document.getElementById('exitToMenuBtn').addEventListener('click', () => {
        window.location.href = '/';
    });

})();