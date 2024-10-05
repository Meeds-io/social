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
    no-text-truncate
    class="px-1"
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
        <space-pending-button
          v-if="requestsCount"
          :count="requestsCount"
          filter="requests"
          label-key="spacesList.label.pendingRequests"
          icon="fa-user-clock"
          badge-color="error-color-background" />
        <space-pending-button
          v-if="invitationsCount"
          :count="invitationsCount"
          filter="invited"
          label-key="spacesList.label.invitationsSent"
          icon="fa-history"
          badge-color="warning-color-background" />
        <space-pending-button
          v-if="pendingCount"
          :count="pendingCount"
          filter="pending"
          label-key="spacesList.label.usersRequests"
          icon="fa-spinner"
          icon-class="fa-rotate-270"
          badge-color="info-color-background" />
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
  data: () => ({
    loading: 0,
    invitationsCount: 0,
    pendingCount: 0,
    requestsCount: 0,
  }),
  created() {
    this.$root.$on('spaces-list-refresh', this.refresh);
    this.$root.$on('space-list-pending-updated', this.refresh);
    this.refresh();
  },
  beforeDestroy() {
    this.$root.$off('spaces-list-refresh', this.refresh);
    this.$root.$off('space-list-pending-updated', this.refresh);
  },
  methods: {
    refresh() {
      this.getSpacesInvitation();
      this.getSpacesPending();
      this.getSpacesRequest();
    },
    getSpacesInvitation() {
      this.loading++;
      this.$spaceService.getSpaces(null, null, null, 'invited')
        .then(data => this.invitationsCount = data && data.size || 0)
        .finally(() => this.loading--);
    },
    getSpacesPending() {
      this.loading++;
      this.$spaceService.getSpaces(null, null, null, 'pending')
        .then(data => this.pendingCount = data?.size || 0)
        .finally(() => this.loading--);
    },
    getSpacesRequest() {
      this.loading++;
      this.$spaceService.getSpaces(null, null, null, 'requests')
        .then(data => this.requestsCount = data?.size || 0)
        .finally(() => this.loading--);
    },
  },
};
</script>

