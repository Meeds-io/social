<template>
  <v-list-item v-if="displayUserAvatar" class="text-truncate">
    <exo-user-avatar
      :identity="posterIdentity"
      :size="42"
      avatar />
    <v-list-item-content class="py-0 accountTitleLabel ms-2">
      <v-list-item-title class="font-weight-bold d-flex body-2 mb-0">
        <exo-user-avatar
          :identity="posterIdentity"
          extra-class="me-5 text-truncate"
          fullname
          bold-title
          link-style
          username-class />
      </v-list-item-title>
      <v-list-item-subtitle>
        <activity-head-time
          :activity="activity"
          :is-activity-shared="isActivityShared"
          is-mobile
          no-icon
          class="d-flex activity-head-time" />
      </v-list-item-subtitle>
    </v-list-item-content>
  </v-list-item>
  <v-list-item v-else class="text-truncate">
    <exo-space-avatar
      :space="space"
      extra-class="text-truncate"
      :size="42"
      avatar />
    <exo-user-avatar
      :identity="posterIdentity"
      :size="31"
      extra-class="ms-n4 mt-6"
      avatar />
    <v-list-item-content class="py-0 accountTitleLabel text-truncate">
      <v-list-item-title class="font-weight-bold d-flex body-2 mb-0">
        <exo-space-avatar
          :space="space"
          extra-class="text-truncate"
          fullname
          bold-title
          link-style
          username-class />
      </v-list-item-title>
      <v-list-item-subtitle class="d-flex flex-row align-center flex-nowrap">
        <exo-user-avatar
          :identity="posterIdentity"
          extra-class="text-truncate ms-2 me-1"
          fullname
          link-style
          small-font-size
          username-class />
        <activity-head-time
          :activity="activity"
          :is-activity-shared="isActivityShared"
          is-mobile
          no-icon />
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
    }
  },
  computed: {
    displayUserAvatar() {
      return eXo.env.portal.spaceId !== '' || !this.space;
    },
  },

};
</script>