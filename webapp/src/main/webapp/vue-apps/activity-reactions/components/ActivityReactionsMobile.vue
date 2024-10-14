<template>
  <div class="actionBarMobile ps-0">
    <a
      v-if="likersNumber === 1"
      class="likesNumber me-2 my-1"
      @click="open">
      {{ likersNumber }} {{ $t('UIActivity.label.single_likeLabel') }}</a>
    <a
      v-else-if="likersNumber > 1"
      class="likesNumber me-2 my-1"
      @click="open">
      {{ likersNumber }} {{ $t('UIActivity.label.likesLabel') }}</a>
    <a
      v-if="commentNumber > 0"
      class="my-1 me-2 CommentsNumber"
      @click="openComments">
      {{ commentNumber }} {{ $t('UIActivity.comment.commentsLabel') }}
    </a>
    <extension-registry-components
      :params="extensionParams"
      name="ActivityReactionsCount"
      type="activity-reaction-count"
      parent-element="div"
      element="div"
      class=" d-flex flex-column" />
  </div>
</template>
<script>
export default {
  props: {
    activity: {
      type: Object,
      default: null,
    },
    likersNumber: {
      type: Number,
      default: 0
    },
    commentNumber: {
      type: Number,
      default: 0
    }
  },
  computed: {
    extensionParams() {
      return {
        activity: this.activity,
      };
    },
  },
  methods: {
    open() {
      this.$emit('openDrawer');
    },
    openComments() {
      document.dispatchEvent(new CustomEvent('activity-comments-display', {detail: {
        activity: this.activity,
        offset: 0,
        limit: (this.commentNumber || 10) * 2, // To display all
      }}));
    },
  }
};
</script>
