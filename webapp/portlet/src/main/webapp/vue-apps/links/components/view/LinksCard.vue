<template>
  <component
    v-bind="isCard && {
      hover: true,
      outlined: true,
    } || {
      text: true,
      class: 'white',
    }"
    :is="isCard && 'v-card' || 'v-btn'"
    :href="url"
    :target="target"
    :width="itemWidth"
    :height="itemHeight"
    class="mx-2">
    <v-card
      :title="description || name"
      :min-width="itemWidth"
      :max-width="itemWidth"
      :min-height="itemHeight"
      :max-height="itemHeight"
      class="d-flex flex-column full-height full-width transparent border-box-sizing align-center justify-start overflow-hidden text-none"
      flat>
      <links-icon
        :icon-size="iconSize"
        :icon-url="iconUrl"
        :class="showName && 'pb-0 col-6 align-end' || 'col-12 align-center'"
        class="justify-center subtitle-1" />
      <div
        v-if="showName && name"
        class="col-6 px-1 full-width text-truncate-2 text-color subtitle-2">
        {{ showName && name || '' }}
      </div>
    </v-card>
  </component>
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
  data: () => ({
    hover: false,
  }),
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
    itemSize() {
      return this.largeIcon && 150 || 135;
    },
    itemWidth() {
      return this.showName && this.itemSize || parseInt(this.itemSize / 2);
    },
    itemHeight() {
      return this.showName && this.itemSize || parseInt(this.itemSize / 2);
    },
    isCard() {
      return this.type === 'CARD';
    },
  },
};
</script>