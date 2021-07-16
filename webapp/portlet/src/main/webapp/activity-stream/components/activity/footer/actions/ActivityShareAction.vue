<template>
  <v-tooltip bottom>
    <template v-slot:activator="{ on, attrs }">
      <v-btn
        v-if="isShareable"
        :id="`ShareActivity${activityId}`"
        :class="shareTextColorClass"
        class="pa-0 ms-4"
        text
        link
        small
        v-bind="attrs"
        v-on="on"
        @click="openShareDrawer()">
        <v-icon
          :class="shareIconColorClass"
          class="me-1 baseline-vertical-align"
          size="14">
          fa-share
        </v-icon>
        {{ $t('UIActivity.share') }}
      </v-btn>
    </template>
    <span>
      {{ $t('UIActivity.share') }}
    </span>
  </v-tooltip>
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
      this.$root.$emit('activity-share-drawer-open', this.activityId);
    },
  },
};
</script>