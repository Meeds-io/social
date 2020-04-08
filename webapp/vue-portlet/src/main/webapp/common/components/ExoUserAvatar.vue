<template>
  <div class="flex-nowrap d-flex flex-shrink-0 align-center">
    <a
      :id="id"
      :href="url"
      class="flex-nowrap d-flex flex-shrink-0 align-center">
      <v-avatar :size="size" :tile="tile">
        <img :src="avatarurl" />
      </v-avatar>
    </a>
    <div v-if="fullname || subtitle" class="d-flex flex-wrap flex-grow-1 flex-shrink-1 ml-2 text-left">
      <p v-if="fullname" class=" mb-0 exo-avatar-item-title">
        <a :href="url" class="text-truncate body-2 font-weight-bold text-color">{{ fullname }}</a>
      </p>
      <p v-if="$slots.subTitle " class="caption text-sub-title mb-0 exo-avatar-item-subtitle">
        <slot name="subTitle"></slot>
      </p>
    </div>
    <template v-if="$slots.actions">
      <slot name="actions"></slot>
    </template>
  </div>
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
      default: () => true,
    },
    size: {
      type: Number,
      // eslint-disable-next-line no-magic-numbers
      default: () => 37,
    },
    tile: {
      type: Boolean,
      default: () => false,
    },
    tiptipPosition: {
      type: String,
      default: function() {
        return null;
      },
    },
    avatarurl: {
      type: String,
      default: function() {
        return `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/${this.username}/avatar`;
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
