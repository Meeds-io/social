<template>
  <v-app id="shareActivity" class="shareActivity">
    <div class="shareActivityButton" @click="openDrawer">
      <i class="uiIconRecurrence uiIconLightBlue uiIcon16x16 ml-3 py-3"></i>
      <span class="ml-1">Share</span>
    </div>
    <share-activity-drawer
      ref="shareActivityDrawer"
      :activity-id="activityId"
      :activity-description="activityDescription" />
  </v-app>
</template>

<script>
export default {
  props: {
    activityId: {
      type: String,
      default: ''
    },
    activityDescription: {
      type: String,
      default: ''
    }
  },
  methods: {
    openDrawer() {
      if (this.$refs.shareActivityDrawer) {
        this.spaces = [];
        this.$refs.shareActivityDrawer.open();
        $(`#dropDownEditActivity${this.activityId}.actLink`).removeClass('open');
        $(`#dropDownEditActivity${this.activityId}.actionLink`).removeClass('open');
      }
    },
  }
};
</script>
