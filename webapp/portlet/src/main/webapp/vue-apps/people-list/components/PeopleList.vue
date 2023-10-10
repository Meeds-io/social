<template>
  <v-app 
    class="transparent peopleList"
    :has-title="false"
    flat>
    <widget-wrapper>
      <people-toolbar
        :filter="filter"
        :people-count="peopleCount"
        @keyword-changed="keyword = $event"
        @filter-changed="filter = $event" />
      <people-card-list
        ref="peopleList"
        :keyword="keyword"
        :filter="filter"
        :loading-people="loadingPeople"
        :people-count="peopleCount"
        @loaded="peopleLoaded" />
    </widget-wrapper>
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
    loadingPeople: false,
  }),
  methods: {
    peopleLoaded(peopleCount) {
      this.peopleCount = peopleCount;
    }
  },
};
</script>
