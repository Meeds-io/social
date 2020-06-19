<template>
  <v-card :id="spaceMenuParentId" class="spaceCardItem d-flex flex-column" flat>
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
    <v-card-text class="align-center pa-0 flex-grow-1">
      <ellipsis
        v-show="space.description"
        :title="space.description"
        :data="space.description"
        :line-clamp="3"
        end-char="...">
      </ellipsis>
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
          max="5"
          @open-detail="$root.$emit('displaySpaceManagers', space)" />
      </v-card-text>
    </template>
  </v-card>
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

