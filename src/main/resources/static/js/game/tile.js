// 游戏瓦片逻辑
class Tile {
    constructor(id, type, x, y) {
        this.id = id;
        this.type = type;
        this.x = x;
        this.y = y;
        this.isSpecial = false;
        this.specialEffect = null;
        this.isSelected = false;
        this.isMatched = false;
        this.isNew = false;
        this.element = null;
    }

    // 创建DOM元素
    createElement() {
        const element = document.createElement('div');
        element.className = `tile ${this.type.toLowerCase()}`;
        element.dataset.id = this.id;
        element.dataset.type = this.type;
        element.dataset.row = this.y;
        element.dataset.col = this.x;

        // 设置特殊瓦片样式
        if (this.isSpecial) {
            element.classList.add('special');
            element.dataset.specialEffect = this.specialEffect;

            // 添加特殊效果标志
            const effectIcon = document.createElement('span');
            effectIcon.className = 'effect-icon';

            switch (this.specialEffect) {
                case 'row_clear':
                    effectIcon.innerHTML = '↔️';
                    break;
                case 'column_clear':
                    effectIcon.innerHTML = '↕️';
                    break;
                case 'bomb':
                    effectIcon.innerHTML = '💣';
                    break;
                case 'color_bomb':
                    effectIcon.innerHTML = '🌈';
                    break;
            }

            element.appendChild(effectIcon);
        }

        this.element = element;
        return element;
    }

    // 设置选中状态
    setSelected(selected) {
        this.isSelected = selected;

        if (this.element) {
            if (selected) {
                this.element.classList.add('selected');
            } else {
                this.element.classList.remove('selected');
            }
        }
    }

    // 设置匹配状态
    setMatched(matched) {
        this.isMatched = matched;

        if (this.element && matched) {
            this.element.classList.add('matched');
        }
    }

    // 设置新瓦片状态
    setNew(isNew) {
        this.isNew = isNew;

        if (this.element && isNew) {
            this.element.classList.add('new');
        }
    }

    // 设置特殊瓦片
    setSpecial(isSpecial, effect) {
        this.isSpecial = isSpecial;
        this.specialEffect = effect;

        if (this.element) {
            if (isSpecial) {
                this.element.classList.add('special');
                this.element.dataset.specialEffect = effect;

                // 添加特殊效果标志
                if (!this.element.querySelector('.effect-icon')) {
                    const effectIcon = document.createElement('span');
                    effectIcon.className = 'effect-icon';

                    switch (effect) {
                        case 'row_clear':
                            effectIcon.innerHTML = '↔️';
                            break;
                        case 'column_clear':
                            effectIcon.innerHTML = '↕️';
                            break;
                        case 'bomb':
                            effectIcon.innerHTML = '💣';
                            break;
                        case 'color_bomb':
                            effectIcon.innerHTML = '🌈';
                            break;
                    }

                    this.element.appendChild(effectIcon);
                }
            } else {
                this.element.classList.remove('special');
                this.element.removeAttribute('data-special-effect');

                // 移除特殊效果标志
                const effectIcon = this.element.querySelector('.effect-icon');
                if (effectIcon) {
                    this.element.removeChild(effectIcon);
                }
            }
        }
    }

    // 播放消除动画
    playDisappearAnimation(callback) {
        if (this.element) {
            this.element.classList.add('disappearing');

            setTimeout(() => {
                this.element.classList.remove('disappearing');
                if (callback) callback();
            }, 500);
        }
    }

    // 播放下落动画
    playDropAnimation(callback) {
        if (this.element) {
            this.element.classList.add('dropping');

            setTimeout(() => {
                this.element.classList.remove('dropping');
                if (callback) callback();
            }, 500);
        }
    }

    // 更新位置信息
    updatePosition(x, y) {
        this.x = x;
        this.y = y;

        if (this.element) {
            this.element.dataset.row = y;
            this.element.dataset.col = x;
        }
    }
}

// 导出Tile类以供board.js使用
window.Tile = Tile;