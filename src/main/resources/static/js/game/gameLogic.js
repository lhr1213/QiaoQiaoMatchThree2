// 游戏逻辑
class GameLogic {
    constructor(board) {
        this.board = board;
        this.isAnimating = false;
        this.isPaused = false;
        this.tileTypes = ['红色', '蓝色', '绿色', '黄色', '紫色'];
    }

    // 检查是否有匹配
    hasMatches() {
        const tiles = this.board.tiles;
        const rows = this.board.rows;
        const columns = this.board.columns;

        // 水平检查
        for (let i = 0; i < rows; i++) {
            for (let j = 0; j < columns - 2; j++) {
                if (tiles[i][j] && tiles[i][j+1] && tiles[i][j+2] &&
                    tiles[i][j].type === tiles[i][j+1].type &&
                    tiles[i][j].type === tiles[i][j+2].type) {
                    return true;
                }
            }
        }

        // 垂直检查
        for (let i = 0; i < rows - 2; i++) {
            for (let j = 0; j < columns; j++) {
                if (tiles[i][j] && tiles[i+1][j] && tiles[i+2][j] &&
                    tiles[i][j].type === tiles[i+1][j].type &&
                    tiles[i][j].type === tiles[i+2][j].type) {
                    return true;
                }
            }
        }

        return false;
    }

    // 查找所有匹配
    findAllMatches() {
        const tiles = this.board.tiles;
        const rows = this.board.rows;
        const columns = this.board.columns;
        const allMatches = [];

        // 水平匹配
        for (let i = 0; i < rows; i++) {
            let j = 0;
            while (j < columns - 2) {
                if (tiles[i][j] && tiles[i][j+1] && tiles[i][j+2] &&
                    tiles[i][j].type === tiles[i][j+1].type &&
                    tiles[i][j].type === tiles[i][j+2].type) {

                    // 找到匹配开始处
                    const type = tiles[i][j].type;
                    const match = [[i, j]];

                    // 继续检查是否有更多连续匹配
                    let endJ = j + 2;
                    for (let k = j + 3; k < columns; k++) {
                        if (tiles[i][k] && tiles[i][k].type === type) {
                            endJ = k;
                        } else {
                            break;
                        }
                    }

                    // 添加所有匹配的位置
                    for (let k = j + 1; k <= endJ; k++) {
                        match.push([i, k]);
                    }

                    allMatches.push(match);
                    j = endJ + 1;
                } else {
                    j++;
                }
            }
        }

        // 垂直匹配
        for (let j = 0; j < columns; j++) {
            let i = 0;
            while (i < rows - 2) {
                if (tiles[i][j] && tiles[i+1][j] && tiles[i+2][j] &&
                    tiles[i][j].type === tiles[i+1][j].type &&
                    tiles[i][j].type === tiles[i+2][j].type) {

                    // 找到匹配开始处
                    const type = tiles[i][j].type;
                    const match = [[i, j]];

                    // 继续检查是否有更多连续匹配
                    let endI = i + 2;
                    for (let k = i + 3; k < rows; k++) {
                        if (tiles[k][j] && tiles[k][j].type === type) {
                            endI = k;
                        } else {
                            break;
                        }
                    }

                    // 添加所有匹配的位置
                    for (let k = i + 1; k <= endI; k++) {
                        match.push([k, j]);
                    }

                    allMatches.push(match);
                    i = endI + 1;
                } else {
                    i++;
                }
            }
        }

        return allMatches;
    }

