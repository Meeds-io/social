<template>
  <component
    :is="list && 'v-list-item-avatar' || 'v-avatar'"
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
    <v-icon
      v-else
      :size="iconSize"
      class="icon-default-color">
      fa-globe
    </v-icon>
  </component>
</template>
<script>
export default {
  props: {
    iconSize: {
      type: Number,
      default: () => 30,
    },
    iconUrl: {
      type: String,
      default: null,
    },
    list: {
      type: Boolean,
      default: false,
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