<template>
  <div
    v-if="popover && show"
    v-identity-popover="popoverIdentity"
    class="profile-popover user-wrapper text-truncate"
    :class="parentClass">
    <component
      :is="clickable && 'v-btn' || 'a'"
      v-if="avatar"
      :id="id"
      :aria-label="$t('popover.userAvatar.title',{0:userFullname})"
      class="flex-nowrap flex-grow-1 d-flex text-truncate container--fluid"
      :class="componentClass"
      :depressed="clickable"
      :fab="clickable"
      :href="profileUrl"
      @click="clickable && $emit('avatar-click', $event)">
      <v-avatar
        class="ma-0 flex-shrink-0"
        :class="[avatarClass, compact && 'border-white content-box-sizing']"
        :size="size">
        <img
          alt=""
          class="object-fit-cover ma-auto"
          loading="lazy"
          :src="userAvatarUrl">
      </v-avatar>
    </component>
    <component
      :is="clickable && 'v-btn' || 'a'"
      v-else-if="fullname"
      :id="id"
      class="d-flex align-start text-truncate"
      :class="componentClass"
      :depressed="clickable"
      :fab="clickable"
      :href="profileUrl"
      @click="clickable && $emit('avatar-click', $event)">
      <span
        v-if="userFullname"
        class="text-truncate my-auto"
        :class="[fullnameStyle, linkStyle && 'primary--text' || '', textStyle && 'text-color' || '']">
        {{ userFullname }}
        <span
          v-if="!enabled"
          class="muted font-weight-regular"
          :title="$t('label.disabled')">
          <v-icon
            class="primary--text mb-1">
            fas fa-user-slash
          </v-icon>
        </span>
        <span
          v-if="isExternal"
          class="muted font-weight-regular">{{ externalTag }} </span>
      </span>
      <span
        v-if="$slots.subTitle"
        class="text-subtitle text-truncate my-auto text-left">
        <slot name="subTitle"></slot>
      </span>
      <span
        v-else-if="displayPosition && userPosition"
        class="text-subtitle text-truncate my-auto text-left">
        {{ userPosition }}
      </span>
    </component>
    <component
      :is="clickable && 'v-btn' || 'a'"
      v-else
      :id="id"
      :aria-label="$t('popover.userAvatar.title',{0:userFullname})"
      class="d-flex flex-nowrap flex-grow-1 text-truncate container--fluid"
      :class="componentClass"
      :depressed="clickable"
      :fab="clickable"
      :href="profileUrl"
      @click="clickable && $emit('avatar-click', $event)">
      <v-avatar
        class="ma-0"
        :class="avatarClass"
        :size="size">
        <img
          alt=""
          class="object-fit-cover ma-auto"
          loading="lazy"
          :src="userAvatarUrl">
      </v-avatar>
      <div
        v-if="userFullname || $slots.subTitle"
        class="ms-2 my-auto overflow-hidden">
        <p
          v-if="userFullname"
          class="text-truncate text-left mb-0"
          :class="[fullnameStyle, linkStyle && 'primary--text' || '', textStyle && 'text-color' || '']">
          {{ userFullname }}
          <span
            v-if="!enabled"
            class="muted font-weight-regular"
            :title="$t('label.disabled')">
            <v-icon
              class="primary--text mb-1">
              fas fa-user-slash
            </v-icon>
          </span>
          <span
            v-if="isExternal"
            class="muted font-weight-regular">{{ externalTag }} </span>
        </p>
        <p
          v-if="$slots.subTitle"
          class="text-subtitle text-truncate text-left mb-0">
          <slot name="subTitle"></slot>
        </p>
        <p
          v-else-if="displayPosition && userPosition"
          class="text-subtitle text-truncate text-left mb-0">
          {{ userPosition }}
        </p>
      </div>
      <template v-if="$slots.actions">
        <slot name="actions"></slot>
      </template>
    </component>
  </div>
  <div
    v-else-if="show"
    class="profile-popover user-wrapper text-truncate"
    :class="parentClass">
    <component
      :is="clickable && 'v-btn' || 'a'"
      v-if="avatar"
      :id="id"
      :aria-label="$t('popover.userAvatar.title',{0:userFullname})"
      class="flex-nowrap flex-grow-1 d-flex text-truncate container--fluid"
      :class="componentClass"
      :depressed="clickable"
      :fab="clickable"
      :href="profileUrl"
      @click="clickable && $emit('avatar-click', $event)">
      <v-avatar
        class="ma-0 flex-shrink-0"
        :class="[avatarClass, compact && 'border-white content-box-sizing']"
        :size="size">
        <img
          alt=""
          class="object-fit-cover ma-auto"
          loading="lazy"
          :src="userAvatarUrl">
      </v-avatar>
    </component>
    <component
      :is="clickable && 'v-btn' || 'a'"
      v-else-if="fullname"
      :id="id"
      class="d-flex align-start text-truncate"
      :class="componentClass"
      :depressed="clickable"
      :fab="clickable"
      :href="profileUrl"
      @click="clickable && $emit('avatar-click', $event)">
      <span
        v-if="userFullname"
        class="text-truncate my-auto"
        :class="[fullnameStyle, linkStyle && 'primary--text' || '', textStyle && 'text-color' || '']">
        {{ userFullname }}
        <span
          v-if="!enabled"
          class="muted font-weight-regular"
          :title="$t('label.disabled')">
          <v-icon
            class="primary--text mb-1">
            fas fa-user-slash
          </v-icon>
        </span>
        <span
          v-if="isExternal"
          class="muted font-weight-regular">{{ externalTag }} </span>
      </span>
      <span
        v-if="$slots.subTitle"
        class="text-subtitle text-truncate my-auto text-left">
        <slot name="subTitle"></slot>
      </span>
      <span
        v-else-if="displayPosition && userPosition"
        class="text-subtitle text-truncate my-auto text-left">
        {{ userPosition }}
      </span>
    </component>
    <component
      :is="clickable && 'v-btn' || 'a'"
      v-else
      :id="id"
      :aria-label="$t('popover.userAvatar.title',{0:userFullname})"
      class="d-flex flex-nowrap flex-grow-1 text-truncate container--fluid"
      :class="componentClass"
      :depressed="clickable"
      :fab="clickable"
      :href="profileUrl"
      @click="clickable && $emit('avatar-click', $event)">
      <v-avatar
        class="ma-0"
        :class="[avatarClass, compact && 'border-white content-box-sizing']"
        :size="size">
        <img
          alt=""
          class="object-fit-cover ma-auto"
          loading="lazy"
          :src="userAvatarUrl">
      </v-avatar>
      <div
        v-if="userFullname || $slots.subTitle"
        class="ms-2 overflow-hidden">
        <p
          v-if="userFullname"
          class="text-truncate text-left mb-0"
          :class="[fullnameStyle, linkStyle && 'primary--text' || '', textStyle && 'text-color' || '']">
          {{ userFullname }}
          <span
            v-if="!enabled"
            class="muted font-weight-regular"
            :title="$t('label.disabled')">
            <v-icon
              class="primary--text mb-1">
              fas fa-user-slash
            </v-icon>
          </span>
          <span
            v-if="isExternal"
            class="muted font-weight-regular">{{ externalTag }} </span>
        </p>
        <p
          v-if="$slots.subTitle"
          class="text-subtitle text-truncate text-left mb-0">
          <slot name="subTitle"></slot>
        </p>
        <p
          v-else-if="displayPosition && userPosition"
          class="text-subtitle text-truncate text-left mb-0">
          {{ userPosition }}
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
      textStyle: {
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
      showDisabledUser: {
        type: Boolean,
        default: () => true,
      },
      displayPosition: {
        type: Boolean,
        default: false,
      },
    },
    data () {
      return {
        id: `userAvatar${parseInt(Math.random() * randomMax)
          .toString()
          .toString()}`,
        retrievedIdentity: null,
        showAnimation: false,
      };
    },
    computed: {
      show () {
        return this.showDisabledUser || this.enabled;
      },
      userIdentity () {
        return this.retrievedIdentity || (this.identity && JSON.parse(JSON.stringify(this.identity)));
      },
      identityId () {
        return this.userIdentity?.id;
      },
      username () {
        return this.userIdentity?.username ||  this.userIdentity?.userName || this.profileId;
      },
      enabled () {
        return this.userIdentity?.enabled;  
      },  
      deleted () {
        return this.userIdentity?.deleted;
      },
      userFullname () {
        return this.userIdentity?.fullname || this.name;
      },
      primaryProperty () {
        return this.userIdentity?.primaryProperty;
      },
      userAvatarUrl () {
        return this.userIdentity?.enabled ? (this.userIdentity.avatar || this.avatarUrl || `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/${this.username || this.profileId}/avatar`) : (this.avatarUrl  || `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/default-image/avatar`);
      },
      userPosition () {
        return this.userIdentity?.position;
      },
      profileUrl () {
        if (this.url && !this.clickable && this.username) {
          return `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/profile/${this.username}`;
        } else {
          return null;
        }
      },
      isExternal () {
        return this.userIdentity?.external === 'true';
      },
      externalTag () {
        return `( ${this.$t('userAvatar.external.label')} )`;
      },
      fullnameStyle () {
        return `${this.boldTitle && 'font-weight-bold ' || ''}${this.smallFontSize && 'caption ' || ''}`;
      },
      itemsAlignStyle () {
        return `${this.alignTop && 'align-start' || 'align-center'}`;
      },
      parentClass () {
        return `${this.avatar && `${this.extraClass} flex-shrink-0 mx-1` || this.extraClass || ''} ${this.marginLeft}`;
      },
      isMobile () {
        return eXo.vuetify.display.name.value === 'xs' || eXo.vuetify.display.name.value === 'sm';
      },
      isCurrentUser () {
        return eXo.env.portal.userName !== this.username;
      },
      params () {
        return {
          identityType: 'USER_PROFILE',
          identityId: this.username,
        };
      },
      popoverIdentity () {
        return {
          id: this.identityId,
          username: this.username,
          enabled: this.enabled,
          deleted: this.deleted,       
          fullName: this.userFullname,
          primaryProperty: this.primaryProperty,
          avatar: this.userAvatarUrl,
          external: this.isExternal,
          allowAnimation: this.compact && this.allowAnimation,
        };
      },
      componentClass () {
        return `${this.clickable && 'width-auto height-auto' || ''} ${this.fullname ? '' : (!this.avatar && this.itemsAlignStyle || '')}`;
      },
      mustRetrieveIdentity () {
        return !this.identity
          || !this.identityId
          || !this.username
          || !this.userFullname
          || !Object.hasOwn(this.identity, 'avatar')
          || !Object.hasOwn(this.identity, 'enabled')
          || !Object.hasOwn(this.identity, 'deleted')
          || !Object.hasOwn(this.identity, 'primaryProperty')
          || !Object.hasOwn(this.identity, 'external');
      },
    },
    watch: {
      identity (newVal, oldVal) {
        if (!newVal
          || !oldVal
          || JSON.stringify(newVal) !== JSON.stringify(oldVal)) {
          this.retrievedIdentity = null;
        }
      },
    },
    created () {
      if (this.username && this.mustRetrieveIdentity) {
        eXo.$userService.getUser(this.username)
          .then(user => this.retrievedIdentity = user && JSON.parse(JSON.stringify(user)));
      }
    },
  };
</script>
