// 游戏动画效果
class AnimationManager {
    constructor() {
        this.animations = [];
    }

    // 创建匹配消除动画
    createMatchAnimation(element, callback) {
        element.classList.add('disappearing');

        // 在瓦片位置创建分数弹出效果
        this.createScorePopup(element);

        // 动画结束后调用回调
        setTimeout(() => {
            if (element.parentNode) {
                element.classList.remove('disappearing');
            }
            if (callback) callback();
        }, 500);
    }

    // 创建下落动画
    createDropAnimation(element, callback) {
        element.classList.add('dropping');

        // 动画结束后调用回调
        setTimeout(() => {
            element.classList.remove('dropping');
            if (callback) callback();
        }, 500);
    }

    // 创建无效移动动画
    createInvalidMoveAnimation(element, callback) {
        element.classList.add('invalid-move');

        // 播放无效移动音效
        if (window.SoundEffects) {
            window.SoundEffects.playInvalidSound();
        }

        // 动画结束后调用回调
        setTimeout(() => {
            element.classList.remove('invalid-move');
            if (callback) callback();
        }, 500);
    }

    // 创建交换动画
    createSwapAnimation(element1, element2, callback) {
        // 获取元素位置
        const rect1 = element1.getBoundingClientRect();
        const rect2 = element2.getBoundingClientRect();

        // 计算移动距离
        const deltaX = rect2.left - rect1.left;
        const deltaY = rect2.top - rect1.top;

        // 设置动画
        element1.style.transition = 'transform 0.3s';
        element2.style.transition = 'transform 0.3s';

        element1.style.zIndex = '10';
        element2.style.zIndex = '10';

        element1.style.transform = `translate(${deltaX}px, ${deltaY}px)`;
        element2.style.transform = `translate(${-deltaX}px, ${-deltaY}px)`;

        // 播放交换音效
        if (window.SoundEffects) {
            window.SoundEffects.playSwapSound();
        }

        // 动画结束后调用回调
        setTimeout(() => {
            element1.style.transition = '';
            element2.style.transition = '';
            element1.style.transform = '';
            element2.style.transform = '';
            element1.style.zIndex = '';
            element2.style.zIndex = '';
            if (callback) callback();
        }, 300);
    }

    // 创建分数弹出效果
    createScorePopup(element, points = 10) {
        const rect = element.getBoundingClientRect();
        const popup = document.createElement('div');
        popup.className = 'score-popup';
        popup.textContent = `+${points}`;

        // 设置位置
        popup.style.left = `${rect.left + rect.width / 2}px`;
        popup.style.top = `${rect.top + rect.height / 2}px`;

        // 添加到文档中
        document.body.appendChild(popup);

        // 播放匹配音效
        if (window.SoundEffects) {
            window.SoundEffects.playMatchSound();
        }

        // 动画结束后移除元素
        setTimeout(() => {
            if (popup.parentNode) {
                document.body.removeChild(popup);
            }
        }, 1000);
    }

    // 创建游戏结束动画
    createGameOverAnimation() {
        // 播放游戏结束音效
        if (window.SoundEffects) {
            window.SoundEffects.playGameOverSound();
        }

        const animation = document.createElement('div');
        animation.className = 'game-over-animation';
        document.body.appendChild(animation);

        // 创建彩色纸屑
        const colors = ['#ff80ab', '#f48fb1', '#f06292', '#ec407a', '#e91e63'];

        for (let i = 0; i < 100; i++) {
            const confetti = document.createElement('div');
            confetti.className = 'confetti';
            confetti.style.left = `${Math.random() * 100}%`;
            confetti.style.top = '-10px';
            confetti.style.animationDelay = `${Math.random() * 3}s`;
            confetti.style.backgroundColor = colors[Math.floor(Math.random() * colors.length)];
            animation.appendChild(confetti);
        }

        // 3秒后移除动画
        setTimeout(() => {
            if (animation.parentNode) {
                document.body.removeChild(animation);
            }
        }, 6000);
    }
}

// 创建并导出动画管理器实例
const animationManager = new AnimationManager();

// 将动画管理器暴露给全局作用域
window.AnimationManager = animationManager;