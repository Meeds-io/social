<template>
  <v-app>
    <widget-wrapper :title="$t('spacesOverview.label.title')">
      <v-card flat>
        <div
          v-if="(invitations > 0 || sentRequests > 0 || receivedRequests > 0 || managing > 0) && displayPlaceholder"
          class="d-flex flex-column ">
          <div v-if="invitations > 0 || sentRequests > 0" class="d-flex justify-space-around mb-5">
            <spaces-overview-card
              :id="spacesInvitationOverview"
              :title="$t('spacesOverview.label.invitations')"
              :count="invitations"
              icon="fas fa-user-plus"
              @click="$refs.spacesDrawer.open('invited', $t('spacesOverview.label.invitations'))" />
            <spaces-overview-card
              :id="spacesRequestsSentOverview"
              :title="$t('spacesOverview.label.sentRequests')"
              :count="sentRequests"
              icon="fas fa-user-clock"
              @click="$refs.spacesDrawer.open('pending', $t('spacesOverview.label.sentPendingRequests'))" />
          </div>
          <div v-if="receivedRequests > 0 || managing > 0" class="d-flex justify-space-around">
            <spaces-overview-card
              :id="spacesRequestsReceivedOverview"
              :title="$t('spacesOverview.label.receivedRequests')"
              :count="receivedRequests"
              icon="fas fa-user-lock"
              @click="$refs.spacesDrawer.open('requests', $t('spacesOverview.label.receivedRequests'))" />
            <spaces-overview-card
              :id="spacesManagingOverview"
              :title="$t('spacesOverview.label.managing')"
              :count="managing"
              icon="fas fa-user-cog"
              @click="$refs.spacesDrawer.open('manager', $t('spacesOverview.label.managedSpaces'))" />
          </div>
        </div>
        <div v-else>
          <div v-if="displayPlaceholder" class="d-flex align-center justify-center mx-lg-6">
            <v-icon size="24" class="tertiary--text me-3">fas fa-user-cog</v-icon>
            <div class="d-flex flex-column">
              <span class="subtitle-1 text-color text-left">{{ $t('spacesOverview.label.emptyMessage') }}</span>
            </div>
          </div>
        </div>
      </v-card>
    </widget-wrapper>
    <spaces-overview-drawer ref="spacesDrawer" @refresh="refresh($event)" />
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
    loaded: false
  }),
  computed: {
    displayPlaceholder() {
      return this.loaded;
    },
  },
  watch: {
    loading(newVal, oldVal) {
      if (oldVal && !newVal) {
        this.$root.$applicationLoaded();
        this.loaded = true;
        if (window.location.pathname.includes('receivedInvitations')) {
          this.$refs?.spacesDrawer?.open?.('invited', this.$t('spacesOverview.label.invitations'));
        }
      }
    },
  },
  created() {
    this.refresh();
  },
  methods: {
    refresh(itemType) {
      this.loading = 0;
      if (!itemType || itemType === 'invitations') {
        this.invitations = '-';
        this.loading++;
        this.$spaceService.getSpaces(null, null, null, 'invited')
          .then(data => {
            this.invitations = data && data.size || 0;
          })
          .finally(() => {
            this.loading--;
          });
      }
      if (!itemType || itemType === 'sentRequests') {
        this.sentRequests = '-';
        this.loading++;
        this.$spaceService.getSpaces(null, null, null, 'pending')
          .then(data => {
            this.sentRequests = data && data.size || 0;
          })
          .finally(() => {
            this.loading--;
          });
      }
      if (!itemType || itemType === 'receivedRequests') {
        this.receivedRequests = '-';
        this.loading++;
        this.$spaceService.getSpaces(null, null, null, 'requests')
          .then(data => {
            this.receivedRequests = data && data.size || 0;
          })
          .finally(() => {
            this.loading--;
          });
      }
      if (!itemType || itemType === 'managing') {
        this.managing = '-';
        this.loading++;
        this.$spaceService.getSpaces(null, null, null, 'manager')
          .then(data => {
            this.managing = data && data.size || 0;
          })
          .finally(() => {
            this.loading--;
          });
      }
    },
  },
};
</script>
