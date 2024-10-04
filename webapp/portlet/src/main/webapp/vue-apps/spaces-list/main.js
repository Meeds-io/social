document.dispatchEvent(new CustomEvent('displayTopBarLoading'));

//getting language of user
const lang = eXo && eXo.env.portal.language || 'en';

//should expose the locale ressources as REST API 
const url = `/social-portlet/i18n/locale.portlet.social.SpacesListApplication?lang=${lang}`;

const appId = 'spacesListApplication';

export function init(filter, canCreateSpace) {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    // init Vue app when locale ressources are ready
    Vue.createApp({
      data: {
        filter: filter || 'all',
      },
      computed: {
        isMobile() {
          return this.$vuetify.breakpoint.mobile;
        },
      },
      created() {
        this.$root.$on('spaces-list-filter-update', this.updateFilter);
      },
      mounted() {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      },
      beforeDestroy() {
        this.$root.$off('spaces-list-filter-update', this.updateFilter);
      },
      methods: {
        updateFilter(filter) {
          this.filter = filter;
        },
      },
      template: `<spaces-list id="${appId}" :filter="filter" :can-create-space="${canCreateSpace}" />`,
      i18n,
      vuetify: Vue.prototype.vuetifyOptions,
    }, `#${appId}`, 'Spaces List');
  });
}