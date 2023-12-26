<template>
  <exo-drawer
    ref="activityShareDrawer"
    id="activityShareDrawer"
    right
    @opened="opened = true"
    @closed="opened = false">
    <template slot="title">
      {{ $t('UIActivity.share.drawer.popupTitle') }}
    </template>
    <template slot="content">
      <v-form
        v-if="activityId"
        ref="activityShareFrom"
        class="flex mx-4"
        flat>
        <div class="d-flex flex-column flex-grow-1">
          <div class="d-flex flex-row">
            <span class="mt-4 mb-2">{{ $t('UIActivity.share.shareInSpaces') }} </span>
          </div>
          <div class="d-flex flex-row flex-grow-1 activitySpaceSuggester">
            <exo-identity-suggester
              id="selectDestinationSpaceToShare"
              ref="activitySpaceSuggester"
              v-model="spaces"
              :labels="spaceSuggesterLabels"
              :include-users="false"
              :width="220"
              name="activitySpaceAutocomplete"
              class="space-suggester activitySpaceAutocomplete"
              include-spaces
              multiple
              only-redactor
              autofocus />
          </div>
          <div class="d-flex flex-row">
            <rich-editor
              id="shareMessageInput"
              ref="activityShareMessage"
              v-model="description"
              :template-params="templateParams"
              :max-length="MESSAGE_MAX_LENGTH"
              :placeholder="$t('UIActivity.share.sharedActivityPlaceholder')"
              ck-editor-type="activityShare"
              class="flex"
              @validity-updated="validInput = $event" />
          </div>
          <div class="d-flex flex-row mt-4">
            <v-icon class="warning--text">warning</v-icon>
            <span class="ms-2 grey--text">
              {{ $t('UIActivity.share.warnMessage') }}
            </span>
          </div>
        </div>
      </v-form>
    </template>
    <template slot="footer">
      <div class="d-flex justify-end">
        <v-btn
          id="cancelShareActivityButton"
          class="btn me-2"
          :aria-label="$t('Confirmation.label.Cancel')"
          @click="close">
          {{ $t('Confirmation.label.Cancel') }}
        </v-btn>
        <v-btn
          id="shareActivityButton"
          :loading="sharing"
          :disabled="buttonDisabled"
          :aria-label="$t('UIActivity.share')"
          class="btn btn-primary me-2"
          @click="shareActivity">
          {{ $t('UIActivity.share') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
export default {
  data: () => ({
    MESSAGE_MAX_LENGTH: 1250,
    opened: false,
    sharing: false,
    validInput: true,
    description: '',
    activityId: null,
    currentApp: '',
    spaces: [],
  }),
  computed: {
    buttonDisabled() {
      return !this.activityId
        || !this.validInput
        || this.sharing
        || !this.spaces
        || !this.spaces.filter(part => part).length;
    },
    spaceSuggesterLabels() {
      return {
        searchPlaceholder: this.$t('UIActivity.share.spaces.searchPlaceholder'),
        placeholder: this.$t('UIActivity.share.spaces.placeholder'),
        noDataLabel: this.$t('UIActivity.share.spaces.noDataLabel'),
      };
    },
  },
  created() {
    this.$root.$on('activity-share-drawer-open', this.open);
  },
  methods: {
    clear() {
      this.activityId = null;
      this.spaces = [];
      this.templateParams = {};
      this.description = '';
      this.sharing = false;
    },
    open(activityId, currentApp) {
      this.activityId = activityId;
      this.currentApp = currentApp;
      if (this.activityId) {
        this.$refs.activityShareDrawer.open();
      }
    },
    close() {
      this.$refs.activityShareDrawer.close();
    },
    shareActivity() {
      const spacePrettyNames = this.spaces.map(space => space.remoteId);
      this.sharing = true;
      this.$activityService.shareActivity(this.activityId, this.description, this.templateParams, spacePrettyNames)
        .then(() => {
          const spaces = this.spaces.map(space => ({
            prettyName: space.remoteId,
            displayName: space && space.profile && space.profile.fullName,
            avatarUrl: space && space.profile && space.profile.avatarUrl,
          }));
          this.$root.$emit('activity-shared', this.activityId, spaces, this.currentApp);
          if (spaces && spaces.length > 0) {
            const spaceDisplayNames = spaces.map(space => space.displayName || '');
            this.$root.$emit('alert-message', `${this.$t('UIActivity.share.message')} ${spaceDisplayNames.join(', ')}`, 'success');
          }
          this.close();
          this.clear();
        })
        .finally(() => this.sharing = false);
    },
  },
};
</script>
