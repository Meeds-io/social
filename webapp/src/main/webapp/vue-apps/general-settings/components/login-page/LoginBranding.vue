<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io

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
  <v-card class="pa-4 col-md-6 col-12" flat v-show="initialized">
    <div class="option-item mt-2"  v-for="(option, index) in options"
      :key="index"
      :option="option">
      <v-list-item
        class="pa-0">
        <v-list-item-content>
          <v-list-item-title>
            {{ $t(`generalSettings.login.pageTile.${option?.name}.label`) }}
          </v-list-item-title>
        </v-list-item-content>
        <v-list-item-action class="d-flex flex-row my-auto">
          <v-tooltip bottom>
            <template #activator="{ on, attrs }">
              <v-btn
                v-bind="attrs"
                v-on="on"
                icon
                @click="preview(option)">
                <v-icon size="18" class="icon-default-color">fa-eye</v-icon>
              </v-btn>
            </template>
            Preview Page
          </v-tooltip>
          <v-tooltip bottom>
            <template #activator="{ on, attrs }">
              <v-btn
                v-bind="attrs"
                v-on="on"
                icon
                @click="edit(option)">
                <v-icon size="18" class="icon-default-color">fa-edit</v-icon>
              </v-btn>
            </template>
            Edit Layout
          </v-tooltip>
        </v-list-item-action>
      </v-list-item>
    </div>
  </v-card>
</template>
<script>
export default {
  data: () => ({
    siteNavigation: null,
    initialized: false,
    options: [
      {
        name: 'login',
        id: null,
      },
      {
        name: 'external-registration',
        id: null,
      },
      {
        name: 'forgot-password',
        id: null,
      },
      {
        name: 'on-boarding',
        id: null,
      },
      {
        name: 'register',
        id: null,
      },
    ]
  }),
  created() {
    Vue.prototype.$siteService.getSite('portal','global',{'expandNavigations': true}).then((site) => {
      this.options.forEach(option => {
        const navigationNode = site.siteNavigations.find(nav => nav.name === option.name);
        if (navigationNode) {
          option.id = navigationNode.id;
        }
      });
      this.initialized = true;
    });
  },
  methods: {
    preview(option) {
      window.open(`${eXo.env.portal.context}/${eXo.env.portal.defaultPortal}/${option.name}`, '_blank');
    },
    edit(option) {
      window.open(`${eXo.env.portal.context}/${eXo.env.portal.portalName}/layout-editor?nodeId=${option.id}`, '_blank');

    }
  }
};
</script>
