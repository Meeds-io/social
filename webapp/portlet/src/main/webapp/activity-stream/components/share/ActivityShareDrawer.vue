<template>
  <exo-drawer
    ref="activityShareDrawer"
    body-classes="hide-scroll decrease-z-index-more"
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
              only-manager
              autofocus />
          </div>
          <div class="d-flex flex-row">
            <exo-activity-rich-editor
              ref="activityShareMessage"
              v-model="description"
              :template-params="templateParams"
              :max-length="MESSAGE_MAX_LENGTH"
              :placeholder="$t('UIActivity.share.sharedActivityPlaceholder')"
              ck-editor-type="activityShare"
              class="flex" />
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
          class="btn me-2"
          @click="close">
          {{ $t('Confirmation.label.Cancel') }}
        </v-btn>
        <v-btn
          :loading="sharing"
          :disabled="buttonDisabled"
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
    description: '',
    activityId: null,
    spaces: [],
  }),
  computed: {
    buttonDisabled() {
      return !this.activityId
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
    open(activityId) {
      this.activityId = activityId;
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
          this.close();
          this.clear();
          this.$root.$emit('activity-shared', this.activityId, this.spaces.map(space => ({
            prettyName: space.remoteId,
            displayName: space && space.profile && space.profile.fullName,
            avatarUrl: space && space.profile && space.profile.avatarUrl,
          })));
        })
        .finally(() => this.sharing = false);
    },
  },
};
</script>