<template>
  <p
    v-if="isBodyNotEmpty && useParagraph"
    v-sanitized-html="body"
    :class="bodyClass"
    class="reset-style-box"></p>
  <div
    v-else-if="isBodyNotEmpty"
    v-sanitized-html="body"
    :class="bodyClass"
    class="reset-style-box"></div>
</template>

<script>
export default {
  props: {
    activity: {
      type: String,
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
    loading: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    body: null,
  }),
  computed: {
    getBody() {
      return this.activityTypeExtension && this.activityTypeExtension.getBody;
    },
    activityId() {
      return this.activity && this.activity.id;
    },
    isBodyNotEmpty() {
      return this.body && this.body.trim() !== '<p></p>';
    },
    useParagraph() {
      return !this.body.includes('</p>');
    },
    isComment() {
      return this.activity && this.activity.activityId;
    },
    bodyClass() {
      return this.isComment && 'rich-editor-content' || 'postContent text-color pt-0 pe-7 pb-4 ps-4';
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
  methods: {
    retrieveActivityProperties() {
      const body = this.getBody && this.getBody(this.activity, this.isActivityDetail);
      this.body = this.$utils.trim(body);
    },
  },
};
</script>