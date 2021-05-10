<template>
  <v-app :id="id" class="shareActivity">
    <div id="shareActivityButton" class="shareActivityButton" @click="openDrawer">
      <i class="uiIconRecurrence uiIconLightBlue uiIcon16x16 ml-3 py-3"></i>
      <span class="ml-1">Share</span>
    </div>
    <share-activity-drawer
      ref="activityDrawer"
      :activity-id="activityId"
      :activity-type="activityType" />
  </v-app>
</template>

<script>
export default {
  props: {
    activityId: {
      type: String,
      default: ''
    },
    activityType: {
      type: String,
      default: ''
    },
    id: {
      type: String,
      default: ''
    }
  },
  methods: {
    openDrawer() {
      if (this.$refs.activityDrawer) {
        $(`#dropDownEditActivity${this.activityId} #dropdown-menu`).removeClass('dropdown-menu');
        $(`#dropDownEditActivity${this.activityId} #dropdown-menu li a`).each(function() {
          $(this).hide();
        });
        $(`#dropDownEditActivity${this.activityId} #dropdown-menu #shareActivityButton`).hide();
        this.spaces = [];
        this.$refs.activityDrawer.open();
      }
    },
  }
  
};
</script>
