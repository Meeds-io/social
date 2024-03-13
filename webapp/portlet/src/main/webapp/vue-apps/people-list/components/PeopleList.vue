<template>
  <v-app 
    class="transparent peopleList card-border-radius overflow-hidden"
    flat>
    <people-toolbar
      :filter="filter"
      :filter-message="$t('peopleList.label.peopleCount', {0: peopleCount})"
      filter-message-class="showingPeopleText text-sub-title ms-3 d-none d-sm-flex"
      @keyword-changed="keyword = $event"
      @filter-changed="filter = $event" />
    <people-card-list
      ref="peopleList"
      :keyword="keyword"
      :filter="filter"
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
    peopleLoaded(peopleCount) {
      this.peopleCount = peopleCount;
    }
  },
};
</script>
