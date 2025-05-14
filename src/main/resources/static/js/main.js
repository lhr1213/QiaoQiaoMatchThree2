/**
 * 巧巧消消乐 - 主JavaScript文件
 * 负责游戏初始化、全局事件处理和页面控制
 */

// 等待文档完全加载
document.addEventListener('DOMContentLoaded', function() {
    // 游戏全局配置
    const gameConfig = {
        boardSize: 8,              // 游戏面板大小
        defaultMoves: 20,          // 默认移动次数
        tileTypes: ['红色', '蓝色', '绿色', '黄色', '紫色', '白色', '黑色', '棕色', '橙色'], // 瓦片类型
        animationSpeed: 300,       // 动画速度（毫秒）
        scorePerTile: 10,          // 每个瓦片的基础分数
        comboMultiplier: 1.5,      // 连击倍数
        themeColor: 'pink',        // 默认主题颜色
        defaultVolume: 0.5,        // 默认音量
        saveDataKey: 'qiaoqiao_game_data' // 本地存储键名
    };

    // 游戏状态
    let gameState = {
        currentPage: getActivePage(),
        isLoggedIn: checkLoginStatus(),
        currentUser: null,
        gameInProgress: false,
        gameBoard: null,
        darkMode: localStorage.getItem('darkMode') === 'true',
        muted: localStorage.getItem('muted') === 'true',
        volume: parseFloat(localStorage.getItem('volume') || gameConfig.defaultVolume)
    };

    // 初始化页面
    initPage();

    // 设置全局事件监听器
    setupEventListeners();

    // 初始化页面路由
    initRouter();

    // 尝试自动登录
    tryAutoLogin();

    /**
     * 获取当前激活的页面
     * @returns {string} 当前页面的ID
     */
    function getActivePage() {
        const path = window.location.pathname;

        if (path === '/' || path === '/index.html') {
            return 'home';
        } else if (path.includes('/game')) {
            return 'game';
        } else if (path.includes('/user/login')) {
            return 'login';
        } else if (path.includes('/user/register')) {
            return 'register';
        } else if (path.includes('/score/leaderboard')) {
            return 'leaderboard';
        } else if (path.includes('/user/profile')) {
            return 'profile';
        }

        return 'home'; // 默认主页
    }

    /**
     * 检查用户登录状态
     * @returns {boolean} 用户是否已登录
     */
    function checkLoginStatus() {
        return sessionStorage.getItem('userId') !== null ||
            localStorage.getItem('userId') !== null;
    }

    /**
     * 初始化当前页面
     */
    function initPage() {
        // 应用主题
        applyTheme();

        // 根据当前页面执行特定初始化
        switch (gameState.currentPage) {
            case 'home':
                setupHomePage();
                break;
            case 'game':
                setupGamePage();
                break;
            case 'login':
                setupLoginPage();
                break;
            case 'register':
                setupRegisterPage();
                break;
            case 'leaderboard':
                setupLeaderboardPage();
                break;
            case 'profile':
                setupProfilePage();
                break;
        }

        // 更新导航菜单
        updateNavigation();

        // 初始化音频
        initAudio();
    }

    /**
     * 设置全局事件监听器
     */
    function setupEventListeners() {
        // 主题切换
        const themeToggle = document.getElementById('theme-toggle');
        if (themeToggle) {
            themeToggle.addEventListener('click', toggleTheme);
        }

        // 移动设备菜单
        const menuToggle = document.getElementById('menu-toggle');
        const mobileMenu = document.getElementById('mobile-menu');

        if (menuToggle && mobileMenu) {
            menuToggle.addEventListener('click', function() {
                mobileMenu.classList.toggle('active');
            });
        }

        // 全局模态框关闭按钮
        const modalCloseButtons = document.querySelectorAll('.modal-close');
        modalCloseButtons.forEach(button => {
            button.addEventListener('click', function() {
                const modal = this.closest('.modal');
                if (modal) {
                    modal.style.display = 'none';
                }
            });
        });

        // 关闭模态框当点击外部
        window.addEventListener('click', function(event) {
            const modals = document.querySelectorAll('.modal');
            modals.forEach(modal => {
                if (event.target === modal) {
                    modal.style.display = 'none';
                }
            });
        });

        // 全局键盘事件
        window.addEventListener('keydown', function(event) {
            // ESC键关闭模态框
            if (event.key === 'Escape') {
                const modals = document.querySelectorAll('.modal');
                modals.forEach(modal => {
                    if (modal.style.display === 'flex') {
                        modal.style.display = 'none';
                    }
                });
            }

            // 处理游戏中的键盘控制
            if (gameState.gameInProgress && gameState.gameBoard) {
                handleGameKeyboardControls(event);
            }
        });

        // 回到顶部按钮
        const backToTopBtn = document.getElementById('back-to-top');
        if (backToTopBtn) {
            window.addEventListener('scroll', function() {
                if (window.pageYOffset > 300) {
                    backToTopBtn.style.display = 'block';
                } else {
                    backToTopBtn.style.display = 'none';
                }
            });

            backToTopBtn.addEventListener('click', function() {
                window.scrollTo({top: 0, behavior: 'smooth'});
            });
        }

        // 注销按钮
        const logoutButton = document.getElementById('logout-button');
        if (logoutButton) {
            logoutButton.addEventListener('click', handleLogout);
        }
    }

    /**
     * 初始化路由器
     */
    function initRouter() {
        // 拦截导航链接
        const navLinks = document.querySelectorAll('a[data-nav]');
        navLinks.forEach(link => {
            link.addEventListener('click', function(event) {
                event.preventDefault();
                const target = this.getAttribute('href');
                navigate(target);
            });
        });
    }

    /**
     * 导航到指定页面
     * @param {string} url 目标URL
     */
    function navigate(url) {
        window.location.href = url;
    }

    /**
     * 尝试自动登录
     */
    function tryAutoLogin() {
        const userId = localStorage.getItem('userId');
        if (userId) {
            // 尝试从API获取当前用户信息
            fetch('/user/api/current')
                .then(response => {
                    if (response.ok) {
                        return response.json();
                    }
                    throw new Error('未登录');
                })
                .then(user => {
                    gameState.currentUser = user;
                    gameState.isLoggedIn = true;
                    updateNavigation();
                })
                .catch(error => {
                    console.warn('自动登录失败:', error);
                    localStorage.removeItem('userId');
                    sessionStorage.removeItem('userId');
                    gameState.isLoggedIn = false;
                    updateNavigation();
                });
        }
    }

    /**
     * 更新导航菜单
     */
    function updateNavigation() {
        const authLinks = document.querySelectorAll('.auth-link');
        const userLinks = document.querySelectorAll('.user-link');
        const userDisplayName = document.getElementById('user-display-name');

        if (gameState.isLoggedIn) {
            // 显示用户相关链接，隐藏登录/注册链接
            authLinks.forEach(link => {
                link.style.display = 'none';
            });

            userLinks.forEach(link => {
                link.style.display = 'block';
            });

            // 显示用户名
            if (userDisplayName && gameState.currentUser) {
                userDisplayName.textContent = gameState.currentUser.nickname || gameState.currentUser.username;
            }
        } else {
            // 显示登录/注册链接，隐藏用户相关链接
            authLinks.forEach(link => {
                link.style.display = 'block';
            });

            userLinks.forEach(link => {
                link.style.display = 'none';
            });

            // 清空用户名
            if (userDisplayName) {
                userDisplayName.textContent = '';
            }
        }
    }

    /**
     * 处理用户注销
     */
    function handleLogout() {
        // 清除存储的用户信息
        localStorage.removeItem('userId');
        sessionStorage.removeItem('userId');

        // 更新游戏状态
        gameState.isLoggedIn = false;
        gameState.currentUser = null;

        // 更新界面
        updateNavigation();

        // 跳转到首页
        navigate('/');
    }

    /**
     * 应用主题设置
     */
    function applyTheme() {
        if (gameState.darkMode) {
            document.body.classList.add('dark-theme');
        } else {
            document.body.classList.remove('dark-theme');
        }
    }

    /**
     * 切换主题
     */
    function toggleTheme() {
        gameState.darkMode = !gameState.darkMode;
        localStorage.setItem('darkMode', gameState.darkMode);
        applyTheme();

        // 更新主题切换按钮文本
        const themeToggle = document.getElementById('theme-toggle');
        if (themeToggle) {
            themeToggle.textContent = gameState.darkMode ? '切换浅色主题' : '切换深色主题';
        }
    }

    /**
     * 初始化音频系统
     */
    function initAudio() {
        // 如果已经存在SoundManager，则配置它
        if (window.SoundManager) {
            // 应用存储的音频设置
            if (gameState.muted) {
                window.SoundManager.toggleMute();
            }

            window.SoundManager.setVolume(gameState.volume);

            // 在用户交互后尝试播放背景音乐
            document.addEventListener('click', function initAudioPlayback() {
                window.SoundManager.playBackgroundMusic();
                document.removeEventListener('click', initAudioPlayback);
            }, { once: true });
        }
    }

    /**
     * 处理游戏键盘控制
     * @param {KeyboardEvent} event 键盘事件
     */
    function handleGameKeyboardControls(event) {
        // 根据键盘按键执行不同操作
        switch (event.key) {
            case 'p':
            case 'P':
                // 暂停/恢复游戏
                togglePauseGame();
                break;
            case 'r':
            case 'R':
                // 重新开始游戏
                restartGame();
                break;
            case 'm':
            case 'M':
                // 静音/取消静音
                if (window.SoundManager) {
                    window.SoundManager.toggleMute();
                    gameState.muted = !gameState.muted;
                    localStorage.setItem('muted', gameState.muted);
                }
                break;
        }
    }

    /**
     * 暂停/恢复游戏
     */
    function togglePauseGame() {
        if (!gameState.gameBoard) return;

        const pauseModal = document.getElementById('pause-modal');
        if (pauseModal) {
            if (pauseModal.style.display === 'flex') {
                // 恢复游戏
                pauseModal.style.display = 'none';
                if (window.SoundManager) {
                    window.SoundManager.resumeBackgroundMusic();
                }
            } else {
                // 暂停游戏
                pauseModal.style.display = 'flex';
                if (window.SoundManager) {
                    window.SoundManager.pauseBackgroundMusic();
                }
            }
        }
    }

    /**
     * 重新开始游戏
     */
    function restartGame() {
        if (confirm('确定要重新开始游戏吗？当前进度将丢失。')) {
            window.location.href = '/game/new';
        }
    }

    /**
     * 设置首页
     */
    function setupHomePage() {
        // 首页特定的初始化逻辑
        const heroAnimation = document.getElementById('hero-animation');

        if (heroAnimation) {
            // 创建简单的动画效果
            createHeroAnimation(heroAnimation);
        }

        // 特性卡片悬停效果
        const featureCards = document.querySelectorAll('.feature');
        featureCards.forEach(card => {
            card.addEventListener('mouseenter', function() {
                this.classList.add('feature-hover');
            });

            card.addEventListener('mouseleave', function() {
                this.classList.remove('feature-hover');
            });
        });

        // 检查并显示欢迎回来消息
        if (gameState.isLoggedIn && gameState.currentUser) {
            const welcomeMessage = document.createElement('div');
            welcomeMessage.className = 'welcome-message';
            welcomeMessage.innerHTML = `<h3>欢迎回来，${gameState.currentUser.nickname || gameState.currentUser.username}！</h3>`;

            const heroSection = document.querySelector('.hero-section');
            if (heroSection) {
                heroSection.appendChild(welcomeMessage);

                // 3秒后淡出
                setTimeout(() => {
                    welcomeMessage.style.opacity = '0';
                    setTimeout(() => {
                        welcomeMessage.remove();
                    }, 1000);
                }, 3000);
            }
        }
    }

    /**
     * 创建首页动画效果
     * @param {HTMLElement} container 动画容器
     */
    function createHeroAnimation(container) {
        // 创建一些随机浮动的瓦片动画
        const tileTypes = gameConfig.tileTypes;

        for (let i = 0; i < 20; i++) {
            const tile = document.createElement('div');
            const type = tileTypes[Math.floor(Math.random() * tileTypes.length)];

            tile.className = `floating-tile ${type.toLowerCase()}`;
            tile.style.left = `${Math.random() * 100}%`;
            tile.style.top = `${Math.random() * 100}%`;
            tile.style.animationDuration = `${3 + Math.random() * 4}s`;
            tile.style.animationDelay = `${Math.random() * 5}s`;

            container.appendChild(tile);
        }
    }

    /**
     * 设置游戏页面
     */
    function setupGamePage() {
        // 游戏页面初始化
        gameState.gameInProgress = true;

        // 游戏页面特有按钮
        const pauseBtn = document.getElementById('pause-btn');
        if (pauseBtn) {
            pauseBtn.addEventListener('click', togglePauseGame);
        }

        const restartBtn = document.getElementById('restart-btn');
        if (restartBtn) {
            restartBtn.addEventListener('click', restartGame);
        }

        // 游戏面板初始化 - GameBoard类在board.js中定义
        if (typeof GameBoard !== 'undefined') {
            gameState.gameBoard = new GameBoard();
        }

        // 游戏结束后继续按钮
        const continueBtn = document.getElementById('continue-btn');
        if (continueBtn) {
            continueBtn.addEventListener('click', function() {
                window.location.href = '/game/new';
            });
        }

        // 分享按钮
        const shareBtn = document.getElementById('share-btn');
        if (shareBtn) {
            shareBtn.addEventListener('click', shareScore);
        }
    }

    /**
     * 分享游戏分数
     */
    function shareScore() {
        const finalScore = document.getElementById('final-score');
        if (!finalScore) return;

        const score = finalScore.textContent;
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
    }

    /**
     * 备用分享方法（复制到剪贴板）
     * @param {string} text 要分享的文本
     */
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

    /**
     * 设置登录页面
     */
    function setupLoginPage() {
        const loginForm = document.querySelector('form');
        const errorAlert = document.querySelector('.alert-danger');

        if (loginForm) {
            loginForm.addEventListener('submit', function(event) {
                const username = document.getElementById('username').value;
                const password = document.getElementById('password').value;

                // 简单的前端验证
                if (!username || !password) {
                    event.preventDefault();
                    if (errorAlert) {
                        errorAlert.textContent = '请输入用户名和密码';
                        errorAlert.style.display = 'block';
                    }
                }
            });
        }

        // 记住我复选框
        const rememberMe = document.getElementById('remember-me');
        if (rememberMe) {
            rememberMe.addEventListener('change', function() {
                localStorage.setItem('remember_login', this.checked);
            });

            // 检查之前是否记住登录
            const remembered = localStorage.getItem('remember_login') === 'true';
            rememberMe.checked = remembered;
        }
    }

    /**
     * 设置注册页面
     */
    function setupRegisterPage() {
        const registerForm = document.querySelector('form');
        const errorAlert = document.querySelector('.alert-danger');

        if (registerForm) {
            registerForm.addEventListener('submit', function(event) {
                const username = document.getElementById('username').value;
                const password = document.getElementById('password').value;
                const confirmPassword = document.getElementById('confirm-password').value;
                const email = document.getElementById('email').value;

                // 前端验证
                if (!username || !password || !confirmPassword || !email) {
                    event.preventDefault();
                    if (errorAlert) {
                        errorAlert.textContent = '请填写所有必填字段';
                        errorAlert.style.display = 'block';
                    }
                    return;
                }

                if (password !== confirmPassword) {
                    event.preventDefault();
                    if (errorAlert) {
                        errorAlert.textContent = '两次输入的密码不一致';
                        errorAlert.style.display = 'block';
                    }
                    return;
                }

                // 邮箱格式验证
                const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                if (!emailPattern.test(email)) {
                    event.preventDefault();
                    if (errorAlert) {
                        errorAlert.textContent = '请输入有效的邮箱地址';
                        errorAlert.style.display = 'block';
                    }
                }
            });

            // 密码强度检查
            const passwordInput = document.getElementById('password');
            const passwordStrength = document.getElementById('password-strength');

            if (passwordInput && passwordStrength) {
                passwordInput.addEventListener('input', function() {
                    checkPasswordStrength(this.value, passwordStrength);
                });
            }

            // 实时用户名和邮箱验证
            setupRealTimeValidation();
        }
    }

    /**
     * 设置实时表单验证
     */
    function setupRealTimeValidation() {
        // 实时验证用户名是否可用
        const usernameInput = document.getElementById('username');
        const usernameError = document.getElementById('username-error');

        if (usernameInput && usernameError) {
            usernameInput.addEventListener('blur', function() {
                const username = this.value.trim();
                if (username) {
                    // 格式验证
                    const usernamePattern = /^[a-zA-Z0-9_]{3,20}$/;
                    if (!usernamePattern.test(username)) {
                        this.classList.add('is-invalid');
                        this.classList.remove('is-valid');
                        usernameError.textContent = '用户名格式不正确';
                        usernameError.style.display = 'block';
                        return;
                    }

                    // 检查用户名是否可用
                    fetch(`/user/api/check-username?username=${encodeURIComponent(username)}`)
                        .then(response => response.json())
                        .then(data => {
                            if (data.exists) {
                                usernameInput.classList.add('is-invalid');
                                usernameInput.classList.remove('is-valid');
                                usernameError.textContent = '该用户名已被使用';
                                usernameError.style.display = 'block';
                            } else {
                                usernameInput.classList.remove('is-invalid');
                                usernameInput.classList.add('is-valid');
                                usernameError.style.display = 'none';
                            }
                        })
                        .catch(error => {
                            console.error('验证用户名失败:', error);
                        });
                }
            });
        }

        // 实时验证邮箱是否可用
        const emailInput = document.getElementById('email');
        const emailError = document.getElementById('email-error');

        if (emailInput && emailError) {
            emailInput.addEventListener('blur', function() {
                const email = this.value.trim();
                if (email) {
                    // 格式验证
                    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                    if (!emailPattern.test(email)) {
                        this.classList.add('is-invalid');
                        this.classList.remove('is-valid');
                        emailError.textContent = '邮箱格式不正确';
                        emailError.style.display = 'block';
                        return;
                    }

                    // 检查邮箱是否可用
                    fetch(`/user/api/check-email?email=${encodeURIComponent(email)}`)
                        .then(response => response.json())
                        .then(data => {
                            if (data.exists) {
                                emailInput.classList.add('is-invalid');
                                emailInput.classList.remove('is-valid');
                                emailError.textContent = '该邮箱已被使用';
                                emailError.style.display = 'block';
                            } else {
                                emailInput.classList.remove('is-invalid');
                                emailInput.classList.add('is-valid');
                                emailError.style.display = 'none';
                            }
                        })
                        .catch(error => {
                            console.error('验证邮箱失败:', error);
                        });
                }
            });
        }

        // 实时验证密码确认
        const passwordInput = document.getElementById('password');
        const confirmPasswordInput = document.getElementById('confirm-password');
        const confirmPasswordError = document.getElementById('confirm-password-error');

        if (passwordInput && confirmPasswordInput && confirmPasswordError) {
            confirmPasswordInput.addEventListener('input', function() {
                if (this.value !== passwordInput.value) {
                    this.classList.add('is-invalid');
                    this.classList.remove('is-valid');
                    confirmPasswordError.textContent = '两次输入的密码不一致';
                    confirmPasswordError.style.display = 'block';
                } else {
                    this.classList.remove('is-invalid');
                    this.classList.add('is-valid');
                    confirmPasswordError.style.display = 'none';
                }
            });
        }
    }

    /**
     * 检查密码强度
     * @param {string} password 密码
     * @param {HTMLElement} strengthElement 强度显示元素
     */
    function checkPasswordStrength(password, strengthElement) {
        // 计算密码强度
        let strength = 0;

        // 长度检查
        if (password.length >= 8) {
            strength += 1;
        }

        // 字符类型检查
        if (/[A-Z]/.test(password)) {
            strength += 1;
        }

        if (/[a-z]/.test(password)) {
            strength += 1;
        }

        if (/[0-9]/.test(password)) {
            strength += 1;
        }

        if (/[^A-Za-z0-9]/.test(password)) {
            strength += 1;
        }

        // 更新强度显示
        let strengthText = '';
        let strengthClass = '';

        switch (strength) {
            case 0:
            case 1:
                strengthText = '弱';
                strengthClass = 'text-danger';
                break;
            case 2:
            case 3:
                strengthText = '中';
                strengthClass = 'text-warning';
                break;
            case 4:
            case 5:
                strengthText = '强';
                strengthClass = 'text-success';
                break;
        }

        strengthElement.textContent = `密码强度: ${strengthText}`;
        strengthElement.className = 'password-strength ' + strengthClass;

        // 更新密码要求列表
        updatePasswordRequirements(password);
    }

    /**
     * 更新密码要求列表
     * @param {string} password 密码
     */
    function updatePasswordRequirements(password) {
        const requirements = {
            'req-length': password.length >= 8,
            'req-uppercase': /[A-Z]/.test(password),
            'req-lowercase': /[a-z]/.test(password),
            'req-number': /[0-9]/.test(password),
            'req-special': /[^A-Za-z0-9]/.test(password)
        };

        for (const [id, met] of Object.entries(requirements)) {
            const element = document.getElementById(id);
            if (element) {
                if (met) {
                    element.classList.add('met');
                    element.innerHTML = `✓ ${element.textContent.replace('✓ ', '')}`;
                } else {
                    element.classList.remove('met');
                    element.innerHTML = element.textContent.replace('✓ ', '');
                }
            }
        }
    }

    /**
     * 设置排行榜页面
     */
    function setupLeaderboardPage() {
        // 排行榜标签切换
        const tabs = document.querySelectorAll('.tab');

        tabs.forEach(tab => {
            tab.addEventListener('click', function() {
                // 移除所有active类
                tabs.forEach(t => t.classList.remove('active'));

                // 添加当前标签的active类
                this.classList.add('active');

                // 获取模式
                const mode = this.getAttribute('data-mode');

                // 加载相应的排行榜数据
                if (mode !== 'all') {
                    loadLeaderboardData(mode);
                } else {
                    loadLeaderboardData();
                }
            });
        });

        // 加载更多按钮
        const loadMoreBtn = document.querySelector('.btn[data-action="load-more"]');
        if (loadMoreBtn) {
            loadMoreBtn.addEventListener('click', function() {
                const currentCount = document.querySelectorAll('.leaderboard-table tbody tr').length;
                const activeTab = document.querySelector('.tab.active');
                const mode = activeTab ? activeTab.getAttribute('data-mode') : 'all';

                loadLeaderboardData(mode, currentCount);
            });
        }
    }

    /**
     * 加载排行榜数据
     * @param {string} mode 游戏模式
     * @param {number} offset 数据偏移量
     */
    function loadLeaderboardData(mode = 'all', offset = 0) {
        let url = '/score/api/leaderboard';

        if (mode !== 'all') {
            url = `/score/api/leaderboard/${mode}`;
        }

        url += `?offset=${offset}&limit=20`;

        fetch(url)
            .then(response => response.json())
            .then(data => {
                updateLeaderboardTable(data, offset === 0);
            })
            .catch(error => {
                console.error('获取排行榜数据失败:', error);
            });
    }

    /**
     * 更新排行榜表格
     * @param {Array} data 排行榜数据
     * @param {boolean} replace 是否替换现有数据
     */
    function updateLeaderboardTable(data, replace = true) {
        const tbody = document.querySelector('.leaderboard-table tbody');

        if (!tbody) return;

        if (replace) {
            tbody.innerHTML = '';
        }

        if (data.length === 0) {
            const row = document.createElement('tr');
            row.innerHTML = `<td colspan="4" class="no-scores">暂无记录</td>`;
            tbody.appendChild(row);
            return;
        }

        const startRank = document.querySelectorAll('.leaderboard-table tbody tr').length + 1;

        data.forEach((score, index) => {
            const row = document.createElement('tr');
            const rank = startRank + index;
            const isTopRank = rank <= 3 ? 'top-rank' : '';

            row.innerHTML = `
                <td class="rank ${isTopRank}">${rank}</td>
                <td>${score.user ? score.user.nickname : '游客'}</td>
                <td class="score">${score.score}</td>
                <td class="date">${formatDate(score.createdAt)}</td>
            `;
            tbody.appendChild(row);
        });

        // 显示/隐藏加载更多按钮
        const loadMoreBtn = document.querySelector('.btn[data-action="load-more"]');
        if (loadMoreBtn) {
            loadMoreBtn.style.display = data.length < 20 ? 'none' : 'inline-block';
        }
    }

    /**
     * 格式化日期
     * @param {string} dateString 日期字符串
     * @returns {string} 格式化后的日期
     */
    function formatDate(dateString) {
        const date = new Date(dateString);
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');

        return `${year}-${month}-${day} ${hours}:${minutes}`;
    }

    /**
     * 设置个人资料页面
     */
    function setupProfilePage() {
        // 选项卡切换
        const tabs = document.querySelectorAll('.profile-tab');
        const tabContents = document.querySelectorAll('.tab-content');

        tabs.forEach(tab => {
            tab.addEventListener('click', function() {
                // 移除所有active类
                tabs.forEach(t => t.classList.remove('active'));
                tabContents.forEach(c => c.classList.remove('active'));

                // 添加当前标签的active类
                this.classList.add('active');

                // 显示对应的内容
                const tabId = this.getAttribute('data-tab');
                document.getElementById(tabId + '-tab').classList.add('active');
            });
        });

        // 头像预览
        const avatarInput = document.getElementById('avatar-input');
        const avatarPreview = document.getElementById('avatar-preview');

        if (avatarInput && avatarPreview) {
            avatarInput.addEventListener('change', function() {
                if (this.files && this.files[0]) {
                    const reader = new FileReader();
                    reader.onload = function(e) {
                        avatarPreview.src = e.target.result;
                    };
                    reader.readAsDataURL(this.files[0]);

                    // 检查文件大小
                    if (this.files[0].size > 2 * 1024 * 1024) {
                        alert('头像图片不能超过2MB');
                        this.value = '';
                    }
                }
            });
        }

        // 密码强度检查
        const newPasswordInput = document.getElementById('new-password');
        const passwordStrength = document.getElementById('password-strength');

        if (newPasswordInput && passwordStrength) {
            newPasswordInput.addEventListener('input', function() {
                checkPasswordStrength(this.value, passwordStrength);
            });
        }

        // 确认密码验证
        const confirmPasswordInput = document.getElementById('confirm-password');

        if (confirmPasswordInput && newPasswordInput) {
            confirmPasswordInput.addEventListener('input', function() {
                if (this.value !== newPasswordInput.value) {
                    this.setCustomValidity('两次输入的密码不一致');
                } else {
                    this.setCustomValidity('');
                }
            });
        }
    }
});