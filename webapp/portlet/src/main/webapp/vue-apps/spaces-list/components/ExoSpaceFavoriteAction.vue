<template>
  <favorite-button
    :id="spaceId"
    type="space"
    :space-id="spaceId"
    :favorite="isFavorite"
    :absolute="absolute"
    :top="top"
    :right="right"
    type-label="space"
    @removed="removed"
    @remove-error="removeError"
    @added="added"
    @add-error="addError"
    class="pt-1 pe-1" />
</template>

<script>
export default {
  props: {
    space: {
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
    spaceId() {
      return this.space?.id;
    },
  },
  created() {
    // eslint-disable-next-line eqeqeq
    this.isFavorite = this.space?.isFavorite == 'true';
  },
  methods: {
    removed() {
      this.displayAlert(this.$t('Favorite.tooltip.SuccessfullyDeletedFavorite', {0: this.$t('spaceList.alert.label')}));
      this.$emit('removed');
    },
    removeError() {
      this.displayAlert(this.$t('Favorite.tooltip.ErrorDeletingFavorite', {0: this.$t('spaceList.alert.label')}), 'error');
    },
    added() {
      this.displayAlert(this.$t('Favorite.tooltip.SuccessfullyAddedAsFavorite', {0: this.$t('spaceList.alert.label')}));
      this.$emit('added');
    },
    addError() {
      this.displayAlert(this.$t('Favorite.tooltip.ErrorAddingAsFavorite', {0: this.$t('spaceList.alert.label')}), 'error');
    },
    displayAlert(message, type) {
      this.$root.$emit('space-notification-alert', {
        activityId: this.activityId,
        message,
        type: type || 'success',
      });
    },
  },
};
</script>