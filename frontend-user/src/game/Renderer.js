/**
 * Canvas 渲染器
 * 负责游戏画面的绘制
 */

export class Renderer {
  constructor(canvas, config) {
    this.canvas = canvas;
    this.ctx = canvas?.getContext('2d');
    this.cellSize = config.cellSize ?? 20;
    this.gridSize = config.gridSize ?? 16;
    
    // 颜色配置
    this.colors = {
      background: '#0A0E1A',
      wall: '#2563EB',
      dot: '#FFFFFF',
      powerDot: '#FFD700',
      pacman: '#FFD700',
      scaredGhost: '#0000FF',
      protectionAura: 'rgba(16, 185, 129, 0.8)',
      powerAura: 'rgba(59, 130, 246, 0.9)',
      powerAuraOuter: 'rgba(59, 130, 246, 0.4)',
    };
  }

  /**
   * 设置画布
   */
  setCanvas(canvas) {
    this.canvas = canvas;
    this.ctx = canvas?.getContext('2d');
  }

  /**
   * 更新配置
   */
  updateConfig(config) {
    if (config.cellSize) this.cellSize = config.cellSize;
    if (config.gridSize) this.gridSize = config.gridSize;
  }

  /**
   * 获取画布尺寸
   */
  getCanvasSize() {
    return this.gridSize * this.cellSize;
  }

  /**
   * 清空画布
   */
  clear() {
    if (!this.ctx) return;
    const size = this.getCanvasSize();
    this.ctx.fillStyle = this.colors.background;
    this.ctx.fillRect(0, 0, size, size);
  }

  /**
   * 绘制墙壁
   */
  drawWalls(walls) {
    if (!this.ctx) return;
    this.ctx.fillStyle = this.colors.wall;
    
    walls.forEach(wall => {
      const x = wall.x * this.cellSize;
      const y = wall.y * this.cellSize;
      this.ctx.fillRect(x + 1, y + 1, this.cellSize - 2, this.cellSize - 2);
    });
  }

  /**
   * 绘制普通豆子
   */
  drawDots(dots) {
    if (!this.ctx) return;
    this.ctx.fillStyle = this.colors.dot;
    
    dots.forEach(dot => {
      this.ctx.beginPath();
      this.ctx.arc(
        dot.x * this.cellSize + this.cellSize / 2,
        dot.y * this.cellSize + this.cellSize / 2,
        2,
        0,
        Math.PI * 2
      );
      this.ctx.fill();
    });
  }

  /**
   * 绘制能量豆
   */
  drawPowerDots(powerDots) {
    if (!this.ctx) return;
    this.ctx.fillStyle = this.colors.powerDot;
    
    powerDots.forEach(dot => {
      this.ctx.beginPath();
      this.ctx.arc(
        dot.x * this.cellSize + this.cellSize / 2,
        dot.y * this.cellSize + this.cellSize / 2,
        5,
        0,
        Math.PI * 2
      );
      this.ctx.fill();
    });
  }

