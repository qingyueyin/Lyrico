import { defineConfig } from 'vitepress'
import { GitChangelog, GitChangelogMarkdownSection } from '@nolebase/vitepress-plugin-git-changelog'

const base = '/Lyrico/'

const zhSearch = {
  provider: 'local' as const,
  options: {
    translations: {
      button: {
        buttonText: '搜索',
        buttonAriaLabel: '搜索',
      },
      modal: {
        noResultsText: '无法找到相关结果',
        resetButtonTitle: '清除查询条件',
        footer: {
          selectText: '选择',
          navigateText: '切换',
          closeText: '关闭',
        },
      },
    },
  },
}

const enSearch = {
  provider: 'local' as const,
}

const socialLinks = [
  { icon: 'github' as const, link: 'https://github.com/Replica0110/Lyrico' },
]

const zhSidebar = {
  '/zh-CN/plugins/': [
    {
      text: '使用插件',
      items: [
        { text: '插件首页', link: '/zh-CN/plugins/' },
        { text: '使用插件', link: '/zh-CN/plugins/using' },
      ],
    },
    {
      text: '开发插件',
      items: [
        { text: '从零编写插件', link: '/zh-CN/plugins/examples' },
        { text: '本地调试插件', link: '/zh-CN/plugins/debugging' },
        { text: '插件包结构', link: '/zh-CN/plugins/composition' },
        { text: '插件函数', link: '/zh-CN/plugins/plugin-functions' },
        { text: '配置与结果字段', link: '/zh-CN/plugins/config-metadata' },
      ],
    },
    {
      text: '参考手册',
      items: [
        { text: 'Manifest 参考', link: '/zh-CN/plugins/manifest' },
        { text: '宿主 API 参考', link: '/zh-CN/plugins/host-api' },
      ],
    },
    {
      text: '运行机制',
      items: [
        { text: '架构与生命周期', link: '/zh-CN/plugins/overview' },
      ],
    },
  ],
}

const enSidebar = {
  '/en-US/plugins/': [
    {
      text: 'Using Plugins',
      items: [
        { text: 'Plugin Home', link: '/en-US/plugins/' },
        { text: 'Using Plugins', link: '/en-US/plugins/using' },
      ],
    },
    {
      text: 'Developing Plugins',
      items: [
        { text: 'Build a Plugin', link: '/en-US/plugins/examples' },
        { text: 'Debug Plugins Locally', link: '/en-US/plugins/debugging' },
        { text: 'Plugin Package Structure', link: '/en-US/plugins/composition' },
        { text: 'Plugin Functions', link: '/en-US/plugins/plugin-functions' },
        { text: 'Configuration and Result Fields', link: '/en-US/plugins/config-metadata' },
      ],
    },
    {
      text: 'Reference',
      items: [
        { text: 'Manifest Reference', link: '/en-US/plugins/manifest' },
        { text: 'Host API Reference', link: '/en-US/plugins/host-api' },
      ],
    },
    {
      text: 'Internals',
      items: [
        { text: 'Architecture and Lifecycle', link: '/en-US/plugins/overview' },
      ],
    },
  ],
}

export default defineConfig({
  title: 'Lyrico',
  description: 'Lyrico 使用说明与插件开发文档',
  lang: 'zh-CN',
  base,
  locales: {
    'zh-CN': {
      label: '简体中文',
      lang: 'zh-CN',
      link: '/zh-CN/',
      title: 'Lyrico',
      description: 'Lyrico 使用说明与插件开发文档',
      themeConfig: {
        nav: [
          { text: '首页', link: '/zh-CN/' },
          { text: '插件', link: '/zh-CN/plugins/' },
        ],
        returnToTopLabel: '回到顶部',
        sidebarMenuLabel: '菜单',
        darkModeSwitchLabel: '主题',
        lightModeSwitchTitle: '切换到浅色模式',
        darkModeSwitchTitle: '切换到深色模式',
        search: zhSearch,
        sidebar: zhSidebar,
        socialLinks,
        outline: {
          level: [2, 3],
          label: '本页目录',
        },
        docFooter: {
          prev: '上一页',
          next: '下一页',
        },
        lastUpdated: {
          text: '最后更新',
        },
      },
    },
    'en-US': {
      label: 'English',
      lang: 'en-US',
      link: '/en-US/',
      title: 'Lyrico',
      description: 'Lyrico user guide and plugin development docs',
      themeConfig: {
        nav: [
          { text: 'Home', link: '/en-US/' },
          { text: 'Plugins', link: '/en-US/plugins/' },
        ],
        returnToTopLabel: 'Return to top',
        sidebarMenuLabel: 'Menu',
        darkModeSwitchLabel: 'Theme',
        lightModeSwitchTitle: 'Switch to light mode',
        darkModeSwitchTitle: 'Switch to dark mode',
        search: enSearch,
        sidebar: enSidebar,
        socialLinks,
        outline: {
          level: [2, 3],
          label: 'On this page',
        },
        docFooter: {
          prev: 'Previous page',
          next: 'Next page',
        },
        lastUpdated: {
          text: 'Last updated',
        },
      },
    },
  },

  head: [
    ['link', { rel: 'icon', href: `/Lyrico/logo.svg`, type: 'image/svg+xml' }],
  ],

  vite: {
    plugins: [
      GitChangelog({
        repoURL: () => 'https://github.com/Replica0110/Lyrico',
      }),
      GitChangelogMarkdownSection(),
    ],
  },

  themeConfig: {
    logo: `/logo.svg`,
    nav: [
      { text: '简体中文', link: '/zh-CN/' },
      { text: 'English', link: '/en-US/' },
    ],
    search: zhSearch,
    socialLinks,
    outline: {
      level: [2, 3],
      label: '本页目录',
    },
  },
})
