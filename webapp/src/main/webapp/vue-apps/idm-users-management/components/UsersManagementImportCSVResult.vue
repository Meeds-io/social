<template>
  <div class="d-flex justify-center">
    <v-progress-circular
      v-if="!finished"
      :value="progressPercentage"
      color="primary"
      class="me-4 my-auto" />
    <div class="d-flex flex-column me-2">
      <users-management-import-csv-error-message v-if="error" :error="error" />
      <template v-else-if="uploading">
        {{ $t('UsersManagement.uploadingCSVFile') }}
      </template>
      <template v-else-if="progress">
        <ul class="ps-0">
          <li>
            <template v-if="finished">
              {{ $t('UsersManagement.finishedImportingCSVFile', {0: progress.count}) }}
            </template>
            <template v-else>
              {{ $t('UsersManagement.processingCSVFile', {0: progress.processedCount, 1: progress.count}) }}
            </template>
          </li>
          <li v-if="errorsCount">
            - {{ $t('UsersManagement.errorImportingCSVFile') }}: <a
              role="button"
              tabindex="0"
              @click="$refs.errorDrawer.open()"
              @keydown.enter.prevent="$refs.errorDrawer.open()">{{ errorsCount }} / {{ progress.processedCount }}</a>
          </li>
          <li v-if="warnsCount">
            - {{ $t('UsersManagement.warningImportingCSVFile') }}: <a
              role="button"
              tabindex="0"
              @click="$refs.warnDrawer.open()"
              @keydown.enter.prevent="$refs.warnDrawer.open()">{{ warnsCount }} / {{ progress.processedCount }}</a>
          </li>
        </ul>
      </template>
    </div>
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
  </div>
</template>
<script>
export default {
  props: {
    options: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    progress: null,
    error: null,
    errorHeaders: [{value: 'userName'},{value: 'message'}],
    itemsPerPageOptions: [20, 50, 100],
  }),
  computed: {
    uploadId() {
      return this.options.uploadId;
    },
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
    progressPercentage() {
      return this.progress ? parseInt(this.progress.processedCount * 100 / this.progress.count) : 0;
    },
    finished() {
      return this.error || this.imported;
    },
  },
  watch: {
    finished(newValue, oldValue) {
      if (this.finished && this.uploadId) {
        this.$userService.cleanImportUsers(this.uploadId)
          .then(result => {
            if (result) {
              this.progress = result;
            }
          });
      }
      // If finished Broacast event
      if (newValue && newValue !== oldValue) {
        document.dispatchEvent(new CustomEvent('alert-message-switch-type', {detail: 'success'}));
        document.dispatchEvent(new CustomEvent('alert-message-switch-dismissible', {detail: true}));
        document.dispatchEvent(new CustomEvent('importCSVFinished', {detail: {
          uploadId: this.uploadId,
          progress: this.progress,
        }}));
      }
    },
  },
  created() {
    this.init();
  },
  methods: {
    init() {
      if (this.uploadId && !this.finished) {
        return this.$userService.checkImportUsersProgress(this.uploadId)
          .then(result => {
            if (result) {
              window.setTimeout(() => this.progress = result, 200);
            }
          })
          .then(() => this.$nextTick())
          .catch(error => this.error = error)
          .finally(() => {
            if (!this.finished) {
              window.setTimeout(this.init, 500);
            }
          });
      }
    },
  },
};
</script>
