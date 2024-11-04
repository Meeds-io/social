<template>
  <v-app class="border-box-sizing transparent" flat>
    <v-dialog
      v-model="dialog"
      content-class="rounded-lg"
      overlay-opacity="0.7"
      width="500"
      @click:outside="dialog = false">
      <v-card class="pb-4">
        <v-card-title class="text-title pb-6">
          {{ $t('changes.reminder.WhatNew') }}
          <v-spacer />
          <v-tooltip bottom>
            <template #activator="{ on, attrs }">
              <div
                v-bind="attrs"
                v-on="on">
                <v-btn
                  class="me-n3"
                  icon
                  @click="dialog = false">
                  <v-icon size="18">fa-times</v-icon>
                </v-btn>
              </div>
            </template>
            <span>
              {{ $t('label.close') }}
            </span>
          </v-tooltip>
        </v-card-title>
        <v-card-text><img :src="reminder.img" alt=""></v-card-text>
        <v-card-text class="text-header">{{ reminder.title }}</v-card-text>
        <v-card-text v-if="$slots.default">
          <slot></slot>
        </v-card-text>
        <v-card-text v-else>
          {{ reminder.description }}
        </v-card-text>
        <v-card-actions class="pe-6">
          <v-spacer />
          <v-btn
            :loading="loading"
            text
            class="primary--text"
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
      loading: false,
    };
  },
  watch: {
    dialog() {
      if (this.dialog) {
        this.$emit('opened');
      } else {
        this.$emit('closed');
      }
    },
  },
  created() {
    document.addEventListener('changes-reminder-open' , this.open);
  },
  beforeDestroy() {
    document.removeEventListener('changes-reminder-open' , this.open);
  },
  methods: {
    open() {
      this.$settingService.getSettingValue('USER', eXo.env.portal.userName, 'APPLICATION', 'changesReminder', this.reminder.name)
        .then(data => this.dialog = !data?.value)
        .catch(() => this.dialog = true);
    },
    close() {
      this.dialog = false;
    },
    doNotRemindMe() {
      this.loading = true;
      this.$settingService.setSettingValue('USER', eXo.env.portal.userName, 'APPLICATION', 'changesReminder', this.reminder.name, true)
        .then(() => this.dialog = false)
        .finally(() => this.loading = false);
    }
  }
};
</script>