<template>
  <exo-drawer
    ref="drawer"
    v-model="drawer"
    :loading="loading"
    class="userNotificationDrawer"
    right>
    <template #title>
      {{ $t('UserSettings.drawer.title.muteSpacesNotifications') }}
    </template>
    <template #content>
      <v-flex class="pa-4">
        <span class="subtitle-1">
          {{ $t('UserSettings.drawer.description.muteSpacesNotifications') }}
        </span>
        <exo-identity-suggester
          v-if="showSuggester"
          ref="spacesSuggester"
          v-model="space"
          :labels="spaceSuggesterLabels"
          :include-users="false"
          :ignore-items="ignoreItems"
          :width="220"
          name="spacesSuggester"
          class="user-suggester mt-n2"
          include-spaces />
        <span class="subtitle-1">
          {{ $t('UserSettings.drawer.label.mutedSpaces') }}
        </span>
        <v-list-item
          v-for="space in mutedSpaces"
          :key="space.id"
          :href="space.url"
          class="pa-1 pb-1"
          dense>
          <v-list-item-avatar
            :size="avatarSize"
            class="me-2"
            tile>
            <v-img
              :src="space.avatarUrl"
              :height="avatarSize"
              :width="avatarSize"
              :max-height="avatarSize"
              :max-width="avatarSize"
              class="mx-auto spaceAvatar"
              role="presentation" />
          </v-list-item-avatar>
          <v-list-item-content class="pa-0">
            <v-list-item-title class="subtitle-2">
              {{ space.displayName }}
            </v-list-item-title>
            <v-list-item-subtitle v-if="space.description" class="caption text-truncate">
              {{ space.description }}
            </v-list-item-subtitle>
          </v-list-item-content>
          <v-list-item-action class="pa-0">
            <v-tooltip bottom>
              <template #activator="{on, bind}">
                <v-btn
                  icon
                  small
                  @click.stop.prevent="muteSpace(space, true)"
                  v-on="on"
                  v-bind="bind">
                  <v-icon class="error-color" small>fa-trash</v-icon>
                </v-btn>
              </template>
              <span>{{ $t('UserSettings.button.tooltip.unmute') }}</span>
            </v-tooltip>
          </v-list-item-action>
        </v-list-item>
      </v-flex>
    </template>
  </exo-drawer>
</template>

<script>
export default {
  props: {
    settings: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    drawer: false,
    avatarSize: 36,
    showSuggester: true,
    space: null,
    mutedSpaces: [],
    loading: false,
  }),
  computed: {
    spaceSuggesterLabels() {
      return {
        placeholder: this.$t('UserSettings.drawer.spaceSuggesterPlaceholder'),
        noDataLabel: this.$t('UserSettings.drawer.spaceSuggesterNoData'),
      };
    },
    ignoreItems() {
      return this.mutedSpaces.map(space => `space:${space.prettyName}`);
    },
    mutedSpaceIds() {
      return this.settings?.mutedSpaces || [];
    },
  },
  watch: {
    mutedSpaceIds() {
      if (this.drawer) {
        this.retrieveSpaces();
      }
    },
    space() {
      if (this.space) {
        this.muteSpace(this.space);
        this.space = null;
        this.resetSuggester();
      }
    },
  },
  methods: {
    resetSuggester() {
      this.showSuggester = false;
      this.$nextTick().then(() => this.showSuggester = true);
    },
    reset() {
      this.mutedSpaces = [];
      this.space = null;
      this.loading = false;
    },
    open() {
      this.reset();
      this.$emit('refresh');
      this.$refs.drawer.open();
    },
    retrieveSpaces() {
      this.loading = true;
      return Promise.all(this.mutedSpaceIds.map(id => this.$spaceService.getSpaceById(id)))
        .then(spaces => {
          const mutedSpaces = spaces || [];
          mutedSpaces.forEach(space => {
            space.url = `${eXo.env.portal.context}/g/${space.groupId.replace(/\//g, ':')}/`;
          });
          this.mutedSpaces = mutedSpaces;
        })
        .finally(() => this.loading = false);
    },
    muteSpace(space, unmute) {
      this.loading = true;
      return this.$spaceService.muteSpace(space.spaceId || space.id, unmute)
        .then(() => {
          this.$emit('refresh');
          if (unmute) {
            this.$root.$emit('alert-message', this.$t('Notification.alert.successfullyUnmuted'), 'success');
          } else {
            this.$root.$emit('alert-message', this.$t('Notification.alert.successfullyMuted'), 'success');
          }
        })
        .catch(() => this.$root.$emit('alert-message', this.$t('Notification.alert.errorChangingSpaceMutingStatus'), 'error'))
        .finally(() => this.loading = false);
    },
  },
};
</script>

