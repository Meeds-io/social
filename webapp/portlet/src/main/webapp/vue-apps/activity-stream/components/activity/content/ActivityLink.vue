<template>
  <dynamic-html-element
    v-if="sourceLink"
    :element="htmlElement"
    :href="link"
    :target="linkTarget"
    :title="tooltipText"
    class="d-flex flex-no-wrap activity-thumbnail-box">
    <template v-if="useMobileView">
      <div class="border-box-sizing flex">
        <v-avatar
          v-if="supportsThumbnail"
          :min-height="thumbnailMobileHeight"
          :height="thumbnailMobileHeight"
          :min-width="thumbnailMobileWidth"
          :width="thumbnailMobileWidth"
          :class="thumbnailMobileNoBorder || 'border-color'"
          rounded
          eager
          tile>
          <img
            v-if="thumbnail"
            :src="`${thumbnail}`"
            :alt="title"
            class="object-fit-cover my-auto"
            loading="lazy">
          <v-icon
            v-else
            :size="defaultIconSize"
            class="grey-text"
            contain>
            {{ defaultIconClass }}
          </v-icon>
        </v-avatar>
        <div
          v-if="title"
          :class="thumbnailMobileNoBorder || 'border-color no-border-top'"
          class="pa-4">
          <div
            v-if="title"
            v-text="titleText"
            :title="titleTooltip"
            class="font-weight-bold text-color ma-0 text-wrap text-break text-truncate-2">
          </div>
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
        class="border-box-sizing align-start my-4 me-4"
        rounded
        eager
        tile>
        <img
          v-if="thumbnail"
          :src="thumbnail"
          :alt="title"
          class="object-fit-cover my-auto"
          loading="lazy">
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
        :min-height="iconHeight"
        :height="iconHeight"
        :min-width="iconWidth"
        :width="iconWidth"
        :class="iconNoBorder || 'border-color'"
        class="border-box-sizing align-start my-4 me-4"
        rounded
        eager
        tile>
        <v-icon
          :size="defaultIconSize"
          class="grey-text"
          contain>
          {{ defaultIconClass }}
        </v-icon>
      </v-avatar>
      <div class="my-4">
        <dynamic-html-element
          v-if="title"
          :child="titleElement"
          :class="useEllipsisOnTitle && 'text-truncate-2' || ''"
          class="font-weight-bold text-color mx-0 mt-0 mb-2 text-wrap text-break"
          dir="auto" />
        <dynamic-html-element
          v-if="summary"
          :child="summaryElement"
          :title="summaryTooltip"
          :class="useEllipsisOnSummary && 'text-light-color text-truncate-3' || 'text-color'"
          class="caption text-wrap text-break reset-style-box rich-editor-content"
          dir="auto" />
      </div>
    </template>
  </dynamic-html-element>
</template>

