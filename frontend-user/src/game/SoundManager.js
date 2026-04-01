/**
 * 音效管理器
 * 使用 Web Audio API 生成游戏音效
 */

export const SoundType = {
  EAT_DOT: 'eatDot',
  EAT_POWER_DOT: 'eatPowerDot',
  EAT_GHOST: 'eatGhost',
  DEATH: 'death',
  USE_ITEM: 'useItem',
  LEVEL_COMPLETE: 'levelComplete',
};

export class SoundManager {
  constructor() {
    this.audioContext = null;
    this.enabled = true;
    this.masterVolume = 0.3;
  }

  init() {
    if (!this.audioContext && typeof window !== 'undefined') {
      this.audioContext = new (window.AudioContext || window.webkitAudioContext)();
    }
  }

  setEnabled(enabled) {
    this.enabled = enabled;
  }

  isEnabled() {
    return this.enabled;
  }

  play(type) {
    if (!this.enabled || !this.audioContext) return;

    switch (type) {
      case SoundType.EAT_DOT:
        this.playEatDot();
        break;
      case SoundType.EAT_POWER_DOT:
        this.playEatPowerDot();
        break;
      case SoundType.EAT_GHOST:
        this.playEatGhost();
        break;
      case SoundType.DEATH:
        this.playDeath();
        break;
      case SoundType.USE_ITEM:
        this.playUseItem();
        break;
      case SoundType.LEVEL_COMPLETE:
        this.playLevelComplete();
        break;
    }
  }

  playEatDot() {
    const ctx = this.audioContext;
    const oscillator = ctx.createOscillator();
    const gainNode = ctx.createGain();

    oscillator.connect(gainNode);
    gainNode.connect(ctx.destination);

    oscillator.type = 'sine';
    oscillator.frequency.setValueAtTime(440, ctx.currentTime);
    oscillator.frequency.exponentialRampToValueAtTime(880, ctx.currentTime + 0.05);

    gainNode.gain.setValueAtTime(this.masterVolume * 0.5, ctx.currentTime);
    gainNode.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.08);

    oscillator.start(ctx.currentTime);
    oscillator.stop(ctx.currentTime + 0.08);
  }

  playEatPowerDot() {
    const ctx = this.audioContext;
    const oscillator = ctx.createOscillator();
    const gainNode = ctx.createGain();

    oscillator.connect(gainNode);
    gainNode.connect(ctx.destination);

    oscillator.type = 'square';
    oscillator.frequency.setValueAtTime(220, ctx.currentTime);
    oscillator.frequency.exponentialRampToValueAtTime(880, ctx.currentTime + 0.15);

    gainNode.gain.setValueAtTime(this.masterVolume * 0.4, ctx.currentTime);
    gainNode.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.2);

    oscillator.start(ctx.currentTime);
    oscillator.stop(ctx.currentTime + 0.2);
  }

  playEatGhost() {
    const ctx = this.audioContext;
    
    for (let i = 0; i < 3; i++) {
      const oscillator = ctx.createOscillator();
      const gainNode = ctx.createGain();

      oscillator.connect(gainNode);
      gainNode.connect(ctx.destination);

      oscillator.type = 'sine';
      oscillator.frequency.setValueAtTime(300 + i * 200, ctx.currentTime + i * 0.08);
      oscillator.frequency.exponentialRampToValueAtTime(600 + i * 200, ctx.currentTime + i * 0.08 + 0.1);

      gainNode.gain.setValueAtTime(this.masterVolume * 0.5, ctx.currentTime + i * 0.08);
      gainNode.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + i * 0.08 + 0.12);

      oscillator.start(ctx.currentTime + i * 0.08);
      oscillator.stop(ctx.currentTime + i * 0.08 + 0.12);
    }
  }

  playDeath() {
    const ctx = this.audioContext;
    const oscillator = ctx.createOscillator();
    const gainNode = ctx.createGain();

    oscillator.connect(gainNode);
    gainNode.connect(ctx.destination);

    oscillator.type = 'sawtooth';
    oscillator.frequency.setValueAtTime(440, ctx.currentTime);
    oscillator.frequency.exponentialRampToValueAtTime(55, ctx.currentTime + 0.5);

    gainNode.gain.setValueAtTime(this.masterVolume * 0.6, ctx.currentTime);
    gainNode.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.5);

    oscillator.start(ctx.currentTime);
    oscillator.stop(ctx.currentTime + 0.5);
  }

  playUseItem() {
    const ctx = this.audioContext;
    const oscillator = ctx.createOscillator();
    const gainNode = ctx.createGain();

    oscillator.connect(gainNode);
    gainNode.connect(ctx.destination);

    oscillator.type = 'sine';
    oscillator.frequency.setValueAtTime(523, ctx.currentTime);
    oscillator.frequency.setValueAtTime(659, ctx.currentTime + 0.1);
    oscillator.frequency.setValueAtTime(784, ctx.currentTime + 0.2);

    gainNode.gain.setValueAtTime(this.masterVolume * 0.5, ctx.currentTime);
    gainNode.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.35);

    oscillator.start(ctx.currentTime);
    oscillator.stop(ctx.currentTime + 0.35);
  }

  playLevelComplete() {
    const ctx = this.audioContext;
    const notes = [523, 659, 784, 1047];
    
    notes.forEach((freq, i) => {
      const oscillator = ctx.createOscillator();
      const gainNode = ctx.createGain();

      oscillator.connect(gainNode);
      gainNode.connect(ctx.destination);

      oscillator.type = 'sine';
      oscillator.frequency.setValueAtTime(freq, ctx.currentTime + i * 0.12);

      gainNode.gain.setValueAtTime(this.masterVolume * 0.4, ctx.currentTime + i * 0.12);
      gainNode.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + i * 0.12 + 0.15);

      oscillator.start(ctx.currentTime + i * 0.12);
      oscillator.stop(ctx.currentTime + i * 0.12 + 0.15);
    });
  }
}

export const soundManager = new SoundManager();
