<template>
  <v-app 
    class="peopleList application-body"
    flat>
    <people-toolbar
      :compact-display="$root.isMobile"
      :filter="filter"
      :filter-message="peopleCount && $t('peopleList.label.peopleCount', {0: peopleCount})"
      filter-message-class="showingPeopleText ms-1 d-none d-sm-flex"
      @filter-changed="filter = $event"
      @keyword-changed="keyword = $event" />
    <people-card-list
      ref="peopleList"
      :filter="filter"
      :keyword="keyword"
      :mobile-display="$root.isMobile"
      :people-count="peopleCount"
      @loaded="peopleLoaded" />
    <people-advanced-filter-drawer />
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
      peopleLoaded (peopleCount) {
        this.peopleCount = peopleCount;
      },
    },
  };
</script>
