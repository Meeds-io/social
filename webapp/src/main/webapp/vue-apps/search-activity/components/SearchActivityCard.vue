<template>
  <v-hover v-slot="{ hover }">
    <v-card
      flat
      class="pa-0"
      @click="openActivity">
      <v-list class="pa-0" :class="hover && 'light-grey-background-color no-border-radius' || ''">
        <v-list-item>
          <v-list-item-icon class="me-2">
            <span class="d-flex align-center justify-center">
              <v-icon size="32" class="icon-default-color mt-2">fas fa-stream</v-icon>
            </span>
          </v-list-item-icon>

          <v-list-item-content>
            <v-list-item-title class="d-flex flex-row full-width align-center">
              <p
                :title="excerptText || activityTitle"
                class="flex-grow-1 title font-weight-bold pt-1 mb-0 ps-0 my-auto align-center text-start text-truncate"
                v-sanitized-html="activityTitle"></p>
              <div class="ml-2 pt-1">
                <span v-if="hover || isMobile" class="d-flex d-inline-flex">
                  <activity-favorite-action
                    :activity="result"
                    class="ms-4"
                    @removed="$emit('refresh-favorite')" />
                </span>
              </div>
            </v-list-item-title>

            <v-list-item-subtitle class="d-flex flex-column">
              <span class="d-flex flex-row align-center mx-auto full-width">
                <span class="d-flex flex-row align-center" v-if="isSpaceStreamOwner">
                  <exo-space-avatar
                    :space-id="streamOwner.id"
                    size="18"
                    text-truncate-class="text-truncate text-sub-title"
                    small-font-size
                    subtitle-new-line-class
                    :avatar="isMobile"
                    popover />
                  <v-icon size="3" class="icon-default-color mx-3">fas fa-circle</v-icon>
                </span>
                <exo-user-avatar
                  :profile-id="posterUsername"
                  :size="18"
                  small-font-size
                  :avatar="isMobile"
                  :popover="!isMobile" />
                <span class="d-flex flex-row align-center" v-if="postedTime">
                  <v-icon
                    size="3"
                    class="icon-default-color mx-3">fas fa-circle</v-icon>
                  <v-icon
                    size="12"
                    class="icon-default-color">fas fa-clock</v-icon>
                  <date-format class="ms-1 my-auto" :value="postedTime" />
                </span>
              </span>
              <div
                v-if="excerptHtml"
                class="pt-2 text-wrap text-body text-break"
                :title="excerptText"
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
    body() {
      return this.activity?.body;
    },
    posterUsername() {
      return this.poster?.username;
    },
    streamOwner() {
      return this.activity?.streamOwner?.space || this.activity?.streamOwner?.profile;
    },
    isSpaceStreamOwner() {
      return this.activity?.streamOwner?.space || false;
    },
    excerpts() {
      return this.activity && this.activity.excerpts || (this.activity.title && [this.activity.title]) || (this.activity.body && [this.activity.body]);
    },
    excerptHtml() {
      return this.excerpts && this.excerpts.join('\r\n...') || this.body || '';
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
    activityTitle() {
      return this.excerptHtml || this.$t('search.activity.no.title.label');
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
        activity: this.activity?.dataEntity?.originalActivity || this.activity?.dataEntity,
        activityTypeExtension: this.activityTypeExtension,
        activityTypes: this.activityTypes,
        isActivityDetail: true,
        collapsed: false,
      };
    },
  },
  created() {
    this.profileActionExtensions = extensionRegistry.loadExtensions('profile-extension', 'action') || [];
    document.addEventListener(`extension-${this.extensionApp}-${this.activityTypeExtensionName}-updated`, this.refreshActivityTypes);
    this.refreshActivityTypes();
  },
  methods: {
    openActivity() {
      if (this.link) {
        window.location.href = this.link;
      }
    },
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
  }
};
</script>
