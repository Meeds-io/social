<template>
  <favorite-button
    :id="activityId"
    :favorite="isFavorite"
    :absolute="absolute"
    :top="top"
    :right="right"
    type="activity"
    @removed="removed"
    @remove-error="removeError"
    @added="added"
    @add-error="addError" />
</template>

<script>
export default {
  props: {
    activity: {
      type: Object,
      default: null,
    },
    absolute: {
      type: Boolean,
      default: false,
    },
    top: {
      type: Number,
      default: () => 0,
    },
    right: {
      type: Number,
      default: () => 0,
    },
  },
  data: () => ({
    isFavorite: false,
  }),
  computed: {
    activityId() {
      return this.activity && this.activity.id;
    },
  },
  created() {
    this.isFavorite = this.activity && this.activity.metadatas && this.activity.metadatas.favorites && this.activity.metadatas.favorites.length;
  },
  methods: {
    removed() {
      this.displayAlert(this.$t('Favorite.tooltip.SuccessfullyDeletedFavorite', {0: this.$t('activity.label')}));
      this.$emit('removed');
    },
    removeError() {
      this.displayAlert(this.$t('Favorite.tooltip.ErrorDeletingFavorite', {0: this.$t('activity.label')}), 'error');
    },
    added() {
      this.displayAlert(this.$t('Favorite.tooltip.SuccessfullyAddedAsFavorite', {0: this.$t('activity.label')}));
      this.$emit('added');
    },
    addError() {
      this.displayAlert(this.$t('Favorite.tooltip.ErrorAddingAsFavorite', {0: this.$t('activity.label')}), 'error');
    },
    displayAlert(message, type) {
      this.$root.$emit('activity-notification-alert', {
        activityId: this.activityId,
        message,
        type: type || 'success',
      });
    },
  },
};
</script>