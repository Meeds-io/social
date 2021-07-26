import ProfileContactInformation from './components/ProfileContactInformation.vue';
import ProfileContactInformationDrawer from './components/ProfileContactInformationDrawer.vue';
import ProfileContactPhone from './components/ProfileContactPhone.vue';
import ProfileContactIms from './components/ProfileContactIms.vue';
import ProfileContactUrls from './components/ProfileContactUrls.vue';
import ProfileContactEditIms from './components/ProfileContactEditIms.vue';
import ProfileContactEditPhone from './components/ProfileContactEditPhone.vue';
import ProfileContactEditUrls from './components/ProfileContactEditUrls.vue';
import ProfileContactEditMultiField from './components/ProfileContactEditMultiField.vue';
import ProfileContactEditMultiFieldSelect from './components/ProfileContactEditMultiFieldSelect.vue';

const components = {
  'profile-contact-information': ProfileContactInformation,
  'profile-contact-phone': ProfileContactPhone,
  'profile-contact-ims': ProfileContactIms,
  'profile-contact-urls': ProfileContactUrls,
  'profile-contact-information-drawer': ProfileContactInformationDrawer,
  'profile-contact-edit-multi-field': ProfileContactEditMultiField,
  'profile-contact-edit-multi-field-select': ProfileContactEditMultiFieldSelect,
  'profile-contact-edit-ims': ProfileContactEditIms,
  'profile-contact-edit-phone': ProfileContactEditPhone,
  'profile-contact-edit-urls': ProfileContactEditUrls,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
