<template>
  <v-app>
    <widget-wrapper
      extra-class="application-body"
      :title="$t('peopleOverview.label.title')">
      <v-card flat>
        <div
          v-if="invitations > 0 || pending > 0"
          class="d-flex flex-row justify-space-around">
          <people-overview-card
            id="peopleInvitationsOverview"
            :class="invitations === '-' && 'text-subtitle'"
            :count="invitations"
            icon="fas fa-user-plus"
            :title="$t('peopleOverview.label.invitations')"
            @click="$refs.peopleDrawer.open('invitations', $t('peopleOverview.label.invitations'))" />
          <people-overview-card
            id="peoplePendingOverview"
            :class="pending === '-' && 'text-subtitle'"
            :count="pending"
            icon="fas fa-user-clock"
            :title="$t('peopleOverview.label.pending')"
            @click="$refs.peopleDrawer.open('pending', $t('peopleOverview.label.pending'))" />
        </div>
        <div v-else>
          <div
            v-if="displayPlaceholder"
            class="d-flex align-center justify-center">
            <v-icon
              class="tertiary--text me-3"
              size="24">
              fas fa-user-plus
            </v-icon>
            <div class="d-flex flex-nowrap">
              {{ $t('peopleOverview.label.network') }}
              {{ $t('peopleOverview.label.connect') }}
            </div>
          </div>
        </div>
      </v-card>
    </widget-wrapper>
    <people-overview-drawer
      ref="peopleDrawer"
      @refresh="refresh()" />
  </v-app>    
</template>

<script>
  export default {
    data: () => ({
      invitations: '-',
      pending: '-',
      initialized: false,
      loaded: false,
    }),
    computed: {
      displayPlaceholder () {
        return this.loaded;
      },
    },
    watch: {
      initialized (newVal, oldVal) {
        if (newVal !== oldVal && newVal) {
          this.loaded = true;
          this.$root.$applicationLoaded();
        }
      },
    },
    created () {
      this.refresh();
    },
    methods: {
      refresh () {
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

