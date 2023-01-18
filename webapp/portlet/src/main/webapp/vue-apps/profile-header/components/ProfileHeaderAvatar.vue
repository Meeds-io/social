<template>
  <v-avatar
    :class="owner && hover && 'profileHeaderAvatarHoverEdit'"
    :size="size"
    class="align-start flex-grow-0 ms-3 my-3 profileHeaderAvatar">
    <v-img
      :lazy-src="(avatarData || user && `${user.avatar}&size=165x165`) || ''"
      :src="(avatarData || user && `${user.avatar}&size=165x165`) || ''"
      transition="none"
      eager
      role="presentation" />
    <v-btn
      v-if="owner"
      v-show="hover"
      ref="avatarInput"
      class="changeAvatarButton"
      icon
      dark
      @click="$emit('edit')">
      <v-icon size="18">fas fa-camera</v-icon>
    </v-btn>
  </v-avatar>
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