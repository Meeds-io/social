import HamburgerMenuNavigation from './components/ExoHamburgerMenuNavigation.vue';
import LegacyExoHamburgerMenuNavigation from './components/LegacyExoHamburgerMenuNavigation.vue';

if (eXo.env.portal.leftMenuReviewEnabled) {
  Vue.component('ExoHamburgerMenuNavigation', HamburgerMenuNavigation);
} else {
  Vue.component('ExoHamburgerMenuNavigation', LegacyExoHamburgerMenuNavigation);
}
