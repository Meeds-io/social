<template>
  <dynamic-html-element
    v-if="isBodyNotEmpty"
    :child="bodyElement"
    :element="element"
    :class="bodyClass"
    class="reset-style-box rich-editor-content text-break overflow-hidden"
    dir="auto" />
</template>

<script>
export default {
  props: {
    activity: {
      type: Object,
      default: null,
    },
    activityTypeExtension: {
      type: Object,
      default: null,
    },
    isActivityDetail: {
      type: Boolean,
      default: false,
    },
    loading: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    body: null,
  }),
  computed: {
    bodyElement() {
      return {
        template: ExtendedDomPurify.purify(`<div>${this.body}</div>`) || '',
      };
    },
    getBody() {
      return this.activityTypeExtension && this.activityTypeExtension.getBody;
    },
    activityId() {
      return this.activity && this.activity.id;
    },
    isBodyNotEmpty() {
      return this.body && this.$utils.trim(this.body).length;
    },
    useParagraph() {
      return !this.body.includes('</p>');
    },
    element() {
      return this.useParagraph && 'p' || 'div';
    },
    isComment() {
      return this.activity && this.activity.activityId;
    },
    bodyClass() {
      return this.isComment && 'rich-editor-content' || 'postContent text-color py-0';
    },
  },
  watch: {
    loading() {
      if (this.loading) {
        this.$nextTick().then(() => this.retrieveActivityProperties());
      }
    },
  },
  created() {
    this.retrieveActivityProperties();
  },
  mounted() {
    this.$tagService.initTags(this.$t('Tag.tooltip.startSearch'));
  },
  methods: {
    retrieveActivityProperties() {
      this.body = this.getBody && this.getBody(this.activity, this.isActivityDetail);
    },
  },
};
</script>