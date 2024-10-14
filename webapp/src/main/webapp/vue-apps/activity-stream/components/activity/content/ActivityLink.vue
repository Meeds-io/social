<template>
  <dynamic-html-element
    v-if="sourceLink"
    :element="htmlElement"
    :href="link"
    :target="linkTarget"
    :title="tooltipText"
    :class="mainClass">
    <template v-if="useMobileView">
      <div class="border-box-sizing flex">
        <v-avatar
          v-if="supportsThumbnail"
          :max-height="thumbnailMobileHeight"
          :min-height="thumbnailMobileHeight"
          :min-width="thumbnailMobileWidth"
          :max-width="thumbnailMobileWidth"
          :width="thumbnailMobileWidth"
          :class="thumbnailMobileNoBorder || 'border-color'"
          :style="`background-color: ${thumbnailBG};`"
          class="overflow-hidden"
          eager
          tile>
          <img
            v-if="thumbnail"
            :src="`${thumbnail}`"
            :alt="title"
            :class="thumbnailClass"
            :style="imageMobileStyle"
            class="my-auto"
            loading="lazy"
            width="auto"
            height="auto">
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
        :min-width="!isMobile && thumbnailWidth || (useEmbeddedLinkView && '100%' || thumbnailWidth)"
        :width="!isMobile && thumbnailWidth || (useEmbeddedLinkView && '100%' || thumbnailWidth)"
        :class="useEmbeddedLinkView && (!isMobile && 'border-bottom-left-radius border-top-left-radius' || 'border-top-right-radius border-top-left-radius')"
        :style="`background-color: ${thumbnailBG};`"
        class="border-box-sizing align-start me-4 rounded-l"
        eager
        tile>
        <img
          v-if="thumbnail"
          :src="thumbnail"
          :alt="title"
          :class="thumbnailClass"
          class="my-auto"
          loading="lazy"
          width="auto"
          height="auto">
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
        eager
        tile>
        <v-icon
          :size="defaultIconSize"
          class="grey-text"
          contain>
          {{ defaultIconClass }}
        </v-icon>
      </v-avatar>
      <div
        :class="isMobile && 'mx-3' || ''"
        class="my-2 position-relative d-flex flex-column width-full">
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
          :class="bodyClass"
          class="text-wrap text-break reset-style-box rich-editor-content"
          dir="auto" />
        <v-btn
          v-if="showReadMore"
          :aria-label="$t('UIActivity.label.seeMore')"
          class="d-flex ms-auto pb-2px mb-0 pl-2 pr-0 height-auto position-absolute r-0 b-0 text-light-color linear-gradient-white-background hover-underline hover-blue-color"
          text
          plain
          @click="displayFullContent">
          <span class="pl-6">{{ $t('UIActivity.label.seeMore') }}</span>
        </v-btn>
        <div
          v-if="activityViews"
          :title="activityViewsTooltip"
          class="d-flex justify-end mt-auto ms-auto width-fit-content me-3">
          <v-icon
            size="20"
            class="icon-default-color">
            fas fa-eye
          </v-icon>
          <span class="ms-1 text-subtitle">
            {{ activityViewsCount }}
          </span>
        </div>
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
    collapsed: {
      type: Boolean,
      default: true
    }
  },
  data: () => ({
    title: null,
    titleTooltip: null,
    summary: null,
    summaryTooltip: null,
    thumbnail: null,
    sourceLink: null,
    tooltip: null,
    regularFontSizeOnSummary: false,
    useEllipsisOnSummary: true,
    useEllipsisOnTitle: true,
    fullContent: false,
    displayReadMoreButton: false,
    useEmbeddedLinkView: true,
    summaryLinesToDisplay: 2,
    isLandscapeThumbnail: false,
    activityViews: null
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
    isDefaultThumbnail() {
      return this.activityTypeExtension && this.activityTypeExtension.isDefaultThumbnail && this.activityTypeExtension.isDefaultThumbnail(this.activity);
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
      return this.$vuetify.breakpoint.name === 'sm' && !this.useSameViewForMobile;
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
      return this.thumbnailProperties && this.thumbnailProperties.height || (!this.useEmbeddedLinkView && '150px' || '120px');
    },
    thumbnailWidth() {
      return this.thumbnailProperties && this.thumbnailProperties.width || (!this.useEmbeddedLinkView && '252px' || '150px');
    },
    thumbnailPreviewHeight() {
      return this.activityTypeExtension && this.activityTypeExtension.getPreviewHeight && this.activityTypeExtension.getPreviewHeight(this.activity) || 0;
    },
    thumbnailPreviewWidth() {
      return this.activityTypeExtension && this.activityTypeExtension.getPreviewWidth && this.activityTypeExtension.getPreviewWidth(this.activity) || 0;
    },
    thumbnailBG() {
      return this.activityTypeExtension && this.activityTypeExtension.getPreviewWidth && this.activityTypeExtension.getThumbnailBG(this.activity) || 'rgb(231, 231, 231)';
    },
    thumbnailNoBorder() {
      return this.thumbnailProperties && this.thumbnailProperties.noBorder;
    },
    iconHeight() {
      return this.defaultIcon && this.defaultIcon.height || '120px';
    },
    iconWidth() {
      return this.defaultIcon && this.defaultIcon.width || '175px';
    },
    iconNoBorder() {
      return this.defaultIcon && this.defaultIcon.noBorder;
    },
    thumbnailMobileHeight() {
      return this.thumbnailProperties && this.thumbnailProperties.mobile && this.thumbnailProperties.mobile.height || '120px';
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
    bodyClass() {
      return `${this.textTruncate || ''} ${this.useEllipsisOnSummary && 'text-light-color' || 'text-color'} ${!this.useEllipsisOnSummary && this.collapsed && !this.fullContent && 'text-truncate-4' || ''} ${this.regularFontSizeOnSummary && 'text-font-size' || 'caption'}`;
    },
    textTruncate() {
      return this.useEllipsisOnSummary && `text-truncate-${this.summaryLinesToDisplay}`;
    },
    canCollapse() {
      return this.activityTypeExtension?.isCollapsed;
    },
    showReadMore() {
      return this.collapsed && !this.fullContent && this.canCollapse && this.displayReadMoreButton;
    },
    isMobile() {
      return this.$vuetify.breakpoint.smAndDown;
    },
    thumbnailClass() {
      return `${this.useEmbeddedLinkView && (!this.isMobile && 'border-bottom-left-radius border-top-left-radius' || 'border-top-right-radius border-top-left-radius')} ${this.isLandscapeThumbnail && 'object-fit-cover' || 'object-fit-contain' }`;
    },
    addMargin() {
      return !!this.activityTypeExtension?.addMargin;
    },
    mainClass() {
      return `${!this.useEmbeddedLinkView && 'd-flex flex-no-wrap' || 'activity-thumbnail-box light-grey-background-color overflow-hidden hover-elevation border-radius border-color mb-4 d-block d-sm-flex flex-sm-nowrap'} ${this.addMargin && 'my-4' || ''}`;
    },
    imageMobileStyle() {
      return {
        'max-width': '100%',
        'min-width': '100%',
        'min-height': '100%'
      };
    },
    getActivityViews() {
      return this.activityTypeExtension && this.activityTypeExtension.getActivityViews;
    },
    activityViewsTooltip() {
      return this.activityViews?.tooltip && this.$t(this.activityViews.tooltip, {0: this.activityViews?.originalViewsCount});
    },
    activityViewsCount() {
      return this.activityViews?.viewsCount;
    }
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
    window.addEventListener('resize', this.displayReadMore);
  },
  mounted() {
    this.displayReadMore();
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.displayReadMore);
  },
  methods: {
    retrieveActivityProperties() {
      this.useEllipsisOnTitle = this.activityTypeExtension && !this.activityTypeExtension.noTitleEllipsis;
      this.useEllipsisOnSummary = this.activityTypeExtension && !this.activityTypeExtension.noSummaryEllipsis;
      this.useEmbeddedLinkView = this.activityTypeExtension && !this.activityTypeExtension.noEmbeddedLinkView;
      this.summaryLinesToDisplay = this.activityTypeExtension?.summaryLinesToDisplay || 2;
      this.regularFontSizeOnSummary = this.activityTypeExtension.regularFontSizeOnSummary === true;
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
        if (this.isDefaultThumbnail) {
          this.isLandscapeThumbnail = true;
        } else {
          if (this.thumbnail && this.thumbnailPreviewWidth > 0 && this.thumbnailPreviewHeight > 0) {
            this.isLandscapeThumbnail = Number(this.thumbnailPreviewWidth) > Number(this.thumbnailPreviewHeight);
          }
        }
      }
      this.activityViews = this.getActivityViews && this.getActivityViews(this.activity);
    },
    displayReadMore() {
      const elem = this.$el?.querySelector?.('.rich-editor-content');
      this.displayReadMoreButton = elem && elem?.scrollHeight > elem?.clientHeight;
    },
    displayFullContent() {
      this.fullContent = !this.fullContent;
    },
  },
};
</script>
