<template>
  <div
    class="d-inline-flex">
    <v-tooltip bottom>
      <template v-slot:activator="{ on, attrs }">
        <v-btn
          :id="`FavoriteLink_${type}_${id}`"
          :style="buttonStyle"
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
    type: {
      type: String,
      default: null,
    },
    id: {
      type: String,
      default: null,
    },
    parentId: {
      type: String,
      default: null,
    },
    favorite: {
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
  },
  data: () => ({
    isFavorite: false,
  }),
  computed: {
    buttonStyle() {
      if (this.absolute) {
        if (this.$vuetify.rtl) {
          return {position: 'absolute', top: `${this.top}px`, left: `${this.right}px`};
        } else {
          return {position: 'absolute', top: `${this.top}px`, right: `${this.right}px`};
        }
      }
      return '';
    },
    favoriteTooltip() {
      return this.isFavorite && this.$t('Favorite.tooltip.DeleteFavorite') || this.$t('Favorite.tooltip.AddAsFavorite');
    },
    favoriteIcon() {
      return this.isFavorite && 'fa-star' || 'far fa-star';
    },
  },
  watch: {
    favorite: {
      immediate: true,
      handler(newVal) {
        this.isFavorite = newVal;
      },
    },
  },
  created() {
    document.addEventListener('metadata.favorite.updated', this.favoriteUpdated);
  },
  destroyed() {
    document.removeEventListener('metadata.favorite.updated', this.favoriteUpdated);
  },
  methods: {
    favoriteUpdated(event) {
      const metadata = event && event.detail;
      if (metadata && metadata.objectType === this.type && metadata.objectId === this.id && metadata.favorite !== this.isFavorite) {
        this.isFavorite = metadata.favorite;
      }
    },
    updateFavorite() {
      document.dispatchEvent(new CustomEvent('metadata.favorite.updated', {detail: {
        objectType: this.type,
        objectId: this.id,
        favorite: this.isFavorite,
      }}));
    },
    changeFavorite(event) {
      if (event) {
        event.stopPropagation();
        event.preventDefault();
      }
      if (this.isFavorite) {
        this.$favoriteService.removeFavorite(this.type, this.id)
          .then(() => {
            this.isFavorite = false;
            this.$emit('removed');
            this.updateFavorite();
          })
          .catch(() => this.$emit('remove-error'));
      } else {
        this.$favoriteService.addFavorite(this.type, this.id, this.parentId)
          .then(() => {
            this.isFavorite = true;
            this.$emit('added');
            this.updateFavorite();
          })
          .catch(() => this.$emit('add-error'));
      }
    },
  },
};
</script>