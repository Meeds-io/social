<template>
  <v-app class="border-box-sizing transparent" flat>
    <v-dialog
      v-model="dialog"
      content-class="uiPopup"
      width="500"
      @click:outside="dialog = false">
      <v-card>
        <v-card-title class="text-header-title pb-6">
          {{ $t('changes.reminder.WhatNew') }}
        </v-card-title>
        <v-card-text outlined><img :src="reminder.img"></v-card-text>
        <v-card-text class="font-weight-bold">{{ reminder.title }}</v-card-text>
        <v-card-text>
          {{ reminder.description }}
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn
            text
            class="primary--text font-weight-bold"
            @click="doNotRemindMe">
            {{ $t('changes.reminder.doNotRemind') }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-app>
</template>

<script>

export default {
  props: {
    reminder: {
      type: Object,
      default: () => ({}),
    },
  },
  data () {
    return {
      dialog: false,
    };
  },
  created() {
    document.addEventListener('exo-changes-reminder-open' , () => this.getReminderStatus());
  },
  methods: {
    getReminderStatus() {
      return this.$changesReminderService.getReminderStatus(this.reminder.name).then(status => {
        this.dialog = !status;
      });
    },
    doNotRemindMe() {
      this.$changesReminderService.markReminderAsRead(this.reminder.name).then(this.dialog = false);
    }
  }

};
</script>