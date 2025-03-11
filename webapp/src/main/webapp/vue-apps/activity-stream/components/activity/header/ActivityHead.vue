<template>
  <v-list-item
    class="activity-head"
    dense>
    <template v-if="isMobile">
      <activity-mobile-head
        :activity="activity"
        class="px-0"
        :is-activity-shared="isActivityShared"
        :poster-identity="posterIdentity"
        :space="space" />
    </template>
    <template v-else>
      <exo-user-avatar
        avatar
        :extra-class="'me-2'"
        :identity="posterIdentity"
        popover
        :size="45" />
      <v-list-item-content class="py-0 accountTitleLabel">
        <v-list-item-title class="d-flex align-center mb-0">
          <exo-user-avatar
            bold-title
            fullname
            :identity="posterIdentity"
            link-style
            popover
            username-class />
          <template v-if="space">
            <v-icon
              v-if="$vuetify.rtl"
              class="mx-1 ps-1"
              size="8">
              fa-chevron-left
            </v-icon>
            <v-icon
              v-else
              class="mx-1 ps-1"
              size="8">
              fa-chevron-right
            </v-icon>
            <exo-space-avatar
              bold-title
              link-style
              popover
              :size="20"
              :space="space" />
          </template>
        </v-list-item-title>
        <activity-head-time
          :activity="activity"
          class="d-flex activity-head-time"
          :is-activity-shared="isActivityShared" />
      </v-list-item-content>
    </template>
    <extension-registry-components
      class="d-flex flex-no-wrap mx-0 mt-0 mb-auto activity-header-actions"
      element="div"
      element-class="mx-auto activity-header-action"
      name="ActivityHeader"
      :params="params"
      parent-element="div"
      type="activity-header-actions" />
    <activity-head-menu
      :activity="activity"
      :activity-actions="activityActions"
      :activity-type-extension="activityTypeExtension"
      :is-activity-detail="isActivityDetail" />
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
        default: () => false,
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
      space () {
        return this.activity && this.activity.activityStream && this.activity.activityStream.space;
      },
      params () {
        return {
          activity: this.activity,
          activityTypeExtension: this.activityTypeExtension,
          isActivityShared: this.isActivityShared,
        };
      },
      posterIdentity () {
        return this.activity && this.activity.identity && this.activity.identity.profile && this.activity.identity.profile.dataEntity;
      },
      isMobile () {
        return this.$vuetify.breakpoint.name === 'xs' || this.$vuetify.breakpoint.name === 'sm';
      },
    },
  };
</script>