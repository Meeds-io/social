import { spacesConstants } from '../js/spacesConstants.js';

document.dispatchEvent(new CustomEvent('displayTopBarLoading'));

Vue.use(Vuetify);
Vue.use(VueEllipsis);
const vuetify = new Vuetify({
  dark: true,
  iconfont: '',
});

//getting language of user
const lang = eXo && eXo.env.portal.language || 'en';

//should expose the locale ressources as REST API 
const url = `${spacesConstants.PORTAL}/${spacesConstants.PORTAL_REST}/i18n/bundle/locale.portlet.social.SpacesListApplication-${lang}.json`;

const appId = 'spacesListApplication';

export function init(filter, canCreateSpace) {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    const appElement = document.createElement('div');
    appElement.id = appId;

    // init Vue app when locale ressources are ready
    new Vue({
      data: () => ({
        loaded: false,
      }),
      created() {
        this.$root.$on('application-loaded', () => {
          if (this.loaded) {
            return;
          }
          try {
            this.cacheDom();
            this.mountApplication();
          } finally {
            this.loaded = true;
          }
        });
      },
      methods: {
        mountApplication() {
          const cachedAppElement = document.querySelector(`.VuetifyApp #${appId}`);
          cachedAppElement.parentElement.replaceChild(this.$root.$el, cachedAppElement);
        },
        cacheDom() {
          window.caches.open('pwa-resources-dom')
            .then(cache => {
              if (cache) {
                cache.put(`/dom-cache?id=${appId}`, new Response(this.$root.$el.innerHTML));
              }
            });
        },
      },
      template: `<exo-spaces-list id="${appId}" app-id="${appId}" filter="${filter || 'all'}" :can-create-space="${canCreateSpace}"></exo-spaces-list>`,
      i18n,
      vuetify,
    }).$mount(appElement);
  });
}