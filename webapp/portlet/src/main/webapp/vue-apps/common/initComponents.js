import ExoDrawer from './components/ExoDrawer.vue';
import ExoConfirmDialog from './components/ExoConfirmDialog.vue';
import ExoUserAvatarsList from './components/ExoUserAvatarsList.vue';
import UserAvatar from './components/UserAvatar.vue';
import SpaceAvatar from './components/SpaceAvatar.vue';
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
import AttachmentsDraggableZone from './components/AttachmentsDraggableZone.vue';
import WidgetWrapper from './components/Widget.vue';
import HelpDrawer from './components/HelpDrawer.vue';
import HelpLabel from './components/HelpLabel.vue';
import HelpTooltip from './components/HelpTooltip.vue';

const components = {
  'card-carousel': CardCarousel,
  'exo-user-avatars-list': ExoUserAvatarsList,
  // FIXME should be deleted, deprecated, use user-avatar instead
  'exo-user-avatar': UserAvatar,
  // FIXME should be deleted, deprecated, use space-avatar instead
  'exo-space-avatar': SpaceAvatar,
  'user-avatar': UserAvatar,
  'space-avatar': SpaceAvatar,
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
  'attachments-draggable-zone': AttachmentsDraggableZone,
  'widget-wrapper': WidgetWrapper,
  'help-drawer': HelpDrawer,
  'help-label': HelpLabel,
  'help-tooltip': HelpTooltip,
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

Vue.prototype.$updateApplicationVisibility = function(visible, element) {
  if (!element) {
    element = this?.$root?.$el;
  }
  if (!element?.className?.includes?.('PORTLET-FRAGMENT')) {
    element = element?.parentElement;
  }
  if (element?.parentElement) {
    if (visible) {
      element.closest?.('.PORTLET-FRAGMENT')?.parentElement?.classList?.remove?.('hidden');
    } else {
      element.closest?.('.PORTLET-FRAGMENT')?.parentElement?.classList?.add?.('hidden');
    }
  }
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
    vueApp.$root.$on('alert-message-html-confeti', (message, type, linkCallback, linkIcon, linkTooltip) => {
      document.dispatchEvent(new CustomEvent('alert-message-html-confeti', {detail: {
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

Vue.directive('draggable', {
  bind(el, binding) {
    let counter = 0;
    const enabled = binding?.value;
    if (enabled) {
      ['drag', 'dragstart', 'dragend', 'dragover', 'dragenter', 'dragleave', 'drop'].forEach((event) => {
        el.addEventListener(event, (e) => {
          if (e?.dataTransfer) {
            e.preventDefault();
            e.stopPropagation();
          }
        });
      });
      ['dragenter', 'dragstart'].forEach((event) => {
        el.addEventListener(event, (e) => {
          if (e?.dataTransfer && e?.dataTransfer?.types?.find?.(f => f === 'Files' || f.includes('image/'))) {
            counter++;
            document.dispatchEvent(new CustomEvent('attachments-show-drop-zone'));
          }
        });
      });
      ['dragleave', 'dragend'].forEach((event) => {
        el.addEventListener(event, (e) => {
          if (e?.dataTransfer) {
            counter--;
            if (counter === 0) {
              document.dispatchEvent(new CustomEvent('attachments-hide-drop-zone'));
            }
          }
        });
      });
      el.addEventListener('drop', (e) => {
        if (e?.dataTransfer) {
          counter--;
          if (counter === 0) {
            document.dispatchEvent(new CustomEvent('attachments-drop-files',{detail: e?.dataTransfer.files || []}));
            document.dispatchEvent(new CustomEvent('attachments-hide-drop-zone'));
          }
        }
      });
    }
  }
});
