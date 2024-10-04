<template>
  <application-toolbar
    id="spacesListToolbar"
    :right-text-filter="{
      minCharacters: 3,
      placeholder: $t('spacesList.label.filterSpaces'),
      tooltip: $t('spacesList.label.filterSpaces')
    }"
    :right-filter-button="{
      text: $t('spaceList.advanced.filter.button.title'),
      displayText: !$root.isMobile,
    }"
    :compact="compactDisplay || $root.isMobile"
    :filters-count="filtersCount"
    @filter-text-input-end-typing="$emit('keyword-changed', $event)"
    @filter-button-click="$root.$emit('spaces-list-filter-open', filter)"
    @loading="$emit('loading', $event)">
    <template #left>
      <div class="d-flex align-center">
        <v-btn
          v-if="canCreateSpace" 
          id="addNewSpaceButton"
          :small="$root.isMobile"
          color="primary"
          elevation="0"
          @click="$root.$emit('addNewSpace')">
          <v-icon size="18" dark>fa-plus</v-icon>
          <span class="ms-2 hidden-xs-only">
            {{ $t('spacesList.label.addNewSpace') }}
          </span>
        </v-btn>
        <div
          v-if="filterMessage"
          class="text-subtitle showingSpaceText d-none d-sm-flex ms-3">
          {{ filterMessage }}
        </div>
      </div>
    </template>
  </application-toolbar>
</template>
<script>
export default {
  props: {
    filter: {
      type: String,
      default: null,
    },
    filtersCount: {
      type: Number,
      default: () => 0,
    },
    compactDisplay: {
      type: Boolean,
      default: false
    },
    filterMessage: {
      type: String,
      default: null
    },
    canCreateSpace: {
      type: Boolean,
      default: false,
    },
  },
};
</script>

