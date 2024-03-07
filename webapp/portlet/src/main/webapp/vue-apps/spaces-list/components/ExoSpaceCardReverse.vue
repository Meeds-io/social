<template>
  <v-hover v-slot="{ hover }">
    <v-card
      :id="spaceMenuParentId"
      :elevation="hover && 4 || 0"
      class="spaceCardItem d-flex flex-column"
      outlined>
      <v-btn
        absolute
        small
        icon
        class="spaceInfoIcon ma-2"
        @click="$emit('flip')">
        <v-icon size="12">fa-info</v-icon>
      </v-btn>
      <div class="d-flex pt-3 pb-2">
        <p class="font-weight-bold ma-auto">
          {{ $t('spacesList.label.description') }}
        </p>
      </div>
      <v-card-text class="align-center py-0 flex-grow-1">
        <div
          v-if="space.description"
          v-sanitized-html="space.description"
          :title="space.description"
          class="text-truncate-3"></div>
      </v-card-text>

      <template v-if="space && space.managers && space.managers.length">
        <div class="d-flex px-3 pt-3">
          <p class="font-weight-bold ma-auto">
            {{ $t('spacesList.title.managers') }}
          </p>
        </div>
        <v-card-text class="align-center flex-grow-0">
          <exo-user-avatars-list
            :users="space.managers"
            :icon-size="28"
            :margin-left="space.managers.length > 1 && 'ml-n4' || ''"
            :compact="space.managers.length > 1"
            max="5"
            clickable="'false'"
            avatar-overlay-position
            @open-detail="$root.$emit('displaySpaceManagers', space)" />
        </v-card-text>
      </template>
    </v-card>
  </v-hover>
</template>

<script>
export default {
  props: {
    space: {
      type: Object,
      default: null,
    },
  },
};
</script>

