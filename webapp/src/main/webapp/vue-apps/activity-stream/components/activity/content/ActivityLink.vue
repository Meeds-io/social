<template>
  <dynamic-html-element
    v-if="sourceLink"
    :class="mainClass"
    :element="htmlElement"
    :href="link"
    :target="linkTarget"
    :title="tooltipText">
    <template v-if="useMobileView">
      <div class="border-box-sizing flex">
        <v-avatar
          v-if="supportsThumbnail"
          class="overflow-hidden"
          :class="thumbnailMobileNoBorder || 'border-color'"
          eager
          :max-height="thumbnailMobileHeight"
          :max-width="thumbnailMobileWidth"
          :min-height="thumbnailMobileHeight"
          :min-width="thumbnailMobileWidth"
          :style="`background-color: ${thumbnailBG};`"
          tile
          :width="thumbnailMobileWidth">
          <img
            v-if="thumbnail"
            :alt="title"
            class="my-auto"
            :class="thumbnailClass"
            height="auto"
            loading="lazy"
            :src="`${thumbnail}`"
            :style="imageMobileStyle"
            width="auto">
          <v-icon
            v-else
            class="grey-text"
            contain
            :size="defaultIconSize">
            {{ defaultIconClass }}
          </v-icon>
        </v-avatar>
        <div
          v-if="title"
          class="pa-4"
          :class="thumbnailMobileNoBorder || 'border-color no-border-top'">
          <div
            v-if="title"
            class="font-weight-bold text-color ma-0 text-wrap text-break text-truncate-2"
            :title="titleTooltip"
            v-text="titleText"></div>
        </div>
      </div>
    </template>
    <template v-else>
      <v-avatar
        v-if="supportsThumbnail"
        class="border-box-sizing align-start me-4 rounded-l"
        :class="useEmbeddedLinkView && (!isMobile && 'border-bottom-left-radius border-top-left-radius' || 'border-top-right-radius border-top-left-radius')"
        eager
        :height="thumbnailHeight"
        :min-height="thumbnailHeight"
        :min-width="!isMobile && thumbnailWidth || (useEmbeddedLinkView && '100%' || thumbnailWidth)"
        :style="`background-color: ${thumbnailBG};`"
        tile
        :width="!isMobile && thumbnailWidth || (useEmbeddedLinkView && '100%' || thumbnailWidth)">
        <img
          v-if="thumbnail"
          :alt="title"
          class="my-auto"
          :class="thumbnailClass"
          height="auto"
          loading="lazy"
          :src="thumbnail"
          width="auto">
        <v-icon
          v-else
          class="grey-text"
          contain
          :size="defaultIconSize">
          {{ defaultIconClass }}
        </v-icon>
      </v-avatar>
      <v-avatar
        v-else-if="supportsIcon"
        class="border-box-sizing align-start my-4 me-4"
        :class="iconNoBorder || 'border-color'"
        eager
        :height="iconHeight"
        :min-height="iconHeight"
        :min-width="iconWidth"
        tile
        :width="iconWidth">
        <v-icon
          class="grey-text"
          contain
          :size="defaultIconSize">
          {{ defaultIconClass }}
        </v-icon>
      </v-avatar>
      <div
        class="my-2 position-relative d-flex flex-column width-full"
        :class="isMobile && 'mx-3' || ''">
        <dynamic-html-element
          v-if="title"
          :child="titleElement"
          class="font-weight-bold text-color mx-0 mt-0 mb-2 text-wrap text-break"
          :class="useEllipsisOnTitle && 'text-truncate-2' || ''"
          dir="auto" />
        <dynamic-html-element
          v-if="summary"
          :child="summaryElement"
          class="text-wrap text-break reset-style-box rich-editor-content"
          :class="bodyClass"
          dir="auto"
          :title="summaryTooltip" />
        <v-btn
          v-if="showReadMore"
          :aria-label="$t('UIActivity.label.seeMore')"
          class="d-flex ms-auto pb-2px mb-0 pl-2 pr-0 height-auto position-absolute r-0 b-0 text-light-color linear-gradient-white-background hover-underline hover-blue-color"
          plain
          text
          @click="displayFullContent">
          <span class="pl-6">{{ $t('UIActivity.label.seeMore') }}</span>
        </v-btn>
        <div
          v-if="activityViews"
          class="d-flex justify-end mt-auto ms-auto width-fit-content me-3"
          :title="activityViewsTooltip">
          <v-icon
            class="icon-default-color"
            size="20">
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
        default: true,
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
      regularFontSizeOnSummary: false,
      useEllipsisOnSummary: true,
      useEllipsisOnTitle: true,
      fullContent: false,
      displayReadMoreButton: false,
      useEmbeddedLinkView: true,
      summaryLinesToDisplay: 2,
      isLandscapeThumbnail: false,
      activityViews: null,
    }),
    computed: {
      getTitle () {
        return this.activityTypeExtension && this.activityTypeExtension.getTitle;
      },
      getSummary () {
        return this.activityTypeExtension && this.activityTypeExtension.getSummary;
      },
      getThumbnail () {
        return this.activityTypeExtension && this.activityTypeExtension.getThumbnail;
      },
      getSourceLink () {
        return this.activityTypeExtension && this.activityTypeExtension.getSourceLink;
      },
      supportsThumbnail () {
        return this.activityTypeExtension && this.activityTypeExtension.supportsThumbnail;
      },
      thumbnailProperties () {
        if (this.activityTypeExtension && this.activityTypeExtension.getThumbnailProperties) {
          return this.activityTypeExtension.getThumbnailProperties(this.activity, this.isActivityDetail);
        }
        return this.activityTypeExtension && this.activityTypeExtension.thumbnailProperties;
      },
      useSameViewForMobile () {
        if (this.activityTypeExtension && this.activityTypeExtension.isUseSameViewForMobile) {
          return this.activityTypeExtension.isUseSameViewForMobile(this.activity, this.isActivityDetail);
        }
        return this.activityTypeExtension && this.activityTypeExtension.useSameViewForMobile;
      },
      isDefaultThumbnail () {
        return this.activityTypeExtension && this.activityTypeExtension.isDefaultThumbnail && this.activityTypeExtension.isDefaultThumbnail(this.activity);
      },
      supportsIcon () {
        return this.supportsThumbnail || (this.activityTypeExtension && this.activityTypeExtension.supportsIcon);
      },
      getTooltip () {
        return this.activityTypeExtension && this.activityTypeExtension.getTooltip;
      },
      defaultIcon () {
        return this.activityTypeExtension && (this.activityTypeExtension.defaultIcon || (this.activityTypeExtension.getDefaultIcon && this.activityTypeExtension.getDefaultIcon(this.comment || this.activity)));
      },
      defaultIconClass () {
        return this.defaultIcon && this.defaultIcon.icon || 'far fa-image';
      },
      defaultIconSize () {
        return this.defaultIcon && this.defaultIcon.size || 58;
      },
      useMobileView () {
        return eXo.vuetify.display.name.value === 'sm' && !this.useSameViewForMobile;
      },
      htmlElement () {
        return this.sourceLink && this.sourceLink !== '#' && 'a' || 'div';
      },
      link () {
        return this.sourceLink !== '#' && this.sourceLink || 'javascript:void(0)';
      },
      linkTarget () {
        return this.sourceLink && (this.sourceLink.indexOf('/') === 0 || this.sourceLink.indexOf('#') === 0) && '_self' || (this.sourceLink && '_blank') || '';
      },
      thumbnailHeight () {
        return this.thumbnailProperties && this.thumbnailProperties.height || (!this.useEmbeddedLinkView && '150px' || '120px');
      },
      thumbnailWidth () {
        return this.thumbnailProperties && this.thumbnailProperties.width || (!this.useEmbeddedLinkView && '252px' || '150px');
      },
      thumbnailPreviewHeight () {
        return this.activityTypeExtension && this.activityTypeExtension.getPreviewHeight && this.activityTypeExtension.getPreviewHeight(this.activity) || 0;
      },
      thumbnailPreviewWidth () {
        return this.activityTypeExtension && this.activityTypeExtension.getPreviewWidth && this.activityTypeExtension.getPreviewWidth(this.activity) || 0;
      },
      thumbnailBG () {
        return this.activityTypeExtension && this.activityTypeExtension.getPreviewWidth && this.activityTypeExtension.getThumbnailBG(this.activity) || 'rgb(231, 231, 231)';
      },
      thumbnailNoBorder () {
        return this.thumbnailProperties && this.thumbnailProperties.noBorder;
      },
      iconHeight () {
        return this.defaultIcon && this.defaultIcon.height || '120px';
      },
      iconWidth () {
        return this.defaultIcon && this.defaultIcon.width || '175px';
      },
      iconNoBorder () {
        return this.defaultIcon && this.defaultIcon.noBorder;
      },
      thumbnailMobileHeight () {
        return this.thumbnailProperties && this.thumbnailProperties.mobile && this.thumbnailProperties.mobile.height || '120px';
      },
      thumbnailMobileWidth () {
        return this.thumbnailProperties && this.thumbnailProperties.mobile && this.thumbnailProperties.mobile.width || '100%';
      },
      thumbnailMobileNoBorder () {
        if (this.thumbnailProperties && this.thumbnailProperties.mobile && (this.thumbnailProperties.mobile.noBorder === false || this.thumbnailProperties.mobile.noBorder === true)) {
          return this.thumbnailProperties.mobile.noBorder;
        }
        if (this.thumbnailNoBorder === false || this.thumbnailNoBorder === true) {
          return this.thumbnailNoBorder;
        }
        return false;
      },
      tooltipText () {
        return this.tooltip && this.$t(this.tooltip) || '';
      },
      titleText () {
        return this.title && eXo.$utils.htmlToText(this.title) || '';
      },
      summaryText () {
        return this.summary && eXo.$utils.htmlToText(this.summary) || '';
      },
      summaryElement () {
        return {
          template: ExtendedDomPurify.purify(`<div>${this.summary}</div>`) || '',
        };
      },
      titleElement () {
        return {
          template: ExtendedDomPurify.purify(`<div>${this.title}</div>`) || '',
        };
      },
      bodyClass () {
        return `${this.textTruncate || ''} ${this.useEllipsisOnSummary && 'text-light-color' || 'text-color'} ${!this.useEllipsisOnSummary && this.collapsed && !this.fullContent && 'text-truncate-4' || ''} ${this.regularFontSizeOnSummary && 'text-font-size' || 'caption'}`;
      },
      textTruncate () {
        return this.useEllipsisOnSummary && `text-truncate-${this.summaryLinesToDisplay}`;
      },
      canCollapse () {
        return this.activityTypeExtension?.isCollapsed;
      },
      showReadMore () {
        return this.collapsed && !this.fullContent && this.canCollapse && this.displayReadMoreButton;
      },
      isMobile () {
        return eXo.vuetify.display.smAndDown.value;
      },
      thumbnailClass () {
        return `${this.useEmbeddedLinkView && (!this.isMobile && 'border-bottom-left-radius border-top-left-radius' || 'border-top-right-radius border-top-left-radius')} ${this.isLandscapeThumbnail && 'object-fit-cover' || 'object-fit-contain' }`;
      },
      addMargin () {
        return !!this.activityTypeExtension?.addMargin;
      },
      mainClass () {
        return `${!this.useEmbeddedLinkView && 'd-flex flex-no-wrap' || 'activity-thumbnail-box light-grey-background-color overflow-hidden hover-elevation border-radius border-color mb-4 d-block d-sm-flex flex-sm-nowrap'} ${this.addMargin && 'my-4' || ''}`;
      },
      imageMobileStyle () {
        return {
          'max-width': '100%',
          'min-width': '100%',
          'min-height': '100%',
        };
      },
      getActivityViews () {
        return this.activityTypeExtension && this.activityTypeExtension.getActivityViews;
      },
      activityViewsTooltip () {
        return this.activityViews?.tooltip && this.$t(this.activityViews.tooltip, { 0: this.activityViews?.originalViewsCount });
      },
      activityViewsCount () {
        return this.activityViews?.viewsCount;
      },
    },
    watch: {
      activityTypeExtension (newVal, oldVal) {
        if (!oldVal || newVal !== oldVal) {
          this.retrieveActivityProperties();
        }
      },
    },
    created () {
      this.retrieveActivityProperties();
      window.addEventListener('resize', this.displayReadMore);
    },
    mounted () {
      this.displayReadMore();
    },
    beforeUnmount () {
      window.removeEventListener('resize', this.displayReadMore);
    },
    methods: {
      retrieveActivityProperties () {
        this.useEllipsisOnTitle = this.activityTypeExtension && !this.activityTypeExtension.noTitleEllipsis;
        this.useEllipsisOnSummary = this.activityTypeExtension && !this.activityTypeExtension.noSummaryEllipsis;
        this.useEmbeddedLinkView = this.activityTypeExtension && !this.activityTypeExtension.noEmbeddedLinkView;
        this.summaryLinesToDisplay = this.activityTypeExtension?.summaryLinesToDisplay || 2;
        this.regularFontSizeOnSummary = this.activityTypeExtension.regularFontSizeOnSummary === true;
        this.title = this.getTitle && this.getTitle(this.activity, this.isActivityDetail);
        if (this.title && this.title.key) {
          this.title = this.$t(this.title.key, this.title.params || {});
        } else {
          this.title = eXo.$utils.trim(this.title);
        }
        this.titleTooltip = eXo.$utils.htmlToText(this.title);
        this.summary = this.getSummary && eXo.$utils.trim(this.getSummary(this.activity, this.isActivityDetail));
        this.summaryTooltip = eXo.$utils.htmlToText(this.summary);
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
      displayReadMore () {
        const elem = this.$el?.querySelector?.('.rich-editor-content');
        this.displayReadMoreButton = elem && elem?.scrollHeight > elem?.clientHeight;
      },
      displayFullContent () {
        this.fullContent = !this.fullContent;
      },
    },
  };
</script>
