<template>
  <a
    :id="id"
    :href="url"
    class="flex-nowrap flex-shrink-0 d-flex spaceAvatar">
    <v-avatar
      :size="size"
      tile
      class="pull-left my-auto">
      <img
        :src="avatarUrl"
        :class="avatarClass"
        class="object-fit-cover ma-auto">
      <v-img
        :src="avatarUrl"
        :height="size"
        :width="size"
        :max-height="size"
        :max-width="size"
        class="mx-auto"
        eager />
    </v-avatar>
    <div v-if="displayName || $slots.subTitle" class="pull-left text-truncate ms-2 d-flex">
      <p
        v-if="displayName"
        :class="fullnameStyle"
        class="text-truncate subtitle-2 my-auto">
        {{ displayName }}
      </p>
      <p v-if="$slots.subTitle" class="text-sub-title my-auto">
        <slot name="subTitle">
        </slot>
      </p>
    </div>
  </a>
</template>

<script>
const randomMax = 10000;

export default {
  props: {
    space: {
      type: Object,
      default: () => null,
    },
    boldTitle: {
      type: Boolean,
      default: () => false,
    },
    linkStyle: {
      type: Boolean,
      default: () => false,
    },
    size: {
      type: Number,
      // eslint-disable-next-line no-magic-numbers
      default: () => 37,
    },
    tiptip: {
      type: Boolean,
      default: () => true,
    },
    tiptipPosition: {
      type: String,
      default: function() {
        return null;
      },
    },
    labels: {
      type: Object,
      default: null,
    },
  },
  data() {
    return {
      id: `spaceAvatar${parseInt(Math.random() * randomMax)
        .toString()
        .toString()}`,
    };
  },
  computed: {
    spaceId() {
      return this.space && this.space.id;
    },
    displayName() {
      return this.space && this.space.displayName;
    },
    prettyName() {
      return this.space && this.space.prettyName;
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
    fullnameStyle() {
      return `${this.boldTitle && 'font-weight-bold ' || ''}${!this.linkStyle && 'text-color' || ''}`;
    },
  },
  mounted() {
    if (this.spaceId && this.groupId && this.tiptip) {
      if (!this.labels) {
        this.labels = this.$spacePopupLabels;
      }
      // TODO disable tiptip because of high CPU usage using its code
      this.initTiptip();
    }
  },
  methods: {
    initTiptip() {
      this.$nextTick(() => {
        $(`#${this.id}`).spacePopup({
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
          defaultPosition: this.tiptipPosition || 'left_bottom',
          maxWidth: '320px',
        });
      });
    },
  },
};
</script>
