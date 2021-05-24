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
            key="UIActivity.label.EditedFrom"
            class="text-capitalize-first-letter d-inline-block"
            :value="activity.updateDate" />
          <relative-date-format
            v-else
            :value="activity.createdDate"
            class="text-capitalize-first-letter d-inline-block" />
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
      return this.activity && this.activity.updateDate !== this.activity.createdDate;
    },
    activityId() {
      return this.activity && this.activity.id;
    },
    activityLink() {
      return `${eXo.env.portal.context}/${eXo.env.portal.portalName}/activity?id=${this.activityId}`;
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