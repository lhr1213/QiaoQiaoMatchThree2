// 游戏音效控制
class SoundManager {
    constructor() {
        this.sounds = {
            match: null,
            swap: null,
            invalid: null,
            background: null,
            gameOver: null
        };

        this.isMuted = false;
        this.volume = 0.5; // 默认音量
        this.soundsLoaded = false;
        this.init();
    }

    init() {
        console.log('初始化音效管理器...');
        try {
            // 加载音效
            this.sounds.match = new Audio('/audio/match.mp3');
            this.sounds.match.addEventListener('error', (e) => {
                console.error('加载match.mp3失败:', e);
            });

            this.sounds.swap = new Audio('/audio/swap.mp3');
            this.sounds.swap.addEventListener('error', (e) => {
                console.error('加载swap.mp3失败:', e);
            });

            this.sounds.invalid = new Audio('/audio/invalid.mp3');
            this.sounds.invalid.addEventListener('error', (e) => {
                console.error('加载invalid.mp3失败:', e);
            });

            this.sounds.gameOver = new Audio('/audio/game-over.mp3');
            this.sounds.gameOver.addEventListener('error', (e) => {
                console.error('加载game-over.mp3失败:', e);
            });

            // 加载背景音乐
            this.sounds.background = new Audio('/audio/background.mp3');
            this.sounds.background.addEventListener('error', (e) => {
                console.error('加载background.mp3失败:', e);
            });
            this.sounds.background.loop = true;
            this.sounds.background.volume = 0.3; // 背景音乐音量较小

            // 为所有音效设置音量
            for (const sound in this.sounds) {
                if (this.sounds[sound]) {
                    this.sounds[sound].volume = this.volume;
                }
            }

            this.soundsLoaded = true;
            console.log('音效加载完成');
        } catch (error) {
            console.error('初始化音效失败:', error);
        }

        // 监听用户交互以启动音频播放
        document.addEventListener('click', () => {
            console.log('用户交互，尝试启动音频...');
            this.preloadSoundEffects();  // 只预加载音效，不包括背景音乐
            this.playBackgroundMusic();  // 单独处理背景音乐
        }, { once: true });

        // 添加静音切换和音量控制按钮
        this.createAudioControls();
    }

    preloadSoundEffects() {
        // 预加载除背景音乐外的所有音效
        for (const sound in this.sounds) {
            if (this.sounds[sound] && sound !== 'background') {
                this.sounds[sound].load();
                // 以极低音量播放并立即暂停以激活音频
                const originalVolume = this.sounds[sound].volume;
                this.sounds[sound].volume = 0.001;
                const playPromise = this.sounds[sound].play();

                if (playPromise !== undefined) {
                    playPromise.then(() => {
                        this.sounds[sound].pause();
                        this.sounds[sound].currentTime = 0;
                        this.sounds[sound].volume = originalVolume;
                        console.log(`预加载音效 ${sound} 成功`);
                    }).catch(error => {
                        console.warn(`预加载音效 ${sound} 失败:`, error);
                    });
                }
            }
        }
    }

    createAudioControls() {
        // 创建音乐控制容器
        const audioControls = document.createElement('div');
        audioControls.className = 'audio-controls';
        audioControls.style.position = 'fixed';
        audioControls.style.bottom = '20px';
        audioControls.style.right = '20px';
        audioControls.style.zIndex = '100';
        audioControls.style.display = 'flex';
        audioControls.style.flexDirection = 'column';
        audioControls.style.gap = '10px';

        // 创建静音按钮
        const muteBtn = document.createElement('button');
        muteBtn.className = 'btn mute-btn';
        muteBtn.innerHTML = '🔊';
        muteBtn.style.width = '50px';
        muteBtn.style.height = '50px';
        muteBtn.style.borderRadius = '50%';
        muteBtn.style.padding = '0';
        muteBtn.style.display = 'flex';
        muteBtn.style.justifyContent = 'center';
        muteBtn.style.alignItems = 'center';

        muteBtn.addEventListener('click', () => {
            this.toggleMute();
            muteBtn.innerHTML = this.isMuted ? '🔇' : '🔊';
        });

        // 创建音量控制滑块
        const volumeControl = document.createElement('input');
        volumeControl.type = 'range';
        volumeControl.min = '0';
        volumeControl.max = '1';
        volumeControl.step = '0.1';
        volumeControl.value = this.volume;
        volumeControl.style.width = '100px';
        volumeControl.style.transform = 'rotate(-90deg)';
        volumeControl.style.position = 'absolute';
        volumeControl.style.bottom = '60px';
        volumeControl.style.right = '-25px';
        volumeControl.style.opacity = '0';
        volumeControl.style.transition = 'opacity 0.3s';

        volumeControl.addEventListener('input', (e) => {
            this.setVolume(parseFloat(e.target.value));
        });

        // 显示/隐藏音量控制
        muteBtn.addEventListener('mouseenter', () => {
            volumeControl.style.opacity = '1';
        });

        audioControls.addEventListener('mouseleave', () => {
            volumeControl.style.opacity = '0';
        });

        // 添加到DOM
        audioControls.appendChild(muteBtn);
        audioControls.appendChild(volumeControl);
        document.body.appendChild(audioControls);
    }

