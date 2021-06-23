<template>
  <v-card-text
    v-if="isBodyNotEmpty"
    v-sanitized-html="body"
    class="postContent text-color pt-0 pe-7" />
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
  },
  created() {
    document.addEventListener('activity-updated', event => {
      const activityId = event && event.detail;
      if (activityId === this.activityId) {
        this.retrieveActivityProperties();
      }
    });

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