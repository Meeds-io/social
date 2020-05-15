<template>
  <v-list-item class="py-2 likerItem">
    <v-list-item-avatar :size="avatarSize">
      <v-img :src="liker.personLikeAvatarImageSource" class="likerAvatar"></v-img>
    </v-list-item-avatar>
    <v-list-item-content class="pb-3">
      <v-list-item-title class="body-2 font-weight-bold text-color">
        <a
          class="text-color">
          {{ liker.personLikeFullName }}
        </a>
      </v-list-item-title>
    </v-list-item-content>
  </v-list-item>
</template>
<script>
export default {
  props: {
    liker: {
      type: Object,
      default: () => null,
    },
    avatarSize: {
      type: Number,
      default: () => 34,
    },
  },
};
</script>