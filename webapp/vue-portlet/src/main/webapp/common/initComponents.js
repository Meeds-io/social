import ExoDrawer from './components/ExoDrawer.vue';
import ExoConfirmDialog from './components/ExoConfirmDialog.vue';
import ExoUserAvatarsList from './components/ExoUserAvatarsList.vue';
import ExoUserAvatar from './components/ExoUserAvatar.vue';
import ExoSpaceAvatar from './components/ExoSpaceAvatar.vue';
import ExoIdentitySuggester from './components/ExoIdentitySuggester.vue';
import ExoActivityRichEditor from './components/ExoActivityRichEditor.vue';
import DatePicker from './components/DatePicker.vue';

const components = {
  'exo-user-avatars-list': ExoUserAvatarsList,
  'exo-user-avatar': ExoUserAvatar,
  'exo-space-avatar': ExoSpaceAvatar,
  'exo-drawer': ExoDrawer,
  'exo-confirm-dialog': ExoConfirmDialog,
  'exo-identity-suggester': ExoIdentitySuggester,
  'exo-activity-rich-editor': ExoActivityRichEditor,
  'date-picker': DatePicker,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
