<template>
  <v-list-item
    class="pa-4 activity-head"
    dense>
    <activity-head-user :identity="posterIdentity" avatar />
    <v-list-item-content class="py-0 accountTitleLabel">
      <v-list-item-title class="font-weight-bold body-2 mb-0">
        <activity-head-user :identity="posterIdentity" />
        <template v-if="isSpaceStream">
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
          <activity-head-space :pretty-name="spacePrettyName" />
        </template>
      </v-list-item-title>
      <activity-head-time :activity="activity" />
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
  },
  data: () => ({
    space: null,
  }),
  computed: {
    isSpaceStream() {
      return this.activity && this.activity.activityStream && this.activity.activityStream.type === 'space';
    },
    spacePrettyName() {
      return this.activity && this.activity.activityStream && this.activity.activityStream.id;
    },
    posterIdentity() {
      return this.activity && this.activity.identity;
    },
  },
};
</script>