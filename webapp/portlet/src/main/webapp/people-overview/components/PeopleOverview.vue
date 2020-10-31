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
export default {
  data: () => ({
    invitations: '-',
    pending: '-',
    initialized: false,
  }),
  watch: {
    initialized(newVal, oldVal) {
      if (newVal !== oldVal && newVal) {
        this.$root.$emit('application-loaded');
      }
    },
  },
  created() {
    this.refresh();
  },
  methods: {
    refresh() {
      let loading = 2;
      this.$userService.getInvitations()
        .then(data => {
          this.invitations = data && data.size || 0;
        })
        .finally(() => {
          loading--;
          if (loading === 0) {
            this.initialized = true;
          }
          if (window.location.pathname.includes('receivedInvitations')) {
            this.$refs.peopleDrawer.open('invitations', this.$t('peopleOverview.label.invitations'));
          }
        });
      this.$userService.getPending()
        .then(data => {
          this.pending = data && data.size || 0;
        })
        .finally(() => {
          loading--;
          if (loading === 0) {
            this.initialized = true;
          }
        });
    },
  },
};
</script>

