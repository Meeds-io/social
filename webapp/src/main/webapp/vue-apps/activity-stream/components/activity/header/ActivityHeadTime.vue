<template>
  <div
    :class="truncateText"
    class="text-subtitle">
    <v-icon
      v-if="!noIcon"
      class="icon-default-color me-1"
      x-small>
      far fa-clock
    </v-icon>
    <v-tooltip :disabled="isMobile" bottom>
      <template #activator="{ on, attrs }">
        <v-btn
          :href="activityLink"
          :height="btnHeight"
          :x-small="btnXSmall"
          :class="btnClass"
          class="hover-underline width-auto px-0 "
          link
          text
          plain
          v-bind="attrs"
          v-on="on">
          <span
            v-if="isScheduled"
            :class="truncateText"
            class="text-subtitle relativeDateFormatClass text-none">
            {{ scheduledForLabel }}
          </span>
          <relative-date-format
            v-else-if="isActivityEdited"
            :value="activity.updateDate"
            :short="isMobile"
            :class="truncateText"
            label="UIActivity.label.EditedFrom"
            class="text-subtitle relativeDateFormatClass text-none" />
          <relative-date-format
            v-else
            :value="activity.createDate"
            :short="isMobile"
            :class="truncateText"
            class="text-subtitle relativeDateFormatClass text-none" />
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
    isScheduled() {
      return !!this.activity?.publicationStartTime;
    },
    scheduledForLabel() {
      const scheduledDate = this.$dateUtil.formatDateObjectToDisplay(
        new Date(this.activity.publicationStartTime),
        this.dateFormat,
        eXo.env.portal.language);
      return this.$t('UIActivity.label.ScheduledFor', {0: scheduledDate});
    },
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
      if (this.isScheduled) {
        return this.activity.publicationStartTime;
      }
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
