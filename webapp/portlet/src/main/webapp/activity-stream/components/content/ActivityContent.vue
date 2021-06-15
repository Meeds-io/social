<template>
  <v-card
    :loading="loading"
    flat>
    <v-card-text
      v-if="isBodyNotEmpty"
      v-sanitized-html="body"
      class="postContent text-color pt-0" />
    <a
      :href="sourceLink"
      :target="sourceLinkTarget"
      :title="tooltipText"
      class="d-flex flex-no-wrap activity-thumbnail-box"
      v-bind="attrs"
      v-on="tooltip && on">
      <template v-if="useMobileView">
        <div
          :class="thumbnailMobileNoBorder || 'border-color'"
          class="border-box-sizing col pa-4">
          <v-avatar
            v-if="supportsThumbnail"
            :min-height="thumbnailMobileHeight"
            :height="thumbnailMobileHeight"
            :min-width="thumbnailMobileWidth"
            :width="thumbnailMobileWidth"
            rounded
            tile>
            <v-img
              v-if="thumbnail"
              :src="thumbnail"
              min-height="100%"
              min-width="100%" />
            <v-icon
              v-else
              :size="defaultIconSize"
              class="grey-text"
              contain>
              {{ defaultIconClass }}
            </v-icon>
          </v-avatar>
          <div v-if="title" class="pa-4 grey-background border-top-color">
            <ellipsis
              v-if="title"
              :title="title"
              :data="title"
              :line-clamp="2"
              end-char="..."
              class="font-weight-bold text-color ma-0 text-wrap" />
          </div>
        </div>
      </template>
      <template v-else>
        <v-avatar
          v-if="supportsThumbnail"
          :min-height="thumbnailHeight"
          :height="thumbnailHeight"
          :min-width="thumbnailWidth"
          :width="thumbnailWidth"
          :class="thumbnailNoBorder || 'border-color'"
          class="border-box-sizing ma-4"
          rounded
          tile>
          <v-img
            v-if="thumbnail"
            :src="thumbnail"
            min-height="100%"
            min-width="100%" />
          <v-icon
            v-else
            :size="defaultIconSize"
            class="grey-text"
            contain>
            {{ defaultIconClass }}
          </v-icon>
        </v-avatar>
        <v-avatar
          v-else-if="supportsIcon"
          class="border-color border-box-sizing ma-4"
          min-height="150px"
          height="150px"
          min-width="90px"
          width="90px"
          rounded
          tile>
          <v-icon
            :size="defaultIconSize"
            class="grey-text"
            contain>
            {{ defaultIconClass }}
          </v-icon>
        </v-avatar>
        <div class="me-4 my-4">
          <ellipsis
            v-if="title"
            :title="title"
            :data="title"
            :line-clamp="3"
            end-char="..."
            class="font-weight-bold text-color ma-0 pb-2 text-wrap" />
          <ellipsis
            v-if="summary"
            :title="summary"
            :data="summary"
            :line-clamp="3"
            end-char="..."
            class="caption text-light-color text-wrap" />
        </div>
      </template>
    </a>
  </v-card>
</template>

<script>
export default {
  props: {
    activityLink: {
      type: String,
      default: null,
    },
    body: {
      type: String,
      default: null,
    },
    title: {
      type: String,
      default: null,
    },
    summary: {
      type: String,
      default: null,
    },
    supportsIcon: {
      type: Boolean,
      default: false,
    },
    supportsThumbnail: {
      type: Boolean,
      default: false,
    },
    useSameViewForMobile: {
      type: Boolean,
      default: false,
    },
    thumbnail: {
      type: String,
      default: null,
    },
    thumbnailProperties: {
      type: Object,
      default: null,
    },
    tooltip: {
      type: String,
      default: null,
    },
    sourceLink: {
      type: String,
      default: null,
    },
    defaultIcon: {
      type: Object,
      default: null,
    },
  },
  computed: {
    defaultIconClass() {
      return this.defaultIcon && this.defaultIcon.icon || 'far fa-image';
    },
    defaultIconSize() {
      return this.defaultIcon && this.defaultIcon.size || 58;
    },
    useMobileView() {
      return this.$vuetify.breakpoint.name === 'xs' && !this.useSameViewForMobile;
    },
    sourceLinkTarget() {
      return this.sourceLink && this.sourceLink.indexOf('/') === 0 && '_self' || '_blank';
    },
    isBodyNotEmpty() {
      return this.body && this.body.trim() !== '<p></p>';
    },
    thumbnailHeight() {
      return this.thumbnailProperties && this.thumbnailProperties.height || '150px';
    },
    thumbnailWidth() {
      return this.thumbnailProperties && this.thumbnailProperties.width || '252px';
    },
    thumbnailNoBorder() {
      return this.thumbnailProperties && this.thumbnailProperties.noBorder;
    },
    thumbnailMobileHeight() {
      return this.thumbnailProperties && this.thumbnailProperties.mobile && this.thumbnailProperties.mobile.height || '75vw';
    },
    thumbnailMobileWidth() {
      return this.thumbnailProperties && this.thumbnailProperties.mobile && this.thumbnailProperties.mobile.width || '100%';
    },
    thumbnailMobileNoBorder() {
      if (this.thumbnailProperties && this.thumbnailProperties.mobile && (this.thumbnailProperties.mobile.noBorder === false || this.thumbnailProperties.mobile.noBorder === true)) {
        return this.thumbnailProperties.mobile.noBorder;
      }
      if (this.thumbnailNoBorder === false || this.thumbnailNoBorder === true) {
        return this.thumbnailNoBorder;
      }
      return false;
    },
    tooltipText() {
      return this.tooltip && this.$t(this.tooltip) || '';
    },
  },
  created() {
    this.$root.$on('activity-stream-updating-activity-start', () => this.loading = true);
    this.$root.$emit('activity-stream-updating-activity-end', () => this.loading = false);
  },
};
</script>