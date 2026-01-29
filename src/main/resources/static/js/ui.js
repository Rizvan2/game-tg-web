// ====== ФУНКЦИИ СООБЩЕНИЙ ======
function chatMsg(msg) {
    const chatMessages = document.getElementById("chatMessages");
    const time = new Date().toLocaleTimeString();
    chatMessages.innerHTML += `<div>[${time}] ${msg}</div>`;
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

function log(msg) {
    const logEl = document.getElementById("log");
    const time = new Date().toLocaleTimeString();
    logEl.innerHTML += `<div>[${time}] ${msg}</div>`;
    logEl.scrollTop = logEl.scrollHeight;
}

// ====== КНОПКИ ОТКРЫТИЯ ПАНЕЛЕЙ ======
document.getElementById("toggleLogBtn").onclick = () => {
    const log = document.getElementById("log");
    log.classList.toggle("open");
    document.getElementById("toggleLogBtn")
        .classList.toggle("hidden", log.classList.contains("open"));
};

document.getElementById("toggleChatBtn").onclick = () => {
    const chat = document.getElementById("chat");
    const box = document.getElementById("chatInputBox");

    chat.classList.toggle("open");
    box.style.right = chat.classList.contains("open") ? "0" : "-260px";
    document.getElementById("toggleChatBtn")
        .classList.toggle("hidden", chat.classList.contains("open"));
};

// ====== КЛИКИ ВНЕ ПАНЕЛЕЙ ======
document.addEventListener('click', (e) => {
    const log = document.getElementById("log");
    const logBtn = document.getElementById("toggleLogBtn");
    if (log.classList.contains("open") && !log.contains(e.target) && e.target !== logBtn) {
        log.classList.remove("open");
        logBtn.classList.remove("hidden");
    }

    const chat = document.getElementById("chat");
    const chatBtn = document.getElementById("toggleChatBtn");
    const chatMessages = document.getElementById("chatMessages");
    const chatInputBox = document.getElementById("chatInputBox");
    if (
        chat.classList.contains("open") &&
        !chatMessages.contains(e.target) &&
        !chatInputBox.contains(e.target) &&
        e.target !== chatBtn
    ) {
        chat.classList.remove("open");
        chatInputBox.style.right = "-260px";
        chatBtn.classList.remove("hidden");
    }
});

// ====== ФУНКЦИЯ ДЛЯ BUBBLE (используется из websocket.js) ======
function showBubble(playerName, text) {
    const p1Name = document.getElementById("player1Name").innerText.trim();
    const p2Name = document.getElementById("player2Name").innerText.trim();

    let bubbleId = null;

    if (playerName === p1Name) {
        bubbleId = "player1Bubble";
    } else if (playerName === p2Name) {
        bubbleId = "player2Bubble";
    } else {
        console.warn("Не могу определить игрока для bubble:", playerName);
        return;
    }

    const bubble = document.getElementById(bubbleId);

    // === СБРОС СТАРЫХ ТАЙМЕРОВ ===
    if (bubble.fadeTimer) clearTimeout(bubble.fadeTimer);
    if (bubble.hideTimer) clearTimeout(bubble.hideTimer);

    // === ПОКАЗ СООБЩЕНИЯ ===
    bubble.style.opacity = "1";
    bubble.innerText = text;
    bubble.style.display = "block";

    // === НОВЫЕ ТАЙМЕРЫ ===
    bubble.fadeTimer = setTimeout(() => {
        bubble.style.opacity = "0"; // плавное исчезновение
    }, 1500);

    bubble.hideTimer = setTimeout(() => {
        bubble.style.display = "none";
    }, 1900);
}

// ====== ОТПРАВКА ЧАТА ======
document.getElementById("sendChatBtn").onclick = () => {
    const input = document.getElementById("chatInput");
    const text = input.value.trim();
    if (!text) return;

    const ok = window.sendChat(text);
    if (!ok) chatMsg("❌ Невозможно отправить: нет соединения с сервером.");

    // 💬 Показываем bubble сразу локально
    const playerName = localStorage.getItem('playerName') || 'Player';
    showBubble(playerName, text);

    input.value = "";
};

document.getElementById("chatInput").addEventListener("keydown", e => {
    if (e.key === "Enter") document.getElementById("sendChatBtn").click();
});