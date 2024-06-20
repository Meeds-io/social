<template>
  <div>
    <activity-reactions
      :activity-id="activityId"
      :likers="likers"
      :likers-number="likersNumber"
      :comment-number="commentNumber" />
  </div>
</template>
<script>
export default {
  props: {
    activityId: {
      type: String,
      default: () => ''
    },
    likers: {
      type: Array,
      default: () => []
    },
    likersNumber: {
      type: Number,
      default: 0
    },
    commentNumber: {
      type: Number,
      default: 0
    },
  },
};
</script>