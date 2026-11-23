<template>
  <v-app 
    class="peopleList application-body"
    flat>
    <v-hover v-model="$root.hover">
      <div
        @focusin="$root.focusWithin = true"
        @focusout="updateFocusWithin">
        <people-toolbar
          :filter="filter"
          :filter-message="peopleCount ? $t('peopleList.label.peopleCount', {0: peopleCount}) : null"
          :compact-display="$root.isMobile"
          filter-message-class="showingPeopleText ms-1 d-none d-sm-flex"
          :initial-keyword="keyword"
          @keyword-changed="keyword = $event"
          @filter-changed="filter = $event" />
        <!-- with advanced filters in the URL, the list waits for the
             advanced filter to keep the ones the viewer can apply: its first
             search is then the shared one -->
        <people-card-list
          v-if="filtersRestored"
          ref="peopleList"
          :keyword="keyword"
          :filter="filter"
          :people-count="peopleCount"
          :items-per-page="$root.pageSize"
          :users-resource-url="$root.usersResourceUrl"
          :directory-filtered="$root.directoryFiltered"
          :mobile-display="$root.isMobile"
          :initial-advanced-filter="advancedFilter"
          @advanced-filter-changed="updateAdvancedFilter"
          @loading="peopleLoading"
          @loaded="peopleLoaded" />
        <div
          v-else
          class="d-flex justify-center py-8">
          <v-progress-circular
            color="primary"
            indeterminate />
        </div>
      </div>
    </v-hover>
    <!-- the number of users after each search, announced without moving
         the focus; the region stays in the page, so that its text changes
         are announced, an empty result included -->
    <div
      role="status"
      class="d-sr-only people-list-results-status">
      {{ resultsStatus }}
    </div>
    <people-advanced-filter-drawer />
    <people-list-settings-drawer
      v-if="$root.canEdit" />
  </v-app>
</template>
<script>
import {readUrlFilters, writeUrlFilters} from '../js/PeopleListUrl.js';

export default {
  props: {
    filter: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    // the search and advanced filter, written to the page URL so that it
    // can be shared, and read from it on load
    keyword: readUrlFilters().keyword,
    advancedFilter: null,
    filtersRestored: !Object.keys(readUrlFilters().attributes).length,
    // the attributes this directory can filter on: the only ones it writes
    ownAttributes: [],
    peopleCount: 0,
    // the live region's text: emptied when a search starts and filled when
    // it ends, so that a search giving the same number is announced too
    resultsStatus: '',
    loadedOnce: false,
  }),
  watch: {
    keyword() {
      this.updateUrl();
    },
  },
  created() {
    this.$root.$on('advanced-filter-attributes', this.setOwnAttributes);
    this.$root.$on('advanced-filter-restored', this.restoreAdvancedFilter);
  },
  beforeDestroy() {
    this.$root.$off('advanced-filter-attributes', this.setOwnAttributes);
    this.$root.$off('advanced-filter-restored', this.restoreAdvancedFilter);
  },
  methods: {
    setOwnAttributes(ownAttributes) {
      this.ownAttributes = ownAttributes || [];
    },
    // the URL is not rewritten on load, unless the search changed while the
    // filter was being restored: its write was held until now
    restoreAdvancedFilter(advancedFilter) {
      this.advancedFilter = Object.keys(advancedFilter || {}).length ? advancedFilter : null;
      this.filtersRestored = true;
      if (this.keyword !== readUrlFilters().keyword) {
        this.updateUrl();
      }
    },
    // the list hands back the restored filter as it applies it: only the
    // viewer's own changes are written
    updateAdvancedFilter(advancedFilter) {
      if (advancedFilter !== this.advancedFilter) {
        this.advancedFilter = advancedFilter;
        this.updateUrl();
      }
    },
    // replaceState: no reload, the focus stays where it is (RGAA 7.4); held
    // while the shared filter is restored, which would otherwise be erased
    updateUrl() {
      if (this.filtersRestored) {
        writeUrlFilters(this.keyword, this.advancedFilter, this.ownAttributes);
      }
    },
    // the gear also shows while the focus is inside the app, so that a
    // keyboard user sees it when tabbing onto it (v-hover only follows the
    // mouse)
    updateFocusWithin(event) {
      this.$root.focusWithin = !!event.relatedTarget && event.currentTarget.contains(event.relatedTarget);
    },
    peopleLoading(loading) {
      if (loading) {
        this.resultsStatus = '';
      }
    },
    peopleLoaded(peopleCount) {
      this.peopleCount = peopleCount;
      // the page's first list is not a search result to announce
      if (this.loadedOnce) {
        this.resultsStatus = peopleCount ? this.$t('peopleList.label.peopleCount', {0: peopleCount})
          : this.$t('peopleList.label.noResults');
      }
      this.loadedOnce = true;
    },
  },
};
</script>
