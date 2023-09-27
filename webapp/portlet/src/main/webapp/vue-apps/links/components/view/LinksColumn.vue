<template>
  <v-list-item
    :title="description || name"
    :href="url"
    :target="target"
    :dense="!largeIcon"
    class="px-0"
    flat>
    <links-icon
      :icon-size="iconSize"
      :icon-url="iconUrl"
      list />
    <v-list-item-content v-if="showName || showDescription">
      <v-list-item-title
        v-if="showName && name"
        class="text-color text-start text-wrap subtitle-1"
        :class="showDescription && description && 'text-truncate' || 'text-truncate-2'">
        {{ name }}
      </v-list-item-title>
      <v-list-item-subtitle
        v-if="showDescription && description"
        :class="largeIcon && 'text-truncate-2' || 'text-truncate'"
        class="text-start text-wrap subtitle-2">
        {{ description }}
      </v-list-item-subtitle>
    </v-list-item-content>
  </v-list-item>
</template>
<script>
export default {
  props: {
    link: {
      type: Object,
      default: null,
    },
    showName: {
      type: Boolean,
      default: false,
    },
    showDescription: {
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
      return this.link?.name?.[this.$root.language] || this.link?.name?.[this.$root.defaultLanguage];
    },
    description() {
      return this.link?.description?.[this.$root.language] || this.link?.description?.[this.$root.defaultLanguage];
    },
    url() {
      return this.$linkService.toLinkUrl(this.link?.url);
    },
    target() {
      return this.link?.sameTab && '_self' || '_blank';
    },
    iconUrl() {
      if (this.link?.iconSrc) {
        return this.$utils.convertImageDataAsSrc(this.link.iconSrc);
      } else {
        return this.link?.iconUrl;
      }
    },
    iconSize() {
      return this.largeIcon && 48 || 30;
    },
  },
};
</script>