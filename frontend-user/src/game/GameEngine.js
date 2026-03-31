/**
 * 游戏引擎核心
 * 整合所有游戏模块，提供统一的游戏控制接口
 */

import { GameConfig, DEFAULT_CONFIG } from "./config.js";
import { GameMap } from "./Map.js";
import { Pacman } from "./Pacman.js";
import { GhostManager } from "./Ghost.js";
import { Renderer } from "./Renderer.js";
import { soundManager } from "./SoundManager.js";

/**
 * 游戏状态枚举
 */
export const GameState = {
  READY: "ready",
  PLAYING: "playing",
  PAUSED: "paused",
  OVER: "over",
};

/**
 * 游戏事件类型
 */
export const GameEvents = {
  SCORE_CHANGE: "scoreChange",
  LEVEL_CHANGE: "levelChange",
  LIVES_CHANGE: "livesChange",
  STATE_CHANGE: "stateChange",
  POWER_MODE_START: "powerModeStart",
  POWER_MODE_END: "powerModeEnd",
  GHOST_EATEN: "ghostEaten",
  DEATH: "death",
  LEVEL_COMPLETE: "levelComplete",
  GAME_OVER: "gameOver",
};

/**
 * 游戏引擎类
 */
export class GameEngine {
  constructor(canvas, customConfig = {}) {
    // 配置
    this.config = new GameConfig(customConfig);

    // 游戏对象
    this.map = null;
    this.pacman = null;
    this.ghostManager = null;
    this.renderer = null;

    // 游戏状态
    this.state = GameState.READY;
    this.score = 0;
    this.level = 1;
    this.lives = this.config.get("initialLives");
    this.startTime = 0;

    // 模式状态
    this.isPowerMode = false;
    this.isInvincibleAfterDeath = false;

    // 效果状态
    this.speedMultiplier = 1;
    this.scoreMultiplier = 1;
    this.hasMagnet = false;

    // 定时器
    this.animationId = null;
    this.lastFrameTime = 0;
    this.powerModeTimer = null;
    this.invincibleTimer = null;

    // 事件监听器
    this.eventListeners = {};

    // 初始化渲染器
    if (canvas) {
      this.setCanvas(canvas);
    }
  }

  /**
   * 设置画布
   */
  setCanvas(canvas) {
    const config = this.config.config;
    this.renderer = new Renderer(canvas, {
      cellSize: config.cellSize,
      gridSize: config.gridSize,
    });
  }

  /**
   * 初始化游戏
   */
  init() {
    const config = this.config.config;

    // 初始化音效管理器
    soundManager.init();

    // 创建地图
    const mapData = this.config.getMapForLevel(this.level);
    this.map = new GameMap(mapData, config.gridSize);

    // 创建吃豆人
    this.pacman = new Pacman(config.pacman);

    // 创建幽灵管理器
    this.ghostManager = new GhostManager(config.ghosts);

    // 重置状态
    this.isPowerMode = false;
    if (this.powerModeTimer) {
      clearTimeout(this.powerModeTimer);
      this.powerModeTimer = null;
    }

    // 渲染初始画面
    this.draw();
  }

  /**
   * 开始游戏
   */
  start() {
    this.state = GameState.PLAYING;
    this.score = 0;
    this.lives = this.config.get("initialLives");
    this.level = 1;
    this.startTime = Date.now();

    this.init();
    this.lastFrameTime = performance.now();
    this.gameLoop(this.lastFrameTime);

    this.emit(GameEvents.STATE_CHANGE, { state: this.state });
  }

  /**
   * 暂停游戏
   */
  pause() {
    if (this.state !== GameState.PLAYING) return;

    this.state = GameState.PAUSED;
    if (this.animationId) {
      cancelAnimationFrame(this.animationId);
      this.animationId = null;
    }

    this.emit(GameEvents.STATE_CHANGE, { state: this.state });
  }

  /**
   * 恢复游戏
   */
  resume() {
    if (this.state !== GameState.PAUSED) return;

    this.state = GameState.PLAYING;
    this.lastFrameTime = performance.now();
    this.gameLoop(this.lastFrameTime);

    this.emit(GameEvents.STATE_CHANGE, { state: this.state });
  }

  /**
   * 切换暂停状态
   */
  togglePause() {
    if (this.state === GameState.PLAYING) {
      this.pause();
    } else if (this.state === GameState.PAUSED) {
      this.resume();
    } else if (this.state === GameState.READY) {
      this.start();
    }
  }

  /**
   * 重新开始
   */
  restart() {
    this.stop();
    this.start();
  }

  /**
   * 停止游戏
   */
  stop() {
    if (this.animationId) {
      cancelAnimationFrame(this.animationId);
      this.animationId = null;
    }
    this.clearAllTimers();
  }

  /**
   * 游戏结束
   */
  end() {
    this.state = GameState.OVER;
    this.stop();

    this.emit(GameEvents.STATE_CHANGE, { state: this.state });
    this.emit(GameEvents.GAME_OVER, {
      score: this.score,
      level: this.level,
      duration: this.getDuration(),
    });
  }

