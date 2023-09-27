<template>
  <v-tooltip bottom>
    <template #activator="{on, bind}">
      <v-card
        :tile="type !== 'CARD'"
        :href="url"
        :target="target"
        :class="isCard && 'flex-column align-center justify-center' || 'flex-row align-center justify-start'"
        :min-width="minWidth"
        :max-width="maxWidth"
        class="d-flex flex-shrink-1 overflow-hidden"
        flat
        v-on="on"
        v-bind="bind">
        <v-avatar
          :size="iconSize"
          tile>
          <img
            v-if="iconUrl"
            :src="iconUrl"
            :alt="name || description || ''"
            width="auto"
            height="auto">
          <v-icon v-else :size="iconSize">fa-globe</v-icon>
        </v-avatar>
        <div
          v-if="showName"
          :class="isCard && 'mt-2' || 'ms-2'"
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
      return this.link?.url?.replace?.('javascript:', '');
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
      return this.largeIcon && 40 || 34;
    },
    isCard() {
      return this.type === 'CARD';
    },
    isColumn() {
      return this.type === 'COLUMN';
    },
    minWidth() {
      return this.isCard && '90' || '10%';
    },
    maxWidth() {
      return this.isColumn && '100%' || `${parseInt(100 / this.count)}%`;
    },
  },
};
</script>