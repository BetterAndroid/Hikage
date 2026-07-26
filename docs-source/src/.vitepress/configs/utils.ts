import { existsSync, statSync } from 'node:fs';
import path from 'node:path';
import { resolveI18nLink } from './anchors';
import type { VitePressMarkdownIt } from './types';

const publicRoot = path.resolve(process.cwd(), 'src/public');

const containerTypes = ['tip', 'warning', 'danger'] as const;
const containerTitles = {
    en: {
        tip: 'Tips',
        warning: 'Notice',
        danger: 'Pay Attention'
    },
    'zh-cn': {
        tip: '小提示',
        warning: '注意',
        danger: '特别注意'
    }
};

const hasUriScheme = (value: string) => /^[a-z][a-z\d+.-]*:/i.test(value);

const normalizeBase = (base = '/') => {
    const trimmed = base.trim();
    if (trimmed === '' || trimmed === '/')
        return '/';
    return `/${trimmed.replace(/^\/+|\/+$/g, '')}/`;
};

const resolvePublicFilePath = (pathname: string) => {
    const relativePath = decodeURI(pathname).replace(/^\/+/, '');
    if (relativePath.length === 0)
        return null;
    const filePath = path.resolve(publicRoot, ...relativePath.split('/'));
    if (filePath !== publicRoot && !filePath.startsWith(`${publicRoot}${path.sep}`))
        return null;
    return filePath;
};

const withBase = (base: string, pathname: string) => {
    const normalizedBase = normalizeBase(base);
    if (normalizedBase === '/')
        return pathname;
    return `${normalizedBase}${pathname.replace(/^\/+/, '')}`;
};

// VitePress does not resolve public directory links to index.html or add base to raw HTML attributes.
// Only paths backed by a real public file are rewritten so ordinary routes keep native link handling.
const resolvePublicLink = (base: string, rawHref: string) => {
    if (!rawHref.startsWith('/') || rawHref.startsWith('//') || hasUriScheme(rawHref))
        return { href: rawHref, resolved: false };
    const [, rawPathname = '', search = '', hash = ''] = rawHref.match(/^([^?#]*)(\?[^#]*)?(#.*)?$/) ?? [];
    const normalizedBase = normalizeBase(base);
    const pathname = normalizedBase !== '/' && rawPathname.startsWith(normalizedBase)
        ? `/${rawPathname.slice(normalizedBase.length)}`
        : rawPathname;
    const publicFilePath = resolvePublicFilePath(pathname);
    if (!publicFilePath || !existsSync(publicFilePath))
        return { href: rawHref, resolved: false };
    let resolvedPathname = pathname;
    if (statSync(publicFilePath).isDirectory()) {
        const indexFilePath = path.join(publicFilePath, 'index.html');
        if (!existsSync(indexFilePath) || !statSync(indexFilePath).isFile())
            return { href: rawHref, resolved: false };
        resolvedPathname = `${pathname.replace(/\/+$/, '')}/index.html`;
    }
    return {
        href: `${withBase(base, resolvedPathname)}${search}${hash}`,
        resolved: true
    };
};

const resolvePublicHtml = (base: string, content: string) =>
    content.replace(/\s(src|href)=(["'])([^"']+)\2/g, (matched, name: string, quote: string, value: string) => {
        const publicLink = resolvePublicLink(base, value);
        if (!publicLink.resolved)
            return matched;
        return ` ${name}=${quote}${publicLink.href}${quote}`;
    });

/** Exposes the active documentation build mode to configuration helpers. */
export const env = {
    dev: process.env.NODE_ENV === 'development'
};

/** Provides Markdown renderer hooks shared by development and production builds. */
export const markdown = {
    /** Localizes default custom-container titles while preserving titles declared in Markdown. */
    localizeContainerTitles: (md: VitePressMarkdownIt) => {
        for (const type of containerTypes) {
            const ruleName = `container_${type}_open`;
            const defaultRender = md.renderer.rules[ruleName];
            if (!defaultRender)
                continue;
            md.renderer.rules[ruleName] = function (tokens, idx, options, renderEnv, self) {
                const token = tokens[idx];
                const originalInfo = token.info;
                if (originalInfo.trim() !== type)
                    return defaultRender(tokens, idx, options, renderEnv, self);
                const locale = renderEnv.relativePath?.startsWith('zh-cn/') ? 'zh-cn' : 'en';
                token.info = `${type} ${containerTitles[locale][type]}`;
                try {
                    return defaultRender(tokens, idx, options, renderEnv, self);
                } finally {
                    token.info = originalInfo;
                }
            };
        }
    },
    injectLinks: (md: VitePressMarkdownIt, maps: Record<string, string>[], base: string) => {
        const defaultRender = md.renderer.rules.link_open || function (tokens, idx, options, _env, self) {
            return self.renderToken(tokens, idx, options);
        };
        const defaultHtmlBlockRender = md.renderer.rules.html_block || function (tokens, idx) {
            return tokens[idx].content;
        };
        const defaultHtmlInlineRender = md.renderer.rules.html_inline || function (tokens, idx) {
            return tokens[idx].content;
        };
        md.renderer.rules.html_block = function (tokens, idx, options, renderEnv, self) {
            if (env.dev)
                tokens[idx].content = resolvePublicHtml(base, tokens[idx].content);
            return defaultHtmlBlockRender(tokens, idx, options, renderEnv, self);
        };
        md.renderer.rules.html_inline = function (tokens, idx, options, renderEnv, self) {
            if (env.dev)
                tokens[idx].content = resolvePublicHtml(base, tokens[idx].content);
            return defaultHtmlInlineRender(tokens, idx, options, renderEnv, self);
        };
        md.renderer.rules.link_open = function (tokens, idx, options, renderEnv, self) {
            const hrefIndex = tokens[idx].attrIndex('href');
            if (hrefIndex < 0 || !tokens[idx].attrs)
                return defaultRender(tokens, idx, options, renderEnv, self);
            let current = tokens[idx].attrs[hrefIndex][1];
            current = resolveI18nLink({
                base,
                filePathRelative: renderEnv.relativePath
            }, current);
            const publicLink = resolvePublicLink(base, current);
            current = publicLink.href;
            for (const map of maps) {
                for (const [search, replace] of Object.entries(map)) {
                    if (current.startsWith(search)) {
                        current = current.replace(search, replace);
                        break;
                    }
                }
            }
            tokens[idx].attrs[hrefIndex][1] = current;
            if (publicLink.resolved) {
                tokens[idx].attrSet('target', '_blank');
                tokens[idx].attrSet('rel', 'noopener noreferrer');
                return self.renderToken(tokens, idx, options);
            }
            return defaultRender(tokens, idx, options, renderEnv, self);
        };
    }
};