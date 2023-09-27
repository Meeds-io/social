<template>
  <v-card
    v-if="links && links.length"
    :min-width="minWidth"
    class="d-flex flex-column pa-4"
    flat>
    <div v-if="header || seeMore" class="d-flex align-center">
      <div v-if="header" class="flex-grow-1 flex-shrink-1 text-truncate caption text-start">
        {{ header }}
      </div>
      <v-spacer />
      <div v-if="seeMore" class="flex-grow-0 flex-shrink-0 text-end">
        <v-btn
          :href="seeMore"
          target="_blank"
          color="primary"
          text
          x-small
          rel="nofollow noreferrer noopener">
          <span class="text-none">{{ $t('links.label.seeMore') }}</span>
        </v-btn>
      </div>
    </div>
    <v-card
      v-if="links && links.length"
      :class="`${parentFlexDirection} ${parentFlexAlign}`"
      :color="!isCard && 'transparent'"
      class="d-flex flex-nowrap mb-n2"
      flat>
      <links-item
        v-for="link in links"
        :key="link.id"
        :link="link"
        :type="type"
        :show-name="showName"
        :large-icon="largeIcon"
        :class="(isCard && 'mx-1') || (isColumn && 'ms-0 me-2') || 'mx-auto pe-2'"
        :count="links.length"
        class="py-2" />
    </v-card>
  </v-card>
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
    minWidth: {
      type: String,
      default: () => '33%',
    },
  },
  computed: {
    type() {
      return this.settings?.type || 'CARD';
    },
    showName() {
      return this.settings?.showName || false;
    },
    largeIcon() {
      return this.settings?.largeIcon || false;
    },
    isRow() {
      return this.type === 'ROW';
    },
    isColumn() {
      return this.type === 'COLUMN';
    },
    isCard() {
      return this.type === 'CARD';
    },
    header() {
      return this.settings?.header?.[this.$root.language] || this.settings?.header?.[this.$root.defaultLanguage];
    },
    seeMore() {
      return this.settings?.seeMore?.replace?.('javascript:', '');
    },
    parentFlexDirection() {
      return this.isColumn && 'flex-column' || 'flex-row';
    },
    parentFlexAlign() {
      return 'align-start justify-center';
    },
  },
};
</script>