    // 处理匹配并移除瓦片
    processMatches() {
        // 如果正在动画中，跳过
        if (this.isAnimating) return false;

        const allMatches = this.findAllMatches();
        if (allMatches.length === 0) return false;

        this.isAnimating = true;

        // 计算分数
        let score = 0;
        for (const match of allMatches) {
            score += this.calculateMatchScore(match);
        }

        // 更新总分
        this.board.score += score;
        this.board.updateScore();

        // 创建特殊瓦片（对于4个或更多瓦片的匹配）
        for (const match of allMatches) {
            if (match.length >= 4) {
                const [row, col] = match[0];
                const type = this.board.tiles[row][col].type;
                let specialEffect = null;

                if (match.length === 4) {
                    // 行或列清除
                    specialEffect = this.isHorizontalMatch(match) ? 'row_clear' : 'column_clear';
                } else if (match.length >= 5) {
                    // 彩色炸弹或普通炸弹
                    specialEffect = match.length >= 5 ? 'color_bomb' : 'bomb';
                }

                // 标记要创建特殊瓦片的位置
                this.board.specialTileToCreate = { row, col, type, specialEffect };
            }
        }

        // 移除匹配的瓦片
        const tilesToRemove = new Set();
        for (const match of allMatches) {
            for (const [row, col] of match) {
                tilesToRemove.add(`${row},${col}`);
            }
        }

        // 移除瓦片并播放动画
        const animations = [];
        for (const posStr of tilesToRemove) {
            const [row, col] = posStr.split(',').map(Number);
            const tile = this.board.tiles[row][col];

            if (tile) {
                // 播放消除动画
                animations.push(new Promise(resolve => {
                    if (window.AnimationManager) {
                        window.AnimationManager.createMatchAnimation(tile.element, resolve);
                    } else {
                        tile.playDisappearAnimation(resolve);
                    }
                }));

                // 移除瓦片
                this.board.tiles[row][col] = null;
            }
        }

        // 所有消除动画完成后
        Promise.all(animations).then(() => {
            // 填充空位
            this.fillEmptySpaces().then(() => {
                // 检查是否有新的匹配
                if (this.hasMatches()) {
                    // 递归处理新的匹配
                    setTimeout(() => {
                        this.processMatches();
                    }, 300);
                } else {
                    // 创建特殊瓦片
                    if (this.board.specialTileToCreate) {
                        const { row, col, type, specialEffect } = this.board.specialTileToCreate;
                        const tile = this.board.tiles[row][col];

                        if (tile) {
                            tile.setSpecial(true, specialEffect);
                        }

                        this.board.specialTileToCreate = null;
                    }

                    // 检查是否有可能的移动
                    if (!this.hasPossibleMoves()) {
                        // 重新洗牌面板
                        this.shuffleBoard();
                    }

                    this.isAnimating = false;

                    // 检查游戏是否结束
                    if (this.board.movesLeft <= 0) {
                        this.board.gameOver = true;
                        this.board.showGameOverModal();
                    }
                }
            });
        });

        return true;
    }

    // 填充空位
    fillEmptySpaces() {
        return new Promise(resolve => {
            const tiles = this.board.tiles;
            const rows = this.board.rows;
            const columns = this.board.columns;
            const animations = [];

            // 下落现有瓦片
            for (let j = 0; j < columns; j++) {
                let emptySpaces = 0;

                // 从底部向上遍历每一列
                for (let i = rows - 1; i >= 0; i--) {
                    if (tiles[i][j] === null) {
                        // 发现空位，增加空位计数
                        emptySpaces++;
                    } else if (emptySpaces > 0) {
                        // 找到非空瓦片，且下方有空位，执行下落
                        const tile = tiles[i][j];
                        const newRow = i + emptySpaces;

                        // 更新位置信息
                        tile.updatePosition(j, newRow);

                        // 移动瓦片
                        tiles[newRow][j] = tile;
                        tiles[i][j] = null;

                        // 播放下落动画
                        animations.push(new Promise(resolve => {
                            if (window.AnimationManager) {
                                window.AnimationManager.createDropAnimation(tile.element, resolve);
                            } else {
                                tile.playDropAnimation(resolve);
                            }
                        }));
                    }
                }

                // 在顶部填充新瓦片
                for (let i = emptySpaces - 1; i >= 0; i--) {
                    const type = this.getRandomTileType();
                    const tile = new window.Tile(i * columns + j, type, j, i);
                    tile.setNew(true);

                    // 添加到面板
                    tiles[i][j] = tile;

                    // 创建DOM元素并添加到面板
                    const tileElement = tile.createElement();
                    this.board.boardElement.appendChild(tileElement);

                    // 播放下落动画
                    animations.push(new Promise(resolve => {
                        if (window.AnimationManager) {
                            window.AnimationManager.createDropAnimation(tileElement, resolve);
                        } else {
                            tile.playDropAnimation(resolve);
                        }
                    }));
                }
            }

            // 所有动画完成后
            Promise.all(animations).then(() => {
                resolve();
            });
        });
    }

    // 判断是否是水平匹配
    isHorizontalMatch(match) {
        const [row, _] = match[0];
        for (const [r, _] of match) {
            if (r !== row) return false;
        }
        return true;
    }

    // 计算匹配分数
    calculateMatchScore(match) {
        const baseScore = match.length * 10;

        if (match.length === 4) {
            return baseScore * 2;
        } else if (match.length >= 5) {
            return baseScore * 3;
        }

        return baseScore;
    }

