import DefaultTheme from 'vitepress/theme';
import { h } from 'vue';
import StudyHome from './StudyHome.vue';
import MermaidDiagram from './MermaidDiagram.vue';
import './style.css';

export default {
  extends: DefaultTheme,
  Layout: () => h(DefaultTheme.Layout, null, {
    'home-hero-before': () => h(StudyHome)
  }),
  enhanceApp({ app }) {
    app.component('MermaidDiagram', MermaidDiagram);
  }
};
