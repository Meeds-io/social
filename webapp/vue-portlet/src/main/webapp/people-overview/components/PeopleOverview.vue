<template>
  <v-app>
    <v-card class="pa-4 text-center" flat>
      <v-card class="border-box-sizing d-flex flex-row justify-center ma-0" flat>
        <people-overview-card
          id="peopleInvitationsOverview"
          :title="$t('peopleOverview.label.invitations')"
          :count="invitations"
          :class="invitations === '-' && 'text-sub-title'"
          @click="$refs.peopleDrawer.open('invitations', $t('peopleOverview.label.invitations'))" />
        <v-divider class="peopleOverviewVertivalSeparator ma-auto" vertical />
        <people-overview-card
          id="peoplePendingOverview"
          :title="$t('peopleOverview.label.pending')"
          :count="pending"
          :class="pending === '-' && 'text-sub-title'"
          @click="$refs.peopleDrawer.open('pending', $t('peopleOverview.label.pending'))" />
      </v-card>
    </v-card>
    <people-overview-drawer ref="peopleDrawer" @refresh="refresh()" />
  </v-app>    
</template>

<script>
import * as userService from '../../common/js/UserService.js'; 

export default {
  data: () => ({
    invitations: '-',
    pending: '-',
  }),
  created() {
    this.refresh();
  },
  methods: {
    refresh() {
      let loading = 2;
      userService.getInvitations()
        .then(data => {
          this.invitations = data && data.size || 0;
        })
        .finally(() => {
          loading--;
          if (loading === 0) {
            document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
          }
        });
      userService.getPending()
        .then(data => {
          this.pending = data && data.size || 0;
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