    playSound(soundName) {
        console.log(`尝试播放音效: ${soundName}`);

        if (this.isMuted || !this.sounds[soundName]) {
            console.log(`音效未播放: ${this.isMuted ? '已静音' : '找不到音效'}`);
            return;
        }

        // 重置音效以便重新播放
        this.sounds[soundName].currentTime = 0;
        this.sounds[soundName].play().catch(error => {
            console.warn(`无法播放音效 ${soundName}:`, error);
        });
    }

    playBackgroundMusic() {
        console.log('尝试播放背景音乐...');
        if (this.isMuted) {
            console.log('背景音乐未播放：已静音');
            return;
        }

        // 确保背景音乐已加载
        this.sounds.background.load();

        // 设置背景音乐音量
        this.sounds.background.volume = this.volume * 0.6;

        // 播放背景音乐
        this.sounds.background.play().then(() => {
            console.log('背景音乐开始播放');
        }).catch(error => {
            console.warn('无法播放背景音乐:', error);

            // 添加重试逻辑
            setTimeout(() => {
                console.log('尝试重新播放背景音乐...');
                this.sounds.background.play().catch(e =>
                    console.warn('重新播放背景音乐失败:', e)
                );
            }, 2000);
        });
    }

    stopBackgroundMusic() {
        if (this.sounds.background) {
            this.sounds.background.pause();
            this.sounds.background.currentTime = 0;
            console.log('背景音乐已停止');
        }
    }

    pauseBackgroundMusic() {
        if (this.sounds.background) {
            this.sounds.background.pause();
            console.log('背景音乐已暂停');
        }
    }

    resumeBackgroundMusic() {
        if (!this.isMuted && this.sounds.background) {
            console.log('尝试恢复背景音乐...');
            this.sounds.background.play().then(() => {
                console.log('背景音乐已恢复');
            }).catch(error => {
                console.warn('无法恢复背景音乐:', error);
            });
        }
    }

    toggleMute() {
        this.isMuted = !this.isMuted;
        console.log(`音频${this.isMuted ? '已静音' : '已取消静音'}`);

        // 更新所有音效的状态
        for (const sound in this.sounds) {
            if (this.sounds[sound]) {
                this.sounds[sound].muted = this.isMuted;
            }
        }

        // 如果取消静音，尝试播放背景音乐
        if (!this.isMuted) {
            this.playBackgroundMusic();
        } else {
            // 静音但不停止背景音乐，只设置为静音
            if (this.sounds.background) {
                this.sounds.background.muted = true;
            }
        }
    }

    setVolume(value) {
        this.volume = value;
        console.log(`音量设置为: ${value}`);

        // 更新所有音效的音量
        for (const sound in this.sounds) {
            if (this.sounds[sound]) {
                // 背景音乐音量稍低
                if (sound === 'background') {
                    this.sounds[sound].volume = value * 0.6;
                } else {
                    this.sounds[sound].volume = value;
                }
            }
        }
    }
}

// 创建并导出音效管理器实例
const soundManager = new SoundManager();

// 导出音效事件函数
function playMatchSound() {
    soundManager.playSound('match');
}

function playSwapSound() {
    soundManager.playSound('swap');
}

function playInvalidSound() {
    soundManager.playSound('invalid');
}

function playGameOverSound() {
    soundManager.playSound('gameOver');
}

// 将函数暴露给全局作用域
window.SoundEffects = {
    playMatchSound,
    playSwapSound,
    playInvalidSound,
    playGameOverSound
};

// 导出声音管理器
window.SoundManager = soundManager;

// 开发环境添加测试按钮
if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
    document.addEventListener('DOMContentLoaded', () => {
        const testAudioBtn = document.createElement('button');
        testAudioBtn.innerText = '测试背景音乐';
        testAudioBtn.style.position = 'fixed';
        testAudioBtn.style.bottom = '80px';
        testAudioBtn.style.right = '20px';
        testAudioBtn.style.zIndex = '101';
        testAudioBtn.style.padding = '5px 10px';

        testAudioBtn.addEventListener('click', () => {
            console.log('手动播放背景音乐');
            soundManager.playBackgroundMusic();
        });

        document.body.appendChild(testAudioBtn);
    });
}