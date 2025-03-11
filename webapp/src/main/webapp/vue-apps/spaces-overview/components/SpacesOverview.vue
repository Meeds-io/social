<template>
  <v-app>
    <widget-wrapper 
      extra-class="application-body"
      :title="$t('spacesOverview.label.title')">
      <v-card flat>
        <div
          v-if="(invitations > 0 || sentRequests > 0 || receivedRequests > 0 || managing > 0) && displayPlaceholder"
          class="d-flex flex-column ">
          <div
            v-if="invitations > 0 || sentRequests > 0"
            class="d-flex justify-space-around mb-5">
            <spaces-overview-card
              :id="spacesInvitationOverview"
              :count="invitations"
              icon="fas fa-user-plus"
              :title="$t('spacesOverview.label.invitations')"
              @click="$refs.spacesDrawer.open('invited', $t('spacesOverview.label.invitations'))" />
            <spaces-overview-card
              :id="spacesRequestsSentOverview"
              :count="sentRequests"
              icon="fas fa-user-clock"
              :title="$t('spacesOverview.label.sentRequests')"
              @click="$refs.spacesDrawer.open('pending', $t('spacesOverview.label.sentPendingRequests'))" />
          </div>
          <div
            v-if="receivedRequests > 0 || managing > 0"
            class="d-flex justify-space-around">
            <spaces-overview-card
              :id="spacesRequestsReceivedOverview"
              :count="receivedRequests"
              icon="fas fa-user-lock"
              :title="$t('spacesOverview.label.receivedRequests')"
              @click="$refs.spacesDrawer.open('requests', $t('spacesOverview.label.receivedRequests'))" />
            <spaces-overview-card
              :id="spacesManagingOverview"
              :count="managing"
              icon="fas fa-user-cog"
              :title="$t('spacesOverview.label.managing')"
              @click="$refs.spacesDrawer.open('manager', $t('spacesOverview.label.managedSpaces'))" />
          </div>
        </div>
        <div v-else>
          <div
            v-if="displayPlaceholder"
            class="d-flex align-center justify-center mx-lg-6">
            <v-icon
              class="tertiary--text me-3"
              size="24">
              fas fa-user-cog
            </v-icon>
            <div class="d-flex flex-column">
              <span class="text-left">{{ $t('spacesOverview.label.emptyMessage') }}</span>
            </div>
          </div>
        </div>
      </v-card>
    </widget-wrapper>
    <spaces-overview-drawer
      ref="spacesDrawer"
      @refresh="refresh($event)" />
  </v-app>    
</template>

<script>
  export default {
    data: () => ({
      invitations: 0,
      sentRequests: 0,
      receivedRequests: 0,
      managing: 0,
      loading: 0,
      loaded: false,
    }),
    computed: {
      displayPlaceholder () {
        return this.loaded;
      },
    },
    watch: {
      loading (newVal, oldVal) {
        if (oldVal && !newVal) {
          this.$root.$applicationLoaded();
          this.loaded = true;
          if (window.location.pathname.includes('receivedInvitations')) {
            this.$refs?.spacesDrawer?.open?.('invited', this.$t('spacesOverview.label.invitations'));
          }
        }
      },
    },
    created () {
      this.refresh();
    },
    methods: {
      refresh (itemType) {
        this.loading = 0;
        if (!itemType || itemType === 'invitations') {
          this.invitations = '-';
          this.loading++;
          this.$spaceService.getSpacesByFilter({
            filter: 'invited',
          })
            .then(data => this.invitations = data && data.size || 0)
            .finally(() => this.loading--);
        }
        if (!itemType || itemType === 'sentRequests') {
          this.sentRequests = '-';
          this.loading++;
          this.$spaceService.getSpacesByFilter({
            filter: 'pending',
          })
            .then(data => this.sentRequests = data?.size || 0)
            .finally(() => this.loading--);
        }
        if (!itemType || itemType === 'receivedRequests') {
          this.receivedRequests = '-';
          this.loading++;
          this.$spaceService.getSpacesByFilter({
            filter: 'requests',
          })
            .then(data => this.receivedRequests = data?.size || 0)
            .finally(() => this.loading--);
        }
        if (!itemType || itemType === 'managing') {
          this.managing = '-';
          this.loading++;
          this.$spaceService.getSpacesByFilter({
            filter: 'manager',
          })
            .then(data => this.managing = data?.size || 0)
            .finally(() => this.loading--);
        }
      },
    },
  };
</script>
