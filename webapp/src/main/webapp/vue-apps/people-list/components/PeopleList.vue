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
          @keyword-changed="keyword = $event"
          @filter-changed="filter = $event" />
        <people-card-list
          ref="peopleList"
          :keyword="keyword"
          :filter="filter"
          :people-count="peopleCount"
          :items-per-page="$root.pageSize"
          :users-resource-url="$root.usersResourceUrl"
          :directory-filtered="$root.directoryFiltered"
          :mobile-display="$root.isMobile"
          @loading="peopleLoading"
          @loaded="peopleLoaded" />
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
export default {
  props: {
    filter: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    keyword: null,
    peopleCount: 0,
    // the live region's text: emptied when a search starts and filled when
    // it ends, so that a search giving the same number is announced too
    resultsStatus: '',
    loadedOnce: false,
  }),
  methods: {
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
