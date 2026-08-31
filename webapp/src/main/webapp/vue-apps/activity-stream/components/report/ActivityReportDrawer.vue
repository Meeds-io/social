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
  <exo-drawer
    v-if="singleton"
    id="activityReportDrawer"
    ref="drawer"
    v-model="drawer"
    :loading="saving"
    right>
    <template #title>
      {{ isComment && $t('activityStream.report.drawer.titleComment') || $t('activityStream.report.drawer.title') }}
    </template>
    <template v-if="drawer" #content>
      <v-radio-group
        v-model="selectedReason"
        class="px-4 pt-2"
        column>
        <v-radio
          v-for="reason in reasons"
          :key="reason"
          :label="$t(`activityStream.report.reason.${reason}`)"
          :value="reason" />
      </v-radio-group>
    </template>
    <template #footer>
      <div class="d-flex">
        <v-spacer />
        <v-btn
          :disabled="saving"
          class="btn me-2"
          @click="close">
          {{ $t('activityStream.report.cancel') }}
        </v-btn>
        <v-btn
          :disabled="!selectedReason || saving"
          :loading="saving"
          class="btn btn-primary"
          @click="report">
          {{ $t('activityStream.report.confirm') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    singleton: true,
    drawer: false,
    saving: false,
    activityId: null,
    parentActivityId: null,
    isComment: false,
    selectedReason: null,
    reasons: [
      'spam',
      'fakeAccount',
      'harassmentOrBullying',
      'hateSpeechOrDiscrimination',
      'violenceOrThreats',
      'falseInformation',
      'intellectualPropertyViolation',
    ],
  }),
  created() {
    document.addEventListener('activity-report-drawer-open', this.openByEvent);
  },
  mounted() {
    if (document.querySelectorAll('#activityReportDrawer').length > 1) {
      this.singleton = false;
      this.clearListeners();
    }
  },
  beforeDestroy() {
    this.clearListeners();
  },
  methods: {
    clearListeners() {
      document.removeEventListener('activity-report-drawer-open', this.openByEvent);
    },
    openByEvent(event) {
      this.open(event?.detail || {});
    },
    open({activityId, parentActivityId, isComment}) {
      this.activityId = activityId;
      this.parentActivityId = parentActivityId;
      this.isComment = !!isComment;
      this.selectedReason = null;
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
    },
    report() {
      this.saving = true;
      return this.$activityService.reportActivity(this.activityId, this.selectedReason)
        .then(() => {
          this.$root.$emit('alert-message', this.$t('activityStream.report.sent'), 'success');
          document.dispatchEvent(new CustomEvent('activity-reported', {detail: {
            activityId: this.activityId,
            parentActivityId: this.parentActivityId,
            isComment: this.isComment,
          }}));
          document.dispatchEvent(new CustomEvent('activity-updated', {detail: this.parentActivityId || this.activityId}));
          this.close();
        })
        .catch(error => {
          if (String(error?.message) === '409') {
            this.$root.$emit('alert-message', this.$t('activityStream.report.alreadyReported'), 'warning');
            // the server already holds an active report from this user: mark
            // the menus locally too instead of waiting for the websocket
            // round-trip
            document.dispatchEvent(new CustomEvent('activity-reported', {detail: {
              activityId: this.activityId,
              parentActivityId: this.parentActivityId,
              isComment: this.isComment,
            }}));
            this.close();
          } else {
            this.$root.$emit('alert-message', this.$t('activityStream.report.error'), 'error');
          }
        })
        .finally(() => this.saving = false);
    },
  },
};
</script>
