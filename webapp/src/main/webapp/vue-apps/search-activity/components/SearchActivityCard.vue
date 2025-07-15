<template>
  <v-hover v-slot="{ hover }">
    <v-card
      flat
      class="pa-0"
      :aria-label="$t('search.access.to.result', {0 :excerptText})"
      :href="link">
      <v-list class="pa-0" :class="hover && 'light-grey-background-color no-border-radius' || ''">
        <v-list-item>
          <v-list-item-icon class="me-2 pt-1">
            <span class="d-flex align-center justify-center pt-2px">
              <v-avatar
                size="32"
                class="flex-shrink-0">
                <img
                  :src="posterAvatar"
                  class="object-fit-cover ma-auto"
                  loading="lazy"
                  alt="">
              </v-avatar>
            </span>
          </v-list-item-icon>

          <v-list-item-content>
            <v-list-item-title class="d-flex flex-row full-width align-center">
              <div class="d-flex flex-row align-center flex-grow-1 text-truncate">
                <span
                  class="title pt-1 mb-0 ps-0 my-auto text-start text-truncate primary--text font-weight-bold"
                  :aria-label="activityTitleText">
                  {{ posterName }}
                  <span v-if="isSpaceStreamOwner">
                    <v-icon class="icon-default-color ms-1" size="14">
                      fas fa-chevron-right
                    </v-icon>
                    <v-avatar
                      :size="22"
                      tile
                      class="pb-2px me-1 spaceAvatar">
                      <img
                        :src="streamOwner.avatarUrl"
                        alt=""
                        class="object-fit-cover ma-auto"
                        loading="lazy">
                    </v-avatar>
                    {{ streamOwner.displayName }}
                  </span>
                </span>
              </div>
              <div v-show="hover || isMobile" class="ml-2 pt-1">
                <span class="d-inline-flex align-center justify-center">
                  <v-btn
                    icon
                    small
                    class="me-2"
                    @click.stop.prevent="openActivityCommentDrawer">
                    <v-icon class="icon-default-color" size="16">
                      fas fa-comment
                    </v-icon>
                  </v-btn>
                  <v-btn
                    icon
                    small
                    class="me-2"
                    @click.stop.prevent="openKudosForm">
                    <v-icon class="icon-default-color" size="16">
                      fas fa-award
                    </v-icon>
                  </v-btn>
                  <activity-favorite-action
                    :activity="result"
                    @removed="$emit('refresh-favorite')" />
                </span>
              </div>
            </v-list-item-title>

            <v-list-item-subtitle class="d-flex flex-column">
              <span class="d-flex flex-row align-center mx-auto full-width" v-if="postedTime">
                <v-icon
                  size="12"
                  class="icon-default-color">fas fa-clock</v-icon>
                <date-format class="ms-1 my-auto" :value="postedTime" />
              </span>
              <div
                v-if="excerptHtml"
                class="pt-2 text-wrap text-body-2 text-color text-break"
                :class="{
                  'text-truncate-2': isMobile,
                  'text-truncate-3': !isMobile,
                }"
                v-sanitized-html="excerptHtml">
              </div>
              <extension-registry-components
                v-else
                :params="extendedComponentParams"
                :class="'activitySearchResultContent'"
                name="ActivityContent"
                type="activity-content-extensions"
                parent-element="div"
                element="div"
                class="d-flex flex-column" />
            </v-list-item-subtitle>
          </v-list-item-content>
        </v-list-item>
      </v-list>
      <div v-if="activityCommentDrawer">
        <activity-comments-drawer
          ref="activitySearchCommentDrawer"
          :comment-types="activityTypes"
          @closed="closeActivityCommentDrawer" />
      </div>
    </v-card>
  </v-hover>
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
    profileActionExtensions: [],
    extensionApp: 'activity',
    activityTypeExtensionName: 'type',
    activityTypes: {},
    activityCommentDrawer: false,
    activityBaseLink: `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/activity`
  }),
  computed: {
    isComment() {
      return this.result?.comment;
    },
    activity() {
      return this.isComment && this.result?.comment || this.result;
    },
    poster() {
      return this.activity?.poster?.profile;
    },
    posterAvatar() {
      return this.poster?.avatar;
    },
    posterName() {
      return this.poster?.fullname;
    },
    streamOwner() {
      return this.activity?.streamOwner?.space || this.activity?.streamOwner?.profile;
    },
    isSpaceStreamOwner() {
      return this.activity?.streamOwner?.space || false;
    },
    excerpts() {
      return this.activity?.excerpts?.length && this.activity.excerpts || (this.activity.body && [this.activity.body]);
    },
    excerptHtml() {
      return this.excerpts && this.excerpts.join('\r\n...') || '';
    },
    excerptText() {
      return this.$utils.htmlToText(this.excerptHtml);
    },
    activityType() {
      if (!this.result) {
        return '';
      }
      return this.result.comment && this.result.comment.type || this.result.type;
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
    activityTitleText() {
      return this.$utils.htmlToText(this.excerptHtml) || this.$t('search.activity.no.title.label');
    },
    isMobile() {
      return this.$vuetify?.breakpoint?.smAndDown;
    },
    activityTypeExtension() {
      if (!this.activity || !this.activityTypes) {
        return {};
      }
      return this.activityTypes[this.activityType] || this.activityTypes['default'] || {};
    },
    extendedComponentParams() {
      return {
        activity: this.activityEntity?.originalActivity || this.activityEntity,
        activityTypeExtension: this.activityTypeExtension,
        activityTypes: this.activityTypes,
        isActivityDetail: true,
        collapsed: false,
      };
    },
    activityEntity() {
      return this.result?.dataEntity;
    },
    entityType() {
      return this.isComment && 'COMMENT' || 'ACTIVITY';
    },
    commentId() {
      return this.isComment && `comment${this.activity.id}`;
    },
    isOwner() {
      const currentUserName = eXo.env.portal?.username;
      return  currentUserName === this.posterUsername || this.isSpaceStreamOwner && this.streamOwner?.isMember;
    },
    spaceUrl() {
      if (!this.streamOwner?.id) {
        return '#';
      }
      return `${eXo.env.portal.context}/s/${this.streamOwner?.id}`;
    }
  },
  created() {
    this.profileActionExtensions = extensionRegistry.loadExtensions('profile-extension', 'action') || [];
    document.addEventListener(`extension-${this.extensionApp}-${this.activityTypeExtensionName}-updated`, this.refreshActivityTypes);
    this.refreshActivityTypes();
    this.$root.activityBaseLink = this.activityBaseLink;
  },
  methods: {
    refreshActivityTypes() {
      const extensions = extensionRegistry.loadExtensions(this.extensionApp, this.activityTypeExtensionName);
      let changed = false;
      extensions.forEach(extension => {
        if (extension.type && extension.options && (!this.activityTypes[extension.type] || this.activityTypes[extension.type] !== extension.options)) {
          this.activityTypes[extension.type] = extension.options;
          changed = true;
        }
      });
      // force update of attribute to re-render switch new extension type
      if (changed) {
        this.activityTypes = Object.assign({}, this.activityTypes);
      }
    },
    async openActivityCommentDrawer() {
      this.activityCommentDrawer = true;
      await this.$nextTick();
      const options = {
        activity: this.activityEntity,
        newComment: true,
      };
      if (this.commentId) {
        options ['commentId'] = this.commentId;
      }
      this.$refs.activitySearchCommentDrawer.displayActivityComments(options);
    },
    async closeActivityCommentDrawer() {
      await this.$nextTick();
      this.activityCommentDrawer = false;
    },
    openKudosForm() {
      document.dispatchEvent(new CustomEvent('exo-kudos-open-send-modal', {detail: {
        id: this.activity.id,
        parentId: this.isComment && this.activityEntity.id || '',
        type: this.entityType,
        owner: !this.isOwner && this.posterUsername || null,
        spacePrettyName: this.isSpaceStreamOwner && this.streamOwner.prettyName
      }}));
    }
  }
};
</script>
