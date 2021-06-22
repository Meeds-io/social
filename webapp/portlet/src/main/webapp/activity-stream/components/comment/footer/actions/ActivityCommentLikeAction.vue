<template>
  <div>
    <v-btn
      :id="`LikeLink${commentId}`"
      :loading="changingLike"
      :title="likeButtonTitle"
      :class="likeTextColorClass"
      class="pe-1 ms-2"
      text
      link
      x-small
      @click="changeLike">
      <span>{{ $t('UIActivity.msg.LikeActivity') }}</span>
    </v-btn>
    <a
      :id="`LikersListLink${commentId}`"
      :title="likersFullnameTitle"
      class="pe-2 primary--text caption">
      {{ likesCount && `(${likesCount})` || '&nbsp;' }}
    </a>
  </div>
</template>

<script>
export default {
  props: {
    comment: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    changingLike: false,
  }),
  computed: {
    commentId() {
      return this.comment && this.comment.id;
    },
    likers() {
      return this.comment && this.comment.likes.slice().reverse() || [];
    },
    likesCount() {
      return this.likers.length;
    },
    likersFullnameTitle() {
      const likersFullname = this.likers.map(liker => liker && liker.fullname).filter(liker => !!liker);
      if (this.likesCount > 10) {
        const likersFullnameTitle = likersFullname.slice(0, 9).join('\r\n');
        return `${likersFullnameTitle}\r\n${this.$t('UIActivity.msg.MoreLikers', {0: (this.likesCount - 9)})}`;
      } else {
        return likersFullname.slice(0, 10).join('\r\n');
      }
    },
    hasLiked() {
      return this.likers.filter(like => like && like.id === eXo.env.portal.userIdentityId).length;
    },
    likeColorClass() {
      return this.hasLiked && 'primary--text' || 'disabled--text';
    },
    likeTextColorClass() {
      return this.hasLiked && 'primary--text' || '';
    },
    likeButtonTitle() {
      return this.hasLiked && this.$t('UIActivity.msg.UnlikeActivity') || this.$t('UIActivity.msg.LikeActivity');
    },
  },
  created() {
    this.$root.$on('activity-stream-comment-updated', this.updateCommentLikers);
  },
  methods: {
    updateCommentLikers(comment) {
      if (comment && comment.id === this.comment.id) {
        this.comment.likes = comment.likes || [];
      }
    },
    changeLike() {
      if (this.changingLike) {
        return;
      }
      if (this.hasLiked) {
        return this.unlikeComment();
      } else {
        return this.likeComment();
      }
    },
    likeComment() {
      this.changingLike = true;
      return this.$activityService.likeActivity(this.commentId)
        .then(() => {
          const liker = Object.assign({}, this.$currentUserIdentity, this.$currentUserIdentity.profile);
          this.comment.likes = [...this.likers, liker];
          this.comment.likesCount++;
          this.$root.$emit('activity-stream-comment-updated', this.comment);
        })
        .finally(() => this.changingLike = false);
    },
    unlikeComment() {
      this.changingLike = true;
      return this.$activityService.unlikeActivity(this.commentId)
        .then(() => {
          this.comment.likes = this.likers.filter(likeIdentity => likeIdentity.id !== eXo.env.portal.userIdentityId);
          this.comment.likesCount--;
          this.$root.$emit('activity-stream-comment-updated', this.comment);
        })
        .finally(() => this.changingLike = false);
    },
  },
};
</script>