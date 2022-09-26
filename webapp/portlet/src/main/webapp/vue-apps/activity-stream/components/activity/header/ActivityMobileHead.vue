<template>
  <v-list-item v-if="displayUserAvatar" class="text-truncate-1">
    <exo-user-avatar
      :identity="posterIdentity"
      :size="42"
      avatar />
    <v-list-item-content class="py-0 accountTitleLabel ms-2">
      <v-list-item-title class="font-weight-bold d-flex body-2 mb-0">
        <exo-user-avatar
          :identity="posterIdentity"
          :extra-class="'me-5 text-truncate-1'"
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
          class="d-flex activity-head-time mt-1 ms-n1" />
      </v-list-item-subtitle>
    </v-list-item-content>
  </v-list-item>
  <v-list-item v-else class="text-truncate-1">
    <exo-space-avatar
      :space="space"
      :extra-class="'text-truncate-1'"
      :size="42"
      avatar />
    <exo-user-avatar
      :identity="posterIdentity"
      :size="31"
      :extra-class="'ms-n4 mt-6'"
      avatar />
    <v-list-item-content class="py-0 accountTitleLabel text-truncate-1">
      <v-list-item-title class="font-weight-bold d-flex body-2 mb-0">
        <exo-space-avatar
          :space="space"
          :extra-class="'text-truncate-1'"
          fullname
          bold-title
          link-style
          username-class />
      </v-list-item-title>
      <v-list-item-subtitle>
        <v-row class="ms-2 text-truncate-1">
          <v-col class="px-0 py-0 text-truncate-1" cols="9">
            <exo-user-avatar
              :identity="posterIdentity"
              :extra-class="'text-truncate-1'"
              fullname
              link-style
              username-class />
          </v-col>
          <v-col class="px-0 py-0 subTitle-2 align-center" cols="3">
            <activity-head-time
              :activity="activity"
              :is-activity-shared="isActivityShared"
              is-mobile
              no-icon
              class="activity-head-time pt-0 ps-0" />
          </v-col>
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
    }
  },
  computed: {
    displayUserAvatar() {
      return eXo.env.portal.spaceId !== '' || !this.space;
    },
  },

};
</script>