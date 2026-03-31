/**
 * PostCSS 配置 - 移动端适配
 * 使用 postcss-pxtorem 将 px 自动转换为 rem
 * 配合 amfe-flexible 实现响应式布局
 */
module.exports = {
  plugins: {
    'postcss-pxtorem': {
      // 设计稿宽度 375px 时，1rem = 37.5px
      // 即设计稿上 75px 会转换为 2rem
      rootValue: 37.5,
      // 需要转换的属性，* 表示所有属性
      propList: ['*'],
      // 不转换的选择器，保留 element-plus 等组件库的原始样式
      selectorBlackList: [
        'el-',           // Element Plus 组件
        'van-',          // Vant 组件（如果有）
        '.ignore-rem',   // 手动忽略的类
        ':root',         // CSS 变量声明
        'html',          // html 根元素
      ],
      // 最小转换像素值，小于该值不转换
      minPixelValue: 2,
      // 不转换的文件（如 node_modules）
      exclude: /node_modules/i,
      // 媒体查询中的 px 也转换
      mediaQuery: false,
    },
  },
};
