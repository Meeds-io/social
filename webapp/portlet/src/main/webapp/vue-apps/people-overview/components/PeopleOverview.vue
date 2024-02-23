<template>
  <v-app>
    <widget-wrapper :title="$t('peopleOverview.label.title')">
      <v-card flat>
        <div class="d-flex flex-row justify-space-around" v-if="invitations > 0 || pending > 0">
          <people-overview-card
            id="peopleInvitationsOverview"
            :title="$t('peopleOverview.label.invitations')"
            :count="invitations"
            icon="fas fa-user-plus"
            :class="invitations === '-' && 'text-sub-title'"
            @click="$refs.peopleDrawer.open('invitations', $t('peopleOverview.label.invitations'))" />
          <people-overview-card
            id="peoplePendingOverview"
            :title="$t('peopleOverview.label.pending')"
            :count="pending"
            icon="fas fa-user-clock"
            :class="pending === '-' && 'text-sub-title'"
            @click="$refs.peopleDrawer.open('pending', $t('peopleOverview.label.pending'))" />
        </div>
        <div v-else>
          <div v-if="displayPlaceholder" class="d-flex align-center justify-center">
            <v-icon size="24" class="tertiary--text me-3">fas fa-user-plus</v-icon>
            <div class="d-flex flex-column">
              <span class="subtitle-1 text-color text-left">{{ $t('peopleOverview.label.network') }}</span>
              <span class="subtitle-1 text-color text-left">{{ $t('peopleOverview.label.connect') }}</span>
            </div>
          </div>
        </div>
      </v-card>
    </widget-wrapper>
    <people-overview-drawer ref="peopleDrawer" @refresh="refresh()" />
  </v-app>    
</template>

<script>
export default {
  data: () => ({
    invitations: '-',
    pending: '-',
    initialized: false,
    loaded: false
  }),
  computed: {
    displayPlaceholder() {
      return this.loaded;
    },
  },
  watch: {
    initialized(newVal, oldVal) {
      if (newVal !== oldVal && newVal) {
        this.loaded = true;
        this.$root.$applicationLoaded();
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

