<template>
  <div
    :class="!displayLabel && 'd-inline-flex'">
    <v-tooltip bottom>
      <template v-if="!displayLabel" #activator="{ on, attrs }">
        <v-btn
          :id="`FavoriteLink_${type}_${id}`"
          :style="buttonStyle"
          :loading="loading"
          :disabled="loading"
          :aria-label="favoriteTooltip"
          class="pa-0 mt-0"
          icon
          :small="small"
          v-bind="attrs"
          v-on="on"
          @click="changeFavorite">
          <div class="d-flex flex-lg-row flex-column">
            <v-icon
              class="mx-auto"
              :class="favoriteIconColor"
              size="16">
              {{ favoriteIcon }}
            </v-icon>
          </div>
        </v-btn>
      </template>
      <template v-else #activator="{ on, attrs }">
        <v-list-item 
          v-bind="attrs" 
          v-on="on" 
          :aria-label="favoriteTooltip"
          @click="changeFavorite">
          <v-icon 
            class="mr-3"
            :class="favoriteIconColor"
            size="16">
            {{ favoriteIcon }}
          </v-icon>
          <span :class="extraClass" class="text-color mt-1">{{ favoriteLabel }}</span>
        </v-list-item> 
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
    typeLabel: {
      type: String,
      default: null,
    },
    entityType: {
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
    spaceId: {
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
    small: {
      type: Boolean,
      default: true,
    },
    displayLabel: {
      type: Boolean,
      default: false
    },
    extraClass: {
      type: String,
      default: () => '',
    }
  },
  data: () => ({
    isFavorite: false,
    loading: false,
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
    favoriteIconColor() {
      return this.isFavorite && 'yellow--text text--darken-2 ml-n2px' || 'icon-default-color';
    },
    favoriteIcon() {
      return this.isFavorite && 'fas fa-star' || 'far fa-star';
    },
    favoriteLabel() {
      return this.isFavorite && this.$t('Favorite.tooltip.unBookmark') || this.$t('Favorite.tooltip.bookmark');
    }
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
      this.loading = true;
      if (this.isFavorite) {
        this.$favoriteService.removeFavorite(this.type, this.id)
          .then(() => {
            document.dispatchEvent(new CustomEvent('favorite-removed', {
              detail: {
                type: this.type,
                typeLabel: this.typeLabel,
                id: this.id,
                spaceId: this.spaceId,
                entityType: this.entityType,
              }
            }));
            this.isFavorite = false;
            this.$emit('removed');
            this.updateFavorite();
          })
          .catch(() => this.$emit('remove-error'))
          .finally(() => this.loading = false);
      } else {
        this.$favoriteService.addFavorite(this.type, this.id, this.parentId, this.spaceId)
          .then(() => {
            document.dispatchEvent(new CustomEvent('favorite-added', {
              detail: {
                type: this.type,
                typeLabel: this.typeLabel,
                id: this.id,
                spaceId: this.spaceId,
                entityType: this.entityType,
              }
            }));
            this.isFavorite = true;
            this.$emit('added');
            this.updateFavorite();
          })
          .catch(() => this.$emit('add-error'))
          .finally(() => this.loading = false);
      }
    },
  },
};
</script>
