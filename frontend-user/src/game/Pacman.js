/**
 * 吃豆人类
 * 负责吃豆人的状态管理和移动逻辑
 */

export class Pacman {
  constructor(config = {}) {
    this.startX = config.startX ?? 1;
    this.startY = config.startY ?? 1;
    this.startDirection = config.startDirection ?? 'right';
    
    this.reset();
  }

  /**
   * 重置到初始状态
   */
  reset() {
    this.x = this.startX;
    this.y = this.startY;
    this.direction = this.startDirection;
    this.nextDirection = this.startDirection;
    this.mouthAngle = 0;
  }

  /**
   * 设置下一个移动方向
   */
  setNextDirection(direction) {
    if (['up', 'down', 'left', 'right'].includes(direction)) {
      this.nextDirection = direction;
    }
  }

  /**
   * 尝试转向
   * @param {Function} canMoveFn - 检查是否可以移动的函数
   * @param {Function} wrapFn - 包装位置的函数
   * @returns {boolean} 是否成功转向
   */
  tryTurn(canMoveFn, wrapFn) {
    let newX = this.x;
    let newY = this.y;

    switch (this.nextDirection) {
      case 'up': newY--; break;
      case 'down': newY++; break;
      case 'left': newX--; break;
      case 'right': newX++; break;
    }

    newX = wrapFn(newX);
    newY = wrapFn(newY);

    if (canMoveFn(newX, newY)) {
      this.direction = this.nextDirection;
      return true;
    }
    return false;
  }

  /**
   * 移动一步
   * @param {Function} canMoveFn - 检查是否可以移动的函数
   * @param {Function} wrapFn - 包装位置的函数
   * @returns {boolean} 是否成功移动
   */
  move(canMoveFn, wrapFn) {
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

    if (canMoveFn(newX, newY)) {
      this.x = newX;
      this.y = newY;
      return true;
    }
    return false;
  }

  /**
   * 更新嘴巴动画
   */
  updateMouthAnimation() {
    this.mouthAngle = (this.mouthAngle + 0.3) % (Math.PI * 2);
  }

  /**
   * 获取当前位置
   */
  getPosition() {
    return { x: this.x, y: this.y };
  }

  /**
   * 获取当前方向
   */
  getDirection() {
    return this.direction;
  }

  /**
   * 获取嘴巴张开大小
   */
  getMouthSize() {
    return Math.abs(Math.sin(this.mouthAngle)) * 0.3 + 0.05;
  }

  /**
   * 获取旋转角度（用于渲染）
   */
  getRotation() {
    switch (this.direction) {
      case 'right': return 0;
      case 'down': return Math.PI / 2;
      case 'left': return Math.PI;
      case 'up': return -Math.PI / 2;
      default: return 0;
    }
  }

  /**
   * 设置位置（用于重生等场景）
   */
  setPosition(x, y) {
    this.x = x;
    this.y = y;
  }

  /**
   * 导出状态
   */
  export() {
    return {
      x: this.x,
      y: this.y,
      direction: this.direction,
      nextDirection: this.nextDirection,
      mouthAngle: this.mouthAngle,
    };
  }

  /**
   * 导入状态
   */
  import(state) {
    this.x = state.x;
    this.y = state.y;
    this.direction = state.direction;
    this.nextDirection = state.nextDirection;
    this.mouthAngle = state.mouthAngle;
  }
}