    // 检查是否有可能的移动
    hasPossibleMoves() {
        const tiles = this.board.tiles;
        const rows = this.board.rows;
        const columns = this.board.columns;

        // 检查每个位置的四个方向
        for (let i = 0; i < rows; i++) {
            for (let j = 0; j < columns; j++) {
                // 向右交换
                if (j < columns - 1) {
                    if (this.checkPotentialMatch(i, j, i, j + 1)) {
                        return true;
                    }
                }

                // 向下交换
                if (i < rows - 1) {
                    if (this.checkPotentialMatch(i, j, i + 1, j)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // 检查交换两个位置是否能形成匹配
    checkPotentialMatch(row1, col1, row2, col2) {
        const tiles = this.board.tiles;

        // 获取瓦片类型
        if (!tiles[row1][col1] || !tiles[row2][col2]) {
            return false;
        }

        const type1 = tiles[row1][col1].type;
        const type2 = tiles[row2][col2].type;

        // 临时交换瓦片
        tiles[row1][col1].type = type2;
        tiles[row2][col2].type = type1;

        const hasMatch = this.hasMatches();

        // 交换回来
        tiles[row1][col1].type = type1;
        tiles[row2][col2].type = type2;

        return hasMatch;
    }

    // 重新洗牌面板
    shuffleBoard() {
        const tiles = this.board.tiles;
        const rows = this.board.rows;
        const columns = this.board.columns;

        // 随机重新分配瓦片类型
        for (let i = 0; i < rows; i++) {
            for (let j = 0; j < columns; j++) {
                if (tiles[i][j]) {
                    const type = this.getRandomTileType();
                    tiles[i][j].type = type;

                    // 更新DOM元素
                    if (tiles[i][j].element) {
                        // 移除所有颜色类
                        this.tileTypes.forEach(t => {
                            tiles[i][j].element.classList.remove(t.toLowerCase());
                        });

                        // 添加新颜色类
                        tiles[i][j].element.classList.add(type.toLowerCase());
                        tiles[i][j].element.dataset.type = type;
                    }
                }
            }
        }

        // 确保没有初始匹配和确保有可能的移动
        if (this.hasMatches() || !this.hasPossibleMoves()) {
            this.shuffleBoard();
        }
    }

    // 随机生成瓦片类型
    getRandomTileType() {
        return this.tileTypes[Math.floor(Math.random() * this.tileTypes.length)];
    }

    // 处理特殊瓦片效果
    processSpecialTile(row, col) {
        const tile = this.board.tiles[row][col];

        if (!tile || !tile.isSpecial) {
            return false;
        }

        const effect = tile.specialEffect;
        const affectedTiles = this.getSpecialEffectArea(row, col, effect);

        // 播放特殊效果动画
        if (window.AnimationManager) {
            // 根据不同效果播放不同动画
            switch (effect) {
                case 'row_clear':
                    window.AnimationManager.createRowClearAnimation(row);
                    break;
                case 'column_clear':
                    window.AnimationManager.createColumnClearAnimation(col);
                    break;
                case 'bomb':
                    window.AnimationManager.createBombAnimation(row, col);
                    break;
                case 'color_bomb':
                    window.AnimationManager.createColorBombAnimation(row, col, tile.type);
                    break;
            }
        }

        // 移除受影响的瓦片
        for (const [r, c] of affectedTiles) {
            if (this.board.tiles[r][c]) {
                this.board.tiles[r][c] = null;
            }
        }

        // 计算分数
        const scoreGain = affectedTiles.length * 10;
        this.board.score += scoreGain;
        this.board.updateScore();

        // 填充空位
        this.fillEmptySpaces().then(() => {
            // 检查是否有新的匹配
            if (this.hasMatches()) {
                this.processMatches();
            }
        });

        return true;
    }

    // 获取特殊瓦片效果区域
    getSpecialEffectArea(row, col, effect) {
        const tiles = this.board.tiles;
        const rows = this.board.rows;
        const columns = this.board.columns;
        const affectedTiles = [];

        switch (effect) {
            case 'row_clear':
                // 整行效果
                for (let j = 0; j < columns; j++) {
                    affectedTiles.push([row, j]);
                }
                break;

            case 'column_clear':
                // 整列效果
                for (let i = 0; i < rows; i++) {
                    affectedTiles.push([i, col]);
                }
                break;

            case 'bomb':
                // 3x3 爆炸效果
                for (let i = Math.max(0, row - 1); i <= Math.min(rows - 1, row + 1); i++) {
                    for (let j = Math.max(0, col - 1); j <= Math.min(columns - 1, col + 1); j++) {
                        affectedTiles.push([i, j]);
                    }
                }
                break;

            case 'color_bomb':
                // 所有同色瓦片效果
                const targetType = tiles[row][col].type;

                for (let i = 0; i < rows; i++) {
                    for (let j = 0; j < columns; j++) {
                        if (tiles[i][j] && tiles[i][j].type === targetType) {
                            affectedTiles.push([i, j]);
                        }
                    }
                }
                break;
        }

        return affectedTiles;
    }
}

// 将GameLogic类导出给全局作用域
window.GameLogic = GameLogic;