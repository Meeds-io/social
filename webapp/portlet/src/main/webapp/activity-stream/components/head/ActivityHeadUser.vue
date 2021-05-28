<template>
  <a
    v-if="avatar"
    :id="id"
    :href="url"
    class="activity-head-user-link">
    <v-list-item-avatar size="45" class="me-3 my-0">
      <v-img :src="avatarUrl" eager />
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
    labels() {
      return {
        CancelRequest: this.$t('UserProfilePopup.label.CancelRequest'),
        Confirm: this.$t('UserProfilePopup.label.Confirm'),
        Connect: this.$t('UserProfilePopup.label.Connect'),
        Ignore: this.$t('UserProfilePopup.label.Ignore'),
        RemoveConnection: this.$t('UserProfilePopup.label.RemoveConnection'),
        StatusTitle: this.$t('UserProfilePopup.label.Loading'),
        External: this.$t('UserProfilePopup.label.External'),
      };
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
          labels: this.labels,
          keepAlive: true,
        });
      });
    },
  },
};
</script>
