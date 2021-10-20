<template>
  <div v-if="displayLockMessage" class="lockReaction py-1 ms-2 disabled--text">
    <v-icon
      small
      class="baseline-vertical-align mx-auto disabled--text">
      mdi-lock
    </v-icon>
    <span class="ms-2 mx-auto">{{ $t('UIActivity.details.cannot.react') }}</span>
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
    displayLockMessage() {
      return this.activity && this.activity.type === 'news' && this.activity.templateParams && this.activity.templateParams.isHiddenActivity === 'true';
    }
  },
};
</script>