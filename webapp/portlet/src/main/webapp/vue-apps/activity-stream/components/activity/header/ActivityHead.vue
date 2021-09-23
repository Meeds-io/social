<template>
  <v-list-item
    class="activity-head"
    dense>
    <activity-head-user
      :identity="posterIdentity"
      class="me-3 py-2"
      avatar />
    <v-list-item-content class="py-0 accountTitleLabel">
      <v-list-item-title class="font-weight-bold body-2 mb-0">
        <activity-head-user :identity="posterIdentity" />
        <template v-if="space">
          <v-icon
            v-if="$vuetify.rtl"
            size="8"
            class="mx-1">
            fa-chevron-left
          </v-icon>
          <v-icon
            v-else
            size="8"
            class="mx-1">
            fa-chevron-right
          </v-icon>
          <activity-head-space :space="space" />
        </template>
      </v-list-item-title>
      <activity-head-time :activity="activity" class="d-flex activity-head-time" />
    </v-list-item-content>
    <extension-registry-components
      :params="params"
      class="d-flex flex-no-wrap mx-0 mt-0 mb-auto activity-header-actions"
      name="ActivityHeader"
      type="activity-header-actions"
      parent-element="div"
      element="div"
      element-class="mx-auto activity-header-action" />
    <activity-head-menu
      :activity="activity"
      :activity-actions="activityActions"
      :activity-type-extension="activityTypeExtension" />
  </v-list-item>
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
    activityActions: {
      type: Object,
      default: null,
    },
    spaceStream: {
      type: Object,
      default: null,
    },
  },
  computed: {
    space() {
      return this.activity && this.activity.activityStream && this.activity.activityStream.space;
    },
    params() {
      return {
        activity: this.activity,
      };
    },
    posterIdentity() {
      return this.activity && this.activity.identity;
    },
  },
};
</script>