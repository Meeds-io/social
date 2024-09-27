<template>
  <v-app 
    class="application-body"
    flat>
    <v-hover v-model="hover">
      <widget-wrapper
        :title="$t('social.space.description.members')"
        ref="spaceMembers"
        key="spaceMembers"
        extra-class="application-body">
        <template #action>
          <exo-user-avatars-list
            :users="$root.members"
            :default-length="peopleCount"
            :margin-left="peopleCount > 1 && 'ml-n5' || ''"
            :icon-size="33"
            :max="4"
            compact
            clickable
            @open-detail="$root.$emit('space-members-drawer-open')" />
        </template>
      </widget-wrapper>
    </v-hover>
    <space-members-drawer v-if="$root.isMember" />
  </v-app>
</template>
<script>
export default {
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

