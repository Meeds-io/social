<template>
  <v-app 
    class="application-body"
    flat>
    <space-members-toolbar
      :keyword="keyword"
      :filter="filter"
      :people-count="peopleCount"
      :is-manager="isManager"
      @keyword-changed="keyword = $event"
      @filter-changed="filter = $event"
      @loading="loading = $event" />
    <space-members-alert
      v-if="space"
      :space-display-name="space.displayName" />
    <people-card-list
      ref="spaceMembers"
      :keyword="keyword"
      :filter="filter"
      :space-id="$root.spaceId"
      :people-count="peopleCount"
      :is-manager="isManager"
      :loading="loading"
      :mobile-display="$root.isMobile"
      md="4"
      lg="3"
      xl="3" />
  </v-app>
</template>
<script>
export default {
  props: {
    isManager: {
      type: Boolean,
      default: false,
    },
    filter: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    keyword: null,
    loading: false,
  }),
  computed: {
    space() {
      return this.$root.space;
    },
    peopleCount() {
      return this.$root.space?.membersCount || 0;
    },
  },
  created() {
    this.$root.$on('space-settings-members-updated', this.refreshMembers);
    this.$root.$on('space-settings-pending-updated', this.refreshPending);

    if (this.isManager) {
      document.dispatchEvent(new CustomEvent('space-member-management-actions-load'));
    }
  },
  beforeDestroy() {
    this.$root.$off('space-settings-members-updated', this.refreshMembers);
    this.$root.$off('space-settings-pending-updated', this.refreshPending);
  },
  methods: {
    refreshMembers() {
      if (this.filter === 'member') {
        this.refreshUsers();
      }
    },
    refreshPending() {
      if (this.filter === 'invited' || this.filter === 'pending') {
        this.refreshUsers();
      }
    },
    refreshUsers() {
      this.$refs.spaceMembers.searchPeople();
    },
  },
};
</script>

