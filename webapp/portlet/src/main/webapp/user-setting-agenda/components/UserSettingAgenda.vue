<template>
  <v-app v-if="displayed">
    <v-card class="ma-4 border-radius" flat>
      <v-list two-line>
        <v-list-item>
          <v-list-item-content>
            <v-list-item-title class="title text-color">
              <div :class="skeleton && 'skeleton-background skeleton-border-radius skeleton-text-width skeleton-text-height my-2'">
                {{ skeleton && '&nbsp;' || $t('UserSettings.agenda') }}
              </div>
            </v-list-item-title>
            <v-list-item-subtitle class="text-sub-title text-capitalize font-italic">
              <div :class="skeleton && 'skeleton-background skeleton-border-radius skeleton-text-width-small skeleton-text-height-fine my-2'">
                {{ skeleton && '&nbsp;' || timezoneLabel }}
              </div>
            </v-list-item-subtitle>
          </v-list-item-content>
          <v-list-item-action>
            <v-btn
              :class="skeleton && 'skeleton-background'"
              small
              icon
              @click="openDrawer">
              <i v-if="!skeleton" class="uiIconEdit uiIconLightBlue pb-2"></i>
            </v-btn>
          </v-list-item-action>
        </v-list-item>
      </v-list>
      <user-setting-agenda-drawer
        ref="agendaDrawer"
        v-model="settings" />
    </v-card>
  </v-app>
</template>

<script>
export default {
  data: () => ({
    id: `Settings${parseInt(Math.random() * 10000)
      .toString()
      .toString()}`,
    settings: {
      agendaDefaultView: '',
      agendaDateFormat: '',
      agendaTimeFormat: '',
      agendaWeekStartOn: '',
      showWorkingTime: false,
      workingTimeStart: '',
      workingTimeEnd: '',
    },
    displayed: true,
    skeleton: true,
  }),
  created() {
    document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
    document.addEventListener('hideSettingsApps', (event) => {
      if (event && event.detail && this.id !== event.detail) {
        this.displayed = false;
      }
    });
    document.addEventListener('showSettingsApps', () => this.displayed = true);
    this.skeleton = false;
    this.$root.$on('refresh', this.refresh);
    this.refresh();
  },
  methods: {
    refresh() {
      Object.keys(this.settings).forEach(key => {
        return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/settings/USER,${eXo.env.portal.userName}/PORTAL,Agenda/${key}`)
          .then(resp => resp && resp.ok && resp.json())
          .then(setting => {
            if (key === 'showWorkingTime') {
              this.settings[key] = setting.value === 'true';
            } else {
              this.settings[key] = setting.value;
            }
          })
          .finally(() => {
            document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
            this.skeleton = false;
          });
      });
    },
    openDrawer()
    {
      this.$refs.agendaDrawer.open();
    }
  }
};
</script>