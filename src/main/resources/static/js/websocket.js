// WebSocket客户端
class GameWebSocket {
    constructor(gameBoard) {
        this.gameBoard = gameBoard;
        this.socket = null;
        this.gameId = null;
        this.userId = null;
        this.connected = false;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectDelay = 2000; // 2秒
    }

    // 连接到WebSocket服务器
    connect() {
        // 获取游戏ID和用户ID
        this.gameId = this.getGameIdFromUrl() || localStorage.getItem('gameId');
        this.userId = this.getUserId();

        // 构建WebSocket URL
        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const host = window.location.host;
        const wsUrl = `${protocol}//${host}/ws/game`;

        // 创建WebSocket连接
        this.socket = new WebSocket(wsUrl);

        // 设置事件处理器
        this.socket.onopen = this.onOpen.bind(this);
        this.socket.onmessage = this.onMessage.bind(this);
        this.socket.onclose = this.onClose.bind(this);
        this.socket.onerror = this.onError.bind(this);
    }

    // 连接打开时
    onOpen(event) {
        console.log('WebSocket连接已建立');
        this.connected = true;
        this.reconnectAttempts = 0;

        // 加入游戏
        this.joinGame();
    }

    // 收到消息时
    onMessage(event) {
        try {
            const message = JSON.parse(event.data);
            this.handleMessage(message);
        } catch (error) {
            console.error('解析WebSocket消息失败:', error);
        }
    }

    // 连接关闭时
    onClose(event) {
        console.log('WebSocket连接已关闭:', event.code, event.reason);
        this.connected = false;

        // 尝试重新连接
        this.tryReconnect();
    }

    // 发生错误时
    onError(error) {
        console.error('WebSocket错误:', error);
        this.connected = false;
    }

    // 尝试重新连接
    tryReconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`尝试重新连接 (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`);

            setTimeout(() => {
                this.connect();
            }, this.reconnectDelay);
        } else {
            console.error('达到最大重连次数，无法重新连接WebSocket');
            alert('与服务器的连接已断开，请刷新页面重试。');
        }
    }

    // 加入游戏
    joinGame() {
        const joinMessage = {
            type: 'join',
            gameId: this.gameId,
            userId: this.userId
        };

        this.sendMessage(joinMessage);
    }

    // 发送移动消息
    sendMove(row1, col1, row2, col2) {
        const moveMessage = {
            type: 'move',
            gameId: this.gameId,
            userId: this.userId,
            row1: row1,
            col1: col1,
            row2: row2,
            col2: col2
        };

        this.sendMessage(moveMessage);
    }

    // 请求新游戏
    requestNewGame() {
        const newGameMessage = {
            type: 'newGame',
            gameId: this.gameId,
            userId: this.userId
        };

        this.sendMessage(newGameMessage);
    }

    // 发送消息
    sendMessage(message) {
        if (this.connected && this.socket) {
            this.socket.send(JSON.stringify(message));
        } else {
            console.warn('WebSocket未连接，无法发送消息');

            // 尝试重新连接
            if (!this.connected) {
                this.tryReconnect();
            }
        }
    }

    // 处理接收到的消息
    handleMessage(message) {
        switch (message.type) {
            case 'gameState':
                this.handleGameState(message);
                break;

            case 'gameOver':
                this.handleGameOver(message);
                break;

            case 'error':
                this.handleError(message);
                break;

            case 'notification':
                this.handleNotification(message);
                break;

            default:
                console.warn('未知的消息类型:', message.type);
        }
    }

    // 处理游戏状态消息
    handleGameState(message) {
        // 保存游戏ID
        if (message.gameId) {
            this.gameId = message.gameId;
            localStorage.setItem('gameId', message.gameId);
        }

        // 更新游戏面板
        if (message.board && this.gameBoard) {
            this.gameBoard.updateFromServerState(message.board);
        }
    }

    // 处理游戏结束消息
    handleGameOver(message) {
        if (this.gameBoard) {
            this.gameBoard.showGameOverModal(message.score);
        }
    }

    // 处理错误消息
    handleError(message) {
        console.error('服务器错误:', message.message);

        // 显示错误消息
        if (message.message) {
            alert(`游戏错误: ${message.message}`);
        }
    }

    // 处理通知消息
    handleNotification(message) {
        console.log('服务器通知:', message.message);

        // 显示通知
        if (message.message && this.gameBoard) {
            this.gameBoard.showNotification(message.message);
        }
    }

    // 从URL获取游戏ID
    getGameIdFromUrl() {
        const urlParams = new URLSearchParams(window.location.search);
        return urlParams.get('gameId');
    }

    // 获取用户ID
    getUserId() {
        // 从会话存储中获取
        return sessionStorage.getItem('userId') || localStorage.getItem('userId') || null;
    }

    // 关闭连接
    disconnect() {
        if (this.socket) {
            this.socket.close();
            this.connected = false;
        }
    }
}

// 导出WebSocket客户端
window.GameWebSocket = GameWebSocket;