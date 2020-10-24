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
import ExoModal from './components/ExoModal.vue';
import ExtendedTextarea from './components/ExtendedTextarea.vue';

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
  'exo-modal': ExoModal,
  'extended-textarea': ExtendedTextarea,
};

for (const key in components) {
  Vue.component(key, components[key]);
}

Vue.directive('cacheable', {
  bind: function (el, binding, vnode) {
    const appId = el.id;

    const mountApplication = function() {
      const cachedAppElement = document.querySelector(`.VuetifyApp #${appId}`);
      cachedAppElement.parentElement.replaceChild(vnode.componentInstance.$root.$el, cachedAppElement);
    };

    const cacheDom = function() {
      window.caches.open('pwa-resources-dom')
        .then(cache => {
          if (cache) {
            window.setTimeout(() => {
              const domToCache = vnode.componentInstance.$root.$el.innerHTML.replaceAll('<input ', '<input disabled ').replaceAll('<button ', '<button disabled ');
              cache.put(`/dom-cache?id=${appId}`, new Response(domToCache));
            }, 200);
          }
        });
    };

    vnode.componentInstance.$root.$on('application-loaded', () => {
      if (vnode.componentInstance.loaded) {
        return;
      }
      try {
        cacheDom();
        mountApplication();
      } finally {
        vnode.componentInstance.loaded = true;
      }
    });
  }
});
