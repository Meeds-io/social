<template>
  <favorite-button
    :id="spaceId"
    :space-id="spaceId"
    :favorite="favorite"
    :absolute="absolute"
    :top="top"
    :right="right"
    type="space"
    type-label="space"
    :entity-type="entityType"
    :display-label="displayLabel"
    @removed="removed"
    @remove-error="removeError"
    @added="added"
    @add-error="addError"
    :class="!displayLabel && 'pt-1 pe-1'" />
</template>

<script>
export default {
  props: {
    spaceId: {
      type: String,
      default: '',
    },
    isFavorite: {
      type: Boolean,
      default: false,
    },
    entityType: {
      type: String,
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
    displayLabel: {
      type: Boolean,
      default: false
    }
  },
  data: () => ({
    favorite: false
  }),
  created() {
    this.favorite = this.isFavorite === 'true';
  },
  methods: {
    removed() {
      this.displayAlert(this.$t('Favorite.tooltip.SuccessfullyDeletedFavorite', {0: this.$t('spaceList.alert.label')}));
      this.$emit('removed');
      document.dispatchEvent(new CustomEvent('space-favorite-removed', {detail: this.spaceId}));
    },
    removeError() {
      this.displayAlert(this.$t('Favorite.tooltip.ErrorDeletingFavorite', {0: this.$t('spaceList.alert.label')}), 'error');
    },
    added() {
      this.displayAlert(this.$t('Favorite.tooltip.SuccessfullyAddedAsFavorite', {0: this.$t('spaceList.alert.label')}));
      this.$emit('added');
      document.dispatchEvent(new CustomEvent('space-favorite-added', {detail: this.spaceId}));
    },
    addError() {
      this.displayAlert(this.$t('Favorite.tooltip.ErrorAddingAsFavorite', {0: this.$t('spaceList.alert.label')}), 'error');
    },
    displayAlert(message, type) {
      document.dispatchEvent(new CustomEvent('notification-alert', {detail: {
        message,
        type: type || 'success',
      }}));
    },
  },
};
</script>