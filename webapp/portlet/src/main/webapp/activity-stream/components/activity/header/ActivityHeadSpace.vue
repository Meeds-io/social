<template>
  <a
    :id="id"
    :href="url"
    class="text-none primary--text space-avatar activity-head-space-link">
    <v-list-item-avatar
      size="20"
      rounded
      class="ma-0">
      <v-img :src="avatarUrl" eager />
    </v-list-item-avatar>
    {{ displayName }}
  </a>
</template>

<script>
const randomMax = 10000;
export default {
  props: {
    space: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    id: `spaceAvatar${parseInt(Math.random() * randomMax)}`,
    tiptipInitialized: false,
  }),
  computed: {
    spaceId() {
      return this.space && this.space.id;
    },
    displayName() {
      return this.space && this.space.displayName;
    },
    groupId() {
      return this.space && this.space.groupId;
    },
    avatarUrl() {
      return this.space && this.space.avatarUrl || `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/${this.prettyName}/avatar`;
    },
    url() {
      if (!this.groupId) {
        return '#';
      }
      const uri = this.groupId.replace(/\//g, ':');
      return `${eXo.env.portal.context}/g/${uri}/`;
    },
    labels() {
      return {
        CancelRequest: this.$t('UserProfilePopup.label.CancelRequest'),
        Confirm: this.$t('UserProfilePopup.label.Confirm'),
        Connect: this.$t('UserProfilePopup.label.Connect'),
        Ignore: this.$t('UserProfilePopup.label.Ignore'),
        RemoveConnection: this.$t('UserProfilePopup.label.RemoveConnection'),
        StatusTitle: this.$t('UserProfilePopup.label.Loading'),
        join: this.$t('UIManagePendingSpaces.label.action_request_to_join'),
        leave: this.$t('UIManagePendingSpaces.label.action_leave_space'),
        members: this.$t('UIManageInvitationSpaces.label.Members'),
      };
    },
  },
  mounted() {
    if (this.spaceId && this.groupId) {
      window.setTimeout(() => {
        this.initTiptip();
      }, 500);
    }
  },
  methods: {
    initTiptip() {
      if (this.tiptipInitialized) {
        return;
      }
      this.$nextTick(() => {
        const $element = $(`#${this.id}`);
        if (!$element.length) {
          window.setTimeout(() => {
            this.initTiptip();
          }, 1000);
          return;
        }
        this.tiptipInitialized = true;
        $element.spacePopup({
          userName: eXo.env.portal.userName,
          spaceID: this.spaceId,
          restURL: '/portal/rest/v1/social/spaces/{0}',
          membersRestURL: '/portal/rest/v1/social/spaces/{0}/users?returnSize=true',
          managerRestUrl: '/portal/rest/v1/social/spaces/{0}/users?role=manager&returnSize=true',
          membershipRestUrl: '/portal/rest/v1/social/spacesMemberships?space={0}&returnSize=true',
          defaultAvatarUrl: this.avatarUrl,
          deleteMembershipRestUrl: '/portal/rest/v1/social/spacesMemberships/{0}:{1}:{2}',
          labels: this.labels,
          content: false,
          keepAlive: true,
          defaultPosition: this.$vuetify.rtl && 'right_bottom' || 'left_bottom',
          maxWidth: '320px',
        });
      });
    },
  },
};
</script>