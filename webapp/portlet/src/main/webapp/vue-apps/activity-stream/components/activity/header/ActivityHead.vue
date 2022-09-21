<template>
  <v-list-item
    class="activity-head"
    dense>
    <template v-if="isMobile">
      <activity-mobile-head
        :activity="activity"
        :is-activity-shared="isActivityShared"
        :poster-identity="posterIdentity"
        :space="space"
        class="px-0" />
    </template>
    <template v-else>
      <exo-user-avatar
        :identity="posterIdentity"
        :size="45"
        :extra-class="'me-2'"
        popover
        avatar />
      <v-list-item-content class="py-0 accountTitleLabel">
        <v-list-item-title class="font-weight-bold d-flex body-2 mb-0">
          <exo-user-avatar
            :identity="posterIdentity"
            fullname
            popover
            bold-title
            link-style
            username-class />
          <template v-if="space">
            <v-icon
              v-if="$vuetify.rtl"
              size="8"
              class="mx-1 ps-1">
              fa-chevron-left
            </v-icon>
            <v-icon
              v-else
              size="8"
              class="mx-1 ps-1">
              fa-chevron-right
            </v-icon>
            <exo-space-avatar
              :space="space"
              :size="20"
              bold-title
              link-style
              popover />
          </template>
        </v-list-item-title>
        <activity-head-time
          :activity="activity"
          :is-activity-shared="isActivityShared"
          class="d-flex activity-head-time" />
      </v-list-item-content>
    </template>
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
      :is-activity-detail="isActivityDetail"
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
    isActivityShared: {
      type: Boolean,
      default: () => false
    },
    activityTypeExtension: {
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
        activityTypeExtension: this.activityTypeExtension,
        isActivityShared: this.isActivityShared
      };
    },
    posterIdentity() {
      return this.activity && this.activity.identity && this.activity.identity.profile && this.activity.identity.profile.dataEntity;
    },
    isMobile() {
      return this.$vuetify.breakpoint.name === 'xs' || this.$vuetify.breakpoint.name === 'sm';
    },
  },
};
</script>