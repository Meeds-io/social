<template>
  <v-avatar
    :class="hover && 'spaceAvatarHoverEdit'"
    :size="size"
    class="spaceAvatar">
    <v-img :src="avatarData || avatarUrl || ''" role="presentation" />
    <v-btn
      v-show="hover"
      id="SpaceBannerEditButton"
      ref="avatarInput"
      class="changeAvatarButton mb-1"
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
    avatarUrl: {
      type: Object,
      default: null,
    },
    avatarData: {
      type: Array,
      default: null,
    },
    hover: {
      type: Boolean,
      default: false,
    },
    size: {
      type: Number,
      default: () => 165,
    },
  },
};
</script>