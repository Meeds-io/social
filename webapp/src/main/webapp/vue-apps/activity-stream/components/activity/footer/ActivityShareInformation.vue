<template>
  <div class="activity-share-information d-flex flex-column align-start">
    <div class="activity-share-stream d-flex align-center flex-row flex text-truncate">
      <exo-user-avatar
        :identity="sharedPoster"
        :size="25"
        class="activity-share-user"
        popover
        bold-title
        link-style />
      <div class="createIn me-2 my-auto">
        <span class="uiIconArrowRightMini uiIconLightGray"></span>
      </div>
      <exo-space-avatar
        :space="space"
        :size="24"
        avatar-class="border-color"
        class="activity-share-space d-inline-block my-auto"
        popover
        bold-title
        link-style />
    </div>
    <div class="activity-share-message">
      <extension-registry-components
        :params="extendedComponentParams"
        name="ActivityContent"
        type="activity-content-extensions"
        parent-element="div"
        element="div"
        class="d-flex flex-column mt-1" />
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
    activityTypes: {
      type: Object,
      default: null,
    },
  },
  computed: {
    extendedComponentParams() {
      return {
        activity: this.activity,
        activityTypeExtension: this.activityTypes['default'],
      };
    },
    sharedPoster() {
      return this.activity && this.activity.identity && this.activity.identity.profile;
    },
    space() {
      return this.activity && this.activity.activityStream && this.activity.activityStream.space;
    },
  },
};
</script>