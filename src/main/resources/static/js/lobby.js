let ws = null;
let wsConnected = false;
let isJoining = false;

const playerName = localStorage.getItem('playerName') || `Player${Math.floor(Math.random()*1000)}`;
localStorage.setItem('playerName', playerName);

// ====== Подключение к серверу для лобби ======
function connectLobbyWebSocket() {
    const wsProtocol = location.protocol === 'https:' ? 'wss' : 'ws';
    ws = new WebSocket(`${wsProtocol}://${location.host}/ws/lobby?player=${encodeURIComponent(playerName)}`);

    ws.onopen = () => {
        wsConnected = true;
        console.log(`✅ Подключено к лобби как ${playerName}`);
        ws.send(JSON.stringify({ type: 'joinLobby', playerName }));
    };

    ws.onclose = () => { wsConnected = false; console.log("🔒 Соединение лобби закрыто"); };
    ws.onerror = () => { wsConnected = false; console.log("⚠️ Ошибка соединения с лобби"); };

    ws.onmessage = (event) => {
        const msg = JSON.parse(event.data);

        // ====== Событие с комнатами ======
        if (msg.type === 'LOBBY_STATE') {
            updateLobbyRooms(msg.rooms); // msg.rooms = [{ gameCode, players: [{name, imagePath, hp, hpMax}] }]
        }
    };
}

// ====== Обновление списка комнат и юнитов ======
function updateLobbyRooms(rooms) {
    const container = document.getElementById('lobbyRooms');
    container.innerHTML = '';

    rooms.forEach(room => {
        const roomDiv = document.createElement('div');
        roomDiv.className = 'lobby-room';
        roomDiv.innerHTML = `
            <span class="room-code">Game: ${room.gameCode}</span>
            <span class="room-players">Players: ${room.players.length}</span>
            <button class="join-btn" ${isJoining ? 'disabled' : ''} onclick="joinDuel('${room.gameCode}', this)">Присоединиться</button>
            <div class="unit-slots" id="units-${room.gameCode}"></div>
        `;
        container.appendChild(roomDiv);

        const slotsContainer = roomDiv.querySelector(`#units-${room.gameCode}`);
        room.players.forEach((unit, idx) => {
            const slotDiv = document.createElement('div');
            slotDiv.className = 'unit-slot';
            slotDiv.innerHTML = `
                <img src="${unit.imagePath}" alt="Unit" style="width:50px;height:50px;">
                <span class="unit-name">${unit.name} (${unit.hp}/${unit.hpMax} HP)</span>
            `;
            slotsContainer.appendChild(slotDiv);
        });
    });
}

// ====== Присоединение к комнате ======
async function joinDuel(gameCode, button) {
    if (isJoining || !wsConnected) return;
    isJoining = true;
    button.disabled = true;

    ws.send(JSON.stringify({ type: 'joinDuel', gameCode, playerName }));
    // Сервер вернёт обновлённое состояние через LOBBY_STATE
    setTimeout(() => { isJoining = false; }, 1000); // фейковый таймер, пока сервер не ответит
}

// ====== Запуск ======
connectLobbyWebSocket();
