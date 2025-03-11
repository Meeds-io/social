<template>
  <div class="activity-share-information d-flex flex-column align-start">
    <div class="activity-share-stream d-flex align-center flex-row flex text-truncate">
      <exo-user-avatar
        bold-title
        class="activity-share-user"
        :identity="sharedPoster"
        link-style
        popover
        :size="25" />
      <div class="createIn me-2 my-auto">
        <span class="uiIconArrowRightMini uiIconLightGray"></span>
      </div>
      <exo-space-avatar
        avatar-class="border-color"
        bold-title
        class="activity-share-space d-inline-block my-auto"
        link-style
        popover
        :size="24"
        :space="space" />
    </div>
    <div class="activity-share-message">
      <extension-registry-components
        class="d-flex flex-column mt-1"
        element="div"
        name="ActivityContent"
        :params="extendedComponentParams"
        parent-element="div"
        type="activity-content-extensions" />
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
      extendedComponentParams () {
        return {
          activity: this.activity,
          activityTypeExtension: this.activityTypes['default'],
        };
      },
      sharedPoster () {
        return this.activity && this.activity.identity && this.activity.identity.profile;
      },
      space () {
        return this.activity && this.activity.activityStream && this.activity.activityStream.space;
      },
    },
  };
</script>