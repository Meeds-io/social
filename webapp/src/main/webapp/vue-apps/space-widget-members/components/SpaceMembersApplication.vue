<template>
  <v-app 
    class="application-body"
    flat>
    <v-hover v-model="hover">
      <widget-wrapper
        key="spaceMembers"
        ref="spaceMembers"
        extra-class="application-body"
        :title="$t('social.space.description.members')">
        <template #action>
          <div class="position-relative">
            <exo-user-avatars-list
              class="absolute-vertical-center"
              :class="$vuetify.rtl && 'l-0' || 'r-0'"
              clickable
              compact
              :default-length="peopleCount"
              :icon-size="33"
              :margin-left="$root.members.length > 1 && 'ml-n5' || ''"
              :max="4"
              :popover="!isAnonymous"
              :users="$root.members"
              @open-detail="$root.$emit('space-members-drawer-open')" />
          </div>
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
      isAnonymous: !eXo.env.portal.userName,
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

