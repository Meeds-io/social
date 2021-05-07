<template>
  <div class="actionBarMobile">
    <a v-if="likersNumber>0" class="likesNumber" @click="open">{{ likersNumber }} {{ $t('UIActivity.label.likesLabel') }}</a>
    <a v-if="commentNumber>0" class="CommentsNumber" >{{ commentNumber }} {{ $t('UIActivity.comment.commentsLabel') }}</a>
    <a v-if="kudosNumber>0" class="KudosNumber" @click="open">{{ kudosNumber }} Kudos</a>
  </div>
</template>
<script>
export default {
  props: {
    likersNumber: {
      type: Number,
      default: 0
    },
    kudosNumber: {
      type: Number,
      default: 0
    },
    commentNumber: {
      type: Number,
      default: 0
    }
  },
  methods: {
    open() {
      this.$emit('openDrawer');
    },
  }
};
</script>
