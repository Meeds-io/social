<!--
 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
 
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
  <v-snackbar
    v-if="snackbar"
    v-model="snackbar"
    :left="!$vuetify.rtl"
    :right="$vuetify.rtl"
    :timeout="timeout"
    color="transparent"
    elevation="0"
    app>
    <v-alert
      :type="alertType"
      :max-width="maxWidth"
      class="white"
      border="left"
      elevation="2"
      light
      outlined
      dismissible
      colored-border
      @input="closeAlertIfDismissed">
      <div class="d-flex flex-nowrap align-center full-width">
        <span
          v-if="useHtml"
          class="text--lighten-1 flex-grow-1 pe-4"
          v-html="alertMessage"
          @click="handleAlertClicked">
        </span>
        <span v-else class="text--lighten-1 flex-grow-1 pe-4">
          {{ alertMessage }}
        </span>
        <v-btn
          v-if="alertLink || alertLinkCallback"
          :href="alertLink"
          :title="alertLinkTooltip"
          name="closeSnackbarButton"
          target="_blank"
          rel="nofollow noreferrer noopener"
          class="secondary--text"
          icon
          link
          @click="linkCallback">
          <v-icon>{{ alertLinkIcon }}</v-icon>
        </v-btn>
      </div>
    </v-alert>
  </v-snackbar>
</template>
<script>
export default {
  data: () => ({
    snackbar: false,
    timeout: 10000,
    alertMessage: null,
    useHtml: false,
    alertType: null,
    alertLink: null,
    alertLinkCallback: null,
    alertLinkIcon: null,
    alertLinkTooltip: null,
    timeoutInstance: null,
    maxIconsSize: '20px',
  }),
  computed: {
    isMobile() {
      return this.$vuetify.breakpoint.name === 'sm' || this.$vuetify.breakpoint.name === 'xs';
    },
    dark() {
      return this.$vuetify.theme.dark;
    },
    maxWidth() {
      return this.isMobile && '100vw' || '50vw';
    },
  },
  created() {
    this.$root.$on('alert-message', (message, type, linkCallback, linkIcon, linkTooltip) => {
      this.openAlert({
        alertType: type,
        alertMessage: message,
        alertLinkCallback: linkCallback,
        alertLinkIcon: linkIcon,
        alertLinkTooltip: linkTooltip,
      });
    });
    this.$root.$on('alert-message-html', (message, type, linkCallback, linkIcon, linkTooltip) => {
      this.openAlert({
        useHtml: true,
        alertType: type,
        alertMessage: message,
        alertLinkCallback: linkCallback,
        alertLinkIcon: linkIcon,
        alertLinkTooltip: linkTooltip,
      });
    });
    this.$root.$on('close-alert-message', this.closeAlert);
  },
  methods: {
    openAlert(params) {
      this.closeAlert();
      this.$nextTick().then(() => {
        this.useHtml = params.useHtml || false;
        this.alertLink = params.alertLink || null;
        this.alertMessage = params.alertMessage || null;
        this.alertLinkIcon = params.alertLinkIcon || null;
        this.alertType = params.alertType || 'info';
        this.alertLinkTooltip = params.alertLinkTooltip || null;
        this.alertLinkCallback = params.alertLinkCallback || null;
        this.timeoutInstance = window.setTimeout(() => this.snackbar = true, 500);
      });
    },
    closeAlertIfDismissed(closed) {
      if (!closed) {
        this.closeAlert();
      }
    },
    closeAlert() {
      if (this.timeoutInstance) {
        window.clearTimeout(this.timeoutInstance);
      }
      this.snackbar = false;
    },
    linkCallback() {
      if (this.alertLinkCallback) {
        this.alertLinkCallback();
      }
    },
    cancelEvent(event) {
      if (event) {
        event.stopPropagation();
        event.preventDefault();
      }
    },
    handleAlertClicked(event) {
      if (event) {
        event.stopPropagation();
        event.preventDefault();
      }
      if (!event || event?.target?.tagName?.toLowerCase() === 'a') {
        this.linkCallback();
      }
    },
  },
};
</script>