<template>
  <div class="d-flex justify-center">
    <v-progress-circular
      v-if="!finished"
      :value="progressPercentage"
      color="primary"
      class="me-4 my-auto" />
    <div v-if="finished" class="d-flex me-2">
      <div>
        {{ $t('UsersManagement.finishedExportingCSVFile') }}
      </div>
      <v-btn
        :href="downloadLink"
        download="users.csv"
        color="primary"
        elevation="0"
        class="ms-2 no-border"
        outlined
        text>
        <v-icon size="14" class="me-2">fa-download</v-icon>
        {{ $t('UsersManagement.button.download') }}
      </v-btn>
    </div>
    <template v-else>
      {{ $t('UsersManagement.exportingCSVFile') }}
    </template>
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
    exportResult: null,
    error: null,
    errorHeaders: [{value: 'userName'},{value: 'message'}],
    itemsPerPageOptions: [20, 50, 100],
  }),
  computed: {
    exportLink() {
      return this.options.exportLink;
    },
    exportId() {
      return this.options.exportId;
    },
    progressLink() {
      return `${this.exportLink}&exportId=${this.exportId}`;
    },
    downloadLink() {
      return `${this.exportLink}&exportId=${this.exportId}&download=true`;
    },
    totalUsers() {
      return this.options.totalUsers;
    },
    progressPercentage() {
      return this.exportResult?.processedCount ? parseInt(this.exportResult.processedCount / this.totalUsers) * 100 : 0;
    },
    finished() {
      return this.exportResult?.finished;
    },
  },
  watch: {
    finished(newValue, oldValue) {
      // If finished Broacast event
      if (newValue && newValue !== oldValue) {
        document.dispatchEvent(new CustomEvent('alert-message-switch-type', {detail: 'success'}));
        document.dispatchEvent(new CustomEvent('alert-message-switch-dismissible', {detail: true}));
        document.dispatchEvent(new CustomEvent('exportCSVFinished', {detail: {
          exportId: this.exportId,
          progress: this.progress,
        }}));
      }
    },
  },
  created() {
    this.init();
  },
  methods: {
    async init() {
      if (!this.finished) {
        try {
          this.exportResult = await fetch(this.progressLink, {
            credentails: 'include',
          }).then(resp => resp?.ok && resp.json());
        } finally {
          if (!this.finished) {
            window.setTimeout(this.watchProgress, 500);
          }
        }
      }
    },
  },
};
</script>
