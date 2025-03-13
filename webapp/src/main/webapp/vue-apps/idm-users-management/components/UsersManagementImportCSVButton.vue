<template>
  <v-btn
    class="btn dropdown-button"
    :disabled="disabled"
    @click="openFileSelection">
    <i class="uiIconImport me-md-3"></i>
    {{ $t('UsersManagement.importCSV') }}
    <v-file-input
      v-if="!disabled"
      ref="usersCSVInput"
      accept=".csv"
      class="importCSVUsersButton hidden me-4"
      clearable
      prepend-icon=""
      @change="importUsers" />
  </v-btn>
</template>

<script>
  export default {
    data: () => ({
      disabled: false,
    }),
    created () {
      this.$root.$on('importCSVStarted', () => this.disabled = true);
      this.$root.$on('importCSVError', () => this.disabled = false);
      this.$root.$on('importCSVFinished', () => this.disabled = false);
    },
    methods: {
      openFileSelection () {
        this.$refs.usersCSVInput.$el.getElementsByTagName('input')[0].click();
      },
      importUsers (file) {
        if (file && file.size) {
          const uploadId = eXo.$uploadService.generateRandomId();
          this.$root.$emit('importCSVStarted', uploadId);
          return eXo.$uploadService.upload(file, uploadId)
            .then(() => eXo.$userService.importUsers(uploadId))
            .then(() => this.$root.$emit('importCSVProgress', uploadId))
            .catch(error => this.$root.$emit('importCSVError', error));
        }
      },
    },
  };
</script>