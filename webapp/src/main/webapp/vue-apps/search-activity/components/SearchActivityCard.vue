<template>
  <v-card
    class="d-flex flex-column border-radius box-shadow"
    flat
    min-height="227">
    <v-card-text v-if="poster" class="px-2 pt-2 pb-0">
      <exo-user-avatar
        :identity="poster"
        popover>
        <template slot="subTitle">
          <date-format :value="postedTime" />
        </template>
        <template slot="actions">
          <activity-favorite-action
            :activity="result"
            class="ms-3"
            absolute
            top="0"
            right="0"
            @removed="$emit('refresh-favorite')" />
        </template>
      </exo-user-avatar>
    </v-card-text>
    <div class="mx-auto flex-grow-1 px-3 py-0">
      <div
        ref="excerptNode"
        :title="excerptText"
        class="text-wrap text-break caption text-truncate-4"
        v-sanitized-html="excerptHtml">
      </div>
    </div>
    <v-list class="light-grey-background flex-grow-0 border-top-color no-border-radius pa-0">
      <v-list-item :href="link" class="px-0 pt-1 pb-2">
        <v-list-item-icon class="mx-0 my-auto">
          <span :class="activityIcon" class="tertiary--text ps-1 pe-2 display-1"></span>
        </v-list-item-icon>
        <v-list-item-content>
          <v-list-item-title :title="activityReactions">
            {{ activityReactions }}
          </v-list-item-title>
          <v-list-item-subtitle>
            {{ activityStreamOwner }}
          </v-list-item-subtitle>
        </v-list-item-content>
      </v-list-item>
    </v-list>
  </v-card>
</template>

<script>
export default {
  props: {
    term: {
      type: String,
      default: null,
    },
    result: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    maxEllipsisHeight: 90,
    lineHeight: 22,
    profileActionExtensions: [],
  }),
  computed: {
    isComment() {
      return this.result && this.result.comment;
    },
    activity() {
      return this.isComment && this.result.comment || this.result;
    },
    poster() {
      return this.activity && this.activity.poster.profile;
    },
    body() {
      return this.activity && this.activity.body;
    },
    posterFullname() {
      return this.poster && this.poster.fullname;
    },
    posterUsername() {
      return this.poster && this.poster.username;
    },
    posterIsExternal() {
      return this.poster && (this.poster.isExternal || this.poster.external);
    },
    streamOwner() {
      return this.activity && this.activity.streamOwner.space || this.activity.streamOwner.profile;
    },
    spaceDisplayName() {
      return this.streamOwner && this.streamOwner.displayName;
    },
    excerpts() {
      return this.activity && this.activity.excerpts || (this.activity.title && [this.activity.title]) || (this.activity.body && [this.activity.body]);
    },
    excerptHtml() {
      return this.excerpts && this.excerpts.join('<br />...') || this.body || '';
    },
    excerptText() {
      return $('<div />').html(this.excerptHtml).text();
    },
    activityType() {
      if (!this.result) {
        return '';
      }
      return this.result.comment && this.result.comment.type || this.result.type;
    },
    activityIcon() {
      if (!this.result) {
        return '';
      }
      let typeIcon = this.activityType && this.activityType.replace(':', '_') || '';
      typeIcon = `uiIcon${typeIcon.charAt(0).toUpperCase()}${typeIcon.substring(1)}`;
      return `uiIconActivity ${typeIcon}`;
    },
    activityLikes() {
      if (!this.result) {
        return '';
      }
      const likesCount = this.result.likesCount || 0;
      return this.$t('Search.activity.likesCount', {0: likesCount});
    },
    activityComments() {
      const commentsCount = this.result.commentsCount || 0;
      return this.$t('Search.activity.commentsCount', {0: commentsCount});
    },
    activityReactions() {
      return `${this.activityLikes}, ${this.activityComments}`;
    },
    activityStreamOwner() {
      if (!this.result) {
        return '';
      }
      return this.result.streamOwner.profile && this.result.streamOwner.profile.fullname || this.result.streamOwner.space && this.result.streamOwner.space.displayName || '';
    },
    postedTime() {
      if (!this.result) {
        return '';
      }
      return this.result.comment && this.result.comment.postedTime || this.result.postedTime;
    },
    link() {
      if (this.isComment) {
        return `/${eXo.env.portal.containerName}/${eXo.env.portal.metaPortalName}/activity?id=${this.result.id}#comment-comment${this.result.comment.id}`;
      } else {
        return `/${eXo.env.portal.containerName}/${eXo.env.portal.metaPortalName}/activity?id=${this.activity.id}`;
      }
    },
  },
  created() {
    this.profileActionExtensions = extensionRegistry.loadExtensions('profile-extension', 'action') || [];
  },
};
</script>
