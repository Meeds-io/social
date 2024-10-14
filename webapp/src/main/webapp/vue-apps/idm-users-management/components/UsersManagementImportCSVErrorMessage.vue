<template>
  <v-flex v-if="error" class="d-flex flex-wrap">
    <template v-if="errorMessage === 'UPLOAD_ID:MANDATORY'">
      {{ $t('UsersManagement.error.importCSV.uploadIdMandatory') }}
    </template>
    <template v-else-if="errorMessage === 'UPLOAD_ID:NOT_FOUND' || errorMessage === 'UPLOAD_ID_FILE:NOT_FOUND'">
      {{ $t('UsersManagement.error.importCSV.uploadedFileNotfound') }}
    </template>
    <template v-else-if="errorMessage === 'UPLOAD_ID_PROGRESS:NOT_FOUND'">
      {{ $t('UsersManagement.error.importCSV.uploadedFileProgressNotfound') }}
    </template>
    <template v-else-if="errorMessage === 'UPLOAD_ID_PROCESSING:ALREADY_PROCESSING'">
      {{ $t('UsersManagement.error.importCSV.uploadedFileIsAlreadyProcessing') }}
    </template>
    <template v-else-if="errorMessage === 'ERROR_READING_FILE'">
      {{ $t('UsersManagement.error.importCSV.errorReadingUploadedFile') }}
    </template>
    <template v-else-if="errorMessage === 'BAD_FORMAT:FILE_EMPTY'">
      {{ $t('UsersManagement.error.importCSV.fileIsEmpty') }}
    </template>
    <template v-else-if="createUserErrorMessage">
      {{ createUserErrorMessage }}
    </template>
    <template v-else-if="errorMessage === 'BAD_LINE_FORMAT:MISSING_USERNAME'">
      {{ $t('UsersManagement.error.importCSV.missingUserName') }}
    </template>
    <template v-else-if="errorMessage === 'BAD_LINE_FORMAT'">
      {{ $t('UsersManagement.error.importCSV.badLineFormat', {0: userName}) }}
    </template>
    <template v-else-if="validationErrorMessage">
      {{ validationErrorMessage }}
    </template>
    <template v-else-if="errorMessage === 'USERNAME:ALREADY_EXISTS'">
      {{ $t('UsersManagement.error.importCSV.userAlreadyExists', {0: userName}) }}
    </template>
    <template v-else-if="errorMessage === 'EMAIL:ALREADY_EXISTS'">
      {{ $t('UsersManagement.error.importCSV.emailAlreadyExists', {0: userName}) }}
    </template>
    <template v-else-if="groupNotExistsErrorMessage">
      {{ groupNotExistsErrorMessage }}
    </template>
    <template v-else-if="membershipNotExistsErrorMessage">
      {{ membershipNotExistsErrorMessage }}
    </template>
    <template v-else-if="membershipImportErrorMessage">
      {{ membershipImportErrorMessage }}
    </template>
    <template v-else-if="createUserProfileErrorMessage">
      {{ createUserProfileErrorMessage }}
    </template>
    <template v-else-if="unauthorizedFieldErrorMessage">
      {{ unauthorizedFieldErrorMessage }}
    </template>
    <template v-else-if="parentPropertyShouldNotHaveValuesErrorMessage">
      {{ parentPropertyShouldNotHaveValuesErrorMessage }}
    </template>
    <template v-else-if="propertyHasMoreThanOneParentErrorMessage">
      {{ propertyHasMoreThanOneParentErrorMessage }}
    </template>
    <template v-else-if="propertyHasAMissingParentPropertyErrorMessage">
      {{ propertyHasAMissingParentPropertyErrorMessage }}
    </template>
    <template v-else-if="customMultivaluedFieldErrorMessage">
      {{ customMultivaluedFieldErrorMessage }}
    </template>
    <template v-else-if="customParentErrorMessage">
      {{ customParentErrorMessage }}
    </template>
    <template v-else>
      {{ error }}
    </template>
  </v-flex>
