<template>
  <v-app 
    id="spacesListApplication"
    class="transparent"
    flat>
    <v-card flat>
      <exo-spaces-toolbar
        :keyword="keyword"
        :filter="filter"
        :spaces-size="spacesSize"
        @keyword-changed="keyword = $event"
        @filter-changed="filter = $event" />
      <v-card-subtitle class="d-sm-none align-center mb-2">
        {{ $t('spacesList.label.spacesSize', {0: spacesSize}) }}
      </v-card-subtitle>
      <exo-spaces-card-list
        :keyword="keyword"
        :filter="filter"
        :loading-spaces="loadingSpaces"
        :spaces-size="spacesSize"
        @spaces-size-changed="spacesSize = $event" />
      <v-card-actions id="spacesListFooter" class="px-5 border-box-sizing">
        <v-btn
          v-if="canShowMore"
          :loading="loadingSpaces"
          :disabled="loadingSpaces"
          class="loadMoreButton ma-auto btn"
          block
          @click="loadNextPage">
          {{ $t('spacesList.button.showMore') }}
        </v-btn>
      </v-card-actions>
    </v-card>

    <exo-space-managers-drawer />
    <exo-space-form-drawer />
  </v-app>    
</template>

<script>

export default {
  props: {
    appId: {
      type: String,
      default: null,
    },
    filter: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    keyword: null,
    spacesSize: 0,
    loadingSpaces: false,
  }),
};
</script>

