<template>
  <div
    :class="truncateText"
    class="text-light-color">
    <v-icon
      v-if="!noIcon"
      class="text-light-color me-1"
      x-small>
      far fa-clock
    </v-icon>
    <v-tooltip bottom>
      <template #activator="{ on, attrs }">
        <v-btn
          :href="activityLink"
          :height="btnHeight"
          :x-small="btnXSmall"
          :class="btnClass"
          class="hover-underline width-auto text-capitalize-first-letter px-0 "
          link
          text
          plain
          v-bind="attrs"
          v-on="on">
          <relative-date-format
            v-if="isActivityEdited"
            :value="activity.updateDate"
            :short="isMobile"
            :class="truncateText"
            label="UIActivity.label.EditedFrom"
            class="text-capitalize-first-letter text-light-color relativeDateFormatClass" />
          <relative-date-format
            v-else
            :value="activity.createDate"
            :short="isMobile"
            :class="truncateText"
            class="text-capitalize-first-letter text-light-color relativeDateFormatClass" />
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
    },
    btnHeight() {
      return this.isMobile && '18' || '20';
    },
    btnXSmall() {
      return !this.isMobile;
    },
    btnClass() {
      return this.isMobile && 'text-caption' || ' ';
    },
    relativeDateFormatClass() {
      return !this.isMobile && 'pt-1 ps-1' || '';
    },
    truncateText() {
      return !this.isMobile && 'text-truncate' || ' ';
    }
  },
};
</script>