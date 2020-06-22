<template>
  <v-card class="d-flex flex-column border-radius box-shadow mr-2 mb-4" flat min-height="227">
    <v-card-text class="px-2 pt-2 pb-1">
      <exo-user-avatar
        :username="poster.username"
        :fullname="poster.fullname"
        :title="poster.fullname"
        avatar-class="border-color">
        <template slot="subTitle">
          <date-format :value="postedTime" />
        </template>
      </exo-user-avatar>
    </v-card-text>
    <v-card-text class="py-0 flex-grow-1 flex-shrink-0">
      <div
        ref="excerptNode"
        :title="excerptText"
        class="text-wrap text-break caption">
      </div>
    </v-card-text>
    <v-list class="light-grey-background flex-grow-0 border-top-color no-border-radius pa-0">
      <v-list-item :href="link" class="px-0 pt-1 pb-2">
        <v-list-item-icon class="mx-0 my-auto">
          <span :class="activityIcon" class="tertiary--text px-1 display-1"></span>
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
    excerptLines: 6,
    lineHeight: 22,
    profileActionExtensions: [],
    format: {
      
    },
  }),
  computed: {
    maxEllipsisHeight() {
      return this.lineHeight * this.excerptLines;
    },
    isComment() {
      return this.result && this.result.comment;
    },
    activity() {
      return this.isComment && this.result.comment || this.result;
    },
    poster() {
      return this.activity && this.activity.poster.profile;
    },
    streamOwner() {
      return this.activity && this.activity.streamOwner.space || this.activity.streamOwner.profile;
    },
    spaceDisplayName() {
      return this.streamOwner && this.streamOwner.displayName;
    },
    excerpts() {
      return this.activity && this.activity.excerpts;
    },
    excerptHtml() {
      return this.excerpts && this.excerpts.join('\r\n...');
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
      let typeIcon = this.activityType.replace(':', '_');
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
        return `/${eXo.env.portal.containerName}/${eXo.env.portal.portalName}/activity?id=${this.result.id}#comment-comment${this.result.comment.id}`;
      } else {
        return `/${eXo.env.portal.containerName}/${eXo.env.portal.portalName}/activity?id=${this.activity.id}`;
      }
    },
  },
  created() {
    this.profileActionExtensions = extensionRegistry.loadExtensions('profile-extension', 'action') || [];
  },
  mounted() {
    this.computeEllipsis();
  },
  methods: {
    computeEllipsis() {
      if (!this.excerptHtml || this.excerptHtml.length === 0) {
        return;
      }
      const stNode = this.$refs.excerptNode;
      if (!stNode) {
        return;
      }
      stNode.innerHTML = this.excerptHtml;

      let stNodeHeight = stNode.getBoundingClientRect().height || this.lineHeight;
      if (stNodeHeight > this.maxEllipsisHeight) {
        while (stNodeHeight > this.maxEllipsisHeight) {
          const newHtml = this.deleteLastChars(stNode.innerHTML.replace(/&[a-z]*;/, ''), 10);
          if (newHtml.length === stNode.innerHTML.length) {
            break;
          }
          stNode.innerHTML = newHtml;
          stNodeHeight = stNode.getBoundingClientRect().height || this.lineHeight;
        }

        stNode.innerHTML = this.deleteLastChars(stNode.innerHTML, 4);
        stNode.innerHTML = `${stNode.innerHTML}...`;
      }
    },
    deleteLastChars(html, charsToDelete) {
      if (html.slice(-1) === '>') {
        // Replace empty tags
        html = html.replace(/<[a-zA-Z 0-9 "'=]*><\/[a-zA-Z 0-9]*>$/g, '');
      }
      html = html.replace(/<br>(\.*)$/g, '');

      charsToDelete = charsToDelete || 1;

      let newHtml = '';
      if (html.slice(-1) === '>') {
        // Delete last inner html char
        html = html.replace(/(<br>)*$/g, '');
        newHtml = html.replace(new RegExp(`([^>]{${charsToDelete}})(</)([a-zA-Z 0-9]*)(>)$`), '$2$3');
        newHtml = $('<div />').html(newHtml).html().replace(/&[a-z]*;/, '');
        if (newHtml.length === html.length) {
          newHtml = html.replace(new RegExp('([^>]*)(</)([a-zA-Z 0-9]*)(>)$'), '$2$3');
        }
      } else {
        newHtml = html.substring(0, html.trimRight().length - charsToDelete);
      }
      return newHtml;
    },
  }
};
</script>
