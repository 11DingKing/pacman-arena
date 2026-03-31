/**
 * 地图管理类
 * 负责地图数据的解析、管理和查询
 */

export class GameMap {
  constructor(mapData, gridSize) {
    this.mapData = mapData;
    this.gridSize = gridSize;
    this.walls = [];
    this.dots = [];
    this.powerDots = [];
    
    this.parseMap();
  }

  /**
   * 解析地图数据
   */
  parseMap() {
    this.walls = [];
    this.dots = [];
    this.powerDots = [];

    for (let y = 0; y < this.gridSize; y++) {
      for (let x = 0; x < this.gridSize; x++) {
        const cell = this.mapData[y]?.[x];
        switch (cell) {
          case 1:
            this.walls.push({ x, y });
            break;
          case 0:
            this.dots.push({ x, y });
            break;
          case 2:
            this.powerDots.push({ x, y });
            break;
        }
      }
    }
  }

  /**
   * 检查指定位置是否可以移动
   */
  canMove(x, y) {
    // 处理边界穿越
    if (x < 0 || x >= this.gridSize || y < 0 || y >= this.gridSize) {
      return true;
    }
    return !this.walls.some(w => w.x === x && w.y === y);
  }

  /**
   * 包装位置（处理边界穿越）
   */
  wrapPosition(pos) {
    if (pos < 0) return this.gridSize - 1;
    if (pos >= this.gridSize) return 0;
    return pos;
  }

  /**
   * 检查并收集指定位置的豆子
   * @returns {object|null} 收集到的豆子类型和分数
   */
  collectDot(x, y) {
    // 检查普通豆子
    const dotIndex = this.dots.findIndex(d => d.x === x && d.y === y);
    if (dotIndex !== -1) {
      this.dots.splice(dotIndex, 1);
      return { type: 'dot', points: 10 };
    }

    // 检查能量豆
    const powerIndex = this.powerDots.findIndex(d => d.x === x && d.y === y);
    if (powerIndex !== -1) {
      this.powerDots.splice(powerIndex, 1);
      return { type: 'powerDot', points: 50 };
    }

    return null;
  }

  /**
   * 检查关卡是否完成（所有豆子被收集）
   */
  isLevelComplete() {
    return this.dots.length === 0 && this.powerDots.length === 0;
  }

  /**
   * 获取所有墙壁位置
   */
  getWalls() {
    return [...this.walls];
  }

  /**
   * 获取所有普通豆子位置
   */
  getDots() {
    return [...this.dots];
  }

  /**
   * 获取所有能量豆位置
   */
  getPowerDots() {
    return [...this.powerDots];
  }

  /**
   * 获取剩余豆子总数
   */
  getRemainingDots() {
    return this.dots.length + this.powerDots.length;
  }

  /**
   * 重置地图（重新解析）
   */
  reset() {
    this.parseMap();
  }

  /**
   * 更新地图数据
   */
  setMapData(mapData) {
    this.mapData = mapData;
    this.parseMap();
  }

  /**
   * 移动豆子位置（用于磁铁效果）
   */
  moveDotToward(dot, targetX, targetY) {
    const occupied = new Set(
      [...this.dots, ...this.powerDots].map(d => `${d.x},${d.y}`)
    );

    let nx = dot.x;
    let ny = dot.y;

    if (Math.abs(targetX - dot.x) >= Math.abs(targetY - dot.y)) {
      nx = dot.x + (targetX > dot.x ? 1 : targetX < dot.x ? -1 : 0);
    } else {
      ny = dot.y + (targetY > dot.y ? 1 : targetY < dot.y ? -1 : 0);
    }

    nx = this.wrapPosition(nx);
    ny = this.wrapPosition(ny);

    if (!this.canMove(nx, ny)) return false;

    const newKey = `${nx},${ny}`;
    const oldKey = `${dot.x},${dot.y}`;

    if (occupied.has(newKey)) return false;

    occupied.delete(oldKey);
    dot.x = nx;
    dot.y = ny;
    occupied.add(newKey);

    return true;
  }

  /**
   * 应用磁铁效果
   */
  applyMagnetEffect(pacmanX, pacmanY, radius) {
    const moveDots = (list) => {
      list.forEach(dot => {
        const dist = Math.abs(dot.x - pacmanX) + Math.abs(dot.y - pacmanY);
        if (dist > radius || dist === 0) return;
        this.moveDotToward(dot, pacmanX, pacmanY);
      });
    };

    moveDots(this.dots);
    moveDots(this.powerDots);
  }
}
