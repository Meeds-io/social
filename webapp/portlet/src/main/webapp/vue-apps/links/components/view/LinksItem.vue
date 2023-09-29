<template>
  <v-tooltip bottom>
    <template #activator="{on, bind}">
      <v-card
        :tile="type !== 'CARD'"
        :href="url"
        :target="target"
        :class="isCard && 'flex-column align-center justify-center' || 'flex-row align-center justify-start'"
        class="d-flex overflow-hidden"
        flat
        v-on="on"
        v-bind="bind">
        <links-item-icon
          :icon-size="iconSize"
          :icon-url="iconUrl" />
        <div
          v-if="showName"
          :class="nameClass"
          class="text-truncate-2 text-color">
          {{ name }}
        </div>
      </v-card>
    </template>
    <span>{{ description || name }}</span>
  </v-tooltip>
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
    count: {
      type: Number,
      default: 1,
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
    isCard() {
      return this.type === 'CARD';
    },
    isColumn() {
      return this.type === 'COLUMN';
    },
    isRow() {
      return this.type === 'ROW';
    },
    nameClass() {
      return (this.isRow && 'ms-2') || (this.isCard && 'mt-4') || (this.isColumn && 'text-start ms-4');
    },
  },
};
</script>