  /**
   * 设置移动方向
   */
  setDirection(direction) {
    if (this.state !== GameState.PLAYING || !this.pacman) return;
    this.pacman.setNextDirection(direction);
  }

  /**
   * 游戏主循环
   */
  gameLoop(currentTime) {
    if (this.state !== GameState.PLAYING) return;

    this.animationId = requestAnimationFrame((t) => this.gameLoop(t));

    const frameTime = this.config.getFrameTime();
    const deltaTime = currentTime - this.lastFrameTime;
    if (deltaTime < frameTime) return;

    this.lastFrameTime = currentTime - (deltaTime % frameTime);
    this.update();
  }

  /**
   * 更新游戏状态
   */
  update() {
    if (!this.pacman || !this.map || !this.ghostManager) return;

    // 尝试转向
    this.pacman.tryTurn(
      (x, y) => this.map.canMove(x, y),
      (pos) => this.map.wrapPosition(pos),
    );

    // 磁铁效果
    if (this.hasMagnet) {
      const pos = this.pacman.getPosition();
      this.map.applyMagnetEffect(pos.x, pos.y, this.config.get("magnetRadius"));
    }

    // 计算移动步数（考虑速度加成）
    const baseSteps = Math.floor(this.speedMultiplier);
    const extraChance = this.speedMultiplier - baseSteps;
    const steps = baseSteps + (Math.random() < extraChance ? 1 : 0);

    // 移动吃豆人并检测碰撞
    for (let i = 0; i < Math.max(1, steps); i++) {
      this.pacman.move(
        (x, y) => this.map.canMove(x, y),
        (pos) => this.map.wrapPosition(pos),
      );
      this.checkDotCollision();
    }

    // 更新嘴巴动画
    this.pacman.updateMouthAnimation();

    // 移动幽灵
    this.ghostManager.moveAll(
      this.pacman.getPosition(),
      this.isPowerMode,
      (x, y) => this.map.canMove(x, y),
      (pos) => this.map.wrapPosition(pos),
    );

    // 检测幽灵碰撞
    this.checkGhostCollision();

    // 检测关卡完成
    if (this.map.isLevelComplete()) {
      this.nextLevel();
    }

    // 如果游戏仍在进行，绘制画面
    if (this.state === GameState.PLAYING) {
      this.draw();
    }
  }

  /**
   * 检测豆子碰撞
   */
  checkDotCollision() {
    const pos = this.pacman.getPosition();
    const result = this.map.collectDot(pos.x, pos.y);

    if (result) {
      const points = Math.floor(result.points * this.scoreMultiplier);
      this.addScore(points);
      soundManager.playEat();

      if (result.type === "powerDot") {
        soundManager.playPowerUp();
        this.activatePowerMode();
      }
    }
  }

  /**
   * 检测幽灵碰撞
   */
  checkGhostCollision() {
    const pos = this.pacman.getPosition();
    const collision = this.ghostManager.checkCollision(pos.x, pos.y);

    if (!collision) return;

    if (this.isPowerMode) {
      // 吃掉幽灵
      const points = Math.floor(
        this.config.get("scores").ghost * this.scoreMultiplier,
      );
      this.addScore(points);
      this.ghostManager.respawnGhost(collision.index);
      this.emit(GameEvents.GHOST_EATEN, { ghost: collision.ghost });
    } else if (!this.isInvincibleAfterDeath) {
      // 被幽灵抓住
      this.handleDeath();
    }
  }

  /**
   * 处理死亡
   */
  handleDeath() {
    this.lives--;
    soundManager.playDeath();
    this.emit(GameEvents.LIVES_CHANGE, { lives: this.lives });
    this.emit(GameEvents.DEATH, { lives: this.lives });

    if (this.lives <= 0) {
      this.end();
    } else {
      // 暂停游戏
      if (this.animationId) {
        cancelAnimationFrame(this.animationId);
        this.animationId = null;
      }
    }
  }

  /**
   * 死亡后继续游戏
   */
  continueAfterDeath() {
    // 重置位置
    this.pacman.reset();
    this.ghostManager.resetAll();

    // 启用短暂无敌
    this.isInvincibleAfterDeath = true;
    setTimeout(() => {
      this.isInvincibleAfterDeath = false;
    }, this.config.get("invincibleAfterDeathDuration"));

    // 继续游戏
    this.draw();
    this.lastFrameTime = performance.now();
    this.gameLoop(this.lastFrameTime);
  }

  /**
   * 进入下一关
   */
  nextLevel() {
    this.level++;
    this.emit(GameEvents.LEVEL_CHANGE, { level: this.level });
    this.emit(GameEvents.LEVEL_COMPLETE, { level: this.level - 1 });
    this.init();
  }

