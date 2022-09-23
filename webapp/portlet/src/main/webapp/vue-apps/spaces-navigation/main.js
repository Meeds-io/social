import './initComponents.js';
import * as spacesAdministrationServices from '../spaces-administration/spacesAdministrationServices.js';

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('SpacesHamburgerNavigation');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

window.Object.defineProperty(Vue.prototype, '$spacesAdministrationServices', {
  value: spacesAdministrationServices,
});

//add menu entry in Hamburger Menu
document.dispatchEvent(new CustomEvent('exo-hamburger-menu-navigation-refresh'));
