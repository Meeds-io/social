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
  },
  data: () => ({
    isFavorite: false,
  }),
  computed: {
    buttonStyle() {
      return {position: 'absolute', top: '8px', right: '30px'};
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
          });
      } else {
        this.$favoriteService.addFavorite('activity', this.activityId)
          .then(favorite => {
            if (!this.activity.metadatas) {
              this.activity.metadatas = {};
            }
            this.activity.metadatas.favorites = [favorite];
            this.computeIsFavorite();
          });
      }
    },
  },
};
</script>