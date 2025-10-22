import './initComponents.js';

// get overrided components if exists
if (extensionRegistry) {
  const overrideComponents = extensionRegistry.loadComponents('SearchApplication');
  if (overrideComponents && overrideComponents.length) {
    overrideComponents.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

const appId = 'SearchApplication';
const appName = 'Search';

let initialized = false;

// Handle Tag Link click
document.onclick = (event) => {
  if (event?.target?.className
      && JSON.stringify(event.target.className).includes('metadata-tag')) {
    const tagName = event.target.innerText;
    if (tagName
      && document.querySelector(`#${appId}`)) {
      event.stopPropagation();
      event.preventDefault();
      if (!initialized) {
        init(tagName.replace('#', ''));
      }
      document.dispatchEvent(new CustomEvent('search-metadata-tag', {detail: tagName.replace('#', '')}));
    }
  }
};

export async function init(tagName) {
  if (initialized || !document.querySelector(`#${appId}`)) {
    return;
  }
  initialized = true;
  const connectors = JSON.parse(document.getElementById('searchConnectorsDefaultValue').value);
  const skinUrls = JSON.parse(document.getElementById('searchSkinUrlsDefaultValue').value);
  await new Promise(resolve => Vue.createApp({
    data: {
      disableAutoSearch: false,
      disablePlaceholder: false,
      tagName,
      connectors,
      skinUrls,
    },
    computed: {
      isMobile() {
        return this.$vuetify.breakpoint.smAndDown;
      }
    },
    created() {
      document.addEventListener('search-disable-auto-search', this.setDisableAutoSearch);
      document.addEventListener('search-disable-no-result', this.setDisablePlaceholder);
      resolve();
    },
    methods: {
      setDisableAutoSearch() {
        this.disableAutoSearch = true;
      },
      setDisablePlaceholder() {
        this.disablePlaceholder = true;
      },
    },
    template: `<search-application id="${appId}" :connectors="connectors" :skin-urls="skinUrls" />`,
    vuetify: Vue.prototype.vuetifyOptions,
    i18n: exoi18n.i18n,
  }, `#${appId}`, appName));
  Vue.prototype.$utils.includeExtensions('SearchExtension');
}
