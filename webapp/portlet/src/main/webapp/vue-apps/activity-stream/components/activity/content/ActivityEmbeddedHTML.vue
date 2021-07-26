<template>
  <div
    v-if="embeddedHTML"
    :style="parentStyle"
    class="d-flex flex-column flex activity-embedded-box mx-auto">
    <div
      v-if="elementReady"
      v-html="embeddedHTML"
      :style="embeddedHTMLStyle"
      class="border-radius">
    </div>
    <a
      v-if="titleText"
      :href="link"
      :target="linkTarget"
      :title="titleText"
      class="py-4 text-color">
      <div
        v-text="titleText"
        class="font-weight-bold text-color ma-0 text-wrap text-break text-truncate-2"></div>
    </a>
  </div>
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
    maxWidth: 320,
    elementReady: false,
  }),
  computed: {
    embeddedHTML() {
      return this.activity && this.activity.templateParams && this.activity.templateParams.html;
    },
    sourceLink() {
      return this.activity && this.activity.templateParams && this.activity.templateParams.link || '';
    },
    link() {
      return this.sourceLink || 'javascript:void(0)';
    },
    linkClass() {
      return this.sourceLink === '#' ? 'not-clickable' : '';
    },
    linkTarget() {
      return this.sourceLink && (this.sourceLink.indexOf('/') === 0 || this.sourceLink.indexOf('#') === 0) && '_self' || (this.sourceLink && '_blank') || '';
    },
    title() {
      return this.activity && this.activity.templateParams && this.activity.templateParams.title;
    },
    titleText() {
      return this.title && this.$utils.htmlToText(this.title) || '';
    },
    previewWidth() {
      return Number(this.activity.templateParams && this.activity.templateParams.previewWidth || this.maxWidth);
    },
    parentStyle() {
      const width = this.previewWidth > this.maxWidth && this.maxWidth || this.previewWidth;
      return {
        width: `${width}px`,
      };
    },
    embeddedHTMLStyle() {
      const width = this.previewWidth > this.maxWidth && this.maxWidth || this.previewWidth;
      return {
        width: `${width}px`,
        maxWidth: `${this.maxWidth}px`,
        maxHeight: `${this.maxWidth}px`,
      };
    },
  },
  mounted() {
    this.maxWidth = this.$el && this.$el.parentElement.offsetWidth < this.maxWidth && String(this.$el.parentElement.offsetWidth - 2) || `${this.maxWidth}`;
    this.elementReady = true;
  },
};
</script>