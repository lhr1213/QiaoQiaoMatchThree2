// 游戏面板逻辑
class GameBoard {
    constructor() {
        this.boardElement = document.getElementById('game-board');
        this.scoreElement = document.getElementById('score');
        this.movesLeftElement = document.getElementById('moves-left');
        this.selectedTile = null;
        this.boardData = null;
        this.isAnimating = false;
        this.gameOver = false;

        this.init();
    }

    async init() {
        try {
            // 获取游戏状态
            const response = await fetch('/game/state');
            if (!response.ok) {
                throw new Error('无法获取游戏状态');
            }

            this.boardData = await response.json();
            this.renderBoard();
            this.updateScore();
            this.updateMovesLeft();
        } catch (error) {
            console.error('初始化游戏失败:', error);
            alert('初始化游戏失败，请刷新页面重试。');
        }
    }

    renderBoard() {
        // 清空游戏面板
        this.boardElement.innerHTML = '';

        const rows = this.boardData.rows;
        const columns = this.boardData.columns;
        const tiles = this.boardData.tiles;

        // 设置网格大小
        this.boardElement.style.gridTemplateColumns = `repeat(${columns}, 1fr)`;
        this.boardElement.style.gridTemplateRows = `repeat(${rows}, 1fr)`;

        // 创建瓦片元素
        for (let i = 0; i < rows; i++) {
            for (let j = 0; j < columns; j++) {
                const tile = tiles[i][j];
                const tileElement = document.createElement('div');
                tileElement.className = `tile ${tile.type.toLowerCase()}`;

                // 设置数据属性
                tileElement.dataset.row = i;
                tileElement.dataset.col = j;
                tileElement.dataset.id = tile.id;
                tileElement.dataset.type = tile.type;

                // 设置图片背景（如果有）
                if (tile.imageUrl) {
                    tileElement.style.backgroundImage = `url('${tile.imageUrl}')`;
                }

                // 添加特殊瓦片样式
                if (tile.special) {
                    tileElement.classList.add('special');
                    tileElement.dataset.specialEffect = tile.specialEffect;
                }

                // 添加点击事件
                tileElement.addEventListener('click', this.onTileClick.bind(this));

                // 添加到游戏面板
                this.boardElement.appendChild(tileElement);
            }
        }
    }

    onTileClick(event) {
        // 如果正在动画中或游戏结束，忽略点击
        if (this.isAnimating || this.gameOver) {
            return;
        }

        const clickedTile = event.currentTarget;
        const row = parseInt(clickedTile.dataset.row);
        const col = parseInt(clickedTile.dataset.col);

        // 如果没有选中的瓦片，选中当前瓦片
        if (!this.selectedTile) {
            this.selectedTile = clickedTile;
            clickedTile.classList.add('selected');
            return;
        }

        // 如果点击了已选中的瓦片，取消选中
        if (this.selectedTile === clickedTile) {
            this.selectedTile.classList.remove('selected');
            this.selectedTile = null;
            return;
        }

        // 获取选中的瓦片的位置
        const selectedRow = parseInt(this.selectedTile.dataset.row);
        const selectedCol = parseInt(this.selectedTile.dataset.col);

        // 检查是否相邻
        if (this.areAdjacent(selectedRow, selectedCol, row, col)) {
            // 尝试交换瓦片
            this.swapTiles(selectedRow, selectedCol, row, col);
        } else {
            // 不相邻，显示无效移动动画
            clickedTile.classList.add('invalid-move');
            // 播放无效移动音效
            if (window.SoundEffects) {
                window.SoundEffects.playInvalidSound();
            }
            setTimeout(() => {
                clickedTile.classList.remove('invalid-move');
            }, 500);

            // 取消之前的选择，选中新的瓦片
            this.selectedTile.classList.remove('selected');
            this.selectedTile = clickedTile;
            clickedTile.classList.add('selected');
        }
    }

    areAdjacent(row1, col1, row2, col2) {
        return (Math.abs(row1 - row2) === 1 && col1 === col2) ||
            (Math.abs(col1 - col2) === 1 && row1 === row2);
    }

