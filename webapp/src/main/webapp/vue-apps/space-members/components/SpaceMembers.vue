<template>
  <v-app 
    class="application-body"
    flat>
    <space-members-toolbar
      :filter="filter"
      :is-manager="isManager"
      :keyword="keyword"
      :people-count="peopleCount"
      @filter-changed="filter = $event"
      @keyword-changed="keyword = $event"
      @loading="loading = $event" />
    <space-members-alert
      v-if="space"
      :space-display-name="space.displayName" />
    <people-card-list
      ref="spaceMembers"
      :filter="filter"
      :is-manager="isManager"
      :keyword="keyword"
      lg="3"
      :loading="loading"
      md="4"
      :mobile-display="$root.isMobile"
      :people-count="peopleCount"
      :space-id="$root.spaceId"
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
      space () {
        return this.$root.space;
      },
      peopleCount () {
        return this.$root.space?.membersCount || 0;
      },
    },
    created () {
      this.$root.$on('space-settings-members-updated', this.refreshMembers);
      this.$root.$on('space-settings-pending-updated', this.refreshPending);

      if (this.isManager) {
        document.dispatchEvent(new CustomEvent('space-member-management-actions-load'));
      }
    },
    beforeUnmount () {
      this.$root.$off('space-settings-members-updated', this.refreshMembers);
      this.$root.$off('space-settings-pending-updated', this.refreshPending);
    },
    methods: {
      refreshMembers () {
        if (this.filter === 'member') {
          this.refreshUsers();
        }
      },
      refreshPending () {
        if (this.filter === 'invited' || this.filter === 'pending') {
          this.refreshUsers();
        }
      },
      refreshUsers () {
        this.$refs.spaceMembers.searchPeople();
      },
    },
  };
</script>

