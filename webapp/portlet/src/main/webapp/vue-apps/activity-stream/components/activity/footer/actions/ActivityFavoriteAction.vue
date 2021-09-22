<template>
  <div
    class="d-inline-flex"
    :style="buttonStyle">
    <v-tooltip bottom>
      <template v-slot:activator="{ on, attrs }">
        <v-btn
          :id="`FavoriteLink${activityId}`"
          class="pa-0 mt-0"
          icon
          small
          v-bind="attrs"
          v-on="on"
          @click="changeFavorite">
          <div class="d-flex flex-lg-row flex-column">
            <v-icon
              class="mx-auto"
              color="orange"
              size="14">
              {{ favoriteIcon }}
            </v-icon>
          </div>
        </v-btn>
      </template>
      <span>
        {{ favoriteTooltip }}
      </span>
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
    top: {
      type: String,
      default: () => '8px',
    },
    right: {
      type: Object,
      default: () => '30px',
    },
  },
  data: () => ({
    isFavorite: false,
  }),
  computed: {
    buttonStyle() {
      if (this.$vuetify.rtl) {
        return {position: 'absolute', top: this.top, left: this.right};
      } else {
        return {position: 'absolute', top: this.top, right: this.right};
      }
    },
    activityId() {
      return this.activity && this.activity.id;
    },
    favoriteTooltip() {
      return this.isFavorite && this.$t('Favorite.tooltip.DeleteFavorite') || this.$t('Favorite.tooltip.AddAsFavorite');
    },
    favoriteIcon() {
      return this.isFavorite && 'fa-star' || 'far fa-star';
    },
  },
  created() {
    this.computeIsFavorite();
  },
  methods: {
    computeIsFavorite() {
      this.isFavorite = this.activity && this.activity.metadatas && this.activity.metadatas.favorites && this.activity.metadatas.favorites.length;
    },
    changeFavorite() {
      if (this.isFavorite) {
        this.$favoriteService.removeFavorite(this.activity.metadatas.favorites[0].id)
          .then(() => {
            delete this.activity.metadatas.favorites;
            this.computeIsFavorite();
            this.displayAlert(this.$t('Favorite.tooltip.SuccessfullyDeletedFavorite', {0: this.$t('activity.label')}));
            this.$emit('removed');
          })
          .catch(() => this.displayAlert(this.$t('Favorite.tooltip.ErrorDeletingFavorite', {0: this.$t('activity.label')})), 'error');
      } else {
        this.$favoriteService.addFavorite('activity', this.activityId)
          .then(favorite => {
            if (!this.activity.metadatas) {
              this.activity.metadatas = {};
            }
            this.activity.metadatas.favorites = [favorite];
            this.computeIsFavorite();
            this.displayAlert(this.$t('Favorite.tooltip.SuccessfullyAddedAsFavorite', {0: this.$t('activity.label')}));
            this.$emit('added');
          })
          .catch(() => this.displayAlert(this.$t('Favorite.tooltip.ErrorAddingAsFavorite', {0: this.$t('activity.label')})), 'error');
      }
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