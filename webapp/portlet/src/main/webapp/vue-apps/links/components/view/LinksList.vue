<template>
  <v-card
    v-if="links && links.length"
    :min-width="minWidth"
    class="d-flex flex-column pa-4"
    flat>
    <div v-if="header || seeMore" class="d-flex align-center pb-4">
      <div v-if="header" class="flex-grow-1 flex-shrink-1 text-truncate subtitle-1 text-sub-title text-start">
        {{ header }}
      </div>
      <v-spacer />
      <div v-if="seeMore" class="flex-grow-0 flex-shrink-0 text-end">
        <v-btn
          v-if="!$root.canEdit || !hover"
          :href="seeMore"
          target="_blank"
          color="primary"
          class="pa-0"
          text
          small
          rel="nofollow noreferrer noopener">
          <span class="text-none text-font-size">{{ $t('links.label.seeMore') }}</span>
        </v-btn>
        <v-btn
          v-if="$root.canEdit && hover"
          :title="$t('links.label.seeMore')"
          :href="seeMore"
          target="_blank"
          rel="nofollow noreferrer noopener"
          outlined
          small
          icon>
          <v-icon size="18" class="primary--text">fa-external-link-alt</v-icon>
        </v-btn>
        <v-fab-transition hide-on-leave>
          <v-btn
            v-if="$root.canEdit && hover"
            :title="$t('links.label.editSettings')"
            small
            icon
            @click="$emit('edit')">
            <v-icon size="18">fa-cog</v-icon>
          </v-btn>
        </v-fab-transition>
      </div>
    </div>
    <v-card
      v-if="links && links.length"
      :class="`${parentFlexDirection} ${parentFlexAlign}`"
      :color="!isCard && 'transparent'"
      class="d-flex"
      flat>
      <v-card
        v-for="link in links"
        :key="link.id"
        :class="childClass"
        :min-width="minChildWidth"
        :max-width="maxChildWidth"
        class="d-flex flex-shrink-1 flex-grow-0 overflow-hidden pb-2"
        flat
        tile
        v-on="on"
        v-bind="bind">
        <links-item
          :link="link"
          :type="type"
          :show-name="showName"
          :large-icon="largeIcon"
          :count="links.length" />
      </v-card>
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
    hover: {
      type: Boolean,
      default: false,
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
      return this.$linkService.toLinkUrl(this.settings?.seeMore);
    },
    parentFlexDirection() {
      return this.isColumn && 'flex-column' || 'flex-row';
    },
    parentFlexAlign() {
      return (this.isCard && 'align-start justify-center flex-wrap') || (this.isColumn && 'align-start justify-center flex-nowrap') || (this.isRow && 'align-start justify-center flex-wrap');
    },
    childClass() {
      return (this.isCard && 'justify-center mx-1') || (this.isColumn && 'mx-1') || (this.isRow && 'justify-center pe-2');
    },
    minChildWidth() {
      return this.isCard && '90' || '10%';
    },
    maxChildWidth() {
      return (this.isColumn && '100%') || (this.isCard && this.links?.length && `${parseInt(100 / this.links.length)}%`) || 'initial';
    },
  },
};
</script>