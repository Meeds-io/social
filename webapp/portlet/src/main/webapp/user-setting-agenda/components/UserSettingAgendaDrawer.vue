<template>
  <exo-drawer
    ref="UserSettingAgendaDrawer"
    class="UserSettingAgendaDrawer"
    body-classes="hide-scroll decrease-z-index-more"
    right>
    <template slot="title">
      {{ $t('UserSettings.agenda') }}
    </template>
    <template slot="content">
      <div class="valueTab">
        <v-layout class="ma-5 d-flex flex-column">
          <div class="d-flex flex-column mb-5">
            <label class="switch-label-text mt-1 text-subtitle-1">{{ $t('UserSettings.agenda.drawer.label.DefaultView') }}</label>
            <select v-model="value.agendaDefaultView" class="width-auto my-auto pr-2 subtitle-1 ignore-vuetify-classes d-none d-sm-inline">
              <option value="day">{{ $t('UserSettings.agenda.label.viewDay') }}</option>
              <option value="week">{{ $t('UserSettings.agenda.label.viewWeek') }}</option>
              <option value="month">{{ $t('UserSettings.agenda.label.viewMonth') }}</option>
            </select>
          </div>
          <div class="d-flex flex-column mb-5">
            <label class="switch-label-text mt-1 text-subtitle-1">{{ $t('UserSettings.agenda.drawer.label.WeekStartOn') }}</label>
            <select v-model="value.agendaWeekStartOn" class="width-auto my-auto pr-2 subtitle-1 ignore-vuetify-classes d-none d-sm-inline">
              <option value="1,2,3,4,5,6,0">{{ $t('UserSettings.agenda.drawer.day.monday') }}</option>
              <option value="2,3,4,5,6,0,1">{{ $t('UserSettings.agenda.drawer.day.tuesday') }}</option>
              <option value="3,4,5,6,0,1,2">{{ $t('UserSettings.agenda.drawer.day.wednesday') }}</option>
              <option value="4,5,6,0,1,2,3">{{ $t('UserSettings.agenda.drawer.day.thursday') }}</option>
              <option value="5,6,0,1,2,3,4">{{ $t('UserSettings.agenda.drawer.day.friday') }}</option>
              <option value="6,0,1,2,3,4,5">{{ $t('UserSettings.agenda.drawer.day.saturday') }}</option>
              <option value="0,1,2,3,4,5,6">{{ $t('UserSettings.agenda.drawer.day.sunday') }}</option>
            </select>
          </div>

          <div class="d-flex flex-row mt-5">
            <label class="switch-label-text mt-1 text-subtitle-1">{{ $t('UserSettings.agenda.drawer.label.showWorkingTime') }}</label>
            <v-switch v-model="value.showWorkingTime" class="mt-0 ml-4"></v-switch>
          </div>
          <div class="d-flex flex-row align-baseline">
            <time-picker v-model="value.workingTimeStart"></time-picker>
            <label class="switch-label-text mx-5 text-subtitle-1">{{ $t('UserSettings.agenda.drawer.label.to') }}</label>
            <time-picker v-model="value.workingTimeEnd"></time-picker>
          </div>
        </v-layout>
      </div>
    </template>
    <template slot="footer">
      <div class="d-flex">
        <v-spacer />
        <v-btn
          class="btn mr-2"
          @click="close">
          <template>
            {{ $t('UserSettings.agenda.drawer.button.cancel') }}
          </template>
        </v-btn>
        <v-btn
          class="btn btn-primary"
          @click="save">
          {{ $t('UserSettings.agenda.drawer.button.save') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
export default {
  props: {
    value: {
      type: Object,
      default: () => ({}),
    },
  },
  methods: {
    open() {
      this.$refs.UserSettingAgendaDrawer.open();
    },
    close() {
      this.$refs.UserSettingAgendaDrawer.close();
    },
    save() {
      this.$refs.UserSettingAgendaDrawer.startLoading();
      Object.keys(this.value).forEach(key => {
        return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/settings/USER,${eXo.env.portal.userName}/PORTAL,Agenda/${key}`, {
          method: 'PUT',
          credentials: 'include',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            value: this.value[key],
          }),
        }).then(resp => {
          if (resp && resp.ok) {
            this.$refs.UserSettingAgendaDrawer.close();
          }
        })
          .finally(() => {
            this.$refs.UserSettingAgendaDrawer.endLoading();
          });
      });
    },
  },
};
</script>

