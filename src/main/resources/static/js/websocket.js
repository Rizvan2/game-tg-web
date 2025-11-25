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

            if (msg.type === 'UNITS_STATE') {
                const u1 = msg.units[0];
                const u2 = msg.units[1];
                const myUnit = u1.player === playerName ? u1 : u2;
                const enemyUnit = u1.player === playerName ? u2 : u1;

                document.getElementById('player1Img').src = myUnit.imagePath;
                document.getElementById('player1Name').textContent = myUnit.player;
                document.getElementById('player1Health').style.width = (myUnit.hp / myUnit.hpMax * 100) + '%';

                document.getElementById('player2Img').src = enemyUnit.imagePath;
                document.getElementById('player2Name').textContent = enemyUnit.player;
                document.getElementById('player2Health').style.width = (enemyUnit.hp / enemyUnit.hpMax * 100) + '%';
                return;
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
})();
