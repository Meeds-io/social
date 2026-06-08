<template>
  <dynamic-html-element
    v-if="sourceLink"
    :element="htmlElement"
    :href="link"
    :target="linkTarget"
    :class="mainClass">
    <template v-if="useMobileView">
      <div class="border-box-sizing flex">
        <v-avatar
          v-if="supportsThumbnail"
          :max-height="thumbnailMobileMaxHeight"
          :min-height="thumbnailMobileMinHeight"
          :min-width="thumbnailMobileWidth"
          :max-width="thumbnailMobileWidth"
          :width="thumbnailMobileWidth"
          :class="thumbnailMobileNoBorder || 'border-color'"
          :style="`background-color: ${thumbnailBG};`"
          class="overflow-hidden"
          eager
          tile>
          <v-img
            v-if="thumbnail"
            :src="`${thumbnail}`"
            :alt="featuredThumbnailAltText"
            :class="thumbnailClass"
            :style="imageMobileStyle"
            :aspect-ratio="8"
            class="my-auto"
            loading="lazy"
            width="auto"
            height="auto" />
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
          class="mt-3 mx-2 pb-2">
          <div
            v-if="title"
            v-text="titleText"
            :title="titleTooltip"
            class="font-weight-bold text-color ma-0 text-wrap text-break text-truncate">
          </div>
          <dynamic-html-element
            v-if="summary"
            :child="summaryElement"
            :class="bodyClass"
            class="text-subtitle mt-3 text-color text-truncate-2 text-wrap text-break reset-style-box rich-editor-content mb-0"
            dir="auto" />
        </div>
        <activity-link-footer
          v-if="showFooter"
          :activity="activity"
          :is-mobile="isMobile"
          :activity-type-extension="activityTypeExtension"
          class="mx-2 mb-2"/>
      </div>
    </template>
    <template v-else>
      <v-avatar
        v-if="supportsThumbnail"
        v-bind="!isMobile &&{
          height: thumbnailHeight,
        } || {
          maxHeight:thumbnailMobileMaxHeight
        }"
        :min-height="!isMobile && thumbnailHeight || thumbnailMobileMinHeight"
        :min-width="!isMobile && thumbnailWidth || (useEmbeddedLinkView && '100%' || thumbnailWidth)"
        :width="!isMobile && thumbnailWidth || (useEmbeddedLinkView && '100%' || thumbnailWidth)"
        :class="{
          'border-bottom-left-radius': useEmbeddedLinkView && !isMobile,
          'border-top-left-radius': useEmbeddedLinkView,
          'border-top-right-radius': useEmbeddedLinkView && isMobile,

        }"
        :style="`background-color: ${thumbnailBG};`"
        class="border-box-sizing align-start rounded-l border-color no-border-top no-border-bottom no-border-left"
        eager
        tile>
        <v-img
          v-if="thumbnail"
          :src="thumbnail"
          :alt="featuredThumbnailAltText"
          :class="thumbnailClass"
          :aspect-ratio="!isMobile && 16/9 || 8"
          class="my-auto"
          loading="lazy"
          width="auto"
          height="auto" />
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
        class="no-min-width position-relative d-flex flex-column flex-grow-1 mx-3">
        <dynamic-html-element
          v-if="title"
          :child="titleElement"
          class="text-truncate text-body mb-3 mt-2 font-weight-bold text-color mx-0 mt-0 text-wrap text-break"
          dir="auto" />
        <dynamic-html-element
          v-if="summary"
          :child="summaryElement"
          :class="bodyClass"
          class="text-subtitle text-color text-truncate-2 text-wrap text-break reset-style-box rich-editor-content mb-0"
          dir="auto" />
        <v-btn
          v-if="showReadMore"
          :aria-label="$t('UIActivity.label.seeMore')"
          class="d-flex ms-auto pb-2px mb-0 pl-2 pr-0 height-auto r-0 b-0 text-light-color linear-gradient-white-background hover-underline hover-blue-color"
          text
          plain
          @click="displayFullContent">
          <span class="pl-6">{{ $t('UIActivity.label.seeMore') }}</span>
        </v-btn>
        <activity-link-footer
          v-if="showFooter"
          :activity="activity"
          :is-mobile="isMobile"
          :activity-type-extension="activityTypeExtension"
          class="mb-2" />
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
    showFooter() {
      return this.activityTypeExtension?.showFooter;
    },
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
      return this.thumbnailProperties && this.thumbnailProperties.height || (!this.useEmbeddedLinkView && '150px' || '124px');
    },
    thumbnailWidth() {
      return this.thumbnailProperties && this.thumbnailProperties.width || (!this.useEmbeddedLinkView && '252px' || '220px');
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
    thumbnailMobileMaxHeight() {
      return this.thumbnailProperties && this.thumbnailProperties.mobile && this.thumbnailProperties.mobile.maxHeight || '75px';
    },
    thumbnailMobileMinHeight() {
      return this.thumbnailProperties && this.thumbnailProperties.mobile && this.thumbnailProperties.mobile.minHeight || '40px';
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
        template: this.summary && ExtendedDomPurify.purify(`<div>${this.summary}</div>`) || '',
      };
    },
    titleElement() {
      return {
        template: this.title && ExtendedDomPurify.purify(`<span>${this.title}</span>`) || '',
      };
    },
    bodyClass() {
      return `${this.textTruncate || ''} ${!this.useEllipsisOnSummary && this.collapsed && !this.fullContent && 'text-truncate-4' || ''} ${this.regularFontSizeOnSummary && 'text-font-size' || 'caption'}`;
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
      return `${!this.useEmbeddedLinkView && 'd-flex flex-no-wrap' || 'activity-thumbnail-box overflow-hidden hover-elevation border-radius border-color mb-4 d-block d-sm-flex flex-sm-nowrap activity-comment-background'} ${this.addMargin && 'my-4' || ''}`;
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
    },
    featuredThumbnailAltText() {
      return this.activity?.news?.properties?.featuredImage?.altText || '';
    },
    summaryClass() {
      return this.activityTypeExtension?.summaryClass;
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
