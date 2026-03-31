import { describe, it, expect, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import GameControls from '../GameControls.vue';

describe('GameControls', () => {
  it('应该正确渲染方向按钮', () => {
    const wrapper = mount(GameControls, {
      props: {
        isPlaying: true,
      },
    });

    expect(wrapper.find('.dpad-up').exists()).toBe(true);
    expect(wrapper.find('.dpad-down').exists()).toBe(true);
    expect(wrapper.find('.dpad-left').exists()).toBe(true);
    expect(wrapper.find('.dpad-right').exists()).toBe(true);
    expect(wrapper.find('.dpad-center').exists()).toBe(true);
  });

  it('点击上方向键应该触发 direction 事件', async () => {
    const wrapper = mount(GameControls, {
      props: {
        isPlaying: true,
      },
    });

    await wrapper.find('.dpad-up').trigger('mousedown');
    
    expect(wrapper.emitted('direction')).toBeTruthy();
    expect(wrapper.emitted('direction')[0]).toEqual(['up']);
  });

  it('点击下方向键应该触发 direction 事件', async () => {
    const wrapper = mount(GameControls, {
      props: {
        isPlaying: true,
      },
    });

    await wrapper.find('.dpad-down').trigger('mousedown');
    
    expect(wrapper.emitted('direction')[0]).toEqual(['down']);
  });

  it('点击左方向键应该触发 direction 事件', async () => {
    const wrapper = mount(GameControls, {
      props: {
        isPlaying: true,
      },
    });

    await wrapper.find('.dpad-left').trigger('mousedown');
    
    expect(wrapper.emitted('direction')[0]).toEqual(['left']);
  });

  it('点击右方向键应该触发 direction 事件', async () => {
    const wrapper = mount(GameControls, {
      props: {
        isPlaying: true,
      },
    });

    await wrapper.find('.dpad-right').trigger('mousedown');
    
    expect(wrapper.emitted('direction')[0]).toEqual(['right']);
  });

  it('点击中央按钮应该触发 toggle-pause 事件', async () => {
    const wrapper = mount(GameControls, {
      props: {
        isPlaying: true,
      },
    });

    await wrapper.find('.dpad-center').trigger('click');
    
    expect(wrapper.emitted('toggle-pause')).toBeTruthy();
  });

  it('游戏进行中中央按钮应该显示暂停图标', () => {
    const wrapper = mount(GameControls, {
      props: {
        isPlaying: true,
      },
    });

    expect(wrapper.find('.dpad-center').text()).toContain('⏸');
  });

  it('游戏暂停时中央按钮应该显示播放图标', () => {
    const wrapper = mount(GameControls, {
      props: {
        isPlaying: false,
      },
    });

    expect(wrapper.find('.dpad-center').text()).toContain('▶');
  });

  it('应该包含键盘提示', () => {
    const wrapper = mount(GameControls, {
      props: {
        isPlaying: true,
      },
    });

    expect(wrapper.find('.keyboard-hint').exists()).toBe(true);
    expect(wrapper.text()).toContain('使用方向键控制');
  });
});
