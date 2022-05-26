<template>
  <v-list-item class="clickable" :href="activityUrl">
    <v-list-item-icon class="me-3 my-auto">
      <v-icon size="22" class="icon-default-color"> fas fa-file-alt </v-icon>
    </v-list-item-icon>

    <v-list-item-content>
      <v-list-item-title class="text-color body-2">
        <p
          class="ma-auto"
          v-sanitized-html="activityTitle"></p>
      </v-list-item-title>
    </v-list-item-content>

    <v-list-item-action>
      <favorite-button
        :id="id"
        :favorite="isFavorite"
        :space-id="spaceId"
        :top="top"
        :right="right"
        type="activity"
        @removed="removed"
        @remove-error="removeError" />
    </v-list-item-action>
  </v-list-item>
</template>
<script>
export default {
  props: {
    id: {
      type: String,
      default: () => null,
    },
  },
  data: () => ({
    activity: null,
    activityTitle: '',
    activityUrl: '#',
    isFavorite: true
  }),
  computed: {
    spaceId() {
      return this.activity?.activityStream?.space?.id;
    },
  },
  created() {
    this.activityUrl = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/activity?id=${this.id}`;
    this.$activityService.getActivityById(this.id)
      .then(fullActivity => {
        this.activity = fullActivity;
        if (fullActivity && fullActivity.title ) {
          this.activityTitle = this.favoriteTitle(fullActivity.title);
        } else {
          this.activityTitle = this.$t('UITopBarFavoritesPortlet.label.activity');
        }
      });
  },
  methods: {
    favoriteTitle(title) {
      const regex = /(<([^>]+)>)/ig;
      const activityTitle = title.replace(regex, '').split(' ').slice(0, 5).join(' ');
      return activityTitle;
    },
    removed() {
      this.isFavorite = !this.isFavorite;
      this.displayAlert(this.$t('Favorite.tooltip.SuccessfullyDeletedFavorite', {0: this.$t('activity.label')}));
      this.$emit('removed');
      this.$root.$emit('refresh-favorite-list');
    },
    removeError() {
      this.displayAlert(this.$t('Favorite.tooltip.ErrorDeletingFavorite', {0: this.$t('activity.label')}), 'error');
    },
    displayAlert(message, type) {
      this.$root.$emit('activity-notification-alert', {
        activityId: this.id,
        message,
        type: type || 'success',
      });
    },
  }
};
</script>
