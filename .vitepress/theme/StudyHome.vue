<script setup>
import { useData, withBase } from 'vitepress';
const { theme } = useData();
const link = path => withBase('/' + path.split('/').map(encodeURIComponent).join('/') + '.html');
const shortcuts = [
  { text: 'Redis의 정체', path: '14-Redis-기초/01-Redis의-정체' },
  { text: 'Redis 클라이언트 부록', path: '14-Redis-기초/부록-Redis-클라이언트-라이브러리' },
  { text: 'Java 방어적 복사', path: '01-Java-기본기와-실행-원리/이펙티브-Java/17-외부 참조로부터 내부 상태 보호/학습 노트' }
];
</script>

<template>
  <main class="study-home">
    <header class="study-heading">
      <p class="study-label">JAVA · SPRING · REDIS</p>
      <h1>공부 노트</h1>
      <p class="study-description">개념 정리와 실습 기록을 주제별로 읽어보세요.</p>
    </header>
    <section class="study-shortcuts" aria-labelledby="notes-heading">
      <h2 id="notes-heading">학습 노트 바로가기</h2>
      <div class="shortcut-links">
        <a v-for="note in shortcuts" :key="note.path" :href="link(note.path)">{{ note.text }}</a>
      </div>
    </section>
    <section aria-labelledby="subjects-heading">
      <div class="subjects-heading">
        <h2 id="subjects-heading">주제별 자료</h2>
        <span>{{ theme.studySubjects.length }}개 주제</span>
      </div>
      <div class="subject-grid">
        <a v-for="subject in theme.studySubjects" :key="subject.number"
           :href="withBase(subject.link + '.html')" class="subject-card">
          <span class="subject-number">{{ subject.number }}</span>
          <h3>{{ subject.text }}</h3>
          <div class="subject-meta">
            <span>{{ subject.count }}개 문서</span>
            <span :class="{ active: subject.status === '학습 중' }">{{ subject.status }}</span>
          </div>
        </a>
      </div>
    </section>
  </main>
</template>
