<template>
  <exo-drawer
    id="activityShareDrawer"
    ref="activityShareDrawer"
    right
    @closed="opened = false"
    @opened="opened = true">
    <template #title>
      {{ $t('UIActivity.share.drawer.popupTitle') }}
    </template>
    <template #content>
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
              autofocus
              class="space-suggester activitySpaceAutocomplete"
              include-spaces
              :include-users="false"
              :labels="spaceSuggesterLabels"
              multiple
              name="activitySpaceAutocomplete"
              only-redactor
              :width="220" />
          </div>
          <div class="d-flex flex-row">
            <rich-editor
              id="shareMessageInput"
              ref="activityShareMessage"
              v-model="description"
              ck-editor-type="activityShare"
              class="flex"
              :max-length="MESSAGE_MAX_LENGTH"
              :placeholder="$t('UIActivity.share.sharedActivityPlaceholder')"
              :template-params="templateParams"
              @validity-updated="validInput = $event" />
          </div>
          <div class="d-flex flex-row mt-4">
            <v-icon class="warning--text">
              warning
            </v-icon>
            <span class="ms-2 grey--text">
              {{ $t('UIActivity.share.warnMessage') }}
            </span>
          </div>
        </div>
      </v-form>
    </template>
    <template #footer>
      <div class="d-flex justify-end">
        <v-btn
          id="cancelShareActivityButton"
          :aria-label="$t('Confirmation.label.Cancel')"
          class="btn me-2"
          @click="close">
          {{ $t('Confirmation.label.Cancel') }}
        </v-btn>
        <v-btn
          id="shareActivityButton"
          :aria-label="$t('UIActivity.share')"
          class="btn btn-primary me-2"
          :disabled="buttonDisabled"
          :loading="sharing"
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
      buttonDisabled () {
        return !this.activityId
          || !this.validInput
          || this.sharing
          || !this.spaces
          || !this.spaces.filter(part => part).length;
      },
      spaceSuggesterLabels () {
        return {
          searchPlaceholder: this.$t('UIActivity.share.spaces.searchPlaceholder'),
          placeholder: this.$t('UIActivity.share.spaces.placeholder'),
          noDataLabel: this.$t('UIActivity.share.spaces.noDataLabel'),
        };
      },
    },
    created () {
      this.$root.$on('activity-share-drawer-open', this.open);
    },
    methods: {
      clear () {
        this.activityId = null;
        this.spaces = [];
        this.templateParams = {};
        this.description = '';
        this.sharing = false;
      },
      open (activityId, currentApp) {
        this.activityId = activityId;
        this.currentApp = currentApp;
        if (this.activityId) {
          this.$refs.activityShareDrawer.open();
        }
      },
      close () {
        this.$refs.activityShareDrawer.close();
      },
      shareActivity () {
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
