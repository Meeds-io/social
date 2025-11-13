<template>
  <div class="d-flex">
    <v-avatar
      id="profileAvatar"
      :class="{ 'profileHeaderAvatarHoverEdit': owner && hover }"
      :size="size"
      :min-width="minSize"
      :min-height="minSize"
      :max-width="maxSize"
      :max-height="maxSize"
      class="align-start flex-grow-0 border-color profileHeaderAvatar">
      <v-img
        :lazy-src="userAvatarUrl"
        :src="userAvatarUrl"
        :min-width="minSize"
        :min-height="minSize"
        :max-width="maxSize"
        :max-height="maxSize"
        id="profileAvatarImg"
        transition="none"
        role="presentation"
        eager />
      <v-btn
        v-if="owner"
        v-show="hover"
        ref="avatarInput"
        :title="$t('UIChangeAvatarContainer.label.ChangeAvatar')"
        id="profileAvatarEditButton"
        class="changeAvatarButton"
        icon
        outlined
        dark
        @click="$emit('edit')">
        <v-icon size="18">fas fa-camera</v-icon>
      </v-btn>
    </v-avatar>
    <slot></slot>
  </div>
</template>

<script>
export default {
  props: {
    user: {
      type: Object,
      default: () => null,
    },
    avatarData: {
      type: Array,
      default: null,
    },
    owner: {
      type: Boolean,
      default: () => true,
    },
    hover: {
      type: Boolean,
      default: () => false,
    },
    size: {
      type: String,
      default: () => '15vw',
    },
    minSize: {
      type: String,
      default: null
    },
    maxSize: {
      type: String,
      default: null
    }
  },
  computed: {
    userAvatarUrl() {
      const dimension = `${this.maxSize}x${this.maxSize}`;
      const baseUrl = `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users`;
      const defaultAvatarUrl = `${baseUrl}/default-image/avatar?size=${dimension}`;

      if (!this.user?.enabled) {
        return defaultAvatarUrl;
      }
      if (this.avatarData) {
        return this.avatarData;
      }

      if (this.user.avatar) {
        const separator = this.user.avatar.includes('?') ? '&' : '?';
        return `${this.user.avatar}${separator}size=${dimension}`;
      }

      return `${baseUrl}/${this.user.username}/avatar?size=${dimension}`;
    }
  }
};
</script>
