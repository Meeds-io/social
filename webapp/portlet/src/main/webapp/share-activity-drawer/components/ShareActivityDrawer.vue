<template>
  <div class="activityDrawer">
    <share-activity-notification-alerts />
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
          class="flex share-activity mx-2"
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
            <div class="d-flex flex-row mt-4 ml-2">
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
  </div>
</template>

<script>
export default {
  props: {
    activityId: {
      type: String,
      default: ''
    },
    activityType: {
      type: String,
      default: ''
    },
  },
  data: () => ({
    description: '',
    spaces: [],
    activityId: '',
    activityType: '',
  }),
  computed: {
    shareDisabled() {
      return !this.spaces || this.spaces.filter(part => part !== '').length === 0;
    },
  },
  created() {
    this.$root.$on('clear-suggester', () => {
      this.spaces = [];
      this.description = '';
    });
    document.addEventListener('activity-stream-share-open', (event) => {
      const params = event && event.detail;
      if (params) {
        this.activityId = params.activityId;
        this.activityType = params.activityType;
        this.open();
      }
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
      const spacesList = [];
      this.spaces.forEach(space => {
        this.$spaceService.getSpaceByPrettyName(space,'identity').then(data => {
          spacesList.push(data.displayName);
        });
      });
      const sharedActivity = {
        title: this.description,
        type: this.activityType,
        targetSpaces: this.spaces,
      };
      this.$spaceService.shareActivityOnSpaces(this.activityId, sharedActivity)
        .then(() => {
          this.close();
          this.$root.$emit('activity-shared', spacesList);
          this.$root.$emit('clear-suggester', spacesList);
        });
    }
  }
};
</script>