import ExoDrawer from './components/ExoDrawer.vue';
import ExoConfirmDialog from './components/ExoConfirmDialog.vue';
import ExoUserAvatarsList from './components/ExoUserAvatarsList.vue';
import ExoUserAvatar from './components/ExoUserAvatar.vue';
import ExoSpaceAvatar from './components/ExoSpaceAvatar.vue';
import ExoIdentitySuggester from './components/ExoIdentitySuggester.vue';
import ExoActivityRichEditor from './components/ExoActivityRichEditor.vue';
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

const components = {
  'exo-user-avatars-list': ExoUserAvatarsList,
  'exo-user-avatar': ExoUserAvatar,
  'exo-space-avatar': ExoSpaceAvatar,
  'exo-drawer': ExoDrawer,
  'exo-confirm-dialog': ExoConfirmDialog,
  'exo-identity-suggester': ExoIdentitySuggester,
  'exo-activity-rich-editor': ExoActivityRichEditor,
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
};

for (const key in components) {
  Vue.component(key, components[key]);
}

Vue.directive('cacheable', {
  bind(el, binding, vnode) {
    const appId = el.id;
    const cacheId = binding && binding.value && binding.value.cacheId || appId;

    const mountApplication = function() {
      const cachedAppElement = document.querySelector(`#UIPortalApplication #${appId}`);
      if (cachedAppElement) {
        cachedAppElement.parentElement.replaceChild(vnode.componentInstance.$root.$el, cachedAppElement);
      } else {
        // eslint-disable-next-line no-console
        console.warn(`Application with identifier ${appId} was not found in page`);
      }
    };

    const cacheDom = function() {
      if (window.caches) {
        window.caches.open('portal-pwa-resources-dom')
          .then(cache => {
            if (cache) {
              window.setTimeout(() => {
                const domToCache = vnode.componentInstance.$root.$el.innerHTML
                  .replaceAll('<input ', '<input disabled ')
                  .replaceAll('<button ', '<button disabled ')
                  .replaceAll('<select ', '<select disabled ')
                  .replaceAll('<textarea ', '<textarea disabled ');
                cache.put(`/dom-cache?id=${cacheId}`, new Response($(`<div>${domToCache}</div>`).html(), {
                  headers: {'content-type': 'text/html;charset=UTF-8'},
                }));
              }, 200);
            }
          });
      }
    };

    vnode.componentInstance.$root.$once('application-mount', () => {
      mountApplication();
    });

    vnode.componentInstance.$root.$on('application-loaded', () => {
      cacheDom();
      vnode.componentInstance.$root.$vuetify.rtl = eXo.env.portal.orientation === 'rtl';
      vnode.componentInstance.$root.$emit('application-mount');
    });

    vnode.componentInstance.$root.$on('application-cache', () => {
      cacheDom();
    });
  },
  inserted(el, binding, vnode) {
    // Wait at maximum 3 seconds to refresh DOM with real application
    // If the application didn't emitted the event 'application-loaded' yet
    // To avoid having for a long time a static content only
    // In addition, by using '$root.$once', we 're sure that the real application
    // is mounted only once
    window.setTimeout(() => {
      vnode.componentInstance.$root.$emit('application-mount');
    }, 3000);
  },
});