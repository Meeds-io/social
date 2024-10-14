/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2024 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

import OrganizationalChartApp from './components/OrganizationalChartApp.vue';
import OrganizationalChart from './components/view/OrganizationalChart.vue';
import OrganizationalChartHeader from './components/view/OrganizationalChartHeader.vue';
import ChartUserCompactCard from './components/view/ChartUserCompactCard.vue';
import OrganizationalChartSettingsDrawer from './components/settings/OrganizationalChartSettingsDrawer.vue';


const components = {
  'organizational-chart-app': OrganizationalChartApp,
  'organizational-chart': OrganizationalChart,
  'organizational-chart-header': OrganizationalChartHeader,
  'chart-user-compact-card': ChartUserCompactCard,
  'organizational-chart-settings-drawer': OrganizationalChartSettingsDrawer
};

for (const key in components) {
  Vue.component(key, components[key]);
}
