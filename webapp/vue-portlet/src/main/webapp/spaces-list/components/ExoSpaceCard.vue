<template>
  <v-col
    :class="flipCard && 'spaceCardFlip spaceCardFlipped' || 'spaceCardFlip'"
    cols="12"
    md="3">
    <div class="spaceCardFront">
      <exo-space-card-front
        :space="space"
        :profile-action-extensions="profileActionExtensions"
        :skeleton="skeleton"
        front
        @flip="flipCard = true"
        @refresh="$emit('refresh')" />
    </div>
    <div class="spaceCardBack">
      <exo-space-card-reverse
        :space="space"
        @flip="flipCard = false" />
    </div>
  </v-col>
</template>

<script>
export default {
  props: {
    space: {
      type: Object,
      default: () => ({}),
    },
    profileActionExtensions: {
      type: Array,
      default: () => [],
    },
    skeleton: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    flipCard: false,
  }),
};
</script>