</template>

<script>
export default {
  props: {
    error: {
      type: String,
      default: null,
    },
    userName: {
      type: String,
      default: null,
    },
  },
  computed: {
    errorMessage() {
      return this.error && String(this.error).replace('Error: ', '');
    },
    validationErrorMessage() {
      return this.errorMessage && this.errorMessage.indexOf('USER_VALIDATION_ERROR:') > -1 && this.errorMessage.split(':')[1];
    },
    createUserErrorMessage() {
      return this.errorMessage && this.errorMessage.indexOf('CREATE_USER_ERROR') > -1 && this.$t('UsersManagement.error.importCSV.errorCreatingUser');
    },
    groupNotExistsErrorMessage() {
      return this.errorMessage && this.errorMessage.indexOf('GROUP_NOT_EXISTS:') > -1 && this.$t('UsersManagement.error.importCSV.groupNotExists', {0: this.errorMessage.split(':')[1]});
    },
    membershipNotExistsErrorMessage() {
      return this.errorMessage && this.errorMessage.indexOf('MEMBERSHIP_TYPE_NOT_EXISTS:') > -1 && this.$t('UsersManagement.error.importCSV.membershipNotExists', {0: this.errorMessage.split(':')[1]});
    },
    membershipImportErrorMessage() {
      return this.errorMessage && this.errorMessage.indexOf('IMPORT_MEMBERSHIP_ERROR:') > -1 && this.$t('UsersManagement.error.importCSV.createMembershipError');
    },
    createUserProfileErrorMessage() {
      return this.errorMessage && this.errorMessage.indexOf('CREATE_USER_PROFILE_ERROR:') > -1 && this.$t('UsersManagement.error.importCSV.createSocialProfileError');
    },
    unauthorizedFieldErrorMessage() {
      return this.errorMessage && this.errorMessage.indexOf('PROFILE_PROPERTY_DOES_NOT_EXIST:') > -1 && `${this.$t('UsersManagement.error.importCSV.profilePropertyDoesNotExist')} : ${this.errorMessage.split(':')[1]}`;
    },
    parentPropertyShouldNotHaveValuesErrorMessage() {
      return this.errorMessage && this.errorMessage.indexOf('PARENT_PROPERTY_SHOULD_NOT_HAVE_VALUES:') > -1 && `${this.$t('UsersManagement.error.importCSV.parentPropertyShouldNotHaveValues')} : ${this.errorMessage.split(':')[1]}`;
    },
    propertyHasMoreThanOneParentErrorMessage() {
      return this.errorMessage && this.errorMessage.indexOf('PROPERTY_HAS_MORE_THAN_ONE_PARENT:') > -1 && `${this.$t('UsersManagement.error.importCSV.propertyHasMoreThanOneParentErrorMessage')} : ${this.errorMessage.split(':')[1]}`;
    },
    propertyHasAMissingParentPropertyErrorMessage() {
      return this.errorMessage && this.errorMessage.indexOf('PROPERTY_HAS_MISSING_PARENT_PROPERTY:') > -1 && `${this.$t('UsersManagement.error.importCSV.propertyHasAMissingParentPropertyErrorMessage')} : ${this.errorMessage.split(':')[1]}`;
    },
    customMultivaluedFieldErrorMessage() {
      return this.errorMessage && this.errorMessage.indexOf('CUSTOM_FIELD_MULTIVALUED:') > -1 && `${this.$t('UsersManagement.error.importCSV.customMultivaluedFieldErrorMessage')} : ${this.errorMessage.split(':')[1]}`;
    },
    customParentErrorMessage() {
      return this.errorMessage && this.errorMessage.indexOf('CUSTOM_PARENT_FIELD:') > -1 && `${this.$t('UsersManagement.error.importCSV.customParentErrorMessage')} : ${this.errorMessage.split(':')[1]}`;
    }
  },
};
</script>
