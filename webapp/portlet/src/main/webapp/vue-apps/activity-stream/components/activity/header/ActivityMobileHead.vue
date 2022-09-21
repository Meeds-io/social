<template>
  <v-list-item>
    <exo-space-avatar
      :space="space"
      :size="42"
      avatar
      popover />
    <exo-user-avatar
      :identity="posterIdentity"
      :size="31"
      :extra-class="'ms-n4 mt-6'"
      popover
      avatar />
    <v-list-item-content class="py-0 accountTitleLabel">
      <v-list-item-title class="font-weight-bold d-flex body-2 mb-0">
        <exo-space-avatar
          :space="space"
          fullname
          popover
          bold-title
          link-style
          username-class />
      </v-list-item-title>
      <v-list-item-subtitle>
        <v-row class="ms-2">
          <exo-user-avatar
            :identity="posterIdentity"
            :extra-class="'me-5'"
            fullname
            popover
            link-style
            username-class />
          <activity-head-time
            :activity="activity"
            :is-activity-shared="isActivityShared"
            :is-mobile="isMobile"
            no-icon
            class="d-flex activity-head-time mt-1" />
        </v-row>
      </v-list-item-subtitle>
    </v-list-item-content>
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
    posterIdentity: {
      type: Object,
      default: null,
    },
    space: {
      type: Object,
      default: null,
    },
    isMobile: {
      type: Boolean,
      default: () => false
    },
  },
};
</script>