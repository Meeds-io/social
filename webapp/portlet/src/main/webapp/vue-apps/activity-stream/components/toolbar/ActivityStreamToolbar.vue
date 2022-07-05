<template>
  <v-toolbar
    id="activityComposer"
    class="activityComposer activityComposerApp pa-0"
    color="white mb-5"
    height="52"
    flat
    dense>
    <v-flex v-if="initialized" class="d-flex">
      <div v-if="UserCanPoste" class="openLink my-auto ps-0 text-truncate">
        <a @click="openComposerDrawer(true)" class="primary--text">
          <i class="uiIconEdit"></i>
          {{ composerButtonLabel }}
        </a>
      </div>
      <div v-else>
          <v-card-text class="text-sub-title text-uppercase center px-0">
            {{ $t('activity.toolbar.title') }}
          </v-card-text>
      </div>
      <div
        v-if="canFilter && !isMobile"
        class="ms-auto my-auto">
        <activity-stream-filter/>
      </div>
    </v-flex>
  </v-toolbar>
</template>

<script>
export default {
  props: {
    activityBody: {
      type: String,
      default: ''
    },
    activityId: {
      type: String,
      default: ''
    },
    activityParams: {
      type: Object,
      default: null
    },
    standalone: {
      type: Boolean,
      default: false
    },
    canPost: {
      type: Boolean,
      default: false
    },
    canFilter: {
      type: Boolean,
      default: false
    },
  },
  data: () => ({
    initialized: false,
  }),
  watch: {
    canPost(newValue, oldValue) {
      if (!oldValue && newValue && !this.initialized) {
        this.initialized = true;
      }
    },
  },
  computed: {
    composerButtonLabel() {
      if (eXo.env.portal.spaceDisplayName){
        return this.$t('activity.composer.link', {0: eXo.env.portal.spaceDisplayName});
      } else {
        return this.$t('activity.composer.post');
      }
    },
    isMobile() {
      return this.$vuetify.breakpoint.name === 'xs' || this.$vuetify.breakpoint.name === 'sm';
    },
    UserCanPoste() {
      return !this.standalone && this.canPost;
    }
  },
  methods: {
    openComposerDrawer() {
      document.dispatchEvent(new CustomEvent('activity-composer-drawer-open', {detail: {
        activityId: this.activityId,
        activityBody: this.activityBody,
        activityParams: this.activityParams,
        files: [],
        activityType: null
      }}));
    },
  },
};
</script>