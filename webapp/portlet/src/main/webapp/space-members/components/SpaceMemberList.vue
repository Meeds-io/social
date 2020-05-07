<template>
  <v-app 
    class="transparent"
    flat>
    <space-member-toolbar
      :keyword="keyword"
      :filter="filter"
      :people-count="peopleCount"
      :skeleton="skeleton"
      @keyword-changed="keyword = $event"
      @filter-changed="filter = $event" />
    <space-member-card-list
      ref="spaceMember"
      :keyword="keyword"
      :filter="filter"
      :loading-people="loadingPeople"
      :skeleton="skeleton"
      :people-count="peopleCount"
      @loaded="peopleLoaded" />

    <space-member-list-drawer />
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

