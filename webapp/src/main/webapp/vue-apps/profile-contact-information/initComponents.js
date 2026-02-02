import ProfileContactInformation from './components/ProfileContactInformation.vue';
import ProfileContactInformationDrawer from './components/ProfileContactInformationDrawer.vue';
import ProfileMultiValuedProperty from './components/property-type/ProfileMultiValuedProperty.vue';
import ProfileContactEditMultiField from './components/property-type/action/ProfileContactEditMultiField.vue';
import ProfileContactEditMultiFieldSelect from './components/property-type/action/ProfileContactEditMultiFieldSelect.vue';
import ProfileSingleValuedProperty from './components/property-type/ProfileSingleValuedProperty.vue';
import QuickSearchUsersListDrawer from './components/QuickSearchUsersListDrawer.vue';
import ProfileHidePropertyButton from './components/property-type/action/ProfileHidePropertyButton.vue';
import ProfileHiddenPropertyInfo from './components/property-type/ProfileHiddenPropertyInfo.vue';
import ProfileContactUserTypeProperty from './components/property-type/ProfileContactUserTypeProperty.vue';
import ProfileUserTypePropertyValues from './components/property-type/ProfileUserTypePropertyValues.vue';
import ProfileDropdownProperty from './components/property-type/ProfileDropdownProperty.vue';
import ProfilePropertyInput from './components/property-type/action/ProfilePropertyInput.vue';

const components = {
  'profile-contact-information': ProfileContactInformation,
  'profile-multi-valued-property': ProfileMultiValuedProperty,
  'profile-contact-information-drawer': ProfileContactInformationDrawer,
  'profile-contact-edit-multi-field': ProfileContactEditMultiField,
  'profile-contact-edit-multi-field-select': ProfileContactEditMultiFieldSelect,
  'profile-single-valued-property': ProfileSingleValuedProperty,
  'quick-search-users-list-drawer': QuickSearchUsersListDrawer,
  'profile-hide-property-button': ProfileHidePropertyButton,
  'profile-hidden-property-info': ProfileHiddenPropertyInfo,
  'profile-contact-user-type-property': ProfileContactUserTypeProperty,
  'profile-user-type-property-values': ProfileUserTypePropertyValues,
  'profile-dropdown-property': ProfileDropdownProperty,
  'profile-property-input': ProfilePropertyInput
};

for (const key in components) {
  Vue.component(key, components[key]);
}
