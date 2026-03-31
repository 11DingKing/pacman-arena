import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import GameHud from '../GameHud.vue';

describe('GameHud', () => {
  it('应该正确渲染分数', () => {
    const wrapper = mount(GameHud, {
      props: {
        score: 1234,
        level: 1,
        lives: 3,
      },
    });

    expect(wrapper.text()).toContain('1,234');
  });

  it('应该正确渲染关卡', () => {
    const wrapper = mount(GameHud, {
      props: {
        score: 0,
        level: 5,
        lives: 3,
      },
    });

    expect(wrapper.text()).toContain('5');
  });

  it('应该正确显示生命数量', () => {
    const wrapper = mount(GameHud, {
      props: {
        score: 0,
        level: 1,
        lives: 2,
        maxLives: 3,
      },
    });

    const activeHearts = wrapper.findAll('.life.active');
    const emptyHearts = wrapper.findAll('.life:not(.active)');
    
    expect(activeHearts.length).toBe(2);
    expect(emptyHearts.length).toBe(1);
  });

  it('双倍积分时分数应该有特殊样式', () => {
    const wrapper = mount(GameHud, {
      props: {
        score: 100,
        level: 1,
        lives: 3,
        scoreMultiplier: 2,
      },
    });

    const scoreValue = wrapper.find('.hud-value');
    expect(scoreValue.classes()).toContain('double');
  });

  it('普通模式下分数不应该有 double 样式', () => {
    const wrapper = mount(GameHud, {
      props: {
        score: 100,
        level: 1,
        lives: 3,
        scoreMultiplier: 1,
      },
    });

    const scoreValue = wrapper.find('.hud-value');
    expect(scoreValue.classes()).not.toContain('double');
  });

  it('应该正确处理 0 分', () => {
    const wrapper = mount(GameHud, {
      props: {
        score: 0,
        level: 1,
        lives: 3,
      },
    });

    expect(wrapper.text()).toContain('0');
  });

  it('应该正确处理大数字格式化', () => {
    const wrapper = mount(GameHud, {
      props: {
        score: 1000000,
        level: 1,
        lives: 3,
      },
    });

    expect(wrapper.text()).toContain('1,000,000');
  });
});