    async swapTiles(row1, col1, row2, col2) {
        this.isAnimating = true;

        // 取消选中状态
        this.selectedTile.classList.remove('selected');

        // 发送移动请求到服务器
        try {
            const response = await fetch('/game/move', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ row1, col1, row2, col2 }),
            });

            if (!response.ok) {
                throw new Error('移动请求失败');
            }

            const result = await response.json();

            if (result.success) {
                // 播放交换音效
                if (window.SoundEffects) {
                    window.SoundEffects.playSwapSound();
                }

                // 更新游戏面板
                if (result.board) {
                    this.boardData = result.board;
                }

                // 显示交换动画
                this.animateSwap(row1, col1, row2, col2, () => {
                    // 如果有匹配，播放匹配音效
                    if (result.matches && result.matches.length > 0) {
                        if (window.SoundEffects) {
                            window.SoundEffects.playMatchSound();
                        }
                    }

                    // 更新游戏状态
                    this.renderBoard();
                    this.updateScore();
                    this.updateMovesLeft();

                    // 检查游戏是否结束
                    if (result.gameOver) {
                        this.gameOver = true;
                        this.showGameOverModal(result.score);
                    }

                    this.isAnimating = false;
                });
            } else {
                // 显示无效移动动画
                const tile1 = this.getTileElement(row1, col1);
                const tile2 = this.getTileElement(row2, col2);

                // 播放无效移动音效
                if (window.SoundEffects) {
                    window.SoundEffects.playInvalidSound();
                }

                tile1.classList.add('invalid-move');
                tile2.classList.add('invalid-move');

                setTimeout(() => {
                    tile1.classList.remove('invalid-move');
                    tile2.classList.remove('invalid-move');
                    this.isAnimating = false;
                }, 500);
            }

            // 清除选中状态
            this.selectedTile = null;

        } catch (error) {
            console.error('移动请求失败:', error);
            this.isAnimating = false;
            this.selectedTile = null;
        }
    }

    animateSwap(row1, col1, row2, col2, callback) {
        const tile1 = this.getTileElement(row1, col1);
        const tile2 = this.getTileElement(row2, col2);

        // 获取位置信息
        const rect1 = tile1.getBoundingClientRect();
        const rect2 = tile2.getBoundingClientRect();

        // 计算移动距离
        const deltaX = rect2.left - rect1.left;
        const deltaY = rect2.top - rect1.top;

        // 设置动画
        tile1.style.transition = 'transform 0.3s';
        tile2.style.transition = 'transform 0.3s';

        tile1.style.transform = `translate(${deltaX}px, ${deltaY}px)`;
        tile2.style.transform = `translate(${-deltaX}px, ${-deltaY}px)`;

        // 动画结束后调用回调
        setTimeout(() => {
            tile1.style.transition = '';
            tile2.style.transition = '';
            tile1.style.transform = '';
            tile2.style.transform = '';
            callback();
        }, 300);
    }

    getTileElement(row, col) {
        return document.querySelector(`.tile[data-row="${row}"][data-col="${col}"]`);
    }

    updateScore() {
        this.scoreElement.textContent = this.boardData.score;
    }

    updateMovesLeft() {
        this.movesLeftElement.textContent = this.boardData.movesLeft;
    }

    showGameOverModal(finalScore) {
        const gameOverModal = document.getElementById('game-over-modal');
        const finalScoreElement = document.getElementById('final-score');

        // 播放游戏结束音效
        if (window.SoundEffects) {
            window.SoundEffects.playGameOverSound();
        }

        finalScoreElement.textContent = finalScore;
        gameOverModal.style.display = 'flex';

        // 创建游戏结束动画
        this.createGameOverAnimation();
    }

    createGameOverAnimation() {
        const animation = document.createElement('div');
        animation.className = 'game-over-animation';
        document.body.appendChild(animation);

        // 创建彩色纸屑
        const colors = ['#ff80ab', '#f48fb1', '#f06292', '#ec407a', '#e91e63'];

        for (let i = 0; i < 100; i++) {
            const confetti = document.createElement('div');
            confetti.className = 'confetti';
            confetti.style.left = `${Math.random() * 100}%`;
            confetti.style.animationDelay = `${Math.random() * 3}s`;
            confetti.style.backgroundColor = colors[Math.floor(Math.random() * colors.length)];
            animation.appendChild(confetti);
        }

        // 3秒后移除动画
        setTimeout(() => {
            document.body.removeChild(animation);
        }, 6000);
    }
}

