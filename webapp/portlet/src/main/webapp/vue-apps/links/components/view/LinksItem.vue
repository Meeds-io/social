<template>
  <v-card
    :tile="type !== 'CARD'"
    :href="url"
    :target="target"
    :title="description"
    :class="isCard && 'flex-column' || 'flex-row'"
    :min-width="isCard && '90' || '10%'"
    class="d-flex align-center justify-center"
    flat>
    <v-avatar :size="iconSize">
      <img
        v-if="iconUrl"
        :src="iconUrl"
        width="auto"
        height="auto">
      <v-icon v-else :size="iconSize">fa-globe</v-icon>
    </v-avatar>
    <div
      v-if="showName"
      :class="isCard && 'mt-2' || 'ms-2'"
      class="text-truncate text-color">
      {{ name }}
    </div>
  </v-card>
</template>
<script>
export default {
  props: {
    link: {
      type: Object,
      default: null,
    },
    type: {
      type: String,
      default: null,
    },
    showName: {
      type: Boolean,
      default: false,
    },
    largeIcon: {
      type: Boolean,
      default: false,
    },
  },
  computed: {
    name() {
      return this.link?.name
          && (this.link?.name[this.$root.language] || this.link?.name[this.$root.defaultLanguage]);
    },
    description() {
      return this.link?.description
          && (this.link?.description[this.$root.language] || this.link?.description[this.$root.defaultLanguage]);
    },
    url() {
      return this.link?.url;
    },
    target() {
      return this.link?.sameTab && '_self' || '_blank';
    },
    iconUrl() {
      return this.link?.iconUrl;
    },
    iconSize() {
      return this.largeIcon && 40 || 34;
    },
    isCard() {
      return this.type === 'CARD';
    },
  },
};
</script>