<script>
export default {
  props: {
    activity: {
      type: Object,
      default: null,
    },
    activityTypeExtension: {
      type: String,
      default: null,
    },
    isActivityDetail: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    title: null,
    titleTooltip: null,
    summary: null,
    summaryTooltip: null,
    thumbnail: null,
    sourceLink: null,
    tooltip: null,
    useEllipsisOnSummary: true,
    useEllipsisOnTitle: true,
  }),
  computed: {
    getTitle() {
      return this.activityTypeExtension && this.activityTypeExtension.getTitle;
    },
    getSummary() {
      return this.activityTypeExtension && this.activityTypeExtension.getSummary;
    },
    getThumbnail() {
      return this.activityTypeExtension && this.activityTypeExtension.getThumbnail;
    },
    getSourceLink() {
      return this.activityTypeExtension && this.activityTypeExtension.getSourceLink;
    },
    supportsThumbnail() {
      return this.activityTypeExtension && this.activityTypeExtension.supportsThumbnail;
    },
    thumbnailProperties() {
      if (this.activityTypeExtension && this.activityTypeExtension.getThumbnailProperties) {
        return this.activityTypeExtension.getThumbnailProperties(this.activity, this.isActivityDetail);
      }
      return this.activityTypeExtension && this.activityTypeExtension.thumbnailProperties;
    },
    useSameViewForMobile() {
      if (this.activityTypeExtension && this.activityTypeExtension.isUseSameViewForMobile) {
        return this.activityTypeExtension.isUseSameViewForMobile(this.activity, this.isActivityDetail);
      }
      return this.activityTypeExtension && this.activityTypeExtension.useSameViewForMobile;
    },
    supportsIcon() {
      return this.supportsThumbnail || (this.activityTypeExtension && this.activityTypeExtension.supportsIcon);
    },
    getTooltip() {
      return this.activityTypeExtension && this.activityTypeExtension.getTooltip;
    },
    defaultIcon() {
      return this.activityTypeExtension && (this.activityTypeExtension.defaultIcon || (this.activityTypeExtension.getDefaultIcon && this.activityTypeExtension.getDefaultIcon(this.comment || this.activity)));
    },
    defaultIconClass() {
      return this.defaultIcon && this.defaultIcon.icon || 'far fa-image';
    },
    defaultIconSize() {
      return this.defaultIcon && this.defaultIcon.size || 58;
    },
    useMobileView() {
      return this.$vuetify.breakpoint.name === 'xs' && !this.useSameViewForMobile;
    },
    htmlElement() {
      return this.sourceLink && this.sourceLink !== '#' && 'a' || 'div';
    },
    link() {
      return this.sourceLink !== '#' && this.sourceLink || 'javascript:void(0)';
    },
    linkTarget() {
      return this.sourceLink && (this.sourceLink.indexOf('/') === 0 || this.sourceLink.indexOf('#') === 0) && '_self' || (this.sourceLink && '_blank') || '';
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
    iconHeight() {
      return this.defaultIcon && this.defaultIcon.height || '150px';
    },
    iconWidth() {
      return this.defaultIcon && this.defaultIcon.width || '90px';
    },
    iconNoBorder() {
      return this.defaultIcon && this.defaultIcon.noBorder;
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
    titleText() {
      return this.title && this.$utils.htmlToText(this.title) || '';
    },
    summaryText() {
      return this.summary && this.$utils.htmlToText(this.summary) || '';
    },
    summaryElement() {
      return {
        template: ExtendedDomPurify.purify(`<div>${this.summary}</div>`) || '',
      };
    },
    titleElement() {
      return {
        template: ExtendedDomPurify.purify(`<div>${this.title}</div>`) || '',
      };
    },
  },
  watch: {
    activityTypeExtension(newVal, oldVal) {
      if (!oldVal || newVal !== oldVal) {
        this.retrieveActivityProperties();
      }
    },
  },
  created() {
    this.retrieveActivityProperties();
  },
  methods: {
    retrieveActivityProperties() {
      this.useEllipsisOnTitle = this.activityTypeExtension && !this.activityTypeExtension.noTitleEllipsis;
      this.useEllipsisOnSummary = this.activityTypeExtension && !this.activityTypeExtension.noSummaryEllipsis;
      this.title = this.getTitle && this.getTitle(this.activity, this.isActivityDetail);
      if (this.title && this.title.key) {
        this.title = this.$t(this.title.key, this.title.params || {});
      } else {
        this.title = this.$utils.trim(this.title);
      }
      this.titleTooltip = this.$utils.htmlToText(this.title);
      this.summary = this.getSummary && this.$utils.trim(this.getSummary(this.activity, this.isActivityDetail));
      this.summaryTooltip = this.$utils.htmlToText(this.summary);
      this.sourceLink = this.getSourceLink && this.getSourceLink(this.activity, this.isActivityDetail);
      this.tooltip = this.getTooltip && this.getTooltip(this.activity, this.isActivityDetail);
      if (this.supportsThumbnail) {
        this.thumbnail = this.getThumbnail && this.getThumbnail(this.activity, this.isActivityDetail);
      }
    },
  },
};
</script>