  /**
   * 激活能量模式
   */
  activatePowerMode() {
    this.isPowerMode = true;
    this.ghostManager.setAllScared(true);

    if (this.powerModeTimer) {
      clearTimeout(this.powerModeTimer);
    }

    this.emit(GameEvents.POWER_MODE_START, {});

    this.powerModeTimer = setTimeout(() => {
      this.isPowerMode = false;
      this.ghostManager.setAllScared(false);
      this.powerModeTimer = null;
      this.emit(GameEvents.POWER_MODE_END, {});
    }, this.config.get("powerModeDuration"));
  }

  /**
   * 添加分数
   */
  addScore(points) {
    this.score += points;
    this.emit(GameEvents.SCORE_CHANGE, { score: this.score });
  }

  /**
   * 绘制游戏画面
   */
  draw() {
    if (!this.renderer || !this.map || !this.pacman || !this.ghostManager)
      return;

    this.renderer.render({
      walls: this.map.getWalls(),
      dots: this.map.getDots(),
      powerDots: this.map.getPowerDots(),
      pacman: this.pacman,
      ghosts: this.ghostManager.getAll(),
      isPowerMode: this.isPowerMode,
      isInvincibleAfterDeath: this.isInvincibleAfterDeath,
    });
  }

  /**
   * 获取游戏时长（秒）
   */
  getDuration() {
    return Math.floor((Date.now() - this.startTime) / 1000);
  }

  /**
   * 格式化时长
   */
  formatDuration() {
    const duration = this.getDuration();
    const mins = Math.floor(duration / 60);
    const secs = duration % 60;
    return `${mins}:${secs.toString().padStart(2, "0")}`;
  }

  /**
   * 清除所有定时器
   */
  clearAllTimers() {
    if (this.powerModeTimer) {
      clearTimeout(this.powerModeTimer);
      this.powerModeTimer = null;
    }
    if (this.invincibleTimer) {
      clearTimeout(this.invincibleTimer);
      this.invincibleTimer = null;
    }
  }

  // ===== 道具效果 =====

  /**
   * 应用加速效果
   */
  applySpeedUp(value, duration) {
    this.speedMultiplier = 1 + value / 100;
    soundManager.playPowerUp();

    setTimeout(() => {
      this.speedMultiplier = 1;
    }, duration * 1000);
  }

  /**
   * 应用无敌效果
   */
  applyInvincible(duration) {
    this.isPowerMode = true;
    this.ghostManager.setAllScared(true);
    soundManager.playPowerUp();

    if (this.invincibleTimer) {
      clearTimeout(this.invincibleTimer);
    }

    this.invincibleTimer = setTimeout(() => {
      if (!this.powerModeTimer) {
        this.isPowerMode = false;
        this.ghostManager.setAllScared(false);
      }
      this.invincibleTimer = null;
    }, duration * 1000);
  }

  /**
   * 应用双倍积分
   */
  applyDoubleScore(duration) {
    this.scoreMultiplier = 2;
    soundManager.playPowerUp();

    setTimeout(() => {
      this.scoreMultiplier = 1;
    }, duration * 1000);
  }

  /**
   * 应用磁铁效果
   */
  applyMagnet(duration) {
    this.hasMagnet = true;
    soundManager.playPowerUp();

    setTimeout(() => {
      this.hasMagnet = false;
    }, duration * 1000);
  }

  /**
   * 添加额外生命
   */
  addLife() {
    const maxLives = this.config.get("maxLives");
    if (this.lives < maxLives) {
      this.lives++;
      soundManager.playPowerUp();
      this.emit(GameEvents.LIVES_CHANGE, { lives: this.lives });
    }
  }

  // ===== 事件系统 =====

  /**
   * 注册事件监听器
   */
  on(event, callback) {
    if (!this.eventListeners[event]) {
      this.eventListeners[event] = [];
    }
    this.eventListeners[event].push(callback);
  }

  /**
   * 移除事件监听器
   */
  off(event, callback) {
    if (!this.eventListeners[event]) return;
    this.eventListeners[event] = this.eventListeners[event].filter(
      (cb) => cb !== callback,
    );
  }

  /**
   * 触发事件
   */
  emit(event, data) {
    if (!this.eventListeners[event]) return;
    this.eventListeners[event].forEach((callback) => callback(data));
  }

  // ===== 状态获取 =====

  /**
   * 获取当前游戏状态
   */
  getState() {
    return this.state;
  }

  /**
   * 获取当前分数
   */
  getScore() {
    return this.score;
  }

  /**
   * 获取当前关卡
   */
  getLevel() {
    return this.level;
  }

  /**
   * 获取当前生命数
   */
  getLives() {
    return this.lives;
  }

  /**
   * 获取画布尺寸
   */
  getCanvasSize() {
    return this.config.getCanvasSize();
  }

  /**
   * 检查是否处于能量模式
   */
  isInPowerMode() {
    return this.isPowerMode;
  }

  /**
   * 获取分数倍率
   */
  getScoreMultiplier() {
    return this.scoreMultiplier;
  }

  /**
   * 销毁引擎
   */
  destroy() {
    this.stop();
    this.eventListeners = {};
  }
}
