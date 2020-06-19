<template>
  <v-flex :class="flipCard && 'peopleCardFlip peopleCardFlipped' || 'peopleCardFlip'">
    <div class="peopleCardFront">
      <people-card-front
        :user="user"
        :profile-action-extensions="profileActionExtensions"
        :skeleton="skeleton"
        front
        @flip="flipCard = true"
        @refresh="$emit('refresh')" />
    </div>
    <div v-if="!skeleton" class="peopleCardBack">
      <people-card-reverse
        :user="user"
        @flip="flipCard = false" />
    </div>
  </v-flex>
</template>

<script>
export default {
  props: {
    user: {
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
