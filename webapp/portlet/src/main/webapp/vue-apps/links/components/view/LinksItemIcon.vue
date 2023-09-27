<template>
  <v-avatar
    :height="iconSize"
    :min-width="width"
    :width="width"
    tile>
    <v-img
      v-if="iconUrl"
      :src="iconUrl"
      :max-height="iconSize"
      :height="iconSize"
      :max-width="width"
      contain
      eager />
    <v-icon v-else :size="iconSize">fa-globe</v-icon>
  </v-avatar>
</template>
<script>
export default {
  props: {
    iconSize: {
      type: Number,
      default: () => 32,
    },
    iconUrl: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    iconWidthRatio: 1.3,
  }),
  computed: {
    width() {
      return this.iconSize * this.iconWidthRatio;
    },
  },
};
</script>