<template>
  <v-list v-if="comments" class="pb-0 pt-5 border-top-color">
    <activity-comment
      v-for="comment in comments"
      :key="comment.id"
      :comment="comment" />
  </v-list>
</template>

<script>
export default {
  props: {
    comments: {
      type: String,
      default: null,
    },
  },
};
</script>