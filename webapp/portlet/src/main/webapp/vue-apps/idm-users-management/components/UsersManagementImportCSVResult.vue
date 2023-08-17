<template>
  <v-flex>
    <v-alert v-if="displayAlert" :type="alertType">
      <users-management-import-csv-error-message v-if="error" :error="error" />
      <template v-else-if="uploading">
        {{ $t('UsersManagement.uploadingCSVFile') }}
      </template>
      <template v-else-if="progress">
        <ul>
          <li>
            <template v-if="finished">
              {{ $t('UsersManagement.finishedImportingCSVFile', {0: progress.count}) }}
            </template>
            <template v-else>
              {{ $t('UsersManagement.processingCSVFile', {0: progress.processedCount, 1: progress.count}) }}
            </template>
          </li>
          <li v-if="errorsCount">
            - {{ $t('UsersManagement.errorImportingCSVFile') }}: <a @click="$refs.errorDrawer.open()">{{ errorsCount }} / {{ progress.processedCount }}</a>
          </li>
          <li v-if="warnsCount">
            - {{ $t('UsersManagement.warningImportingCSVFile') }}: <a @click="$refs.warnDrawer.open()">{{ warnsCount }} / {{ progress.processedCount }}</a>
          </li>
        </ul>
      </template>
    </v-alert>
    <exo-drawer
      ref="errorDrawer"
      right>
      <template slot="title">{{ $t('UsersManagement.importCSVErrors') }}</template>
      <template slot="content">
        <v-data-table
          :headers="errorHeaders"
          :items="errorMessages"
          :items-per-page="20"
          :footer-props="{ itemsPerPageOptions }"
          hide-default-header>
          <template slot="item.message" slot-scope="{ item }">
            <users-management-import-csv-error-message
              :error="item.message"
              :user-name="item.userName" />
          </template>
        </v-data-table>
      </template>
    </exo-drawer>
    <exo-drawer
      ref="warnDrawer"
      right>
      <template slot="title">{{ $t('UsersManagement.importCSVWarnings') }}</template>
      <template slot="content">
        <v-data-table
          :headers="errorHeaders"
          :items="warnMessages"
          :items-per-page="20"
          :footer-props="{ itemsPerPageOptions }"
          hide-default-header>
          <template slot="item.userName" slot-scope="{ item }">
            {{ item.userName === 'ALL' ? $t('UsersManagement.error.importCSV.all') : item.userName }}
          </template>
          <template slot="item.message" slot-scope="{ item }">
            <users-management-import-csv-error-message
              :error="item.message"
              :user-name="item.userName" />
          </template>
        </v-data-table>
      </template>
    </exo-drawer>
  </v-flex>
</template>

<script>
export default {
  data: () => ({
    uploadId: null,
    progress: null,
    error: null,
    errorHeaders: [{value: 'userName'},{value: 'message'}],
    itemsPerPageOptions: [20, 50, 100],
  }),
  computed: {
    warnsCount() {
      return this.warnMessages.length;
    },
    warnMessages() {
      if (!this.progress || !this.progress.warnMessages) {
        return [];
      }
      return Object.keys(this.progress.warnMessages)
        .flatMap(userName => 
          this.progress.warnMessages[userName]
            .map(message => ({userName: userName, message: message}))
        );
    },
    errorsCount() {
      return this.errorMessages.length;
    },
    errorMessages() {
      if (!this.progress || !this.progress.errorMessages) {
        return [];
      }
      return Object.keys(this.progress.errorMessages)
        .map(userName => ({
          userName: userName,
          message: this.progress.errorMessages[userName]
        }));
    },
    uploading() {
      return !this.finished && !this.progress && this.uploadId;
    },
    imported() {
      return this.progress && this.progress.processedCount >= this.progress.count;
    },
    finished() {
      return this.error || this.imported;
    },
    alertType() {
      return this.error ? 'error' : this.finished ? 'success' : 'info';
    },
    displayAlert() {
      return this.uploading || this.progress || this.finished;
    },
  },
  watch: {
    uploadId() {
      this.progress = null;
      this.error = null;
    },
    finished(newValue, oldValue) {
      if (this.finished && this.uploadId) {
        this.$userService.cleanImportUsers(this.uploadId)
          .then(result => {
            this.progress = result;
          });
      }
      // If finished Broacast event
      if (newValue && newValue !== oldValue) {
        this.$root.$emit('importCSVFinished', this.uploadId, this.progress);
      }
    },
  },
  created() {
    this.$root.$on('importCSVStarted', uploadId => this.uploadId = uploadId);
    this.$root.$on('importCSVProgress', this.watchProgress);
    this.$root.$on('importCSVError', (error) => this.error = error);
  },
  methods: {
    watchProgress() {
      if (this.uploadId && !this.finished) {
        return this.$userService.checkImportUsersProgress(this.uploadId)
          .then(result => {
            window.setTimeout(() => this.progress = result,200);
          })
          .then(() => this.$nextTick())
          .catch(error => {
            if (String(error).indexOf('SyntaxError') < 0) {
              this.error = error;
            }
          })
          .finally(() => {
            if (!this.finished) {
              window.setTimeout(this.watchProgress, 500);
            }
          });
      }
    },
  },
};
</script>
