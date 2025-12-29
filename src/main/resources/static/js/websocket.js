// ====== WEBSOCKET ПОДКЛЮЧЕНИЕ ======
(function() {
    const params = new URLSearchParams(window.location.search);
    const gameCode = params.get('gameCode') || params.get('id');
    const playerName = params.get('player') || localStorage.getItem('playerName') || `Player${Math.floor(Math.random()*1000)}`;
    localStorage.setItem('playerName', playerName);

    const wsProtocol = location.protocol === 'https:' ? 'wss' : 'ws';
    let ws = null;
    let wsConnected = false;

    try {
        ws = new WebSocket(
            `${wsProtocol}://${location.host}/ws/duel?gameCode=${encodeURIComponent(gameCode)}&player=${encodeURIComponent(playerName)}`
        );
    } catch (e) {
        log("❌ Ошибка: WebSocket не может быть создан.");
    }

    // ====== КНОПКА АТАКИ ======
    const attackBtn = document.getElementById('attackBtn');

    // ====== СОБЫТИЯ WEBSOCKET (СТАРАЯ ЛОГИКА) ======
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

            if (msg.type === 'join') {
                log(`👤 ${msg.message}`);
                return;
            }
            if (msg.type === 'reconnect') {
                log(`🔄 ${msg.message}`); // Сообщение о реконнекте
                // Можно обновить UI, если нужно:
                // Например, сбросить таймер, включить кнопки атаки
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
                console.log("➡️ full payload:", msg); // весь объект для отладки

                showDuelResult(msg.resultText);
                return;
            }


            if (msg.type === 'UNITS_STATE') {
                if (!Array.isArray(msg.units)) return;

                const slots = [null, null]; // Слот 1 и Слот 2

                msg.units.forEach(u => {
                    // Сначала проверяем, не занят ли юнит уже слотом
                    if (slots[0] && slots[0].playerId === u.playerId) {
                        slots[0] = u; // обновляем
                        return;
                    }
                    if (slots[1] && slots[1].playerId === u.playerId) {
                        slots[1] = u;
                        return;
                    }

                    // Если есть пустой слот, ставим туда
                    if (!slots[0]) slots[0] = u;
                    else if (!slots[1]) slots[1] = u;
                });

                // Обновляем UI
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

                if (name.textContent === unit.player) {
                    console.log(`ℹ️ Слот ${slot} уже содержит юнита ${unit.player}, обновляем HP и картинку`);
                } else {
                    console.log(`✅ Слот ${slot} обновлен: ${unit.player} (${unit.hp}/${unit.hpMax} HP)`);
                }

                img.src = unit.imagePath;
                name.textContent = unit.player;
                health.style.width = (unit.hp / unit.hpMax * 100) + '%';
            }

            function clearSlot(slot) {
                const img = document.getElementById(`player${slot}Img`);
                const name = document.getElementById(`player${slot}Name`);
                const health = document.getElementById(`player${slot}Health`);

                img.src = '/img/waiting.png';
                name.textContent = slot === 1 ? 'Ожидание вашего юнита…' : 'Ожидание соперника…';
                health.style.width = '0%';
                console.log(`ℹ️ Слот ${slot} очищен`);
            }



            // --- ЧАТ (как в старом скрипте) ---
            if (msg.type === 'chat') {
                let inner = null;
                try { inner = JSON.parse(msg.message); } catch {}

                if (inner && inner.turnMessages) {
                    // 👉 это не настоящий чат, а боевой лог раунда
                    chatMsg("💥 Результат раунда:");
                    inner.turnMessages.forEach(m => chatMsg(`→ ${m}`));
                    chatMsg(`❤️ HP Плеер 1: ${inner.attackerHp}, Плеер 2: ${inner.defenderHp}`);

                    attackBtn.disabled = false;
                    resetSelectedBody();

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


    document.getElementById('exitToMenuBtn').addEventListener('click', () => {
        window.location.href = '/';
    });

})();
