<template>
  <v-app class="border-box-sizing transparent" flat>
    <v-dialog
      v-model="dialog"
      content-class="rounded-lg"
      overlay-opacity="0.7"
      width="500"
      @click:outside="dialog = false">
      <v-card class="pb-4">
        <v-card-title class="text-h5 font-weight-bold dark-grey-color pb-6">
          {{ $t('changes.reminder.WhatNew') }}
          <v-spacer />
          <v-tooltip bottom>
            <template #activator="{ on, attrs }">
              <div
                v-bind="attrs"
                v-on="on">
                <v-btn
                  icon
                  @click="dialog = false">
                  <v-icon size="18">mdi-close</v-icon>
                </v-btn>
              </div>
            </template>
            <span>
              {{ $t('label.close') }}
            </span>
          </v-tooltip>
        </v-card-title>
        <v-card-text><img :src="reminder.img" alt=""></v-card-text>
        <v-card-text class="text-subtitle-1 font-weight-bold dark-grey-color">{{ reminder.title }}</v-card-text>
        <v-card-text>
          {{ reminder.description }}
        </v-card-text>
        <v-card-actions class="pe-6">
          <v-spacer />
          <v-btn
            text
            class="primary--text text-subtitle-1 font-weight-bold"
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
    document.addEventListener('changes-reminder-open' , () => this.getReminderStatus());
  },
  methods: {
    getReminderStatus() {
      return this.$featureService.isFeatureEnabled(this.reminder.name).then(status => {
        this.dialog = status;
      });
    },
    doNotRemindMe() {
      this.$settingService.setSettingValue('USER', eXo.env.portal.userName, 'APPLICATION', 'changesReminder', this.reminder.name, true)
        .then(() => {
          this.dialog = false;
        });
    }
  }

};
</script>