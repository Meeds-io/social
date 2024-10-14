<template>
  <div
    v-if="initialized"
    :id="id"
    class="activity-detail overflow-hidden activity-comment-detail flex"
    dir="auto">
    <template v-if="extendedComponent">
      <extension-registry-component
        :component="extendedComponentOptions"
        :element="extendedComponent.element"
        :element-class="extendedComponent.class"
        :params="extendedComponentParams" />
    </template>
    <template v-else>
      <extension-registry-components
        :params="extendedComponentParams"
        name="ActivityContent"
        type="activity-content-extensions"
        parent-element="div"
        element="div"
        class=" d-flex flex-column" />
    </template>
  </div>
</template>

<script>
export default {
  props: {
    activity: {
      type: Object,
      default: null,
    },
    comment: {
      type: Object,
      default: null,
    },
    commentTypeExtension: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    loading: false,
    initialized: false,
    initializedEmited: false,
  }),
  computed: {
    id() {
      return `activity-comment-detail-${this.commentId}`;
    },
    commentId() {
      return this.comment && this.comment.id;
    },
    extendedComponent() {
      return this.activityExtendedComponent || this.commentExtendedComponent;
    },
    activityExtendedComponent() {
      return this.commentTypeExtension && this.commentTypeExtension.getExtendedComponent && this.commentTypeExtension.getExtendedComponent(this.activity, this.isActivityDetail);
    },
    commentExtendedComponent() {
      return this.commentTypeExtension && this.commentTypeExtension.getCommentExtendedComponent && this.commentTypeExtension.getCommentExtendedComponent(this.activity, this.comment, this.isActivityDetail);
    },
    extendedComponentOptions() {
      return this.extendedComponent && {
        componentName: `Activity-${this.commentTypeExtension.id}`,
        componentOptions: {
          vueComponent: this.extendedComponent.component,
        },
      };
    },
    extendedComponentParams() {
      return {
        activity: this.comment,
        activityTypeExtension: this.commentTypeExtension,
      };
    },
    init() {
      return this.commentTypeExtension && this.commentTypeExtension.init;
    },
    refresh() {
      return this.commentTypeExtension && this.commentTypeExtension.refresh;
    },
  },
  watch: {
    activity() {
      this.retrieveCommentProperties();
    },
    commentTypeExtension() {
      this.retrieveCommentProperties();
    },
    loading() {
      if (!this.loading) {
        this.$root.$emit('activity-comment-refreshed');
      }
    },
    initialized() {
      if (this.initialized && !this.initializedEmited) {
        this.$emit('comment-initialized');
        this.initializedEmited = true;
      }
    },
  },
  created() {
    this.retrieveCommentProperties();
    this.$root.$on('activity-comment-updated', this.commentUpdated);
  },
  mounted() {
    if (this.initialized && !this.initializedEmited) {
      this.$emit('comment-initialized');
      this.initializedEmited = true;
    }
  },
  methods: {
    commentUpdated(comment) {
      if (comment.id === this.comment.id) {
        this.comment.updated = true;
        this.retrieveCommentProperties();
      }
    },
    retrieveCommentProperties() {
      if (this.refresh && (this.comment.updated || this.comment.added)) {
        this.refresh(this.comment, false, this.activity);
      }
      delete this.comment.updated;
      delete this.comment.added;
      this.loading = true;
      this.$nextTick().then(() => {
        if (this.init) {
          const initPromise = this.init(this.comment, false, this.activity);
          if (initPromise && initPromise.finally) {
            this.initialized = false;
            return initPromise.finally(() => {
              this.initialized = true;
              this.loading = false;
            });
          }
        }
        this.loading = false;
        this.initialized = true;
      });
    },
  },
};
</script>
