<template>
  <v-btn
    v-if="isShareable"
    :id="`ShareActivity${activityId}`"
    :title="$t('UIActivity.share.share')"
    class="pa-0 mx-2"
    text
    link
    small
    @click="openShareDrawer()">
    <v-icon
      class="disabled--text me-1"
      size="12">
      fa-share
    </v-icon>
    {{ $t('UIActivity.share.share') }}
  </v-btn>
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
  },
  computed: {
    isShareable() {
      return this.activity && this.activityTypeExtension && this.activityTypeExtension.canShare && this.activityTypeExtension.canShare(this.activity);
    },
    activityId() {
      return this.activity && this.activity.id;
    },
    activityType() {
      return this.activity && this.activity.type;
    },
  },
  methods: {
    openShareDrawer() {
      document.dispatchEvent(new CustomEvent('activity-stream-share-open', {detail: {
        activityId: this.activityId,
        activityType: this.activityType,
      }}));
    },
  },
};
</script>