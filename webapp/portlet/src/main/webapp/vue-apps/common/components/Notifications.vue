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
    :content-class="isMobile && 'pa-0 ma-0'"
    :class="isMobile && 'full-height'"
    class="z-index-snackbar"
    color="transparent ma-0"
    elevation="0"
    app
    @input="dispatchDismissed">
    <confeti-animation
      v-if="confeti"
      class="overflow-hidden" />
    <v-alert
      :type="alertType"
      :min-width="minWidth"
      :max-width="maxWidth"
      :min-height="minHeight"
      :dense="isMobile"
      :dismissible="!isMobile"
      :icon="false"
      :border="!isMobile && 'left' || false"
      :class="isMobile && 'no-border-radius b-0 mb-0' || 'mb-5'"
      :style="absolute && {
        left: `${left}px`,
      }"
      v-touch="{
        start: moveStart,
        end: moveEnd,
        move: moveSwipe,
      }"
      class="d-flex flex-column justify-center white mt-0 mx-0 py-2 px-4 border-box-sizing"
      elevation="2"
      light
      outlined
      colored-border
      @input="closeAlertIfDismissed">
      <div
        :class="isMobile && 'mt-2'"
        class="d-flex flex-nowrap text-start align-center justify-center full-width">
        <v-progress-linear
          v-if="isMobile"
          :color="`${alertType}-color-background`"
          :value="progression"
          height="6"
          class="position-absolute t-0 l-0 r-0 mt-n1" />
        <span
          v-if="useHtml"
          class="text--lighten-1 flex-grow-1 me-4"
          v-sanitized-html="alertMessage"
          @click="handleAlertClicked">
        </span>
        <span v-else class="text--lighten-1 flex-grow-1 me-4">
          {{ alertMessage }}
        </span>
        <v-btn
            v-for="(item, index) in alertLinks"
            :key="index"
            :href="item.alertLink"
            :title="item.alertLinkTooltip"
            :class="item.alertLinkText && 'elevation-0 transparent primary--text' || 'secondary--text'"
            :target="item.alertLinkTarget || '_blank'"
            :icon="!item.alertLinkText && item.alertLinkIcon"
            name="closeSnackbarButton"
            rel="nofollow noreferrer noopener"
            link
            @click="linkCallback(item.alertLinkCallback)">
          <div v-if="item.alertLinkText" class="text-none">{{ item.alertLinkText }}</div>
          <v-icon v-else-if="item.alertLinkIcon">{{ item.alertLinkIcon }}</v-icon>
        </v-btn>
      </div>
      <template v-if="!isMobile" #close="{toggle}">
        <v-btn
          icon
          @click="dispatchDismissed(!toggle)">
          <v-icon size="16" class="icon-default-color">fa-times</v-icon>
        </v-btn>
      </template>
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
    confeti: false,
    alertType: null,
    timeoutInstance: null,
    maxIconsSize: '20px',
    interval: 0,
    progression: 0,
    absolute: false,
    left: 0,
    startEvent: null,
    moving: false,
    alertLinks: [],
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
    minWidth() {
      return this.isMobile && '100vw' || 400;
    },
    minHeight() {
      return this.isMobile && 69 || 57;
    },
  },
  watch: {
    snackbar() {
      if (this.isMobile) {
        if (this.snackbar) {
          this.interval = window.setInterval(() => {
            this.progression -= this.timeout / 1000;
          }, 1000);
        } else if (this.interval) {
          window.clearInterval(this.interval);
          this.interval = 0;
        }
      }
    },
  },
  created() {
    document.addEventListener('alert-message', (event) => {
      const alertObj = event?.detail;
      if (alertObj) {
        this.openAlert(alertObj);
      }
    });
    document.addEventListener('alert-message-html', (event) => {
      const alertObj = event?.detail && Object.assign({
        useHtml: true,
      }, event?.detail);
      if (alertObj) {
        this.openAlert(alertObj);
      }
    });
    document.addEventListener('alert-message-html-confeti', (event) => {
      const alertObj = event?.detail && Object.assign({
        useHtml: true,
        confeti: true,
      }, event?.detail);
      if (alertObj) {
        this.openAlert(alertObj);
      }
    });
    document.addEventListener('close-alert-message', () => {
      this.closeAlert();
    });

    this.$root.$on('alert-message', (message, type, linkCallback, linkIcon, linkTooltip, alertLinks) => {
      this.openAlert({
        alertType: type,
        alertMessage: message,
        alertLinkCallback: linkCallback,
        alertLinkIcon: linkIcon,
        alertLinkTooltip: linkTooltip,
        alertLinks: alertLinks
      });
    });
    this.$root.$on('alert-message-html', (message, type, linkCallback, linkIcon, linkTooltip, alertLinks) => {
      this.openAlert({
        useHtml: true,
        alertType: type,
        alertMessage: message,
        alertLinkCallback: linkCallback,
        alertLinkIcon: linkIcon,
        alertLinkTooltip: linkTooltip,
        alertLinks: alertLinks
      });
    });
    this.$root.$on('alert-message-html-confeti', (message, type, linkCallback, linkIcon, linkTooltip, alertLinks) => {
      this.openAlert({
        confeti: true,
        useHtml: true,
        alertType: type,
        alertMessage: message,
        alertLinkCallback: linkCallback,
        alertLinkIcon: linkIcon,
        alertLinkTooltip: linkTooltip,
        alertLinks: alertLinks
      });
    });
    this.$root.$on('close-alert-message', this.closeAlert);

    // Kept for backward compatibility
    document.addEventListener('notification-alert', event => {
      this.openAlert({
        alertType: event?.detail?.type,
        alertMessage: event?.detail?.message,
      });
    });
  },
  methods: {
    dispatchDismissed(opened) {
      if (!opened) {
        document.dispatchEvent(new CustomEvent('alert-message-dismissed'));
      }
    },
    openAlert(params) {
      this.reset();
      this.closeAlert();
      this.alertLinks = [];
      this.progression = 100;
      this.$nextTick().then(() => {
        this.useHtml = params.useHtml || false;
        this.confeti = params.confeti || false;
        this.alertMessage = params.alertMessage || null;
        this.alertType = params.alertType || 'info';
        this.timeoutInstance = window.setTimeout(() => this.snackbar = true, 500);
        // push alert link to alertLinks list
        if (params.alertLink || params.alertLinkCallback) {
          this.alertLinks.push({
            alertLink: params.alertLink || null,
            alertLinkText: params.alertLinkText || null,
            alertLinkTarget: params.alertLinkTarget || null,
            alertLinkIcon: params.alertLinkIcon || null,
            alertLinkTooltip: params.alertLinkTooltip || null,
            alertLinkCallback: params.alertLinkCallback || null,
          });
        }
        if (params.alertLinks) {
          this.alertLinks.push(...params.alertLinks);
        }
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
    linkCallback(callback) {
      if (callback) {
        callback();
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
    reset() {
      this.absolute = false;
      this.left = -8;
      this.startEvent = null;
      this.moving = false;
    },
    moveStart() {
      if (this.absolute) {
        return;
      }
      this.reset();
      window.setTimeout(() => this.absolute = true, 50);
    },
    moveEnd() {
      const confirm = Math.abs(this.left) > (window.innerWidth / 4);
      if (confirm) {
        this.snackbar = false;
      } else {
        this.reset();
      }
    },
    moveSwipe(event) {
      if (!this.absolute) {
        return;
      }
      if (!this.startEvent) {
        this.startEvent = event;
      } else if (!this.moving) {
        this.moving = true;
        this.$nextTick().then(() => {
          this.left = parseInt(event.touchmoveX - this.startEvent.touchmoveX) - 8;
          this.moving = false;
        });
      }
    },
  },
};
</script>