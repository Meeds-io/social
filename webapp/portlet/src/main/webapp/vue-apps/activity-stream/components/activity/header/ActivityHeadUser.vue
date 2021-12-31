<template>
  <a
    v-if="avatar"
    :id="id"
    :href="url"
    class="activity-head-user-link">
   
    <v-list-item-avatar :size="size" class="ma-0">
      <img
        :src="avatarUrl"
        class="object-fit-cover my-auto"
        loading="lazy"
        role="presentation">
    </v-list-item-avatar>
  </a>
  <a
    v-else
    :id="id"
    :href="url"
    class="text-none primary--text activity-head-user-link">
    {{ fullName }}
    <span v-if="isExternal" class="externalFlagClass">
      ({{ $t('userAvatar.external.label') }})
    </span>
  </a>
</template>

<script>
const randomMax = 10000;

export default {
  props: {
    identity: {
      type: Object,
      default: () => null,
    },
    avatar: {
      type: Boolean,
      default: false,
    },
    size: {
      type: Number,
      default: () => 45,
    },
  },
  data() {
    return {
      id: `userAvatar${parseInt(Math.random() * randomMax)}`,
    };
  },
  computed: {
    username() {
      return this.identity && this.identity.remoteId;
    },
    url() {
      return `${eXo.env.portal.context}/${eXo.env.portal.portalName}/profile/${this.username}`;
    },
    avatarUrl() {
      return this.identity && this.identity.profile && this.identity.profile.avatar;
    },
    fullName() {
      return this.identity && this.identity.profile && this.identity.profile.fullname;
    },
    isExternal() {
      return this.identity && this.identity.profile && this.identity.profile.dataEntity && this.identity.profile.dataEntity.external === 'true';
    },
  },
  mounted() {
    // TODO disable tiptip because of high CPU usage using its code
    this.initTiptip();
  },
  methods: {
    initTiptip() {
      this.$nextTick(() => {
        $(`#${this.id}`).userPopup({
          restURL: '/portal/rest/social/people/getPeopleInfo/{0}.json',
          userId: this.username,
          keepAlive: true,
        });
      });
    },
  },
};
</script>
