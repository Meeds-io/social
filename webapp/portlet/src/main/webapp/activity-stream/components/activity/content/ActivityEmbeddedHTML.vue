<template>
  <div
    v-if="embeddedHTML"
    :style="parentStyle"
    class="d-flex flex-column activity-embedded-box mx-auto">
    <div
      v-html="embeddedHTML"
      :style="embeddedHTMLStyle"
      class="border-radius">
    </div>
    <div
      v-if="title"
      :href="link"
      :target="linkTarget"
      :title="tooltipText"
      class="pb-4 pt-3">
      <ellipsis
        v-if="title"
        :title="titleTooltip"
        :data="titleText"
        :line-clamp="2"
        :delay-time="200"
        end-char="..."
        class="font-weight-bold text-color ma-0 text-wrap text-break" />
    </div>
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
    parentStyle() {
      return {
        width: '450px',
        maxWidth: '450px',
      };
    },
    embeddedHTMLStyle() {
      return {
        maxHeight: '250px',
        width: '450px',
        maxWidth: '450px',
      };
    },
  },
};
</script>