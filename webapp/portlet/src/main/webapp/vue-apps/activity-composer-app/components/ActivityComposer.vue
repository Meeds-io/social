<template>
  <div id="activityComposer" class="activityComposer activityComposerApp pa-0">
    <div v-if="!standalone" class="openLink mb-4 text-truncate">
      <a @click="openComposerDrawer(true)" class="primary--text">
        <i class="uiIconEdit"></i>
        {{ composerButtonLabel }}
      </a>
    </div>
    <activity-composer-drawer ref="activityComposerDrawer" />
  </div>
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
      this.$refs.activityComposerDrawer.open({
        activityId: this.activityId,
        activityBody: this.activityBody,
        activityParams: this.activityParams,
        files: [],
      });
    },
  },
};
</script>