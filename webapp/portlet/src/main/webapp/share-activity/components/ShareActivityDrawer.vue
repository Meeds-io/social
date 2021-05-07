<template>
  <exo-drawer
    ref="shareActivityDrawer"
    body-classes="hide-scroll decrease-z-index-more"
    right
    @closed="close">
    <template slot="title">
      {{ $t('news.share.drawer.popupTitle') }}
    </template>
    <template slot="content">
      <v-form
        v-if="activityId"
        ref="newsShareFrom"
        class="flex news-form share-news"
        flat>
        <div class="d-flex flex-column flex-grow-1">
          <div class="d-flex flex-row">
            <span class="mt-4 ml-2 mb-2">{{ $t('news.share.shareInSpaces') }} </span>
          </div>
          <div class="d-flex flex-row flex-grow-1 newsSpaceSuggester">
            <share-activity-suggester :spaces="spaces" class="ml-2" />
          </div>
          <div class="d-flex flex-row">
            <textarea
              v-model="description"
              :placeholder="$t('news.share.sharedActivityPlaceholder')"
              class="ml-2 ignore-vuetify-classes newsShareDescription">
              </textarea>
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
          {{ $t('news.share.share') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
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
    }
  },
  data: () => ({
    showShareNewsDrawer: false,
    description: '',
    spaces: [],
  }),
  computed: {
    shareDisabled() {
      return !this.spaces || this.spaces.filter(part => part !== '').length === 0;
    },
  },
  methods: {
    open() {
      this.$refs.shareActivityDrawer.open();
    },
    close() {
      $('#dropdown-menu').addClass('dropdown-menu');
      this.$refs.shareActivityDrawer.close();
    },
    shareActivity() {
      const sharedActivityRestIn = {
        title: this.description,
        type: this.activityType,
        targetSpaces: this.spaces,
      };
      this.$spaceService.shareActivityOnSpaces(this.activityId,sharedActivityRestIn).then(this.refreshActivityStream());
    },
    refreshActivityStream() {
      const refreshButton = document.querySelector('.activityStreamStatus #RefreshButton');
      if (refreshButton) {
        refreshButton.click();
      }
    },
  }
};
</script>