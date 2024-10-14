<template>
  <div
    :class="collapsed && !fullContent && readMore && 'mb-3'"
    class="position-relative">
    <dynamic-html-element
      v-if="isBodyNotEmpty"
      :child="bodyElement"
      :element="element"
      :class="bodyClass"
      class="reset-style-box rich-editor-content text-break"
      dir="auto" />
    <v-btn
      v-if="collapsed && !fullContent && readMore"
      :aria-label="$t('UIActivity.label.seeMore')"
      :class="!isComment && 'linear-gradient-white-background' || 'linear-gradient-comment-background'"
      class="d-flex ms-auto mb-0 pb-2px pl-2 pr-0 height-auto position-absolute text-light-color r-0 b-0 hover-underline hover-blue-color"
      text
      plain
      @click="displayFullContent">
      <span class="pl-6">{{ $t('UIActivity.label.seeMore') }}</span>
    </v-btn>
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
    collapsed: {
      type: Boolean,
      default: true
    }
  },
  data: () => ({
    body: null,
    fullContent: false,
    displayReadMoreButton: false
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
      return `${this.isComment && 'rich-editor-content' || 'postContent text-color py-0'} ${this.collapsed && !this.fullContent && 'text-truncate-4' || ''}`;
    },
    readMore() {
      return this.displayReadMoreButton;
    }
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
    window.addEventListener('resize', this.displayReadMore);
  },
  mounted() {
    this.$tagService.initTags(this.$t('Tag.tooltip.startSearch'));
    this.displayReadMore();
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.displayReadMore);
  },
  methods: {
    retrieveActivityProperties() {
      this.body = this.getBody && this.getBody(this.activity, this.isActivityDetail);
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
