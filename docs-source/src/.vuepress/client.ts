import { defineClientConfig } from '@vuepress/client';
import Mermaid from './components/Mermaid.vue';

export default defineClientConfig({
    enhance: ({ app }) => {
        app.component('Mermaid', Mermaid);
    }
});