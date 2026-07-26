type Locale = 'en' | 'zh-cn';

interface PageLinkRefs {
    dev: Record<string, string>[];
    prod: Record<string, string>[];
}

interface NavigationLink {
    path: string;
    title: Record<Locale, string>;
}

interface NavigationSection {
    title: Record<Locale, string>;
    links: NavigationLink[];
}

const navigationSections: NavigationSection[] = [{
    title: { en: 'Get Started', 'zh-cn': '入门' },
    links: [
        { path: '/guide/home', title: { en: 'Introduction', 'zh-cn': '介绍' } },
        { path: '/guide/architecture', title: { en: 'Architecture', 'zh-cn': '架构' } },
        { path: '/guide/quick-start', title: { en: 'Quick Start', 'zh-cn': '快速开始' } }
    ]
}, {
    title: { en: 'Libraries', 'zh-cn': '依赖' },
    links: [
        { path: '/library/hikage-bom', title: { en: 'hikage-bom', 'zh-cn': 'hikage-bom' } },
        { path: '/library/hikage-core', title: { en: 'hikage-core', 'zh-cn': 'hikage-core' } },
        { path: '/library/hikage-compiler', title: { en: 'hikage-compiler', 'zh-cn': 'hikage-compiler' } },
        { path: '/library/hikage-runtime', title: { en: 'hikage-runtime', 'zh-cn': 'hikage-runtime' } },
        { path: '/library/hikage-runtime-attribute', title: { en: 'hikage-runtime-attribute', 'zh-cn': 'hikage-runtime-attribute' } },
        { path: '/library/hikage-extension', title: { en: 'hikage-extension', 'zh-cn': 'hikage-extension' } },
        { path: '/library/hikage-extension-betterandroid', title: { en: 'hikage-extension-betterandroid', 'zh-cn': 'hikage-extension-betterandroid' } },
        { path: '/library/hikage-extension-compose', title: { en: 'hikage-extension-compose', 'zh-cn': 'hikage-extension-compose' } },
        { path: '/library/hikage-widget-foundation', title: { en: 'hikage-widget-foundation', 'zh-cn': 'hikage-widget-foundation' } },
        { path: '/library/hikage-widget-androidx', title: { en: 'hikage-widget-androidx', 'zh-cn': 'hikage-widget-androidx' } },
        { path: '/library/hikage-widget-material', title: { en: 'hikage-widget-material', 'zh-cn': 'hikage-widget-material' } }
    ]
}, {
    title: { en: 'Plugins', 'zh-cn': '插件' },
    links: [
        { path: '/plugin/hikage-gradle-plugin', title: { en: 'hikage-gradle-plugin', 'zh-cn': 'hikage-gradle-plugin' } },
        { path: '/plugin/hikage-declaration-gradle-plugin', title: { en: 'hikage-declaration-gradle-plugin', 'zh-cn': 'hikage-declaration-gradle-plugin' } }
    ]
}, {
    title: { en: 'Configs', 'zh-cn': '配置' },
    links: [
        { path: '/config/r8-proguard', title: { en: 'R8 & Proguard Obfuscation', 'zh-cn': 'R8 与 Proguard 混淆' } }
    ]
}, {
    title: { en: 'About', 'zh-cn': '关于' },
    links: [
        { path: '/about/changelog', title: { en: 'Changelog', 'zh-cn': '更新日志' } },
        { path: '/about/future', title: { en: 'Looking Toward the Future', 'zh-cn': '展望未来' } },
        { path: '/about/contacts', title: { en: 'Contact Us', 'zh-cn': '联系我们' } },
        { path: '/about/about', title: { en: 'About This Document', 'zh-cn': '关于此文档' } }
    ]
}];

const topNavigationLinks: NavigationLink[] = [
    { path: '/', title: { en: 'Home', 'zh-cn': '首页' } },
    { path: '/guide/quick-start', title: { en: 'Quick Start', 'zh-cn': '快速开始' } },
    { path: '/about/changelog', title: { en: 'Changelog', 'zh-cn': '更新日志' } },
    { path: '/about/contacts', title: { en: 'Contact Us', 'zh-cn': '联系我们' } }
];

const localizedLink = (link: NavigationLink, locale: Locale) => ({
    text: link.title[locale],
    link: `/${locale}${link.path}`
});

/** Creates the VitePress navigation and sidebar for the requested locale. */
export const createThemeNavigation = (locale: Locale) => {
    const sections = navigationSections.map((section) => ({
        text: section.title[locale],
        items: section.links.map((link) => localizedLink(link, locale))
    }));
    return {
        nav: topNavigationLinks.map((link) => localizedLink(link, locale)),
        sidebar: {
            [`/${locale}/`]: sections.map((section) => ({
                text: section.text,
                collapsed: false,
                items: section.items
            }))
        }
    };
};

/** Defines shared site, development server, and repository settings. */
export const configs = {
    dev: {
        dest: '../dist',
        port: 9000
    },
    website: {
        base: '/Hikage/',
        icon: '/Hikage/images/logo.svg',
        logo: '/images/logo.svg',
        title: 'Hikage',
        locales: {
            en: {
                lang: 'en-US',
                description: 'A real-time Android View runtime powered by Kotlin DSL'
            },
            'zh-cn': {
                lang: 'zh-CN',
                description: '一个由 Kotlin DSL 驱动的 Android View 实时运行时框架'
            }
        }
    },
    github: {
        repo: 'https://github.com/BetterAndroid/Hikage',
        page: 'https://betterandroid.github.io/Hikage',
        branch: 'main',
        dir: 'docs-source/src'
    }
};

/** Defines custom Markdown link protocol replacements for each build mode. */
export const pageLinkRefs: PageLinkRefs = {
    dev: [
        { 'repo://': `${configs.github.repo}/` },
        // KDoc URL for local debugging, non-fixed value, adjust according to your own needs.
        // You can run ./build-dokka.sh and start the local server in dist/KDoc.
        { 'kdoc://': 'http://localhost:9001/' }
    ],
    prod: [
        { 'repo://': `${configs.github.repo}/` },
        { 'kdoc://': `${configs.github.page}/KDoc/` }
    ]
};