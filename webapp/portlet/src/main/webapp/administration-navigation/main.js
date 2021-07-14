import './initComponents.js';

// get overrided components if exists
if (extensionRegistry) {
  // eslint-disable-next-line max-len
  const components = extensionRegistry.loadComponents('SiteHamburgerNavigation');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

//add menu entry in Hamburger Menu
// eslint-disable-next-line max-len
document.dispatchEvent(new CustomEvent('exo-hamburger-menu-navigation-refresh'));
