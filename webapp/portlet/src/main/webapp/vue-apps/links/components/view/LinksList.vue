<template>
  <component
    v-if="links?.length"
    :is="isColumn && 'v-list' || 'card-carousel'"
    v-bind="isColumn && {
      dense: !largeIcon
    }">
    <component
      v-for="link in links"
      :key="link.id"
      :is="componentName"
      :link="link"
      :type="type"
      :large-icon="largeIcon"
      :show-name="showName"
      :show-description="showDescription" />
  </component>
</template>
<script>
export default {
  props: {
    settings: {
      type: Object,
      default: null,
    },
    links: {
      type: Array,
      default: null,
    },
  },
  computed: {
    type() {
      return this.settings?.type || 'CARD';
    },
    showName() {
      return this.settings?.showName || false;
    },
    showDescription() {
      return this.settings?.showDescription || false;
    },
    largeIcon() {
      return this.settings?.largeIcon || false;
    },
    header() {
      return this.settings?.header?.[this.$root.language] || this.settings?.header?.[this.$root.defaultLanguage];
    },
    seeMoreUrl() {
      return this.$linkService.toLinkUrl(this.settings?.seeMore);
    },
    isColumn() {
      return this.type === 'COLUMN';
    },
    componentName() {
      return this.isColumn && 'links-column' || 'links-card';
    },
  },
};
</script>