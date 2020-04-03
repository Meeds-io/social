<template>
  <v-card :id="spaceMenuParentId" class="spaceCardItem">
    <v-card-title>
      <v-btn icon absolute class="spaceInfoIcon">
        <v-icon small @click="$emit('flip')">fa-info</v-icon>
      </v-btn>
      <span class="ma-auto">
        {{ $t('spacesList.title.description') }}
      </span>
    </v-card-title>
    <v-card-text class="align-center pa-0">
      <ellipsis
        :title="space.description"
        :data="space.description"
        :line-clamp="3"
        end-char="...">
      </ellipsis>
    </v-card-text>

    <v-card-title class="pa-3">
      <span class="ma-auto">
        {{ $t('spacesList.title.managers') }}
      </span>
    </v-card-title>
    <v-card-text class="align-center">
      <exo-user-avatars-list :users="managers" max="5" />
    </v-card-text>
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
  computed: {
    managers() {
      return this.space && this.space.managers && this.space.managers.map(user => user.username);
    },
  },
};
</script>

