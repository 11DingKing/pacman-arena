/**
 * 幽灵类
 * 负责单个幽灵的状态管理和 AI 逻辑
 */

export class Ghost {
  constructor(config = {}) {
    this.startX = config.x ?? 7;
    this.startY = config.y ?? 7;
    this.color = config.color ?? '#FF0000';
    this.startDirection = config.direction ?? 'left';
    
    this.reset();
  }

  /**
   * 重置到初始状态
   */
  reset() {
    this.x = this.startX;
    this.y = this.startY;
    this.direction = this.startDirection;
    this.scared = false;
  }

  /**
   * 设置恐惧状态
   */
  setScared(scared) {
    this.scared = scared;
  }

  /**
   * 获取位置
   */
  getPosition() {
    return { x: this.x, y: this.y };
  }

  /**
   * 检查是否与指定位置碰撞
   */
  collidesWith(x, y) {
    return this.x === x && this.y === y;
  }

  /**
   * AI 移动决策
   * @param {object} pacmanPos - 吃豆人位置 {x, y}
   * @param {boolean} isPowerMode - 是否处于能量模式
   * @param {Function} canMoveFn - 检查是否可以移动的函数
   * @param {Function} wrapFn - 包装位置的函数
   */
  move(pacmanPos, isPowerMode, canMoveFn, wrapFn) {
    const directions = ['up', 'down', 'left', 'right'];

    // AI: 30% 概率追踪玩家，20% 随机转向
    if (Math.random() < 0.3 && !isPowerMode) {
      const dx = pacmanPos.x - this.x;
      const dy = pacmanPos.y - this.y;
      if (Math.abs(dx) > Math.abs(dy)) {
        this.direction = dx > 0 ? 'right' : 'left';
      } else {
        this.direction = dy > 0 ? 'down' : 'up';
      }
    } else if (Math.random() < 0.2) {
      this.direction = directions[Math.floor(Math.random() * 4)];
    }

    // 能量模式下幽灵逃跑
    if (isPowerMode && Math.random() < 0.5) {
      const dx = pacmanPos.x - this.x;
      const dy = pacmanPos.y - this.y;
      if (Math.abs(dx) > Math.abs(dy)) {
        this.direction = dx > 0 ? 'left' : 'right';
      } else {
        this.direction = dy > 0 ? 'up' : 'down';
      }
    }

    // 计算新位置
    let newX = this.x;
    let newY = this.y;

    switch (this.direction) {
      case 'up': newY--; break;
      case 'down': newY++; break;
      case 'left': newX--; break;
      case 'right': newX++; break;
    }

    newX = wrapFn(newX);
    newY = wrapFn(newY);

    // 如果可以移动，则移动
    if (canMoveFn(newX, newY)) {
      this.x = newX;
      this.y = newY;
    } else {
      // 碰到墙壁随机转向
      this.direction = directions[Math.floor(Math.random() * 4)];
    }
  }

  /**
   * 重生到指定位置
   */
  respawn(x, y) {
    this.x = x ?? this.startX;
    this.y = y ?? this.startY;
  }

  /**
   * 导出状态
   */
  export() {
    return {
      x: this.x,
      y: this.y,
      color: this.color,
      direction: this.direction,
      scared: this.scared,
    };
  }

  /**
   * 导入状态
   */
  import(state) {
    this.x = state.x;
    this.y = state.y;
    this.direction = state.direction;
    this.scared = state.scared;
  }
}

/**
 * 幽灵管理器
 * 负责管理所有幽灵
 */
export class GhostManager {
  constructor(ghostConfigs = []) {
    this.ghosts = ghostConfigs.map(config => new Ghost(config));
    this.ghostConfigs = ghostConfigs;
  }

  /**
   * 重置所有幽灵
   */
  resetAll() {
    this.ghosts.forEach(ghost => ghost.reset());
  }

  /**
   * 设置所有幽灵的恐惧状态
   */
  setAllScared(scared) {
    this.ghosts.forEach(ghost => ghost.setScared(scared));
  }

  /**
   * 移动所有幽灵
   */
  moveAll(pacmanPos, isPowerMode, canMoveFn, wrapFn) {
    this.ghosts.forEach(ghost => {
      ghost.move(pacmanPos, isPowerMode, canMoveFn, wrapFn);
    });
  }

  /**
   * 检查是否有幽灵与指定位置碰撞
   * @returns {object|null} 碰撞的幽灵和索引，或 null
   */
  checkCollision(x, y) {
    for (let i = 0; i < this.ghosts.length; i++) {
      if (this.ghosts[i].collidesWith(x, y)) {
        return { ghost: this.ghosts[i], index: i };
      }
    }
    return null;
  }

  /**
   * 让指定幽灵重生
   */
  respawnGhost(index) {
    const config = this.ghostConfigs[index];
    if (this.ghosts[index] && config) {
      this.ghosts[index].respawn(config.x, config.y);
    }
  }

  /**
   * 获取所有幽灵
   */
  getAll() {
    return this.ghosts;
  }

  /**
   * 获取幽灵数量
   */
  count() {
    return this.ghosts.length;
  }

  /**
   * 导出所有幽灵状态
   */
  exportAll() {
    return this.ghosts.map(ghost => ghost.export());
  }

  /**
   * 导入所有幽灵状态
   */
  importAll(states) {
    states.forEach((state, index) => {
      if (this.ghosts[index]) {
        this.ghosts[index].import(state);
      }
    });
  }
}
