<template>
  <div class="caption text-light-color text-truncate">
    <v-icon
      v-if="!noIcon"
      class="text-light-color"
      x-small>
      far fa-clock
    </v-icon>
    <v-tooltip bottom>
      <template #activator="{ on, attrs }">
        <v-btn
          :href="activityLink"
          :height="20"
          :disabled="isActivityShared"
          class="hover-underline width-auto text-capitalize-first-letter d-inline px-0"
          x-small
          link
          text
          plain
          v-bind="attrs"
          v-on="on">
          <relative-date-format
            v-if="isActivityEdited"
            label="UIActivity.label.EditedFrom"
            class="text-capitalize-first-letter text-light-color text-truncate pt-1 ps-1"
            :value="activity.updateDate"
            :short="isMobile" />
          <relative-date-format
            v-else
            class="text-capitalize-first-letter text-light-color text-truncate pt-1 ps-1"
            :value="activity.createDate"
            :short="isMobile" />
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
    isActivityShared: {
      type: Boolean,
      default: () => false
    },
    isMobile: {
      type: Boolean,
      default: () => false
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
    }
  },
};
</script>