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
      @filter-changed="filter = $event" />
    <alert-space-members
      v-if="space"
      :space-display-name="space.displayName" />
    <people-card-list
      ref="spaceMembers"
      :keyword="keyword"
      :filter="filter"
      :space-id="spaceId"
      :people-count="peopleCount"
      :is-manager="isManager"
      md="4"
      lg="3"
      xl="3"
      @loaded="peopleLoaded" />
    <people-compact-card-options-drawer />
  </v-app>
</template>
<script>
export default {
  props: {
    isManager: {
      type: Boolean,
      default: false,
    },
    spaceId: {
      type: Number,
      default: 0,
    },
    filter: {
      type: String,
      default: null,
    },
    isExternalFeatureEnabled: {
      type: Boolean,
      default: true,
    },
  },
  data: () => ({
    keyword: null,
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

    this.$spaceService.getSpaceById(eXo.env.portal.spaceId)
      .then( space => {
        this.space = space;
      })
      .finally(() => this.$root.$applicationLoaded());
    if (this.isManager) {
      extensionRegistry.registerExtension('space-member-extension', 'action', {
        id: 'spaceMembers-removeMember',
        title: this.$t('peopleList.button.removeMember'),
        icon: 'fa-user-minus',
        class: 'fas fa-user-minus',
        order: 2,
        enabled: (user) => {
          return ['member', 'manager', 'redactor', 'publisher', 'disabled'].includes(this.filter)
                 && (user.isMember || !user.enabled) && user?.username !== eXo?.env?.portal?.userName
                 && !user.isGroupBound;
        },
        click: (user) => {
          this.$spaceService.removeMember(eXo.env.portal.spaceId, user.username)
            .then(() => this.$refs.spaceMembers.searchPeople());
        },
      });
      extensionRegistry.registerExtension('space-member-extension', 'action', {
        id: 'spaceMembers-removeManager',
        title: this.$t('peopleList.button.removeManager'),
        icon: 'uiIconMemberAdmin',
        class: 'fas fa-user-cog',
        order: 1,
        enabled: (user) => {
          return user.isManager && (this.filter === 'member' || this.filter === 'manager' || this.filter === 'redactor' || this.filter === 'publisher');
        },
        click: (user) => {
          this.$spaceService.removeManager(eXo.env.portal.spaceId, user.username)
            .then(() => this.$refs.spaceMembers.searchPeople());
        },
      });
      extensionRegistry.registerExtension('space-member-extension', 'action', {
        id: 'spaceMembers-promoteManager',
        title: this.$t('peopleList.button.promoteManager'),
        icon: 'uiIconMemberAdmin',
        class: 'fas fa-user-cog',
        order: 1,
        enabled: (user) => {
          return user.enabled && !user.deleted && (this.filter === 'member' || this.filter === 'manager' || this.filter === 'redactor' || this.filter === 'publisher') && !user.isManager;
        },
        click: (user) => {
          this.$spaceService.promoteManager(eXo.env.portal.spaceId, user.username)
            .then(() => this.$refs.spaceMembers.searchPeople());
        },
      });
      extensionRegistry.registerExtension('space-member-extension', 'action', {
        id: 'spaceMembers-cancelInvitation',
        title: this.$t('peopleList.button.cancelInvitation'),
        icon: 'uiIconTrash',
        order: 1,
        enabled: (user) => {
          return this.filter === 'invited' && user.isInvited;
        },
        click: (user) => {
          this.$spaceService.cancelInvitation(eXo.env.portal.spaceId, user.username)
            .then(() => this.$refs.spaceMembers.searchPeople());
        },
      });
      extensionRegistry.registerExtension('space-member-extension', 'action', {
        id: 'spaceMembers-acceptPending',
        title: this.$t('peopleList.button.acceptPending'),
        icon: 'uiIconUserCheck',
        order: 1,
        enabled: (user) => {
          return user.enabled && !user.deleted && this.filter === 'pending' && user.isPending;
        },
        click: (user) => {
          this.$spaceService.acceptUserRequest(eXo.env.portal.spaceId, user.username)
            .then(() => this.$refs.spaceMembers.searchPeople());
        },
      });
      extensionRegistry.registerExtension('space-member-extension', 'action', {
        id: 'spaceMembers-refusePending',
        title: this.$t('peopleList.button.refusePending'),
        icon: 'uiIconTrash',
        order: 1,
        enabled: (user) => {
          return this.filter === 'pending' && user.isPending;
        },
        click: (user) => {
          this.$spaceService.refuseUserRequest(eXo.env.portal.spaceId, user.username)
            .then(() => this.$refs.spaceMembers.searchPeople());
        },
      });
      document.dispatchEvent(new CustomEvent('space-member-extension-updated'));
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

