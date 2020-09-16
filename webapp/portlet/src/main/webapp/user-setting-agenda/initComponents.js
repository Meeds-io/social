import UserSettingAgendaDrawer from './components/UserSettingAgendaDrawer.vue';
import UserSettingAgenda from './components/UserSettingAgenda.vue';

const components = {
  'user-setting-agenda': UserSettingAgenda,
  'user-setting-agenda-drawer': UserSettingAgendaDrawer,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
