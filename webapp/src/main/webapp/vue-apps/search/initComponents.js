import SearchApplication from './components/SearchApplication.vue';
import SearchToolbar from './components/SearchToolbar.vue';
import SearchDrawer from './components/SearchDrawer.vue';
import SearchResults from './components/SearchResults.vue';
import SearchResultCard from './components/SearchResultCard.vue';
import SearchTagSelector from './components/SearchTagSelector.vue';
import SearchTagList from './components/SearchTagList.vue';
import SearchOptions from './components/SearchOptions.vue';

const components = {
  'search-application': SearchApplication,
  'search-toolbar': SearchToolbar,
  'search-drawer': SearchDrawer,
  'search-results': SearchResults,
  'search-result-card': SearchResultCard,
  'search-tag-selector': SearchTagSelector,
  'search-tag-list': SearchTagList,
  'search-options': SearchOptions
};

for (const key in components) {
  Vue.component(key, components[key]);
}