// 当文档加载完成后初始化游戏
document.addEventListener('DOMContentLoaded', () => {
    // 预加载音频，确保在用户交互时可以播放
    document.addEventListener('click', function initAudio() {
        if (window.SoundManager) {
            console.log('预加载音频...');
            // 尝试激活所有音频
            for (const sound in window.SoundManager.sounds) {
                if (window.SoundManager.sounds[sound]) {
                    window.SoundManager.sounds[sound].load();
                    window.SoundManager.sounds[sound].volume = 0.01;
                    window.SoundManager.sounds[sound].play().then(() => {
                        window.SoundManager.sounds[sound].pause();
                        window.SoundManager.sounds[sound].currentTime = 0;
                        window.SoundManager.sounds[sound].volume = window.SoundManager.volume;
                    }).catch(err => console.log(`预加载 ${sound} 失败`, err));
                }
            }
        }
        document.removeEventListener('click', initAudio);
    }, { once: true });

    const gameBoard = new GameBoard();

    // 添加新游戏按钮事件
    document.getElementById('new-game-btn').addEventListener('click', () => {
        window.location.href = '/game/new';
    });

    // 添加重新开始按钮事件
    document.getElementById('restart-btn').addEventListener('click', () => {
        window.location.href = '/game/new';
    });

    // 添加帮助按钮事件
    document.getElementById('help-btn').addEventListener('click', () => {
        document.getElementById('help-modal').style.display = 'flex';
    });

    // 添加关闭帮助按钮事件
    document.getElementById('close-help-btn').addEventListener('click', () => {
        document.getElementById('help-modal').style.display = 'none';
    });

    // 添加分享按钮事件
    document.getElementById('share-btn').addEventListener('click', () => {
        const score = document.getElementById('final-score').textContent;
        const shareText = `我在巧巧消消乐中获得了${score}分！来挑战我吧！`;

        // 检查是否支持分享API
        if (navigator.share) {
            navigator.share({
                title: '巧巧消消乐',
                text: shareText,
                url: window.location.origin + '/game',
            })
                .catch(error => {
                    console.error('分享失败:', error);

                    // 如果分享API失败，使用剪贴板
                    fallbackShare(shareText);
                });
        } else {
            // 不支持分享API，使用剪贴板
            fallbackShare(shareText);
        }
    });

    // 分享失败后的备用方案
    function fallbackShare(text) {
        // 创建临时输入框
        const input = document.createElement('input');
        input.value = text;
        document.body.appendChild(input);

        // 选择文本
        input.select();
        input.setSelectionRange(0, 99999);

        // 复制文本
        document.execCommand('copy');

        // 移除临时输入框
        document.body.removeChild(input);

        // 显示提示
        alert('分享文本已复制到剪贴板！');
    }

    // 开发环境添加测试音效按钮
    if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
        const testBtn = document.createElement('button');
        testBtn.innerText = '测试音效';
        testBtn.style.position = 'fixed';
        testBtn.style.top = '10px';
        testBtn.style.right = '10px';
        testBtn.style.zIndex = '9999';
        testBtn.style.padding = '5px 10px';

        testBtn.addEventListener('click', () => {
            console.log('测试音效播放');
            if (window.SoundEffects) {
                window.SoundEffects.playMatchSound();
                setTimeout(() => window.SoundEffects.playSwapSound(), 1000);
                setTimeout(() => window.SoundEffects.playInvalidSound(), 2000);
                setTimeout(() => window.SoundEffects.playGameOverSound(), 3000);
            } else {
                console.error('SoundEffects 未定义');
            }
        });

        document.body.appendChild(testBtn);
    }
});