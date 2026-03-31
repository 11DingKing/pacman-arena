/**
 * 游戏配置模块
 * 支持从外部加载配置，实现可扩展性
 */

// 默认游戏配置
export const DEFAULT_CONFIG = {
  // 画布配置
  cellSize: 20,
  gridSize: 16,
  
  // 游戏性能配置
  fps: 7,
  
  // 吃豆人配置
  pacman: {
    startX: 1,
    startY: 1,
    startDirection: 'right',
  },
  
  // 幽灵配置
  ghosts: [
    { x: 7, y: 7, color: '#FF0000', direction: 'left' },   // 红色幽灵
    { x: 8, y: 7, color: '#00FFFF', direction: 'right' },  // 青色幽灵
    { x: 7, y: 8, color: '#FFB8FF', direction: 'up' },     // 粉色幽灵
    { x: 8, y: 8, color: '#FFB852', direction: 'down' },   // 橙色幽灵
  ],
  
  // 分数配置
  scores: {
    dot: 10,
    powerDot: 50,
    ghost: 200,
  },
  
  // 能量模式持续时间（毫秒）
  powerModeDuration: 5000,
  
  // 死亡后无敌时间（毫秒）
  invincibleAfterDeathDuration: 2000,
  
  // 死亡提示显示时间（毫秒）
  deathHintDuration: 2000,
  
  // 磁铁吸引范围
  magnetRadius: 5,
  
  // 初始生命数
  initialLives: 3,
  
  // 最大生命数
  maxLives: 5,
};

// 默认地图 (0=路径/豆子, 1=墙壁, 2=能量豆)
export const DEFAULT_MAP = [
  [1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1],
  [1, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 1],
  [1, 0, 1, 1, 0, 1, 0, 0, 0, 0, 1, 0, 1, 1, 0, 1],
  [1, 2, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 2, 1],
  [1, 0, 1, 0, 1, 1, 0, 0, 0, 0, 1, 1, 0, 1, 0, 1],
  [1, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 1],
  [1, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 1],
  [0, 0, 0, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 0, 0, 0],
  [1, 1, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 1, 1],
  [1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1],
  [1, 0, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 0, 1],
  [1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1],
  [1, 2, 1, 1, 0, 1, 0, 1, 1, 0, 1, 0, 1, 1, 2, 1],
  [1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1],
  [1, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 1],
  [1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1],
];

// 备用关卡地图（可扩展更多关卡）
export const LEVEL_MAPS = {
  1: DEFAULT_MAP,
  // 可以添加更多关卡地图
  2: [
    [1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1],
    [1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1],
    [1, 0, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 0, 1],
    [1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 1],
    [1, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, 1],
    [1, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0, 1],
    [1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1],
    [0, 0, 0, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 0, 0, 0],
    [1, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, 1],
    [1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1],
    [1, 0, 1, 0, 1, 0, 1, 1, 1, 1, 0, 1, 0, 1, 0, 1],
    [1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1],
    [1, 2, 1, 1, 1, 0, 1, 0, 0, 1, 0, 1, 1, 1, 2, 1],
    [1, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 1],
    [1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1],
    [1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1],
  ],
};

/**
 * 游戏配置管理器
 */
export class GameConfig {
  constructor(customConfig = {}) {
    this.config = { ...DEFAULT_CONFIG, ...customConfig };
    this.currentMap = null;
  }

  /**
   * 获取配置项
   */
  get(key) {
    return this.config[key];
  }

  /**
   * 设置配置项
   */
  set(key, value) {
    this.config[key] = value;
  }

  /**
   * 批量更新配置
   */
  update(newConfig) {
    this.config = { ...this.config, ...newConfig };
  }

  /**
   * 获取指定关卡的地图
   */
  getMapForLevel(level) {
    // 优先使用自定义地图，否则使用预设地图，最后回退到默认地图
    if (this.currentMap) {
      return this.currentMap;
    }
    return LEVEL_MAPS[level] || DEFAULT_MAP;
  }

  /**
   * 设置自定义地图
   */
  setCustomMap(mapData) {
    this.currentMap = mapData;
  }

  /**
   * 清除自定义地图
   */
  clearCustomMap() {
    this.currentMap = null;
  }

  /**
   * 获取画布尺寸
   */
  getCanvasSize() {
    return this.config.gridSize * this.config.cellSize;
  }

  /**
   * 获取帧时间间隔
   */
  getFrameTime() {
    return 1000 / this.config.fps;
  }

  /**
   * 从远程加载配置（支持动态配置）
   */
  async loadFromRemote(url) {
    try {
      const response = await fetch(url);
      const remoteConfig = await response.json();
      this.update(remoteConfig);
      return true;
    } catch (error) {
      console.warn('Failed to load remote config:', error);
      return false;
    }
  }

  /**
   * 导出当前配置
   */
  export() {
    return {
      ...this.config,
      currentMap: this.currentMap,
    };
  }

  /**
   * 从导出的配置恢复
   */
  import(exportedConfig) {
    const { currentMap, ...config } = exportedConfig;
    this.config = { ...DEFAULT_CONFIG, ...config };
    this.currentMap = currentMap || null;
  }
}

// 导出默认实例
export const gameConfig = new GameConfig();
