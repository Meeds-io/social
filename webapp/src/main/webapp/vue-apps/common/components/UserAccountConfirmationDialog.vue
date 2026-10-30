<!--
This file is part of the Meeds project (https://meeds.io/).

Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program; if not, write to the Free Software Foundation,
Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
-->
<template>
  <exo-modal
    ref="modal"
    :title="$t('userAccountConfirmation.title')"
    width="460px"
    persistent
    hide-actions
    @dialog-closed="closed">
    <v-form
      ref="form"
      class="pa-4"
      @submit.prevent="submit">
      <p class="text-body-2 mb-4">{{ $t('userAccountConfirmation.description') }}</p>
      <p
        v-if="sendMessage"
        class="text-caption text-sub-title mb-2"
        role="status">
        {{ sendMessage }}
      </p>
      <v-sheet max-width="400">
        <v-text-field
          ref="codeInput"
          v-model="otpCode"
          :placeholder="$t('userAccountConfirmation.codePlaceholder')"
          :aria-label="$t('userAccountConfirmation.codePlaceholder')"
          :error-messages="errorMessage"
          :disabled="confirming"
          prepend-inner-icon="fa-lock"
          autocomplete="one-time-code"
          inputmode="numeric"
          id="userAccountConfirmationCodeInput"
          class="pt-0"
          outlined
          dense
          autofocus
          @input="errorMessage = null" />
      </v-sheet>
      <div class="d-flex flex-wrap justify-center mt-2">
        <v-btn
          :disabled="!canSendAgain"
          :loading="sending"
          id="userAccountConfirmationSendAgainButton"
          class="btn me-2"
          @click="sendCode">
          {{ $t('userAccountConfirmation.sendAgain') }}
        </v-btn>
        <v-btn
          :disabled="!otpCode"
          :loading="confirming"
          id="userAccountConfirmationVerifyButton"
          class="btn btn-primary"
          type="submit">
          {{ $t('userAccountConfirmation.confirm') }}
        </v-btn>
      </div>
    </v-form>
  </exo-modal>
</template>
<script>
import * as confirmationService from '../js/UserAccountConfirmationService.js';

export default {
  data: () => ({
    otpCode: null,
    errorMessage: null,
    sending: false,
    confirming: false,
    confirmed: false,
    sendFailed: false,
    settings: null,
    lastSentAt: null,
    sentByAnother: false,
    sentByAnotherAt: null,
    now: Date.now(),
    timer: null,
    resolveConfirmation: null,
    rejectConfirmation: null,
    confirmation: null,
  }),
  computed: {
    sendIntervalMillis() {
      return (this.settings?.sendIntervalSeconds || 0) * 1000;
    },
    nextSendAt() {
      if (this.lastSentAt) {
        return this.lastSentAt + this.sendIntervalMillis;
      } else if (this.sentByAnother) {
        return this.sentByAnotherAt + this.sendIntervalMillis;
      }
      return 0;
    },
    canSendAgain() {
      return !this.sending && this.now >= this.nextSendAt;
    },
    sendMessage() {
      if (this.sendFailed) {
        return this.$t('userAccountConfirmation.sendError');
      } else if (this.sentByAnother) {
        return this.$t('userAccountConfirmation.codeSentRecently', {0: this.settings?.sendIntervalSeconds || 0});
      } else if (this.lastSentAt) {
        const sentSecondsAgo = Math.max(0, Math.floor((this.now - this.lastSentAt) / 1000));
        const remainingSeconds = Math.max(0, Math.ceil((this.nextSendAt - this.now) / 1000));
        if (remainingSeconds > 0 && sentSecondsAgo > 0) {
          return this.$t('userAccountConfirmation.codeSentAgo', {0: sentSecondsAgo, 1: remainingSeconds});
        }
        return this.$t('userAccountConfirmation.codeSent', {0: this.settings?.ttlMinutes || 0});
      }
      return null;
    },
  },
  beforeDestroy() {
    this.stopTimer();
  },
  methods: {
    /**
     * Opens the dialog and settles the returned promise once, when it closes:
     * resolved when the session was confirmed, rejected with
     * CONFIRMATION_CANCELLED otherwise (Escape or the close icon)
     *
     * @returns {Promise} the confirmation of this opening
     */
    confirm() {
      if (!this.confirmation) {
        this.confirmation = new Promise((resolve, reject) => {
          this.resolveConfirmation = resolve;
          this.rejectConfirmation = reject;
          this.open();
        });
      }
      return this.confirmation;
    },
    async open() {
      this.otpCode = null;
      this.errorMessage = null;
      this.confirming = false;
      this.confirmed = false;
      this.sendFailed = false;
      this.now = Date.now();
      this.startTimer();
      this.$refs.modal.open();
      if (!this.settings) {
        try {
          this.settings = await confirmationService.getSettings();
        } catch (e) {
          this.settings = {sendIntervalSeconds: 60, ttlMinutes: 5};
        }
      }
      if (this.canSendAgain) {
        await this.sendCode();
      }
    },
    async sendCode() {
      this.sending = true;
      this.sendFailed = false;
      try {
        await confirmationService.sendCode();
        this.lastSentAt = Date.now();
        this.sentByAnother = false;
      } catch (e) {
        if (e?.message === '429') {
          // Sent from another tab or before a reload: its time is unknown
          this.lastSentAt = null;
          this.sentByAnother = true;
          this.sentByAnotherAt = Date.now();
        } else {
          this.sendFailed = true;
        }
      } finally {
        this.sending = false;
        this.now = Date.now();
      }
    },
    async submit() {
      if (!this.otpCode || this.confirming) {
        return;
      }
      this.confirming = true;
      this.errorMessage = null;
      try {
        await confirmationService.confirmSession(this.otpCode.trim());
        this.confirmed = true;
        this.$refs.modal.close();
      } catch (e) {
        this.errorMessage = this.$t('userAccountConfirmation.invalidCode');
        // A wrong code lifts the server's send lock: a new code can be sent
        // at once, without waiting out the interval
        this.lastSentAt = null;
        this.sentByAnother = false;
        this.$nextTick(() => this.$refs.codeInput?.focus?.());
      } finally {
        this.confirming = false;
      }
    },
    closed() {
      this.stopTimer();
      const resolve = this.resolveConfirmation;
      const reject = this.rejectConfirmation;
      this.resolveConfirmation = null;
      this.rejectConfirmation = null;
      this.confirmation = null;
      if (this.confirmed) {
        resolve?.();
      } else {
        reject?.(new Error(confirmationService.CONFIRMATION_CANCELLED));
      }
    },
    startTimer() {
      this.stopTimer();
      this.timer = window.setInterval(() => this.now = Date.now(), 1000);
    },
    stopTimer() {
      if (this.timer) {
        window.clearInterval(this.timer);
        this.timer = null;
      }
    },
  },
};
</script>
