<template>
  <v-list-item>
    <activity-head-user
      :identity="posterIdentity"
      :size="33"
      :class="subComment && 'ms-10'"
      avatar
      class="mt-0 mb-auto me-2" />
    <v-list-item-content class="col-auto ps-2 pt-1 pb-0 mb-6 flex-shrink-1 grey-background border-box-sizing rounded-lg">
      <v-list-item-title class="pt-1 font-weight-bold subtitle-2">
        <activity-head-user :identity="posterIdentity" />
      </v-list-item-title>
      <div v-sanitized-html="comment.body"></div>
    </v-list-item-content>
  </v-list-item>
</template>

<script>
export default {
  props: {
    comment: {
      type: String,
      default: null,
    },
    subComment: {
      type: Boolean,
      default: false,
    },
  },
  computed: {
    posterIdentity() {
      return this.comment && this.comment.identity;
    },
  },
};
</script>