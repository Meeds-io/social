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
          :filter-message="peopleCount && $t('peopleList.label.peopleCount', {0: peopleCount})"
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
          :mobile-display="$root.isMobile"
          @loaded="peopleLoaded" />
      </div>
    </v-hover>
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
  }),
  methods: {
    // the gear also shows while the focus is inside the app, so that a
    // keyboard user sees it when tabbing onto it (v-hover only follows the
    // mouse)
    updateFocusWithin(event) {
      this.$root.focusWithin = !!event.relatedTarget && event.currentTarget.contains(event.relatedTarget);
    },
    peopleLoaded(peopleCount) {
      this.peopleCount = peopleCount;
    },
  },
};
</script>
