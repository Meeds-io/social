<template>
  <a
    :id="id"
    :href="url"
    class="flex-nowrap flex-shrink-0">
    <v-avatar :size="size">
      <img :src="avatarUrl" />
    </v-avatar>
    <span v-if="fullname" class="text-truncate subtitle-1 text-color ml-2">
      {{ fullname }}
    </span>
  </a>
</template>

<script>
const randomMax = 10000;

export default {
  props: {
    username: {
      type: String,
      default: () => null,
    },
    fullname: {
      type: String,
      default: () => null,
    },
    tiptip: {
      type: Boolean,
      default: () => false,
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
      if (this.username && this.tiptip) {
        // TODO disable tiptip because of high CPU usage using its code
        this.initTiptip();
      }
    },
  },
  created() {
    if (this.username && this.tiptip) {
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
