<template>
  <div class="d-flex">
    <!-- the camera button shows, and the photo darkens behind it, on the photo's hover or the button's keyboard focus -->
    <v-hover v-slot="{ hover: avatarHover }">
      <v-avatar
        id="profileAvatar"
        :class="{ 'profileHeaderAvatarHoverEdit': owner && (avatarHover || focused) }"
        :size="size"
        :min-width="minSize"
        :min-height="minSize"
        :max-width="maxSize"
        :max-height="maxSize"
        class="align-start flex-grow-0 border-color profileHeaderAvatar hover-reveal-parent">
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
          ref="avatarInput"
          :title="$t('UIChangeAvatarContainer.label.ChangeAvatar')"
          :aria-label="$t('UIChangeAvatarContainer.label.ChangeAvatar')"
          id="profileAvatarEditButton"
          class="changeAvatarButton hover-reveal"
          icon
          outlined
          dark
          @click="$emit('edit')"
          @focus="focused = $event.target.matches(':focus-visible')"
          @blur="focused = false">
          <v-icon size="18">fas fa-camera</v-icon>
        </v-btn>
      </v-avatar>
    </v-hover>
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
  data: () => ({
    focused: false,
  }),
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
