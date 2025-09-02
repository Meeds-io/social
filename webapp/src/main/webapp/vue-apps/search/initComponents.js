import SearchApplication from './components/SearchApplication.vue';
import SearchToolbar from './components/SearchToolbar.vue';
import SearchDrawer from './components/SearchDrawer.vue';
import SearchResults from './components/SearchResults.vue';
import SearchResultCard from './components/SearchResultCard.vue';
import SearchTagSelector from './components/searchOptions/SearchTagSelector.vue';
import SearchTagList from './components/searchOptions/SearchTagList.vue';
import SearchOptions from './components/searchOptions/SearchOptions.vue';
import SearchResultCardGroup from './components/SearchResultCardGroup.vue';
import SearchSpaceSelector from './components/searchOptions/SearchSpaceSelector.vue';
import SearchFavoriteSelector from './components/searchOptions/SearchFavoriteSelector.vue';
import SearchSelectedSpaceItem from './components/searchOptions/SearchSelectedSpaceItem.vue';
import SearchSortSelector from './components/searchOptions/SearchSortSelector.vue';

const components = {
  'search-application': SearchApplication,
  'search-toolbar': SearchToolbar,
  'search-drawer': SearchDrawer,
  'search-results': SearchResults,
  'search-result-card': SearchResultCard,
  'search-tag-selector': SearchTagSelector,
  'search-tag-list': SearchTagList,
  'search-options': SearchOptions,
  'search-result-card-group': SearchResultCardGroup,
  'search-favorites-selector': SearchFavoriteSelector,
  'search-space-selector': SearchSpaceSelector,
  'search-selected-space-item': SearchSelectedSpaceItem,
  'search-sort-selector': SearchSortSelector
};

for (const key in components) {
  Vue.component(key, components[key]);
}
