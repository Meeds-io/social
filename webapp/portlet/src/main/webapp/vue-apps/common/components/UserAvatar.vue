<template>
  <div
    v-if="popover"
    v-identity-popover="popoverIdentity"
    class="profile-popover user-wrapper text-truncate"
    :class="parentClass">
    <component
      v-if="avatar"
      :is="clickable && 'v-btn' || 'a'"
      :id="id"
      :fab="clickable"
      :depressed="clickable"
      :href="profileUrl"
      :aria-label="$t('popover.userAvatar.title',{0:userFullname})"
      :class="componentClass"
      class="flex-nowrap flex-grow-1 d-flex text-truncate container--fluid"
      @click="clickable && $emit('avatar-click', $event)">
      <v-avatar
        :size="size"
        :class="[avatarClass, compact && 'border-white content-box-sizing']"
        class="ma-0 flex-shrink-0">
        <img
          :src="userAvatarUrl"
          class="object-fit-cover ma-auto"
          loading="lazy"
          alt="">
      </v-avatar>
    </component>
    <component
      v-else-if="fullname"
      :is="clickable && 'v-btn' || 'a'"
      :id="id"
      :fab="clickable"
      :depressed="clickable"
      :href="profileUrl"
      :class="componentClass"
      class="d-flex align-start text-truncate"
      @click="clickable && $emit('avatar-click', $event)">
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
    </component>
    <component
      v-else
      :is="clickable && 'v-btn' || 'a'"
      :id="id"
      :fab="clickable"
      :depressed="clickable"
      :href="profileUrl"
      :aria-label="$t('popover.userAvatar.title',{0:userFullname})"
      :class="componentClass"
      class="d-flex flex-nowrap flex-grow-1 text-truncate container--fluid"
      @click="clickable && $emit('avatar-click', $event)">
      <v-avatar
        :size="size"
        :class="avatarClass"
        class="ma-0">
        <img
          :src="userAvatarUrl"
          class="object-fit-cover ma-auto"
          loading="lazy"
          alt="">
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
    </component>
  </div>
  <div 
    v-else
    class="profile-popover user-wrapper text-truncate"
    :class="parentClass">
    <component 
      v-if="avatar"
      :is="clickable && 'v-btn' || 'a'"
      :id="id"
      :fab="clickable"
      :depressed="clickable"
      :href="profileUrl"
      :aria-label="$t('popover.userAvatar.title',{0:userFullname})"
      :class="componentClass"
      class="flex-nowrap flex-grow-1 d-flex text-truncate container--fluid"
      @click="clickable && $emit('avatar-click', $event)">
      <v-avatar
        :size="size"
        :class="[avatarClass, compact && 'border-white content-box-sizing']"
        class="ma-0 flex-shrink-0">
        <img
          :src="userAvatarUrl"
          class="object-fit-cover ma-auto"
          loading="lazy"
          alt="">
      </v-avatar>
    </component>
    <component 
      v-else-if="fullname"
      :is="clickable && 'v-btn' || 'a'"
      :id="id"
      :fab="clickable"
      :depressed="clickable"
      :href="profileUrl"
      :class="componentClass"
      class="d-flex align-start text-truncate"
      @click="clickable && $emit('avatar-click', $event)">
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
    </component>
    <component
      v-else
      :is="clickable && 'v-btn' || 'a'"
      :id="id"
      :fab="clickable"
      :depressed="clickable"
      :href="profileUrl"
      :aria-label="$t('popover.userAvatar.title',{0:userFullname})"
      :class="componentClass"
      class="d-flex flex-nowrap flex-grow-1 text-truncate container--fluid"
      @click="clickable && $emit('avatar-click', $event)">
      <v-avatar
        :size="size"
        :class="[avatarClass, compact && 'border-white content-box-sizing']"
        class="ma-0">
        <img
          :src="userAvatarUrl"
          class="object-fit-cover ma-auto"
          loading="lazy"
          alt="">
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
    </component>
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
    clickable: {
      type: Boolean,
      default: () => false,
    },
    profileId: {
      type: String,
      default: () => null,
    },
    avatarUrl: {
      type: String,
      default: null,
    },
    name: {
      type: String,
      default: null,
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
    marginLeft: {
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
    compact: {
      type: Boolean,
      default: () => false,
    },
    allowAnimation: {
      type: Boolean,
      default: () => false,
    },
  },
  data() {
    return {
      id: `userAvatar${parseInt(Math.random() * randomMax)
        .toString()
        .toString()}`,
      retrievedIdentity: null,
      showAnimation: false
    };
  },
  computed: {
    userIdentity() {
      return this.retrievedIdentity || this.identity;
    },
    identityId() {
      return this.userIdentity?.id;
    },
    username() {
      return this.userIdentity?.username ||  this.userIdentity?.userName || this.profileId;
    },
    enabled() {
      return this.userIdentity?.enabled;  
    },  
    deleted() {
      return this.userIdentity?.deleted;
    },
    userFullname() {
      return this.userIdentity?.fullname || this.name;
    },
    position() {
      return this.userIdentity?.position;
    },
    userAvatarUrl() {
      return this.userIdentity?.enabled ? (this.userIdentity.avatar || this.avatarUrl || `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/${this.username || this.profileId}/avatar`) : (this.avatarUrl  || `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/default-image/avatar`);
    },
    profileUrl() {
      if (this.url && !this.clickable && this.username) {
        return `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/profile/${this.username}`;
      } else {
        return null;
      }
    },
    isExternal() {
      return this.userIdentity?.external === 'true';
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
    parentClass() {
      return `${this.avatar && `${this.extraClass} flex-shrink-0 mx-1` || this.extraClass || ''} ${this.marginLeft}`;
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
    popoverIdentity() {
      return {
        id: this.identityId,
        username: this.username,
        enabled: this.enabled,
        deleted: this.deleted,       
        fullName: this.userFullname,
        position: this.position,
        avatar: this.userAvatarUrl,
        external: this.isExternal,
        allowAnimation: this.compact && this.allowAnimation,
      };
    },
    componentClass() {
      return `${this.clickable && 'width-auto height-auto' || ''} ${this.fullname ? '' : (!this.avatar && this.itemsAlignStyle || '')}`;
    },
    mustRetrieveIdentity() {
      return !this.identity
          || !this.identityId
          || !this.username
          || !this.userFullname
          || !this.identity.avatar
          || !this.identity.hasOwnProperty('enabled')
          || !this.identity.hasOwnProperty('deleted')
          || !this.identity.hasOwnProperty('position')
          || !this.identity.hasOwnProperty('external');
    },
  },
  created() {
    if (this.username && this.mustRetrieveIdentity) {
      this.$userService.getUser(this.username)
        .then(user => this.retrievedIdentity = user);
    }
  },
  watch: {
    identity() {
      this.retrievedIdentity = null;
    }
  },
};
</script>
