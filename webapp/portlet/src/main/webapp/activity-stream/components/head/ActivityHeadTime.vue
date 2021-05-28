<template>
  <v-list-item-subtitle class="caption text-light-color">
    <i class="uiIconClock  uiIconLightGray"></i>
    <v-tooltip bottom>
      <template v-slot:activator="{ on, attrs }">
        <a
          :title="absolutePostedTime"
          :href="activityLink"
          class="hover-underline"
          v-bind="attrs"
          v-on="on">
          <relative-date-format
            v-if="isActivityEdited"
            label="UIActivity.label.EditedFrom"
            class="text-capitalize-first-letter d-inline-block"
            :value="activity.updateDate" />
          <relative-date-format
            v-else
            :value="activity.createDate" />
        </a>
      </template>
      <date-format :value="activity.updateDate" :format="dateFormat" />
    </v-tooltip>
  </v-list-item-subtitle>
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
    dateFormat: {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: 'numeric',
      minute: 'numeric',
    },
  }),
  computed: {
    isActivityEdited() {
      return this.activity && this.activity.updateDate !== this.activity.createDate;
    },
    activityId() {
      return this.activity && this.activity.id;
    },
    activityLink() {
      return `${this.$root.activityBaseLink}?id=${this.activityId}`;
    },
    activityPostedTime() {
      if (!this.isSpaceStream) {
        return null;
      }
      return this.activity.updateDate;
    },
  },
};
</script>