import { readdirSync, readFileSync } from 'node:fs';
import { join } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = fileURLToPath(new URL('../', import.meta.url));
const sort = (a, b) => a.localeCompare(b, 'ko', { numeric: true });
const label = name => name.replace(/^\d+-/, '').replaceAll('-', ' ');
export const pageLink = path => '/' + path.replace(/\.md$/, '').split('/').map(encodeURIComponent).join('/');

function pages(folder) {
  const entries = readdirSync(join(root, folder), { withFileTypes: true });
  const files = entries.filter(entry => entry.isFile() && entry.name.endsWith('.md'));
  const order = name => name === '공부 목표.md' ? 0 : name === '학습 노트.md' ? 1 : 2;
  const items = files.sort((a, b) => order(a.name) - order(b.name) || sort(a.name, b.name))
    .map(entry => ({ text: label(entry.name.replace(/\.md$/, '')), link: pageLink(`${folder}/${entry.name}`) }));
  for (const entry of entries.filter(entry => entry.isDirectory()).sort((a, b) => sort(a.name, b.name))) {
    const children = pages(`${folder}/${entry.name}`);
    if (children.length) items.push({ text: label(entry.name), collapsed: true, items: children });
  }
  return items;
}

const count = items => items.reduce((total, item) => total + (item.link ? 1 : count(item.items)), 0);
export const subjects = readdirSync(root).filter(name => /^\d{2}-/.test(name)).sort(sort).map(folder => {
  const items = pages(folder);
  const goal = readFileSync(join(root, folder, '공부 목표.md'), 'utf8');
  return {
    text: label(folder), number: folder.slice(0, 2), link: pageLink(`${folder}/공부 목표.md`),
    status: goal.match(/상태:\s*([^·\n]+)/)?.[1].trim() || '시작 전',
    count: count(items), items
  };
});

export const sidebar = [
  { text: '전체 공부 목표', link: pageLink('공부 목표.md') },
  ...subjects.map(subject => ({ text: `${subject.number} ${subject.text}`, collapsed: true, items: subject.items })),
  { text: '실습 프로젝트', collapsed: true, items: [
    { text: 'Java Lab', link: '/java-lab/README' },
    { text: 'Spring Lab', link: '/spring-lab/README' },
    { text: 'Spring 실습 순서', link: '/spring-lab/docs/exercises' }
  ] }
];
