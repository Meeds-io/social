import ExoDrawer from './components/ExoDrawer.vue';
import ExoConfirmDialog from './components/ExoConfirmDialog.vue';
import ExoUserAvatarsList from './components/ExoUserAvatarsList.vue';
import ExoUserAvatar from './components/ExoUserAvatar.vue';
import ExoSpaceAvatar from './components/ExoSpaceAvatar.vue';
import ExoIdentitySuggester from './components/ExoIdentitySuggester.vue';
import RichEditor from './components/RichEditor.vue';
import DatePicker from './components/DatePicker.vue';
import TimePicker from './components/TimePicker.vue';
import DateFormat from './components/DateFormat.vue';
import RelativeDateFormat from './components/RelativeDateFormat.vue';
import ExoModal from './components/ExoModal.vue';
import ExtendedTextarea from './components/ExtendedTextarea.vue';
import ExoNotificationAlert from './components/ExoNotificationAlert.vue';
import ExtensionRegistryComponent from './components/ExtensionRegistryComponent.vue';
import ExtensionRegistryComponents from './components/ExtensionRegistryComponents.vue';
import DynamicHTMLElement from './components/DynamicHTMLElement.vue';
import ExoGroupSuggester from './components/ExoGroupSuggester.vue';
import CardCarousel from './components/CardCarousel.vue';
import DrawersOverlay from './components/DrawersOverlay.vue';
import ActivityShareDrawer from './components/ActivityShareDrawer.vue';
import FavoriteButton from './components/FavoriteButton.vue';
import ChangesReminder from './components/ChangesReminder.vue';
import UnreadBadge from './components/UnreadBadge.vue';
import Notifications from './components/Notifications.vue';
import RippleHoverButton from './components/RippleHoverButton.vue';

const components = {
  'card-carousel': CardCarousel,
  'exo-user-avatars-list': ExoUserAvatarsList,
  'exo-user-avatar': ExoUserAvatar,
  'exo-space-avatar': ExoSpaceAvatar,
  'exo-drawer': ExoDrawer,
  'activity-share-drawer': ActivityShareDrawer,
  'drawers-overlay': DrawersOverlay,
  'exo-confirm-dialog': ExoConfirmDialog,
  'exo-identity-suggester': ExoIdentitySuggester,
  // FIXME should be deleted, deprecated, use rich-editor instead
  'exo-activity-rich-editor': RichEditor,
  'rich-editor': RichEditor,
  'date-picker': DatePicker,
  'time-picker': TimePicker,
  'date-format': DateFormat,
  'relative-date-format': RelativeDateFormat,
  'exo-modal': ExoModal,
  'extended-textarea': ExtendedTextarea,
  'exo-notification-alert': ExoNotificationAlert,
  'extension-registry-component': ExtensionRegistryComponent,
  'extension-registry-components': ExtensionRegistryComponents,
  'dynamic-html-element': DynamicHTMLElement,
  'exo-group-suggester': ExoGroupSuggester,
  'favorite-button': FavoriteButton,
  'changes-reminder': ChangesReminder,
  'unread-badge': UnreadBadge,
  'alert-notifications': Notifications,
  'ripple-hover-button': RippleHoverButton,
};

for (const key in components) {
  Vue.component(key, components[key]);
}

document.addEventListener('readystatechange', function (event){
  if (!eXo.env.portal.onLoad) {
    return;
  }
  if (event.target.readyState === 'interactive') {
    if (eXo.developing) {
      // eslint-disable-next-line no-console
      console.warn('Document "interactive" loading started, load deferred applications');
    }
    eXo.env.portal.onLoadCalled = true;
    eXo.env.portal.onLoad();
  } else if (event.target.readyState === 'complete') {
    if (!eXo.env.portal.onLoadCalled) {
      eXo.env.portal.onLoadCalled = true;
      eXo.env.portal.onLoad();
    }
  }
}, false);


Vue.prototype.$applicationLoaded = function() {
  this.$root.$emit('application-loaded');
  document.dispatchEvent(new CustomEvent('vue-app-loading-end', {detail: {
    appName: this.appName,
    time: Date.now(),
  }}));
};

Vue.createApp = function(params, el, appName) {
  const element = typeof el === 'string' ? document.querySelector(el) : el;
  if (element) {
    if (!params.data) {
      params.data = {};
    } else if (typeof params.data === 'function') {
      params.data = params.data();
    }
    params.data.appName = appName || element.id;
    document.dispatchEvent(new CustomEvent('vue-app-loading-start', {detail: {
      appName: params.data.appName,
      time: Date.now(),
    }}));
    const vueApp = new Vue(params);
    vueApp.$root.$on('alert-message', (message, type, linkCallback, linkIcon, linkTooltip) => {
      document.dispatchEvent(new CustomEvent('alert-message', {detail: {
        alertType: type,
        alertMessage: message,
        alertLinkCallback: linkCallback,
        alertLinkIcon: linkIcon,
        alertLinkTooltip: linkTooltip,
      }}));
    });
    vueApp.$root.$on('alert-message-html', (message, type, linkCallback, linkIcon, linkTooltip) => {
      document.dispatchEvent(new CustomEvent('alert-message-html', {detail: {
        alertType: type,
        alertMessage: message,
        alertLinkCallback: linkCallback,
        alertLinkIcon: linkIcon,
        alertLinkTooltip: linkTooltip,
      }}));
    });
    vueApp.$root.$on('close-alert-message', () => {
      document.dispatchEvent(new CustomEvent('close-alert-message'));
    });
    vueApp.$mount(element);
    return vueApp;
  } else {
    // eslint-disable-next-line no-console
    console.warn(`Can't mount ${el} application because DOM element doesn't exist'`);
  }
};

Vue.startApp = function(jsModule, methodName, params) {
  window.require([jsModule], app => {
    if (methodName) {
      app[methodName](params);
    }
  });
};

Vue.directive('cacheable', {
  bind(el, binding, vnode) {
    const appId = el.id;

    const mountApplication = function() {
      const cachedAppElement = document.querySelector(`#UIPortalApplication #${appId}`);
      if (cachedAppElement) {
        cachedAppElement.parentElement.replaceChild(vnode.componentInstance.$root.$el, cachedAppElement);
      } else {
        // eslint-disable-next-line no-console
        console.warn(`Application with identifier ${appId} was not found in page`);
      }
    };

    vnode.componentInstance.$root.$once('application-mount', automaticMount => {
      if (automaticMount) {
        // eslint-disable-next-line no-console
        console.warn(`It seems that ${el.id} didn't stopped displaying top bar loading`);
      }
      mountApplication();
    });

    vnode.componentInstance.$root.$on('application-loaded', () => {
      vnode.componentInstance.$root.$vuetify.rtl = eXo.env.portal.orientation === 'rtl';
      vnode.componentInstance.$root.$emit('application-mount');
    });
  },
  inserted(el, binding, vnode) {
    // Wait at maximum 3 seconds to refresh DOM with real application
    // If the application didn't emitted the event 'application-loaded' yet
    // To avoid having for a long time a static content only
    // In addition, by using '$root.$once', we 're sure that the real application
    // is mounted only once
    window.setTimeout(() => {
      vnode.componentInstance.$root.$emit('application-mount', true);
    }, 10000);
  },
});