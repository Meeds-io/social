<template>
  <v-app>
    <v-card class="pa-4 text-center" flat>
      <v-card class="border-box-sizing d-flex flex-row justify-center ma-0" flat>
        <spaces-overview-card
          :id="spacesInvitationOverview"
          :title="$t('spacesOverview.label.invitations')"
          :count="invitations"
          :class="invitations === '-' && 'text-sub-title'"
          @click="$refs.spacesDrawer.open('invited', $t('spacesOverview.label.invitations'))"
          @refresh="refresh()" />
        <v-divider class="spacesOverviewVertivalSeparator ma-auto" vertical />
        <spaces-overview-card
          :id="spacesRequestsSentOverview"
          :title="$t('spacesOverview.label.sentRequests')"
          :count="sentRequests"
          :class="sentRequests === '-' && 'text-sub-title'"
          @click="$refs.spacesDrawer.open('pending', $t('spacesOverview.label.sentPendingRequests'))"
          @refresh="refresh()" />
      </v-card>
      <v-divider class="spacesOverviewHorizontalSeparator ma-auto" />
      <v-card class="border-box-sizing d-flex flex-row justify-center ma-0" flat>
        <spaces-overview-card
          :id="spacesRequestsReceivedOverview"
          :title="$t('spacesOverview.label.receivedRequests')"
          :count="receivedRequests"
          :class="receivedRequests === '-' && 'text-sub-title'"
          @click="$refs.spacesDrawer.open('requests', $t('spacesOverview.label.receivedRequests'))"
          @refresh="refresh()" />
        <v-divider class="spacesOverviewVertivalSeparator ma-auto" vertical />
        <spaces-overview-card
          :id="spacesManagingOverview"
          :title="$t('spacesOverview.label.managing')"
          :count="managing"
          :class="managing === '-' && 'text-sub-title'"
          @click="$refs.spacesDrawer.open('manager', $t('spacesOverview.label.managedSpaces'))"
          @refresh="refresh()" />
      </v-card>
    </v-card>
    <spaces-overview-drawer ref="spacesDrawer" />
  </v-app>    
</template>

<script>
import * as spaceService from '../../common/js/SpaceService.js'; 

export default {
  data: () => ({
    invitations: '-',
    sentRequests: '-',
    receivedRequests: '-',
    managing: '-',
  }),
  created() {
    this.refresh();
  },
  methods: {
    refresh() {
      let loading = 4;
      spaceService.getSpaces(null, null, null, 'manager')
        .then(data => {
          this.managing = data && data.size || 0;
        })
        .finally(() => {
          loading--;
          if (loading === 0) {
            document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
          }
        });
      spaceService.getSpaces(null, null, null, 'pending')
        .then(data => {
          this.sentRequests = data && data.size || 0;
        })
        .finally(() => {
          loading--;
          if (loading === 0) {
            document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
          }
        });
      spaceService.getSpaces(null, null, null, 'invited')
        .then(data => {
          this.invitations = data && data.size || 0;
        })
        .finally(() => {
          loading--;
          if (loading === 0) {
            document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
          }
        });
      spaceService.getSpaces(null, null, null, 'requests')
        .then(data => {
          this.receivedRequests = data && data.size || 0;
        })
        .finally(() => {
          loading--;
          if (loading === 0) {
            document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
          }
        });
    },
  },
};
</script>

