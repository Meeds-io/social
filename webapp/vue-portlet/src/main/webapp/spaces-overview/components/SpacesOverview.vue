<template>
  <v-app>
    <v-card class="pa-4 text-center" flat>
      <v-card class="border-box-sizing d-flex flex-row justify-center ma-0" flat>
        <spaces-overview-card
          :id="spacesInvitationOverview"
          :title="$t('spacesOverview.label.invitations')"
          :count="invitations"
          @click="$refs.spacesDrawer.open('invited', $t('spacesOverview.label.invitations'))" />
        <v-divider class="spacesOverviewVertivalSeparator ma-auto" vertical />
        <spaces-overview-card
          :id="spacesRequestsSentOverview"
          :title="$t('spacesOverview.label.requestsSent')"
          :count="sentRequests"
          @click="$refs.spacesDrawer.open('pending', $t('spacesOverview.label.sentPendingRequests'))" />
      </v-card>
      <v-divider class="spacesOverviewHorizontalSeparator ma-auto" />
      <v-card class="border-box-sizing d-flex flex-row justify-center ma-0" flat>
        <spaces-overview-card
          :id="spacesRequestsReceivedOverview"
          :title="$t('spacesOverview.label.requestsReceived')"
          :count="receivedRequests" />
        <v-divider class="spacesOverviewVertivalSeparator ma-auto" vertical />
        <spaces-overview-card
          :id="spacesManagingOverview"
          :title="$t('spacesOverview.label.managing')"
          :count="managing"
          @click="$refs.spacesDrawer.open('manager', $t('spacesOverview.label.managedSpaces'))" />
      </v-card>
    </v-card>

    <spaces-overview-drawer ref="spacesDrawer" />
  </v-app>    
</template>

<script>
import * as spaceService from '../../common/js/SpaceService.js'; 

export default {
  data: () => ({
    invitations: 0,
    sentRequests: 0,
    receivedRequests: 0,
    managing: 0,
  }),
  created() {
    spaceService.getSpaces(null, null, null, 'manager')
      .then(data => {
        this.managing = data && data.size || 0;
      });
    spaceService.getSpaces(null, null, null, 'pending')
      .then(data => {
        this.sentRequests = data && data.size || 0;
      });
    spaceService.getSpaces(null, null, null, 'invited')
      .then(data => {
        this.invitations = data && data.size || 0;
      });
  },
};
</script>

