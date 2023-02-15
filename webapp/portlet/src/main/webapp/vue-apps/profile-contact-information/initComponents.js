import ProfileContactInformation from './components/ProfileContactInformation.vue';
import ProfileContactInformationDrawer from './components/ProfileContactInformationDrawer.vue';
import ProfileContactEditMultiField from './components/ProfileContactEditMultiField.vue';
import ProfileContactEditMultiFieldSelect from './components/ProfileContactEditMultiFieldSelect.vue';
import ProfileContactProperties from './components/ProfileContactProperties.vue';
import ProfileContactProperty from './components/ProfileContactProperty.vue';
import ProfileContactPropertyChild from './components/ProfileContactPropertyChild.vue';
import ProfileContactPropertyValue from './components/ProfileContactPropertyValue.vue';

const components = {
  'profile-contact-information': ProfileContactInformation,
  'profile-contact-information-properties': ProfileContactProperties,
  'profile-contact-information-property': ProfileContactProperty,
  'profile-contact-information-property-child': ProfileContactPropertyChild,
  'profile-contact-information-property-value': ProfileContactPropertyValue,
  'profile-contact-information-drawer': ProfileContactInformationDrawer,
  'profile-contact-edit-multi-field': ProfileContactEditMultiField,
  'profile-contact-edit-multi-field-select': ProfileContactEditMultiFieldSelect,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
