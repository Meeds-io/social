<template>
  <a
    :id="id"
    :href="url"
    :class="!this.space.isMember && 'not-clickable-link hidden-space'"
    class="text-none space-avatar primary--text activity-head-space-link">
    <v-avatar
      size="20"
      rounded
      class="ma-0">
      <img
        :src="avatarUrl"
        class="object-fit-cover my-auto"
        loading="lazy"
        role="presentation">
    </v-avatar>
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
    size: {
      type: Number,
      default: () => 20,
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
      return this.space && this.space.isMember ? this.space.displayName : this.$t('spacesList.label.hiddenSpace');
    },
    groupId() {
      return this.space && this.space.groupId;
    },
    avatarUrl() {
      return this.space && !this.space.isMember ? this.defaultAvatarUrl : this.space.avatarUrl || `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/${this.prettyName}/avatar`;
    },
    defaultAvatarUrl() {
      return `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/default-image/avatar`;
    },
    url() {
      if (!this.groupId) {
        return '#';
      }
      const uri = this.groupId.replace(/\//g, ':');
      return `${eXo.env.portal.context}/g/${uri}/`;
    },
  },
  mounted() {
    if (this.spaceId && this.groupId && this.space.isMember) {
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
