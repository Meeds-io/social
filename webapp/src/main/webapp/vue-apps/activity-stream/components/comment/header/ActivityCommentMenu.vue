<template>
  <v-list-item-icon v-show="enabledActions && enabledActions.length" class="mx-0 mb-0 mt-n1 flex-shrink-0">
    <v-menu
      v-model="menu"
      content-class="white"
      bottom
      offset-y
      :left="!$vuetify.rtl"
      :right="$vuetify.rtl"
      :close-on-content-click="!$root.isMobile"
      :attach="attachMenu">
      <template #activator="{ on, attrs }">
        <v-btn
          icon
          small
          :aria-label="$t('activity.head.menu.title.open')"
          v-bind="attrs"
          v-on="on">
          <v-icon size="16" class="icon-default-color">fas fa-ellipsis-v</v-icon>
        </v-btn>
      </template>
      <v-list class="pa-0" dense>
        <v-menu
          v-for="action of enabledActions"
          :key="action.id"
          :disabled="!action.children.length"
          :left="!$vuetify.rtl"
          :right="$vuetify.rtl"
          open-on-hover
          offset-x>
          <template #activator="{ on, attrs }">
            <v-list-item
              v-on="action.click && {
                ...on,
                click: () => !isActionDisabled(action) && clickOnAction(action),
              } || on"
              v-bind="attrs"
              :class="isActionDisabled(action) && 'v-list-item--disabled' || ''"
              :title="actionDisabledTitle(action)"
              :aria-label="isActionDisabled(action) && actionDisabledTitle(action) || $t(action.labelKey)"
              class="px-3"
              dense>
              <v-list-item-icon class="d-flex align-center justify-center ma-auto">
                <v-card
                  class="d-flex align-center justify-center"
                  color="transparent"
                  min-height="24"
                  min-width="20"
                  flat>
                  <v-img
                    v-if="action.icon?.includes?.('base64') || action.icon?.includes?.('/')"
                    :src="action.icon"
                    max-height="16"
                    height="16"
                    max-width="16"
                    contain
                    eager />
                  <v-icon
                    v-else
                    size="16"
                    class="icon-default-color">
                    {{ $t(action.icon) }}
                  </v-icon>
                </v-card>
              </v-list-item-icon>
              <v-list-item-content class="mx-2">
                <v-list-item-title class="menu-text-color">{{ $t(isActionDisabled(action) && action.disabledLabelKey || action.labelKey) }}</v-list-item-title>
              </v-list-item-content>
              <v-list-item-icon
                v-if="action.children.length"
                class="ms-2 me-0 width-auto">
                <v-icon size="16">{{ $vuetify.rtl ? 'fa-caret-left' : 'fa-caret-right' }}</v-icon>
              </v-list-item-icon>
            </v-list-item>
          </template>
          <v-list
            v-if="action.children.length"
            class="pa-0"
            dense>
            <v-list-item
              v-for="act of action.children"
              :key="act.id"
              class="px-3"
              dense
              @click="clickOnAction(act)">
              <v-list-item-icon class="d-flex align-center justify-center ma-auto">
                <v-card
                  class="d-flex align-center justify-center"
                  color="transparent"
                  min-height="24"
                  min-width="20"
                  flat>
                  <v-img
                    v-if="act.icon?.includes?.('base64') || act.icon?.includes?.('/')"
                    :src="act.icon"
                    max-height="16"
                    height="16"
                    max-width="16"
                    contain
                    eager />
                  <v-icon
                    v-else
                    size="16"
                    class="icon-default-color">
                    {{ $t(act.icon) }}
                  </v-icon>
                </v-card>
              </v-list-item-icon>
              <v-list-item-content class="mx-2">
                <v-list-item-title class="menu-text-color">{{ $t(act.labelKey) }}</v-list-item-title>
              </v-list-item-content>
            </v-list-item>
          </v-list>
        </v-menu>
      </v-list>
    </v-menu>
  </v-list-item-icon>
</template>

<script>
export default {
  props: {
    activity: {
      type: Object,
      default: null,
    },
    comment: {
      type: Object,
      default: null,
    },
    actions: {
      type: Object,
      default: null,
    },
    commentTypeExtension: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    menu: false,
    attachMenu: true,
  }),
  created() {
    document.addEventListener('activity-reported', this.handleActivityReported);
    this.$root.$on('activity-stream-activity-updateComment', this.handleCommentUpdatedByWebSocket);
  },
  beforeDestroy() {
    document.removeEventListener('activity-reported', this.handleActivityReported);
    this.$root.$off('activity-stream-activity-updateComment', this.handleCommentUpdatedByWebSocket);
  },
  computed: {
    enabledActions() {
      const enabledActions = this.actions && Object.values(this.actions).filter(action => action.isEnabled && action.id && !action.parentId && (action.click || action.type === 'group') && action.isEnabled(this.activity, this.comment, this.commentTypeExtension));
      enabledActions.sort((a, b) => a.rank - b.rank);
      enabledActions.forEach(ext => {
        ext.children = Object.values(this.actions).filter(e => e.parentId === ext.id);
      });
      return enabledActions;
    },
  },
  mounted() {
    if (this.$el.closest('#activityCommentsDrawer')) {
      this.attachMenu = false;
    }
  },
  watch: {
    menu() {
      if (!this.$root.isMobile) {
        if (this.menu) {
          document.addEventListener('mousedown', this.closeMenu);
        } else {
          document.removeEventListener('mousedown', this.closeMenu);
        }
      }
    },
  },
  methods: {
    handleActivityReported(event) {
      if (this.comment && event?.detail?.isComment && event?.detail?.activityId === this.comment.id) {
        this.$set(this.comment, 'hasReported', true);
      }
    },
    handleCommentUpdatedByWebSocket(activityId, spaceId, commentId) {
      if (this.comment && commentId && commentId === this.comment.id) {
        // Report flags are computed per viewer: the websocket message carries
        // no report data, so the comment has to be refetched to refresh them
        this.$activityService.getActivityById(commentId)
          .then(comment => {
            if (comment) {
              this.$set(this.comment, 'hasReported', comment.hasReported);
              this.$set(this.comment, 'canReport', comment.canReport);
            }
          });
      }
    },
    isActionDisabled(action) {
      return !!(action.disabled && action.disabled(this.activity, this.comment, this.commentTypeExtension));
    },
    actionDisabledTitle(action) {
      return this.isActionDisabled(action) && action.disabledTitleKey && this.$t(action.disabledTitleKey) || '';
    },
    clickOnAction(action) {
      this.closeMenu();
      if (action.confirmDialog) {
        this.$root.$emit('activity-stream-display-confirm', {
          title: action.confirmTitleKey,
          message: action.confirmMessageKey,
          ok: action.confirmOkKey,
          cancel: action.confirmCancelKey,
          callback: () => action.click(this.activity, this.comment, this.commentTypeExtension),
        });
      } else {
        action.click(this.activity, this.comment, this.commentTypeExtension);
      }
    },
    closeMenu() {
      window.setTimeout(() => {
        this.menu = false;
      },200);
    },
  },
};
</script>
