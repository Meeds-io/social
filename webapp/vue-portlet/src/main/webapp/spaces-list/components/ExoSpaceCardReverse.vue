<template>
  <v-card :id="spaceMenuParentId" class="spaceCardItem d-flex flex-column">
    <v-btn
      absolute
      small
      icon
      class="spaceInfoIcon ma-2"
      @click="$emit('flip')">
      <v-icon size="14">fa-info</v-icon>
    </v-btn>
    <v-card-title class="flex-grow-0 pa-2">
      <span class="mx-auto">
        {{ $t('spacesList.label.description') }}
      </span>
    </v-card-title>
    <v-card-text class="align-center pa-0 flex-grow-1">
      <ellipsis
        :title="space.description"
        :data="space.description"
        :line-clamp="3"
        end-char="...">
      </ellipsis>
    </v-card-text>

    <template v-if="space && space.managers && space.managers.length">
      <v-card-title class="pa-3 flex-grow-0">
        <span class="ma-auto">
          {{ $t('spacesList.title.managers') }}
        </span>
      </v-card-title>
      <v-card-text class="align-center flex-grow-0">
        <exo-user-avatars-list :space="space" max="5" />
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

