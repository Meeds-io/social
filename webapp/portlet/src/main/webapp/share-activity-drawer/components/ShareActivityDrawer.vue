<template>
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
        v-if="activityId"
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
    description: '',
    spaces: [],
  }),
  computed: {
    shareDisabled() {
      return !this.spaces || this.spaces.filter(part => part !== '').length === 0;
    },
  },
  created() {
    this.$root.$on('activity-shared', () => {
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
    }
  }
};
</script>