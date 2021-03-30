<template>
  <div class="flex-nowrap d-flex flex-shrink-0 align-center">
    <a
      :id="id"
      :href="url"
      class="flex-nowrap flex-shrink-0 d-flex text-truncate container--fluid">
      <v-avatar
        :size="size"
        :class="avatarClass"
        class="pull-left my-auto">
        <img :src="avatarUrl" :alt="title"/>
      </v-avatar>
      <div v-if="fullname || $slots.subTitle" class="pull-left ml-2 d-flex flex-column text-truncate">
        <p v-if="fullname" :class="boldTitle && 'font-weight-bold'" class="text-truncate subtitle-2 text-color my-auto">
          {{ fullname }}
          <span v-if="isExternal" class="muted">{{ externalTag }} </span>
        </p>
        <p v-if="$slots.subTitle" class="text-sub-title my-auto text-left">
          <slot name="subTitle"></slot>
        </p>
      </div>
      <template v-if="$slots.actions">
        <slot name="actions"></slot>
      </template>
    </a>
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
    external: {
      type: Boolean,
      default: false,
    },
    retrieveExtraInformation: {
      type: Boolean,
      default: true,
    },
    boldTitle: {
      type: Boolean,
      default: () => false,
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
    tiptipPosition: {
      type: String,
      default: () => null,
    },
    avatarClass: {
      type: String,
      default: () => '',
    },
    avatarUrl: {
      type: String,
      default: function() {
        return `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/${this.username}/avatar`;
      },
    },
    url: {
      type: String,
      default: function() {
        return `${eXo.env.portal.context}/${eXo.env.portal.portalName}/profile/${this.username}`;
      },
    },
    labels: {
      type: Object,
      default: null,
    },
    title: {
      type: String,
      default: function() {
        return `${this.title}`;
      }
    },
  },
  data() {
    return {
      id: `userAvatar${parseInt(Math.random() * randomMax)
        .toString()
        .toString()}`,
      isExternal : false,
    };
  },
  computed: {
    externalTag() {
      return `( ${this.$t('userAvatar.external.label')} )`;
    },
  },
  watch: {
    external() {
      this.isExternal = this.external && Boolean(this.external) || false;
    },
  },
  created() {
    if (this.retrieveExtraInformation) {
      this.$userService.getUser(this.username)
        .then(user => {
          this.isExternal = user.external === 'true';
        });
    } else {
      this.isExternal = this.external;
    }
  },
  mounted() {
    if (this.username && this.tiptip) {
      if (!this.labels) {
        this.labels = {
          CancelRequest: this.$t('spacesList.label.profile.CancelRequest'),
          Confirm: this.$t('spacesList.label.profile.Confirm'),
          Connect: this.$t('spacesList.label.profile.Connect'),
          Ignore: this.$t('spacesList.label.profile.Ignore'),
          RemoveConnection: this.$t('spacesList.label.profile.RemoveConnection'),
          StatusTitle: this.$t('spacesList.label.profile.StatusTitle'),
          External: this.$t('spacesList.label.profile.External'),
        };
      }
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
