import { existsSync, statSync } from 'node:fs';
import path from 'node:path';
import type MarkdownIt from 'markdown-it';
import { resolveI18nLink } from './anchors';

const publicRoot = path.resolve(process.cwd(), 'src/.vuepress/public');

export const env = {
    dev: process.env.NODE_ENV === 'development'
};

export const i18n = {
    space: ' ',
    string: (content: string, locale: string) => {
        return '/' + locale + content;
    },
    array: (contents: string[], locale: string) => {
        const newContents: string[] = [];
        contents.forEach((content) => {
            newContents.push(i18n.string(content, locale));
        });
        return newContents;
    }
};

const hasUriScheme = (value: string): boolean => /^[a-z][a-z\d+.-]*:/i.test(value);

const normalizeBase = (base = '/'): string => {
    const trimmed = base.trim();
    if (trimmed === '' || trimmed === '/') {
        return '/';
    }
    return `/${trimmed.replace(/^\/+|\/+$/g, '')}/`;
};

const resolvePublicFilePath = (pathname: string): string | null => {
    const relativePath = decodeURI(pathname).replace(/^\/+/, '');
    if (relativePath.length === 0) {
        return null;
    }
    const filePath = path.resolve(publicRoot, ...relativePath.split('/'));
    if (filePath !== publicRoot && !filePath.startsWith(`${publicRoot}${path.sep}`)) {
        return null;
    }
    return filePath;
};

const withBase = (base: string | undefined, pathname: string): string => {
    const normalizedBase = normalizeBase(base);
    if (normalizedBase === '/') {
        return pathname;
    }
    return `${normalizedBase}${pathname.replace(/^\/+/, '')}`;
};

const resolvePublicLink = (base: string | undefined, rawHref: string): { href: string; resolved: boolean } => {
    if (!rawHref.startsWith('/') || rawHref.startsWith('//') || hasUriScheme(rawHref)) {
        return { href: rawHref, resolved: false };
    }
    const [, rawPathname = '', search = '', hash = ''] = rawHref.match(/^([^?#]*)(\?[^#]*)?(#.*)?$/) ?? [];
    const normalizedBase = normalizeBase(base);
    const pathname = normalizedBase !== '/' && rawPathname.startsWith(normalizedBase)
        ? `/${rawPathname.slice(normalizedBase.length)}`
        : rawPathname;
    const publicFilePath = resolvePublicFilePath(pathname);
    if (!publicFilePath || !existsSync(publicFilePath)) {
        return { href: rawHref, resolved: false };
    }
    let resolvedPathname = pathname;
    if (statSync(publicFilePath).isDirectory()) {
        const indexFilePath = path.join(publicFilePath, 'index.html');
        if (!existsSync(indexFilePath) || !statSync(indexFilePath).isFile()) {
            return { href: rawHref, resolved: false };
        }
        resolvedPathname = `${pathname.replace(/\/+$/, '')}/index.html`;
    }
    return {
        href: `${withBase(base, resolvedPathname)}${search}${hash}`,
        resolved: true
    };
};

const resolvePublicHtml = (base: string | undefined, content: string): string =>
    content.replace(/\s(src|href)=(["'])([^"']+)\2/g, (matched, name: string, quote: string, value: string) => {
        const publicLink = resolvePublicLink(base, value);
        if (!publicLink.resolved) {
            return matched;
        }
        return ` ${name}=${quote}${publicLink.href}${quote}`;
    });

export const markdown = {
    injectLinks: (md: MarkdownIt, maps: Record<string, string>[]) => {
        const defaultRender = md.renderer.rules.link_open || function (tokens, idx, options, _env, self) {
            return self.renderToken(tokens, idx, options);
        };
        const defaultHtmlBlockRender = md.renderer.rules.html_block || function (tokens, idx) {
            return tokens[idx].content;
        };
        const defaultHtmlInlineRender = md.renderer.rules.html_inline || function (tokens, idx) {
            return tokens[idx].content;
        };
        md.renderer.rules.html_block = function (tokens, idx, options, markdownEnv, self) {
            if (env.dev) {
                tokens[idx].content = resolvePublicHtml(markdownEnv.base, tokens[idx].content);
            }
            return defaultHtmlBlockRender(tokens, idx, options, markdownEnv, self);
        };
        md.renderer.rules.html_inline = function (tokens, idx, options, markdownEnv, self) {
            if (env.dev) {
                tokens[idx].content = resolvePublicHtml(markdownEnv.base, tokens[idx].content);
            }
            return defaultHtmlInlineRender(tokens, idx, options, markdownEnv, self);
        };
        md.renderer.rules.link_open = function (tokens, idx, options, env, self) {
            const hrefIndex = tokens[idx].attrIndex('href');
            if (hrefIndex < 0 || !tokens[idx].attrs) {
                return defaultRender(tokens, idx, options, env, self);
            }
            let current = tokens[idx].attrs[hrefIndex][1];
            current = resolveI18nLink({
                base: env.base,
                filePathRelative: env.filePathRelative
            }, current);
            const publicLink = resolvePublicLink(env.base, current);
            current = publicLink.href;
            for (const map of maps) {
                for (const [search, replace] of Object.entries(map)) {
                    if (current.startsWith(search)) {
                        current = current.replace(search, replace);
                        tokens[idx].attrs[hrefIndex][1] = current;
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
            return defaultRender(tokens, idx, options, env, self);
        };
    }
};