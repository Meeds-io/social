<template>
  <a
    :id="id"
    :href="url"
    rel="nofollow"
    target="_blank">
    <v-avatar :size="size">
      <img :src="avatarUrl" />
    </v-avatar>
  </a>
</template>

<script>
const randomMax = 10000;

export default {
  props: {
    username: {
      type: String,
      default: function() {
        return null;
      },
    },
    size: {
      type: Number,
      // eslint-disable-next-line no-magic-numbers
      default: () => 37,
    },
    tiptipPosition: {
      type: String,
      default: function() {
        return null;
      },
    },
  },
  data() {
    return {
      id: `chip${parseInt(Math.random() * randomMax)
        .toString()
        .toString()}`,
    };
  },
  computed: {
    labels() {
      return {
        CancelRequest: this.$t('spacesList.label.profile.CancelRequest'),
        Confirm: this.$t('spacesList.label.profile.Confirm'),
        Connect: this.$t('spacesList.label.profile.Connect'),
        Ignore: this.$t('spacesList.label.profile.Ignore'),
        RemoveConnection: this.$t('spacesList.label.profile.RemoveConnection'),
        StatusTitle: this.$t('spacesList.label.profile.StatusTitle'),
      };
    },
    avatarUrl() {
      return `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/${this.username}/avatar`;
    },
    url() {
      return `${eXo.env.portal.context}/${eXo.env.portal.portalName}/profile/${this.username}`;
    },
  },
  watch: {
    username() {
      if (this.username) {
        // TODO disable tiptip because of high CPU usage using its code
        this.initTiptip();
      }
    },
  },
  created() {
    if (this.username) {
      // TODO disable tiptip because of high CPU usage using its code
      this.initTiptip();
    }
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
