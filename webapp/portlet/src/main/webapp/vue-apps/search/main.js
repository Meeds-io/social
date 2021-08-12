import './initComponents.js';

const appId = 'SearchApplication';
const appName = 'Search';

export function init() {
  document.dispatchEvent(new CustomEvent('displayTopBarLoading'));
  const connectors = JSON.parse(document.getElementById('searchConnectorsDefaultValue').value);
  const skinUrls = JSON.parse(document.getElementById('searchSkinUrlsDefaultValue').value);
  Vue.createApp({
    data: {
      connectors,
      skinUrls,
    },
    template: `<search-application id="${appId}" :connectors="connectors" :skin-urls="skinUrls" />`,
    vuetify: Vue.prototype.vuetifyOptions,
    i18n: exoi18n.i18n,
  }, `#${appId}`, appName);
}
