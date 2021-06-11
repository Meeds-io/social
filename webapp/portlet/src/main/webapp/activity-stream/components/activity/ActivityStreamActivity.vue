<template>
  <div :id="id" class="white border-radius activity-detail">
    <template v-if="extendedComponent">
      <activity-head
        v-if="!extendedComponent.overrideHeader"
        :activity="activity"
        :activity-actions="activityActions"
        :activity-type-extension="activityTypeExtension" />
      <extension-registry-component
        :component="extendedComponentOptions"
        :element="extendedComponent.element"
        :element-class="extendedComponent.class"
        :params="extendedComponentParams" />
      <activity-footer
        v-if="!extendedComponent.overrideFooter"
        :activity="activity"
        :activity-type-extension="activityTypeExtension" />
    </template>
    <template v-else>
      <activity-head
        :activity="activity"
        :activity-actions="activityActions"
        :activity-type-extension="activityTypeExtension" />
      <activity-content
        :activity-link="activityLink"
        :body="body"
        :title="title"
        :summary="summary"
        :source-link="sourceLink"
        :supports-icon="supportsIcon"
        :default-icon="defaultIcon"
        :thumbnail="thumbnail"
        :supports-thumbnail="supportsThumbnail"
        :thumbnail-properties="thumbnailProperties"
        :use-same-view-for-mobile="useSameViewForMobile"
        :tooltip="tooltip" />
      <activity-footer
        :activity="activity"
        :activity-type-extension="activityTypeExtension" />
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
    activityTypes: {
      type: Object,
      default: null,
    },
    activityActions: {
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
    tooltip: null,
  }),
  computed: {
    id() {
      return `activity-detail-${this.activityId}`;
    },
    activityId() {
      return this.activity && this.activity.id;
    },
    activityTypeExtension() {
      if (!this.activity || !this.activityTypes) {
        return {};
      }
      return this.activityTypes[this.activity.type] || this.activityTypes['default'] || {};
    },
    extendedComponent() {
      return this.activityTypeExtension && this.activityTypeExtension.getExtendedComponent && this.activityTypeExtension.getExtendedComponent(this.activity, this.isActivityDetail);
    },
    extendedComponentOptions() {
      return this.extendedComponent && {
        componentName: `Activity-${this.activityTypeExtension.id}`,
        componentOptions: {
          vueComponent: this.extendedComponent.component,
        },
      };
    },
    extendedComponentParams() {
      return this.$options && this.$options.propsData;
    },
    init() {
      return this.activityTypeExtension && this.activityTypeExtension.init;
    },
    getBody() {
      return this.activityTypeExtension && this.activityTypeExtension.getBody;
    },
    getTitle() {
      return this.activityTypeExtension && this.activityTypeExtension.getTitle;
    },
    getSummary() {
      return this.activityTypeExtension && this.activityTypeExtension.getSummary;
    },
    getThumbnail() {
      return this.activityTypeExtension && this.activityTypeExtension.getThumbnail;
    },
    getSourceLink() {
      return this.activityTypeExtension && this.activityTypeExtension.getSourceLink;
    },
    getActivityLink() {
      return this.activityTypeExtension && this.activityTypeExtension.getActivityLink;
    },
    supportsThumbnail() {
      return this.activityTypeExtension && this.activityTypeExtension.supportsThumbnail;
    },
    thumbnailProperties() {
      return this.activityTypeExtension && this.activityTypeExtension.thumbnailProperties;
    },
    useSameViewForMobile() {
      return this.activityTypeExtension && this.activityTypeExtension.useSameViewForMobile;
    },
    supportsIcon() {
      return this.supportsThumbnail || (this.activityTypeExtension && this.activityTypeExtension.supportsIcon);
    },
    getTooltip() {
      return this.activityTypeExtension && this.activityTypeExtension.getTooltip;
    },
    defaultIcon() {
      return this.activityTypeExtension && this.activityTypeExtension.defaultIcon;
    },
  },
  watch: {
    activity() {
      this.retrieveActivityProperties();
    },
    activityTypeExtension() {
      this.retrieveActivityProperties();
    },
  },
  created() {
    this.retrieveActivityProperties();
    document.addEventListener('activity-stream-activity-updated', event => {
      const activityId = event && event.detail;
      if (activityId === this.activityId) {
        this.retrieveActivityProperties();
      }
    });
  },
  methods: {
    retrieveActivityProperties() {
      if (this.init) {
        const initPromise = this.init(this.activity, this.isActivityDetail);
        if (initPromise && initPromise.then) {
          return initPromise.then(this.computeActivityProperties);
        }
      }
      this.computeActivityProperties();
    },
    computeActivityProperties() {
      if (this.extendedComponent) {
        return;
      }
      if (this.getActivityLink) {
        this.activityLink = this.getActivityLink(this.activity);
      } else {
        this.activityLink = `${this.$root.activityBaseLink}?id=${this.activityId}`;
      }
      this.body = this.getBody && this.trim(this.getBody(this.activity, this.isActivityDetail));
      this.title = this.getTitle && this.trim(this.getTitle(this.activity, this.isActivityDetail));
      this.summary = this.getSummary && this.trim(this.getSummary(this.activity, this.isActivityDetail));
      this.sourceLink = this.getSourceLink && this.getSourceLink(this.activity, this.isActivityDetail);
      this.tooltip = this.getTooltip && this.getTooltip(this.activity, this.isActivityDetail);
      if (this.supportsThumbnail) {
        this.thumbnail = this.getThumbnail && this.getThumbnail(this.activity, this.isActivityDetail);
      }
    },
    trim(text) {
      return text && text.trim() || '';
    },
  },
};
</script>