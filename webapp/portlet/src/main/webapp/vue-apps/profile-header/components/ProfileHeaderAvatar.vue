<template>
  <div class="d-flex">
    <v-avatar
      :class="owner && hover && 'profileHeaderAvatarHoverEdit'"
      :size="size"
      id="profileAvatar"
      min-width="44"
      min-height="44"
      max-width="165"
      max-height="165"
      class="align-start flex-grow-0 border-color profileHeaderAvatar">
      <v-img
        :lazy-src="(avatarData || user && `${user.avatar}&size=165x165`) || ''"
        :src="(avatarData || user && `${user.avatar}&size=165x165`) || ''"
        id="profileAvatarImg"
        transition="none"
        min-width="44"
        min-height="44"
        max-width="165"
        max-height="165"
        eager
        role="presentation" />
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
      type: Number,
      default: () => 165,
    },
  },
};
</script>