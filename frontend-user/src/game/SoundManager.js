export class SoundManager {
  constructor() {
    this.audioContext = null;
    this.enabled = true;
  }

  init() {
    if (!this.audioContext) {
      this.audioContext = new (window.AudioContext || window.webkitAudioContext)();
    }
  }

  setEnabled(enabled) {
    this.enabled = enabled;
  }

  playTone(frequency, duration, type = 'sine', volume = 0.3) {
    if (!this.enabled) return;
    if (!this.audioContext) this.init();
    
    const oscillator = this.audioContext.createOscillator();
    const gainNode = this.audioContext.createGain();
    
    oscillator.connect(gainNode);
    gainNode.connect(this.audioContext.destination);
    
    oscillator.frequency.value = frequency;
    oscillator.type = type;
    
    gainNode.gain.setValueAtTime(volume, this.audioContext.currentTime);
    gainNode.gain.exponentialRampToValueAtTime(0.01, this.audioContext.currentTime + duration);
    
    oscillator.start(this.audioContext.currentTime);
    oscillator.stop(this.audioContext.currentTime + duration);
  }

  playEatDot() {
    this.playTone(440, 0.1, 'square', 0.2);
  }

  playEatPowerDot() {
    this.playTone(880, 0.15, 'square', 0.25);
    setTimeout(() => this.playTone(660, 0.15, 'square', 0.25), 50);
  }

  playDeath() {
    this.playTone(200, 0.3, 'sawtooth', 0.3);
    setTimeout(() => this.playTone(150, 0.4, 'sawtooth', 0.25), 100);
    setTimeout(() => this.playTone(100, 0.5, 'sawtooth', 0.2), 200);
  }

  playUseItem() {
    this.playTone(523, 0.1, 'sine', 0.3);
    setTimeout(() => this.playTone(659, 0.1, 'sine', 0.3), 80);
    setTimeout(() => this.playTone(784, 0.15, 'sine', 0.3), 160);
  }

  playGhostEaten() {
    this.playTone(600, 0.1, 'triangle', 0.25);
    setTimeout(() => this.playTone(800, 0.1, 'triangle', 0.25), 50);
  }

  playLevelComplete() {
    const notes = [523, 587, 659, 698, 784, 880, 988, 1047];
    notes.forEach((freq, i) => {
      setTimeout(() => this.playTone(freq, 0.15, 'sine', 0.25), i * 80);
    });
  }
}

export const soundManager = new SoundManager();
