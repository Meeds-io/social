<template>
  <v-btn
    :loading="uploading"
    class="btn"
    @click="openFileSelection">
    <i class="uiIconImport me-md-3"></i>
    {{ $t('UsersManagement.importCSV') }}
    <v-file-input
      v-if="!uploading"
      ref="usersCSVInput"
      class="importCSVUsersButton hidden me-4"
      prepend-icon=""
      accept=".csv"
      clearable
      @change="importUsers" />
  </v-btn>
</template>
<script>
export default {
  data: () => ({
    uploading: false,
  }),
  watch: {
    uploading() {
      if (this.uploading) {
        document.dispatchEvent(new CustomEvent('displayTopBarLoading'));
      } else {
        window.setTimeout(() => document.dispatchEvent(new CustomEvent('hideTopBarLoading')), 200);
      }
    },
  },
  methods: {
    openFileSelection() {
      this.$refs.usersCSVInput.$el.getElementsByTagName('input')[0].click();
    },
    importUsers(file) {
      if (!file?.size) {
        return;
      }
      const uploadId = this.$uploadService.generateRandomId();
      this.uploading = true;
      return this.$uploadService.upload(file, uploadId)
        .then(() => this.$userService.importUsers(uploadId))
        .then(() => document.dispatchEvent(new CustomEvent('alert-message', {detail: {
          alertComponent: 'users-management-import-csv-result',
          alertComponentParams: {
            options: {
              uploadId,
            },
          },
          alertDismissible: false,
          alertTimeout: 864000000,
        }})))
        .catch(error => this.$root.$emit('alert-message', error, 'error'))
        .finally(() => this.uploading = false);
    },
  },
};
</script>