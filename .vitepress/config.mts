import { defineConfig } from 'vitepress';
import { posix } from 'node:path';
import { subjects, sidebar, pageLink } from './catalog.mjs';

export default defineConfig({
  lang: 'ko-KR',
  title: '공부 노트',
  description: 'Java · Spring · Redis 학습 기록',
  base: '/study/',
  cleanUrls: false,
  srcExclude: ['AGENTS.md', '**/build/**', '**/.gradle/**', 'node_modules/**'],
  head: [['meta', { name: 'theme-color', content: '#2563eb' }]],
  themeConfig: {
    nav: [
      { text: '전체 목표', link: pageLink('공부 목표.md') },
      { text: 'Redis', link: pageLink('14-Redis-기초/학습 노트.md') }
    ],
    sidebar,
    studySubjects: subjects.map(({ items, ...subject }) => subject),
    socialLinks: [{ icon: 'github', link: 'https://github.com/wisehero/study' }],
    outline: { level: [2, 3], label: '이 페이지에서' },
    sidebarMenuLabel: '주제 목록',
    outlineTitle: '이 페이지에서',
    darkModeSwitchLabel: '화면 테마',
    darkModeSwitchTitle: '어두운 화면으로',
    lightModeSwitchTitle: '밝은 화면으로',
    returnToTopLabel: '맨 위로',
    docFooter: { prev: '이전 문서', next: '다음 문서' },
    search: { provider: 'local', options: { locales: { root: { translations: {
      button: { buttonText: '검색', buttonAriaLabel: '학습 자료 검색' },
      modal: {
        displayDetails: '자세히 보기', resetButtonTitle: '검색 초기화',
        backButtonTitle: '검색 닫기', noResultsText: '검색 결과가 없습니다',
        footer: { selectText: '선택', navigateText: '이동', closeText: '닫기' }
      }
    } } } } }
  },
  markdown: {
    html: false,
    config(md) {
      // Markdown 본문은 유지하고, 웹에서 코드 파일 링크를 GitHub로 연결한다.
      md.core.ruler.after('inline', 'source-links', state => {
        const current = state.env.relativePath || '';
        for (const token of state.tokens) {
          for (const child of token.children || []) {
            if (child.type !== 'link_open') continue;
            const href = child.attrGet('href');
            if (!href || /^(?:[a-z][a-z\d+.-]*:|\/|#)/i.test(href)) continue;
            const [path, hash] = href.split('#');
            if (!path || /\.md$/i.test(path)) {
              // VitePress의 한글 제목 ID와 기존 Markdown의 앵커 표기를 맞춘다.
              if (hash) child.attrSet('href', `${path}#${encodeURIComponent(decodeURIComponent(hash).normalize('NFKD'))}`);
              continue;
            }
            const target = posix.normalize(posix.join(posix.dirname(current), decodeURIComponent(path)));
            child.attrSet('href', 'https://github.com/wisehero/study/blob/main/' +
              target.split('/').map(encodeURIComponent).join('/') + (hash ? `#${hash}` : ''));
          }
        }
      });
      const fence = md.renderer.rules.fence!;
      md.renderer.rules.fence = (tokens, index, options, env, self) => {
        if (tokens[index].info.trim() === 'mermaid') {
          return `<MermaidDiagram code="${md.utils.escapeHtml(tokens[index].content)}" />`;
        }
        return fence(tokens, index, options, env, self);
      };
      // 그림은 화면 폭에 맞추고, 누르면 ImageViewer가 크게 보여 준다.
      const image = md.renderer.rules.image!;
      md.renderer.rules.image = (tokens, index, options, env, self) =>
        `<button class="study-image" type="button">${image(tokens, index, options, env, self)}</button>`;
      // 짧은 표 셀은 한 줄로 유지해 좁은 화면에서 글자 단위로 쪼개지지 않게 한다.
      md.core.ruler.after('inline', 'short-cells', state => {
        state.tokens.forEach((token, index) => {
          if (/^t[hd]_open$/.test(token.type) && state.tokens[index + 1].content.length <= 10) {
            token.attrJoin('class', 'short-cell');
          }
        });
      });
    }
  }
});
