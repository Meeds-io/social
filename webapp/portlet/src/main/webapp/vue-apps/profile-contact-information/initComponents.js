import ProfileContactInformation from './components/ProfileContactInformation.vue';
import ProfileContactInformationDrawer from './components/ProfileContactInformationDrawer.vue';
import ProfileMultiValuedProperty from './components/ProfileMultiValuedProperty.vue';
import ProfileContactEditMultiField from './components/ProfileContactEditMultiField.vue';
import ProfileContactEditMultiFieldSelect from './components/ProfileContactEditMultiFieldSelect.vue';
import ProfileSingleValuedProperty from './components/ProfileSingleValuedProperty.vue';
import QuickSearchUsersListDrawer from './components/QuickSearchUsersListDrawer.vue';
import ProfileHidePropertyButton from './components/ProfileHidePropertyButton.vue';
import ProfileHiddenPropertyInfo from './components/ProfileHiddenPropertyInfo.vue';

const components = {
  'profile-contact-information': ProfileContactInformation,
  'profile-multi-valued-property': ProfileMultiValuedProperty,
  'profile-contact-information-drawer': ProfileContactInformationDrawer,
  'profile-contact-edit-multi-field': ProfileContactEditMultiField,
  'profile-contact-edit-multi-field-select': ProfileContactEditMultiFieldSelect,
  'profile-single-valued-property': ProfileSingleValuedProperty,
  'quick-search-users-list-drawer': QuickSearchUsersListDrawer,
  'profile-hide-property-button': ProfileHidePropertyButton,
  'profile-hidden-property-info': ProfileHiddenPropertyInfo
};

for (const key in components) {
  Vue.component(key, components[key]);
}
