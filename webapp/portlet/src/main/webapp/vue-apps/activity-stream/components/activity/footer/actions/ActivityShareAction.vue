<template>
  <div class="d-inline-flex ms-xl-4 ms-lg-3">
    <!-- Added for mobile -->
    <v-tooltip :disabled="isMobile" bottom>
      <template #activator="{ on, attrs }">
        <v-btn
          v-if="isShareable"
          :id="`ShareActivity${activityId}`"
          :class="shareTextColorClass"
          class="pa-0 mt-0"
          text
          link
          small
          v-bind="attrs"
          v-on="on"
          @click="openShareDrawer()">
          <div class="d-flex flex-lg-row flex-column">
            <v-icon
              :class="shareIconColorClass"
              class="me-lg-1 baseline-vertical-align"
              :size="isMobile && '20' || '14'">
              fa-share
            </v-icon>
            <span v-if="!isMobile" class="mx-auto mt-1 mt-lg-0 ms-lg-1">
              {{ $t('UIActivity.share') }}
            </span>
          </div>
        </v-btn>
      </template>
      <span>
        {{ $t('UIActivity.share') }}
      </span>
    </v-tooltip>
  </div>
</template>

<script>
export default {
  props: {
    activity: {
      type: Object,
      default: null,
    },
    activityTypeExtension: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    hasShared: false,
  }),
  computed: {
    isShareable() {
      return this.activity
        && this.activityTypeExtension
        && this.activityTypeExtension.canShare
        && this.activityTypeExtension.canShare(this.activity);
    },
    activityId() {
      return this.activity && this.activity.id;
    },
    activityType() {
      return this.activity && this.activity.type;
    },
    shareTextColorClass() {
      return this.hasShared && 'primary--text' || '';
    },
    shareIconColorClass() {
      return this.hasShared && 'primary--text' || 'disabled--text';
    },
    isMobile() {
      return this.$vuetify.breakpoint.name === 'sm' || this.$vuetify.breakpoint.name === 'xs';
    },
  },
  created() {
    this.$root.$on('activity-shared', this.setHasShared);
  },
  mounted() {
    this.hasShared = this.activity && this.activity.shareActions && this.activity.shareActions.find(shareAction => `${shareAction.userIdentityId}` === eXo.env.portal.userIdentityId);
  },
  beforeDestroy() {
    this.$root.$off('activity-shared', this.setHasShared);
  },
  methods: {
    setHasShared(activityId) {
      if (this.activity && this.activity.id === activityId) {
        this.hasShared = true;
      }
    },
    checkHasShared() {
      if (!this.hasShared) {
        this.hasShared = this.activity && this.activity.shareActions && this.activity.shareActions.find(shareAction => `${shareAction.userIdentityId}` === eXo.env.portal.userIdentityId);
      }
    },
    openShareDrawer() {
      this.$root.$emit('activity-share-drawer-open', this.activityId, 'activityStream');
    },
  },
};
</script>
