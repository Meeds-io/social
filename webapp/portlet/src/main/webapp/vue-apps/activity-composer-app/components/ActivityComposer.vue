<template>
  <div id="activityComposer">
    <span
      v-if="standalone"
      class=" text-sub-title text-uppercase pt-3">{{ $t('activity.stream.title') }}</span>
    <div v-else>
      <a
        @click="openComposerDrawer(true)"
        :class="displayStreamFilter && 'pt-3'"
        class="primary--text">
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
    selectedFilter: {
      type: String,
      default: ''
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
    displayStreamFilter() {
      return !eXo.env.portal.spaceId;
    }
  },
  methods: {
    openComposerDrawer() {
      this.$refs.activityComposerDrawer.open({
        activityId: this.activityId,
        activityBody: this.activityBody,
        activityParams: this.activityParams,
        files: [],
        activityType: null
      });
    },
  },
};
</script>