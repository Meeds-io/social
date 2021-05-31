<template>
  <v-app>
    <exo-drawer
      ref="shareActivityDrawer"
      body-classes="hide-scroll decrease-z-index-more"
      right
      @closed="close">
      <template slot="title">
        {{ $t('UIActivity.share.drawer.popupTitle') }}
      </template>
      <template slot="content">
        <v-form
          ref="activityShareFrom"
          class="flex share-activity"
          flat>
          <div class="d-flex flex-column flex-grow-1">
            <div class="d-flex flex-row">
              <span class="mt-4 ml-2 mb-2">{{ $t('UIActivity.share.shareInSpaces') }} </span>
            </div>
            <div class="d-flex flex-row flex-grow-1 activitySpaceSuggester">
              <share-activity-suggester :spaces="spaces" class="ml-2" />
            </div>
            <div class="d-flex flex-row">
              <textarea
                v-model="description"
                :placeholder="$t('UIActivity.share.sharedActivityPlaceholder')"
                class="ml-2 ignore-vuetify-classes activityShareDescription">
              </textarea>
            </div>
            <div v-if="descriptionHasLink" class="d-flex flex-row mt-4 ml-2">
              <v-icon class="warningStyle">warning</v-icon>
              <span class="ml-2 grey--text">{{ $t('UIActivity.share.warnMessage') }}</span>
            </div>
          </div>
        </v-form>
      </template>
      <template slot="footer">
        <div class="d-flex justify-end">
          <v-btn
            :disabled="shareDisabled"
            class="btn btn-primary mr-2"
            @click="shareActivity">
            {{ $t('UIActivity.share.share') }}
          </v-btn>
        </div>
      </template>
    </exo-drawer>
  </v-app>
</template>

<script>
const LINK_REGEX = /(((https?:\/\/)|(www\.))[^\s]+)/g;
export default {
  data: () => ({
    description: '',
    spaces: [],
  }),
  computed: {
    shareDisabled() {
      return !this.spaces || this.spaces.filter(part => part !== '').length === 0;
    },
    descriptionHasLink() {
      return this.description && this.description.match(LINK_REGEX) !== null;
    }
  },
  created() {
    this.$root.$on('clear-suggester', () => {
      this.spaces = [];
      this.description = '';
    });
  },
  methods: {
    open() {
      this.$refs.shareActivityDrawer.open();
    },
    close() {
      this.$refs.shareActivityDrawer.close();
    },
    shareActivity() {
      this.$emit('share-activity', this.spaces, this.description);
    },
  }
};
</script>