<template>
  <p
    v-if="useParagraph"
    v-sanitized-html="commentBody"
    class="rich-editor-content"></p>
  <div
    v-else
    v-sanitized-html="commentBody"
    class="rich-editor-content"></div>
</template>

<script>
export default {
  props: {
    comment: {
      type: String,
      default: null,
    },
    commentTypes: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    initialized: false,
  }),
  computed: {
    commentExtensionType() {
      return this.commentTypes && this.comment && this.comment.type && this.commentTypes[this.comment.type];
    },
    commentBody() {
      if (this.commentExtensionType && this.commentExtensionType.getBody) {
        return this.initialized && this.commentExtensionType.getBody(this.comment) || '';
      } else {
        return this.comment && (this.comment.body || this.comment.title) || '';
      }
    },
    useParagraph() {
      return !this.commentBody.includes('</p>');
    },
  },
  created() {
    if (this.commentExtensionType && this.commentExtensionType.init) {
      const initPromise = this.commentExtensionType.init(this.comment);
      if (initPromise && initPromise.then) {
        return initPromise.then(() => this.initialized = true);
      }
    }
    this.initialized = true;
  },
};
</script>