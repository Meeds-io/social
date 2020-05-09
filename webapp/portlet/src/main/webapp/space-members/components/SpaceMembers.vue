<template>
  <v-app 
    class="transparent"
    flat>
    <space-members-toolbar
      :keyword="keyword"
      :filter="filter"
      :people-count="peopleCount"
      :skeleton="skeleton"
      :is-manager="isManager"
      @keyword-changed="keyword = $event"
      @filter-changed="filter = $event"
      @invite-users="$refs.spaceInvitationDrawer.open()"
      @refresh="refreshInvited" />

    <people-card-list
      ref="spaceMembers"
      :keyword="keyword"
      :filter="filter"
      :loading-people="loadingPeople"
      :skeleton="skeleton"
      :space-id="spaceId"
      :people-count="peopleCount"
      :is-manager="isManager"
      ignore-profile-extensions
      @loaded="peopleLoaded" />

    <space-invitation-drawer
      ref="spaceInvitationDrawer"
      @refresh="refreshInvited" />
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
  },
  data: () => ({
    keyword: null,
    peopleCount: 0,
    loadingPeople: false,
    skeleton: true,
  }),
  created() {
    if (this.isManager) {
      extensionRegistry.registerExtension('profile-extension', 'action', {
        title: this.$t('peopleList.button.removeMember'),
        icon: 'uiIconTrash',
        order: 0,
        enabled: (user) => {
          return (this.filter === 'member' || this.filter === 'manager') && user.isMember;
        },
        click: (user) => {
          this.$spaceService.removeMember(eXo.env.portal.spaceName, user.username)
            .then(() => this.$refs.spaceMembers.searchPeople());
        },
      });
      extensionRegistry.registerExtension('profile-extension', 'action', {
        title: this.$t('peopleList.button.removeManager'),
        icon: 'uiIconMemberAdmin',
        order: 1,
        enabled: (user) => {
          return (this.filter === 'member' || this.filter === 'manager') && user.isManager;
        },
        click: (user) => {
          this.$spaceService.removeManager(eXo.env.portal.spaceName, user.username)
            .then(() => this.$refs.spaceMembers.searchPeople());
        },
      });
      extensionRegistry.registerExtension('profile-extension', 'action', {
        title: this.$t('peopleList.button.promoteManager'),
        icon: 'uiIconMemberAdmin',
        order: 1,
        enabled: (user) => {
          return (this.filter === 'member' || this.filter === 'manager') && !user.isManager;
        },
        click: (user) => {
          this.$spaceService.promoteManager(eXo.env.portal.spaceDisplayName, user.username)
            .then(() => this.$refs.spaceMembers.searchPeople());
        },
      });
      extensionRegistry.registerExtension('profile-extension', 'action', {
        title: this.$t('peopleList.button.cancelInvitation'),
        icon: 'uiIconTrash',
        order: 1,
        enabled: (user) => {
          return this.filter === 'invited' && user.isInvited;
        },
        click: (user) => {
          this.$spaceService.cancelInvitation(eXo.env.portal.spaceDisplayName, user.username)
            .then(() => this.$refs.spaceMembers.searchPeople());
        },
      });
      extensionRegistry.registerExtension('profile-extension', 'action', {
        title: this.$t('peopleList.button.acceptPending'),
        icon: 'uiIconUserCheck',
        order: 1,
        enabled: (user) => {
          return this.filter === 'pending' && user.isPending;
        },
        click: (user) => {
          this.$spaceService.acceptUserRequest(eXo.env.portal.spaceDisplayName, user.username)
            .then(() => this.$refs.spaceMembers.searchPeople());
        },
      });
      extensionRegistry.registerExtension('profile-extension', 'action', {
        title: this.$t('peopleList.button.refusePending'),
        icon: 'uiIconTrash',
        order: 1,
        enabled: (user) => {
          return this.filter === 'pending' && user.isPending;
        },
        click: (user) => {
          this.$spaceService.refuseUserRequest(eXo.env.portal.spaceDisplayName, user.username)
            .then(() => this.$refs.spaceMembers.searchPeople());
        },
      });
      document.dispatchEvent(new CustomEvent('profile-extension-updated'));
    }
  },
  methods: {
    refreshInvited() {
      if (this.filter === 'invited') {
        this.$refs.spaceMembers.searchPeople();
      }
    },
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

