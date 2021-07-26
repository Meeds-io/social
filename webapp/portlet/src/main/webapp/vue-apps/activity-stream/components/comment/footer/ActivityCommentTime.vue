<template>
  <div class="caption text-light-color text-truncate">
    <v-tooltip bottom>
      <template v-slot:activator="{ on, attrs }">
        <v-btn
          :href="activityLink"
          :height="20"
          class="hover-underline width-auto text-capitalize-first-letter d-inline px-0"
          x-small
          link
          text
          plain
          v-bind="attrs"
          v-on="on">
          <relative-date-format
            v-if="isActivityEdited"
            :value="activity.updateDate"
            label="TimeConvert.label.Short.Edited"
            class="text-capitalize-first-letter text-light-color text-truncate pt-1 ps-1"
            short />
          <relative-date-format
            v-else
            :value="activity.createDate"
            class="text-capitalize-first-letter text-light-color text-truncate pt-1 ps-1"
            short />
        </v-btn>
      </template>
      <date-format :value="activityPostedTime" :format="dateFormat" />
    </v-tooltip>
  </div>
</template>

<script>
export default {
  props: {
    activity: {
      type: Object,
      default: null,
    },
    noIcon: {
      type: Boolean,
      default: false,
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
      return this.activity && (this.activity.updateDate || this.activity.createDate);
    },
  },
};
</script>