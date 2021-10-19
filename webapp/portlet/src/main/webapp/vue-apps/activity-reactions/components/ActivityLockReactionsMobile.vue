<template>
  <div v-if="displayLockMessage" class="lockReaction ps-0 disabled--text">
    <v-icon
      small
      class="baseline-vertical-align mx-auto disabled--text">
      mdi-lock
    </v-icon>
    <span class="ms-2 mx-auto">{{ $t('news.details.cannot.react') }}</span>
  </div>
</template>
<script>
export default {
  props: {
    activity: {
      type: Object,
      default: null,
    },
  },
  computed: {
    isMobile() {
      return this.$vuetify && this.$vuetify.breakpoint && this.$vuetify.breakpoint.name === 'xs';
    },
    displayLockMessage() {
      return this.activity && this.activity.type === 'news' && this.activity.templateParams && this.activity.templateParams.isHiddenActivity === 'true';
    }
  },
};
</script>