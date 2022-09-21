<template>
  <v-list-item v-if="isSpaceStream">
    <exo-user-avatar
      :identity="posterIdentity"
      :size="42"
      avatar />
    <v-list-item-content class="py-0 accountTitleLabel ms-2">
      <v-list-item-title class="font-weight-bold d-flex body-2 mb-0">
        <exo-user-avatar
          :identity="posterIdentity"
          :extra-class="'me-5'"
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
  <v-list-item v-else>
    <exo-space-avatar
      :space="space"
      :size="42"
      avatar />
    <exo-user-avatar
      :identity="posterIdentity"
      :size="31"
      :extra-class="'ms-n4 mt-6'"
      avatar />
    <v-list-item-content class="py-0 accountTitleLabel">
      <v-list-item-title class="font-weight-bold d-flex body-2 mb-0">
        <exo-space-avatar
          :space="space"
          fullname
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
            link-style
            username-class />
          <activity-head-time
            :activity="activity"
            :is-activity-shared="isActivityShared"
            is-mobile
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
    }
  },
  computed: {
    isSpaceStream() {
      return eXo.env.portal.spaceId !== '';
    },
  },

};
</script>