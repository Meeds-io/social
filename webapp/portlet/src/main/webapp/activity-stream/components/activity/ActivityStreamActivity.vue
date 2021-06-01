<template>
  <div :id="id" class="white border-radius activity-detail">
    <activity-head :activity="activity" />
    <activity-content
      :activity-link="activityLink"
      :body="body"
      :title="title"
      :summary="summary"
      :thumbnail="thumbnail"
      :supports-thumbnail="supportsThumbnail"
      :source-link="sourceLink" />
  </div>
</template>

<script>
export default {
  props: {
    activity: {
      type: Object,
      default: null,
    },
    activityTypes: {
      type: Object,
      default: null,
    },
    isActivityDetail: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    activityLink: null,
    body: null,
    title: null,
    summary: null,
    thumbnail: null,
    sourceLink: null,
  }),
  computed: {
    id() {
      return `activity-detail-${this.activityId}`;
    },
    activityId() {
      return this.activity && this.activity.id;
    },
    activityOptions() {
      if (!this.activity || !this.activityTypes) {
        return {};
      }
      return this.activityTypes[this.activity.type] || this.activityTypes['default'] || {};
    },
    init() {
      return this.activityOptions && this.activityOptions.init;
    },
    getBody() {
      return this.activityOptions && this.activityOptions.getBody;
    },
    getTitle() {
      return this.activityOptions && this.activityOptions.getTitle;
    },
    getSummary() {
      return this.activityOptions && this.activityOptions.getSummary;
    },
    getThumbnail() {
      return this.activityOptions && this.activityOptions.getThumbnail;
    },
    getSourceLink() {
      return this.activityOptions && this.activityOptions.getSourceLink;
    },
    getActivityLink() {
      return this.activityOptions && this.activityOptions.getActivityLink;
    },
    supportsThumbnail() {
      return this.activityOptions && this.activityOptions.supportsThumbnail;
    },
  },
  watch: {
    activity() {
      this.retrieveActivityProperties();
    },
    activityOptions() {
      this.retrieveActivityProperties();
    },
  },
  created() {
    this.retrieveActivityProperties();
  },
  methods: {
    retrieveActivityProperties() {
      if (this.init) {
        const initPromise = this.init(this.activity);
        if (initPromise && initPromise.then) {
          return initPromise.then(this.computeActivityProperties);
        }
      }
      this.computeActivityProperties();
    },
    computeActivityProperties() {
      if (this.getActivityLink) {
        this.activityLink = this.getActivityLink(this.activity);
      } else {
        this.activityLink = `${this.$root.activityBaseLink}?id=${this.activityId}`;
      }
      this.body = this.getBody && this.getBody(this.activity, this.isActivityDetail);
      this.title = this.getTitle && this.getTitle(this.activity, this.isActivityDetail);
      this.summary = this.getSummary && this.getSummary(this.activity, this.isActivityDetail);
      this.sourceLink = this.getSourceLink && this.getSourceLink(this.activity, this.isActivityDetail);
      if (this.supportsThumbnail) {
        this.thumbnail = this.getThumbnail && this.getThumbnail(this.activity, this.isActivityDetail);
      }
    },
  },
};
</script>