  /**
   * 绘制吃豆人
   */
  drawPacman(pacman, options = {}) {
    if (!this.ctx) return;
    
    const { isInvincible = false, isPowerMode = false } = options;
    
    // 无敌闪烁效果
    const shouldDraw = !isInvincible || Math.floor(Date.now() / 100) % 2 === 0;
    if (!shouldDraw) return;

    const px = pacman.x * this.cellSize + this.cellSize / 2;
    const py = pacman.y * this.cellSize + this.cellSize / 2;
    const mouthSize = pacman.getMouthSize();
    const rotation = pacman.getRotation();

    // 绘制吃豆人身体
    this.ctx.fillStyle = this.colors.pacman;
    this.ctx.beginPath();
    this.ctx.arc(
      px,
      py,
      this.cellSize / 2 - 2,
      rotation + mouthSize * Math.PI,
      rotation + (2 - mouthSize) * Math.PI
    );
    this.ctx.lineTo(px, py);
    this.ctx.closePath();
    this.ctx.fill();

    // 保护期间添加绿色光环效果
    if (isInvincible) {
      this.ctx.strokeStyle = this.colors.protectionAura;
      this.ctx.lineWidth = 2;
      this.ctx.beginPath();
      this.ctx.arc(px, py, this.cellSize / 2 + 2, 0, Math.PI * 2);
      this.ctx.stroke();
    }

    // 能量模式蓝色光环效果
    if (isPowerMode) {
      this.ctx.strokeStyle = this.colors.powerAura;
      this.ctx.lineWidth = 3;
      this.ctx.beginPath();
      this.ctx.arc(px, py, this.cellSize / 2 + 4, 0, Math.PI * 2);
      this.ctx.stroke();

      // 外圈发光效果
      this.ctx.strokeStyle = this.colors.powerAuraOuter;
      this.ctx.lineWidth = 2;
      this.ctx.beginPath();
      this.ctx.arc(px, py, this.cellSize / 2 + 7, 0, Math.PI * 2);
      this.ctx.stroke();
    }
  }

  /**
   * 绘制单个幽灵
   */
  drawGhost(ghost, isPowerMode = false) {
    if (!this.ctx) return;

    const gx = ghost.x * this.cellSize + this.cellSize / 2;
    const gy = ghost.y * this.cellSize + this.cellSize / 2;
    const radius = this.cellSize / 2 - 2;

    // 幽灵身体
    this.ctx.fillStyle = isPowerMode ? this.colors.scaredGhost : ghost.color;
    this.ctx.beginPath();
    this.ctx.arc(gx, gy - 2, radius, Math.PI, 0);
    this.ctx.lineTo(gx + radius, gy + radius - 2);

    // 波浪底部
    const waveCount = 3;
    const waveWidth = (radius * 2) / waveCount;
    for (let i = 0; i < waveCount; i++) {
      const wx = gx + radius - (i + 1) * waveWidth;
      this.ctx.lineTo(wx + waveWidth / 2, gy + radius - 6);
      this.ctx.lineTo(wx, gy + radius - 2);
    }
    this.ctx.closePath();
    this.ctx.fill();

    // 眼睛
    if (!isPowerMode) {
      // 正常眼睛
      this.ctx.fillStyle = '#FFFFFF';
      this.ctx.beginPath();
      this.ctx.arc(gx - 3, gy - 2, 3, 0, Math.PI * 2);
      this.ctx.arc(gx + 3, gy - 2, 3, 0, Math.PI * 2);
      this.ctx.fill();

      this.ctx.fillStyle = '#000000';
      this.ctx.beginPath();
      this.ctx.arc(gx - 3, gy - 2, 1.5, 0, Math.PI * 2);
      this.ctx.arc(gx + 3, gy - 2, 1.5, 0, Math.PI * 2);
      this.ctx.fill();
    } else {
      // 害怕的眼睛
      this.ctx.fillStyle = '#FFFFFF';
      this.ctx.beginPath();
      this.ctx.arc(gx - 3, gy - 2, 2, 0, Math.PI * 2);
      this.ctx.arc(gx + 3, gy - 2, 2, 0, Math.PI * 2);
      this.ctx.fill();
    }
  }

  /**
   * 绘制所有幽灵
   */
  drawGhosts(ghosts, isPowerMode = false) {
    ghosts.forEach(ghost => {
      this.drawGhost(ghost, isPowerMode);
    });
  }

  /**
   * 完整渲染一帧
   */
  render(gameState) {
    if (!this.ctx) return;

    const {
      walls,
      dots,
      powerDots,
      pacman,
      ghosts,
      isPowerMode,
      isInvincibleAfterDeath,
    } = gameState;

    this.clear();
    this.drawWalls(walls);
    this.drawDots(dots);
    this.drawPowerDots(powerDots);
    this.drawPacman(pacman, {
      isInvincible: isInvincibleAfterDeath,
      isPowerMode,
    });
    this.drawGhosts(ghosts, isPowerMode);
  }
}
