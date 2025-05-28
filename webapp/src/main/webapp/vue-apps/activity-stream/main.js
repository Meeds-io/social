import './initComponents.js';
import './extensions.js';
import './services.js';

const activityBaseLink = `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/activity`;

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('ActivityStream');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

// Disable swipe for Mobile when Stream pages are displayed
window.disableSwipeOnPage = true;

export async function init({
  appId,
  settings,
  saveSettingsUrl,
  canEdit,
  maxUploadSize,
  spaceId,
}) {
  document.dispatchEvent(new CustomEvent('displayTopBarLoading'));
  const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';
  const urls = [
    `/social/i18n/locale.portlet.Portlets?lang=${lang}`,
    `/social/i18n/locale.commons.Commons?lang=${lang}`,
    `/social/i18n/locale.social.Webui?lang=${lang}`,
  ];
  const i18n = await exoi18n.loadLanguageAsync(lang, urls);
  let settingsSubcategoryIds;
  if (settings?.categoryIds?.length) {
    settingsSubcategoryIds = await getSubcategoryIds(settings.categoryIds, 1);
  }
  try {
    await Vue.createApp({
      data: {
        settings: {
          allowPostToNetwork: true,
          nameTranslations: [],
          allowFilteringPerCategory: true,
          categoryDepth: 4,
          categoryIds: [],
          ...settings,
        },
        appId,
        spaceId: spaceId || eXo.env.portal.spaceId,
        saveSettingsUrl,
        canEdit,
        maxFileSize: maxUploadSize,
        activityBaseLink: activityBaseLink,
        selectedActivityId: null,
        selectedCommentId: null,
        canPost: null,
        replyToComment: false,
        displayCommentActionTypes: [],
        selectedCategoryId: null,
        selectedCategoryIds: null,
        settingsSubcategoryIds,
      },
      computed: {
        isMobile() {
          return this.$vuetify?.breakpoint?.mobile;
        },
        isDesktop() {
          return this.$vuetify.breakpoint.width >= this.$vuetify.breakpoint.thresholds.lg;
        },
        categoryIds() {
          return this.settingsSubcategoryIds;
        },
        allowFilteringPerCategory() {
          return this.settings.allowFilteringPerCategory;
        },
        categoryDepth() {
          return this.settings.categoryDepth || 4;
        },
      },
      watch: {
        async selectedCategoryId() {
          if (this.selectedCategoryId) {
            this.selectedCategoryIds = await getSubcategoryIds([this.selectedCategoryId], -1);
          } else {
            this.selectedCategoryIds = await getSubcategoryIds(this.settings.categoryIds || [], -1);
          }
        },
      },
      created() {
        this.replyToComment = window.location.hash.includes('#comment-reply');
        this.$root.$on('activity-stream-settings-updated', this.handleSettingsUpdate);
        this.$root.$on('categories-updated', this.handleCategoryUpdate);
      },
      mounted() {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      },
      beforeDestroy() {
        this.$root.$off('activity-stream-settings-updated', this.handleSettingsUpdate);
        this.$root.$off('categories-updated', this.handleCategoryUpdate);
      },
      methods: {
        async handleSettingsUpdate() {
          this.settings = JSON.parse(JSON.stringify(this.settings)); // Force update
          this.settingsSubcategoryIds = await getSubcategoryIds(this.settings.categoryIds, 1);
          this.selectedCategoryIds = await getSubcategoryIds(this.settings.categoryIds || [], -1);
        },
        handleCategoryUpdate(objectType, objectId) {
          if (objectType === 'activity' && !this.timeout) {
            this.timeout = window.setTimeout(() => {
              this.timeout = null;
              this.$root.$emit('activity-updated', objectId);
            }, 50);
          }
        },
      },
      template: `<activity-stream id="${appId}" />`,
      vuetify: Vue.prototype.vuetifyOptions,
      i18n,
    }, `#${appId}`, 'Stream');
  } finally {
    Vue.prototype.$utils.includeExtensions('ActivityStreamExtension');
  }
}

async function getSubcategoryIds(categoryIds, depth) {
  if (!categoryIds?.length) {
    return [];
  }
  const subcategoyIds = await Promise.all(categoryIds.map(id => Vue.prototype.$categoryService.getSubcategoryIds(id, {
    offset: 0,
    limit: -1,
    depth,
  })));
  const subcategoyIdsFlat = subcategoyIds.flatMap(s => s);
  subcategoyIdsFlat.push(...categoryIds);
  return [...new Set(subcategoyIdsFlat)];
}
