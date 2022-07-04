<template>
  <v-toolbar
    id="activityComposer"
    class="activityComposer activityComposerApp pa-0"
    color="white mb-5"
    height="52"
    flat
    dense>
    <v-flex class="d-flex">
      <div v-if="!standalone && canPost" class="openLink my-auto text-truncate">
        <a @click="openComposerDrawer(true)" class="primary--text">
          <i class="uiIconEdit"></i>
          {{ composerButtonLabel }}
        </a>
      </div>
      <div
        v-if="!spaceId"
        class="ms-auto my-auto">
        <activity-stream-filter :stream-filter="streamFilter" @filterChanged="applyFilter" />
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
  },
  computed: {
    composerButtonLabel() {
      if (eXo.env.portal.spaceDisplayName){
        return this.$t('activity.composer.link', {0: eXo.env.portal.spaceDisplayName});
      } else {
        return this.$t('activity.composer.post');
      }
    },
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