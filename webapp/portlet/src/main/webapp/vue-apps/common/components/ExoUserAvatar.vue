<template>
  <div 
    v-if="popover"
    v-identity-popover="userIdentity"
    class="profile-popover user-wrapper"
    :class="extraClass">
    <a 
      v-if="avatar"
      :id="id"
      :href="profileUrl"
      class="flex-nowrap flex-grow-1 d-flex text-truncate container--fluid"
      :class="avatarClass">
      <v-avatar
        :size="size"
        :class="pullLeft"
        class="ma-0">
        <img
          :src="avatarUrl"
          class="object-fit-cover ma-auto"
          loading="lazy"
          role="presentation">
      </v-avatar>
    </a>
    <a 
      v-else-if="fullname"
      :id="id"
      :href="profileUrl"
      :class="pullLeft"
      class="d-flex align-start text-truncate">
      <span
        v-if="userFullname"
        :class="[fullnameStyle, linkStyle && 'primary--text' || '']"
        class="text-truncate subtitle-2 my-auto">
        {{ userFullname }}
        <span v-if="isExternal" class="muted font-weight-regular">{{ externalTag }} </span>
      </span>
      <span v-if="$slots.subTitle" class="text-sub-title text-truncate my-auto text-left">
        <slot name="subTitle"></slot>
      </span>
    </a>
    <a
      v-else
      :id="id"
      :href="profileUrl"
      class="d-flex flex-nowrap flex-grow-1 text-truncate container--fluid"
      :class="itemsAlignStyle">
      <v-avatar
        :size="size"
        class="ma-0">
        <img
          :src="avatarUrl"
          class="object-fit-cover ma-auto"
          loading="lazy"
          role="presentation">
      </v-avatar>
      <div v-if="userFullname || $slots.subTitle" class="ms-2 overflow-hidden">
        <p
          v-if="userFullname"
          :class="[fullnameStyle, linkStyle && 'primary--text' || '']"
          class="text-truncate subtitle-2 text-left mb-0">
          {{ userFullname }}
          <span v-if="isExternal" class="muted font-weight-regular">{{ externalTag }} </span>
        </p>
        <p v-if="$slots.subTitle" class="text-sub-title  text-truncate text-left mb-0">
          <slot name="subTitle"></slot>
        </p>
      </div>
      <template v-if="$slots.actions">
        <slot name="actions"></slot>
      </template>
    </a>
  </div>
  <div 
    v-else
    class="profile-popover user-wrapper"
    :class="extraClass">
    <a 
      v-if="avatar"
      :id="id"
      :href="profileUrl"
      class="flex-nowrap flex-grow-1 d-flex text-truncate container--fluid"
      :class="avatarClass">
      <v-avatar
        :size="size"
        :class="pullLeft"
        class="ma-0">
        <img
          :src="avatarUrl"
          class="object-fit-cover ma-auto"
          loading="lazy"
          role="presentation">
      </v-avatar>
    </a>
    <a 
      v-else-if="fullname"
      :id="id"
      :href="profileUrl"
      :class="pullLeft"
      class="d-flex align-start text-truncate">
      <span
        v-if="userFullname"
        :class="[fullnameStyle, linkStyle && 'primary--text' || '']"
        class="text-truncate subtitle-2 my-auto">
        {{ userFullname }}
        <span v-if="isExternal" class="muted font-weight-regular">{{ externalTag }} </span>
      </span>
      <span v-if="$slots.subTitle" class="text-sub-title text-truncate my-auto text-left">
        <slot name="subTitle"></slot>
      </span>
    </a>
    <a
      v-else
      :id="id"
      :href="profileUrl"
      class="d-flex flex-nowrap flex-grow-1 text-truncate container--fluid"
      :class="itemsAlignStyle">
      <v-avatar
        :size="size"
        class="ma-0">
        <img
          :src="avatarUrl"
          class="object-fit-cover ma-auto"
          loading="lazy"
          role="presentation">
      </v-avatar>
      <div v-if="userFullname || $slots.subTitle" class="ms-2 overflow-hidden">
        <p
          v-if="userFullname"
          :class="[fullnameStyle, linkStyle && 'primary--text' || '']"
          class="text-truncate subtitle-2 text-left mb-0">
          {{ userFullname }}
          <span v-if="isExternal" class="muted font-weight-regular">{{ externalTag }} </span>
        </p>
        <p v-if="$slots.subTitle" class="text-sub-title  text-truncate text-left mb-0">
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
    identity: {
      type: Object,
      default: () => null,
    },
    profileId: {
      type: String,
      default: () => null,
    },
    avatar: {
      type: Boolean,
      default: () => false,
    },
    fullname: {
      type: Boolean,
      default: () => false,
    },
    boldTitle: {
      type: Boolean,
      default: () => false,
    },
    linkStyle: {
      type: Boolean,
      default: () => false,
    },
    smallFontSize: {
      type: Boolean,
      default: () => false,
    },
    alignTop: {
      type: Boolean,
      default: () => false,
    },
    popover: {
      type: Boolean,
      default: () => true,
    },
    popoverLeftPosition: {
      type: Boolean,
      default: () => false,
    },
    url: {
      type: Boolean,
      default: () => true,
    },
    size: {
      type: Number,
      // eslint-disable-next-line no-magic-numbers
      default: () => 37,
    },
    extraClass: {
      type: String,
      default: () => '',
    },
    usernameClass: {
      type: String,
      default: () => '',
    },
    avatarClass: {
      type: String,
      default: () => '',
    },
    offsetX: {
      type: Boolean,
      default: () => false,
    },
    offsetY: {
      type: Boolean,
      default: () => true,
    },
  },
  data() {
    return {
      id: `userAvatar${parseInt(Math.random() * randomMax)
        .toString()
        .toString()}`,
      retrievedIdentity: null,
    };
  },

  computed: {
    identityId() {
      return this.identity?.id || this.retrievedIdentity?.id;
    },
    username() {
      return this.identity?.username || this.retrievedIdentity?.username || this.profileId;
    },
    enabled() {
      return this.identity?.enabled || this.retrievedIdentity?.enabled;  
    },  
    deleted() {
      return this.identity?.deleted || this.retrievedIdentity?.deleted;
    },    
    userFullname() {
      return this.identity?.fullname || this.retrievedIdentity?.fullname;
    },
    position() {
      return this.identity?.position || this.retrievedIdentity?.position;
    },
    avatarUrl() {
      return this.identity?.avatar || this.retrievedIdentity?.avatar || `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/${this.username ? this.username : this.profileId}/avatar`;
    },
    profileUrl() {
      if ( this.url ) {
        return `${eXo.env.portal.context}/${eXo.env.portal.portalName}/profile/${this.username}`;
      } else {
        return null;
      }
    },
    isExternal() {
      return this.identity?.external === 'true' || this.retrievedIdentity?.external === 'true';
    },
    externalTag() {
      return `( ${this.$t('userAvatar.external.label')} )`;
    },
    fullnameStyle() {
      return `${this.boldTitle && 'font-weight-bold ' || ''}${this.smallFontSize && 'caption ' || ''}`;
    },
    itemsAlignStyle() {
      return `${this.alignTop && 'align-start' || 'align-center'}`;
    },
    isMobile() {
      return this.$vuetify.breakpoint.name === 'xs' || this.$vuetify.breakpoint.name === 'sm';
    },
    isCurrentUser() {
      return eXo.env.portal.userName !== this.username;
    },
    params() {
      return {
        identityType: 'USER_PROFILE',
        identityId: this.username,        
      };
    },
    userIdentity() {
      return {
        id: this.identityId,
        username: this.username,
        enabled: this.enabled,
        deleted: this.deleted,       
        fullName: this.userFullname,
        position: this.position,
        avatar: this.avatarUrl,
        external: this.isExternal,
      };
    },
    pullLeft() {
      return this.isMobile && ' ' || 'pull-left';
    }
  },
  watch: {
    profileId() {
      if (this.profileId) {
        this.retrieveIdentity();
      }
    }
  },
  created() {
    if (this.profileId) {
      this.retrieveIdentity();
    }
  },
  methods: {
    retrieveIdentity() {
      this.$userService.getUser(this.profileId)
        .then(user => this.retrievedIdentity = user);
    }
  },
};
</script>
