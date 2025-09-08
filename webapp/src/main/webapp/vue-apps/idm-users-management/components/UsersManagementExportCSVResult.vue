<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

-->
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
      return this.exportResult?.processedCount ? parseInt(this.exportResult.processedCount * 100 / this.totalUsers) : 0;
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
            window.setTimeout(this.init, 500);
          }
        }
      }
    },
  },
};
</script>
