<template>
  <div v-if="!isActivityShared" class="d-inline-flex">
    <v-tooltip :disabled="!isScheduled" bottom>
      <template #activator="{ on, attrs }">
        <div
          :aria-disabled="isScheduled"
          :aria-label="isScheduled && $t('activityStream.scheduledActionsDisabled') || null"
          v-bind="attrs"
          v-on="on">
          <favorite-button
            :id="metadataObjectId"
            :type="metadataObjectType"
            :space-id="spaceId"
            :favorite="isFavorite"
            :absolute="absolute"
            :top="top"
            :right="right"
            :template-params="templateParams"
            :type-label="extensionName"
            :disabled="isScheduled"
            :disabled-label="isScheduled && $t('activityStream.scheduledActionsDisabled') || null"
            @removed="removed"
            @remove-error="removeError"
            @added="added"
            @add-error="addError" />
        </div>
      </template>
      <span>{{ $t('activityStream.scheduledActionsDisabled') }}</span>
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
    activityTypeExtension: {
      type: String,
      default: null,
    },
    isActivityShared: {
      type: Boolean,
      default: false,
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
    isScheduled: {
      type: Boolean,
      default: () => false
    },
  },
  data: () => ({
    isFavorite: false,
  }),
  computed: {
    activityId() {
      return this.activity && this.activity.id;
    },
    spaceId() {
      return this.activity && this.activity.activityStream && this.activity.activityStream.space && this.activity.activityStream.space.id;
    },
    templateParams() {
      return this.activity && this.activity.templateParams;
    },
    extensionName() {
      return this.activityTypeExtension && this.activityTypeExtension.name;
    },
    metadataObjectId() {
      return this.activity?.templateParams?.metadataObjectId || this.activityId;
    },
    metadataObjectType() {
      return this.activity?.templateParams?.metadataObjectType || 'activity';
    },
  },
  created() {
    this.isFavorite = this.activity?.metadatas?.favorites?.length;
  },
  methods: {
    removed() {
      this.displayAlert(this.$t('Favorite.tooltip.SuccessfullyDeletedFavorite', {0: this.$t('activity.label')}));
      this.$emit('removed');
      this.$root.$emit('activity-favorite-removed', this.activity);
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
      this.$root.$emit('alert-message',  message, type || 'success');
    },
  },
};
</script>