<template>
  <div
    class="text-subtitle"
    :class="truncateText">
    <v-icon
      v-if="!noIcon"
      class="icon-default-color me-1"
      x-small>
      far fa-clock
    </v-icon>
    <v-tooltip bottom>
      <template #activator="{ on, attrs }">
        <v-btn
          class="hover-underline width-auto text-capitalize-first-letter px-0 "
          :class="btnClass"
          :height="btnHeight"
          :href="activityLink"
          link
          plain
          text
          :x-small="btnXSmall"
          v-bind="attrs"
          v-on="on">
          <relative-date-format
            v-if="isActivityEdited"
            class="text-capitalize-first-letter text-subtitle relativeDateFormatClass"
            :class="truncateText"
            label="UIActivity.label.EditedFrom"
            :short="isMobile"
            :value="activity.updateDate" />
          <relative-date-format
            v-else
            class="text-capitalize-first-letter text-subtitle relativeDateFormatClass"
            :class="truncateText"
            :short="isMobile"
            :value="activity.createDate" />
        </v-btn>
      </template>
      <date-format
        :format="dateFormat"
        :value="activityPostedTime" />
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
        default: () => false,
      },
      isMobile: {
        type: Boolean,
        default: () => false,
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
      isActivityEdited () {
        return this.activity && this.activity.updateDate !== this.activity.createDate;
      },
      activityId () {
        return this.activity && this.activity.id;
      },
      activityLink () {
        return `${this.$root.activityBaseLink}?id=${this.activityId}`;
      },
      activityPostedTime () {
        return this.activity && (this.activity.updateDate || this.activity.createDate);
      },
      btnHeight () {
        return this.isMobile && '18' || '20';
      },
      btnXSmall () {
        return !this.isMobile;
      },
      btnClass () {
        return this.isMobile && 'text-caption' || ' ';
      },
      relativeDateFormatClass () {
        return !this.isMobile && 'pt-1 ps-1' || '';
      },
      truncateText () {
        return !this.isMobile && 'text-truncate' || ' ';
      },
    },
  };
</script>