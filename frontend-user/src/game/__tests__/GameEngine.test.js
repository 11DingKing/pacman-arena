import { describe, it, expect, beforeEach, vi } from 'vitest';
import { GameEngine, GameState, GameEvents } from '../GameEngine.js';
import { DEFAULT_CONFIG } from '../config.js';

// Mock canvas context
const mockCtx = {
  fillStyle: '',
  strokeStyle: '',
  lineWidth: 0,
  fillRect: vi.fn(),
  beginPath: vi.fn(),
  arc: vi.fn(),
  fill: vi.fn(),
  stroke: vi.fn(),
  lineTo: vi.fn(),
  closePath: vi.fn(),
};

const mockCanvas = {
  getContext: vi.fn(() => mockCtx),
  width: 320,
  height: 320,
};

describe('GameEngine', () => {
  let engine;

  beforeEach(() => {
    vi.clearAllMocks();
    engine = new GameEngine(mockCanvas);
  });

  describe('初始化', () => {
    it('应该使用默认配置初始化', () => {
      expect(engine.getState()).toBe(GameState.READY);
      expect(engine.getScore()).toBe(0);
      expect(engine.getLevel()).toBe(1);
      expect(engine.getLives()).toBe(DEFAULT_CONFIG.initialLives);
    });

    it('应该正确计算画布尺寸', () => {
      const expectedSize = DEFAULT_CONFIG.gridSize * DEFAULT_CONFIG.cellSize;
      expect(engine.getCanvasSize()).toBe(expectedSize);
    });
  });

  describe('游戏状态控制', () => {
    it('start() 应该将状态改为 PLAYING', () => {
      engine.start();
      expect(engine.getState()).toBe(GameState.PLAYING);
    });

    it('pause() 应该将状态改为 PAUSED', () => {
      engine.start();
      engine.pause();
      expect(engine.getState()).toBe(GameState.PAUSED);
    });

    it('resume() 应该从 PAUSED 恢复到 PLAYING', () => {
      engine.start();
      engine.pause();
      engine.resume();
      expect(engine.getState()).toBe(GameState.PLAYING);
    });

    it('togglePause() 应该切换暂停状态', () => {
      engine.start();
      expect(engine.getState()).toBe(GameState.PLAYING);
      
      engine.togglePause();
      expect(engine.getState()).toBe(GameState.PAUSED);
      
      engine.togglePause();
      expect(engine.getState()).toBe(GameState.PLAYING);
    });

    it('restart() 应该重置游戏', () => {
      engine.start();
      engine.addScore(100);
      engine.restart();
      
      expect(engine.getScore()).toBe(0);
      expect(engine.getLevel()).toBe(1);
      expect(engine.getState()).toBe(GameState.PLAYING);
    });
  });

  describe('事件系统', () => {
    it('应该正确触发分数变化事件', () => {
      const callback = vi.fn();
      engine.on(GameEvents.SCORE_CHANGE, callback);
      
      engine.addScore(50);
      
      expect(callback).toHaveBeenCalledWith({ score: 50 });
    });

    it('应该正确触发状态变化事件', () => {
      const callback = vi.fn();
      engine.on(GameEvents.STATE_CHANGE, callback);
      
      engine.start();
      
      expect(callback).toHaveBeenCalledWith({ state: GameState.PLAYING });
    });

    it('off() 应该移除事件监听器', () => {
      const callback = vi.fn();
      engine.on(GameEvents.SCORE_CHANGE, callback);
      engine.off(GameEvents.SCORE_CHANGE, callback);
      
      engine.addScore(50);
      
      expect(callback).not.toHaveBeenCalled();
    });
  });

  describe('道具效果', () => {
    beforeEach(() => {
      engine.start();
    });

    it('applySpeedUp() 应该增加速度倍率', () => {
      engine.applySpeedUp(50, 10);
      expect(engine.speedMultiplier).toBe(1.5);
    });

    it('applyDoubleScore() 应该设置双倍积分', () => {
      engine.applyDoubleScore(10);
      expect(engine.scoreMultiplier).toBe(2);
    });

    it('applyInvincible() 应该开启无敌模式', () => {
      engine.applyInvincible(10);
      expect(engine.isInPowerMode()).toBe(true);
    });

    it('applyMagnet() 应该开启磁铁效果', () => {
      engine.applyMagnet(10);
      expect(engine.hasMagnet).toBe(true);
    });

    it('addLife() 应该增加生命', () => {
      const initialLives = engine.getLives();
      engine.addLife();
      expect(engine.getLives()).toBe(initialLives + 1);
    });

    it('addLife() 不应超过最大生命数', () => {
      for (let i = 0; i < 10; i++) {
        engine.addLife();
      }
      expect(engine.getLives()).toBeLessThanOrEqual(DEFAULT_CONFIG.maxLives);
    });
  });

  describe('方向控制', () => {
    it('setDirection() 应该设置移动方向', () => {
      engine.start();
      engine.setDirection('up');
      expect(engine.pacman.nextDirection).toBe('up');
    });

    it('setDirection() 在非 PLAYING 状态下不应生效', () => {
      engine.setDirection('up');
      // pacman 还未初始化
      expect(engine.pacman).toBeNull();
    });
  });

  describe('时间相关', () => {
    it('getDuration() 应该返回游戏时长', () => {
      engine.start();
      // 模拟时间流逝
      const duration = engine.getDuration();
      expect(typeof duration).toBe('number');
      expect(duration).toBeGreaterThanOrEqual(0);
    });

    it('formatDuration() 应该返回格式化的时长', () => {
      engine.start();
      const formatted = engine.formatDuration();
      expect(formatted).toMatch(/^\d+:\d{2}$/);
    });
  });

  describe('销毁', () => {
    it('destroy() 应该清理资源', () => {
      const callback = vi.fn();
      engine.on(GameEvents.SCORE_CHANGE, callback);
      
      engine.destroy();
      
      // 事件监听器应该被清理
      engine.emit(GameEvents.SCORE_CHANGE, { score: 100 });
      expect(callback).not.toHaveBeenCalled();
    });
  });
});
