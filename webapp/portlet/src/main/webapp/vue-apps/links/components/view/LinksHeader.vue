<template>
  <div
    v-if="hasLinks && (showHeader || canEdit)"
    :class="{
      'position-relative pb-4': showHeader,
      'pb-2': !showHeader,
    }"
    class="d-flex align-center">
    <div v-if="header" class="flex-grow-1 flex-shrink-1 text-truncate subtitle-1 text-sub-title text-start">
      {{ header }}
    </div>
    <v-spacer />
    <div
      v-if="hasLinks && (seeMoreUrl || hoverEdit)"
      class="flex-grow-0 flex-shrink-0 text-end">
      <v-btn
        v-if="seeMoreUrl"
        :href="seeMoreUrl"
        :title="$t('links.label.seeMore')"
        target="_blank"
        :outlined="hoverEdit"
        :icon="hoverEdit"
        :class="!hoverEdit && 'pa-0'"
        :color="!hoverEdit && 'primary'"
        :text="!hoverEdit"
        rel="nofollow noreferrer noopener"
        small>
        <v-icon
          v-if="hoverEdit"
          size="18"
          class="primary--text">
          fa-external-link-alt
        </v-icon>
        <span
          v-else-if="seeMoreUrl"
          class="text-none text-font-size">
          {{ $t('links.label.seeMore') }}
        </span>
      </v-btn>
      <v-fab-transition hide-on-leave>
        <v-btn
          v-if="hoverEdit"
          :title="$t('links.label.editSettings')"
          :class="{
            'position-absolute z-index-two t-0': !seeMoreUrl,
            'r-0': !$vuetify.rtl,
            'l-0': $vuetify.rtl,
            'ma-1': !showHeader,
          }"
          small
          icon
          @click="$emit('edit')">
          <v-icon size="18">fa-cog</v-icon>
        </v-btn>
      </v-fab-transition>
    </div>
  </div>
  <div v-else-if="!hasLinks && canEdit" class="d-flex align-center justify-center">
    <v-btn
      class="primary"
      elevation="0"
      outlined
      border
      @click="$emit('edit')">
      {{ $t('links.label.addLinksButton') }}
    </v-btn>
  </div>
</template>
<script>
export default {
  props: {
    settings: {
      type: Object,
      default: null,
    },
    minWidth: {
      type: String,
      default: () => '33%',
    },
    canEdit: {
      type: Boolean,
      default: false,
    },
    hasLinks: {
      type: Boolean,
      default: false,
    },
    hover: {
      type: Boolean,
      default: false,
    },
  },
  computed: {
    hoverEdit() {
      return this.canEdit && this.hover;
    },
    showHeader() {
      return this.header?.length || this.seeMoreUrl?.length;
    },
    header() {
      return this.settings?.header?.[this.$root.language] || this.settings?.header?.[this.$root.defaultLanguage];
    },
    seeMoreUrl() {
      return this.$linkService.toLinkUrl(this.settings?.seeMore);
    },
  },
};
</script>