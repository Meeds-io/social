<template>
  <v-app 
    class="transparent peopleList"
    flat>
    <people-toolbar
      :keyword="keyword"
      :filter="filter"
      :people-count="peopleCount"
      :skeleton="skeleton"
      @keyword-changed="keyword = $event"
      @filter-changed="filter = $event" />
    <people-card-list
      ref="peopleList"
      :keyword="keyword"
      :filter="filter"
      :loading-people="loadingPeople"
      :skeleton="skeleton"
      :people-count="peopleCount"
      @loaded="peopleLoaded" />
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
    skeleton: true,
  }),
  methods: {
    peopleLoaded(peopleCount) {
      document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      this.peopleCount = peopleCount;
      if (this.skeleton) {
        this.skeleton = false;
      }
    }
  },
};
</script>