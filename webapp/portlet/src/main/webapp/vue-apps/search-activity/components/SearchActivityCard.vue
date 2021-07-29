<template>
  <v-card
    class="d-flex flex-column border-radius box-shadow"
    flat
    min-height="227">
    <v-card-text v-if="poster" class="px-2 pt-2 pb-0">
      <exo-user-avatar
        :username="posterUsername"
        :fullname="posterFullname"
        :title="posterFullname"
        :external="posterIsExternal"
        :retrieve-extra-information="false"
        avatar-class="border-color">
        <template slot="subTitle">
          <date-format :value="postedTime" />
        </template>
      </exo-user-avatar>
    </v-card-text>
    <div class="mx-auto flex-grow-1 px-3 py-0">
      <div
        ref="excerptNode"
        :title="excerptText"
        class="text-wrap text-break caption">
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
      if ((!this.excerptHtml || this.excerptHtml.length === 0) && (!this.result || !this.result.body || this.result.body.length === 0)) {
        return;
      }
      const excerptParent = this.$refs.excerptNode;
      if (!excerptParent) {
        return;
      }
      excerptParent.innerHTML = this.excerptHtml || this.result.body;

      let charsToDelete = 20;
      let excerptParentHeight = excerptParent.getBoundingClientRect().height || this.lineHeight;
      if (excerptParentHeight > this.maxEllipsisHeight) {
        while (excerptParentHeight > this.maxEllipsisHeight) {
          const newHtml = this.deleteLastChars(excerptParent.innerHTML.replace(/&[a-z]*;/, ''), charsToDelete);
          const oldLength = excerptParent.innerHTML.length;
          excerptParent.innerHTML = newHtml;
          if (excerptParent.innerHTML.length === oldLength) {
            charsToDelete = charsToDelete * 2;
          }
          excerptParentHeight = excerptParent.getBoundingClientRect().height || this.lineHeight;
        }
        excerptParent.innerHTML = this.deleteLastChars(excerptParent.innerHTML, 4);
        excerptParent.innerHTML = `${excerptParent.innerHTML}...`;
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
