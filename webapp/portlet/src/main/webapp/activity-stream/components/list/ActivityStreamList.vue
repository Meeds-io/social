<template>
  <div v-show="display">
    <activity-stream-activity
      v-for="activity in activities"
      :key="activity.id"
      :activity="activity"
      class="mb-6" />
  </div>
</template>

<script>
export default {
  data: () => ({
    activities: [],
    pageSize: 10,
    limit: 10,
    spaceId: eXo.env.portal.spaceId,
    userName: eXo.env.portal.userName,
    loading: false,
    display: false,
  }),
  watch: {
    loading() {
      if (this.loading) {
        document.dispatchEvent(new CustomEvent('displayTopBarLoading'));
      } else {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
        window.setTimeout(() => {
          socialUIProfile.initUserProfilePopup('ActivityStream', {});
        }, 500);
      }
    },
  },
  created() {
    this.$root.$on('activity-stream-display', () => {
      this.display = true;
      this.loadActivities();
    });
    this.$root.$on('activity-stream-hide', () => {
      this.display = false;
    });
  },
  methods: {
    loadActivities() {
      this.loading = true;
      if (this.spaceId) {
        this.$spaceService.getSpaceActivities(this.spaceId, this.limit, 'identity')
          .then(data => this.activities = data && data.activities || [])
          .finally(() => this.loading = false);
      } else {
        this.$userService.getUserActivities(this.userName, this.limit, 'identity')
          .then(data => this.activities = data && data.activities || [])
          .finally(() => this.loading = false);
      }
    },
  },
};
</script>