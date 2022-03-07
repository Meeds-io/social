import './initComponents.js';

const appId = 'SearchApplication';
const appName = 'Search';

let initialized = false;

// Handle Tag Link click
document.onclick = (event) => {
  if (event && event.target && event.target.className && event.target.className.includes('metadata-tag')) {
    const tagName = event.target.innerText;
    if (tagName) {
      event.stopPropagation();
      event.preventDefault();
      if (!initialized) {
        init(tagName.replace('#', ''));
      }
      document.dispatchEvent(new CustomEvent('search-metadata-tag', {detail: tagName.replace('#', '')}));
    }
  }
};

export function init(tagName) {
  if (initialized) {
    return;
  }
  initialized = true;
  document.dispatchEvent(new CustomEvent('displayTopBarLoading'));
  const connectors = JSON.parse(document.getElementById('searchConnectorsDefaultValue').value);
  const skinUrls = JSON.parse(document.getElementById('searchSkinUrlsDefaultValue').value);
  Vue.createApp({
    data: {
      tagName,
      connectors,
      skinUrls,
    },
    template: `<search-application id="${appId}" :connectors="connectors" :skin-urls="skinUrls" />`,
    vuetify: Vue.prototype.vuetifyOptions,
    i18n: exoi18n.i18n,
  }, `#${appId}`, appName);
}
