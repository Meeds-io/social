<template>
  <v-app 
    class="peopleList application-body"
    flat>
    <people-toolbar
      :filter="filter"
      :filter-message="$t('peopleList.label.peopleCount', {0: peopleCount})"
      :compact-display="$root.isMobile"
      filter-message-class="showingPeopleText ms-3 d-none d-sm-flex"
      @keyword-changed="keyword = $event"
      @filter-changed="filter = $event" />
    <people-card-list
      ref="peopleList"
      :keyword="keyword"
      :filter="filter"
      :people-count="peopleCount"
      :mobile-display="$root.isMobile"
      @loaded="peopleLoaded" />
    <people-advanced-filter-drawer />
    <people-compact-card-options-drawer />
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
    peopleLoaded(peopleCount) {
      this.peopleCount = peopleCount;
    }
  },
};
</script>
