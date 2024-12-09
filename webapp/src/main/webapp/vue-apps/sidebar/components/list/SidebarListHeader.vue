<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io

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
  <v-card
    class="no-border-radius"
    min-height="57"
    flat>
    <v-list-item
      :href="defaultUserPath"
      :target="defaultUserPathTarget"
      :aria-label="$t('menu.userHomeLink')"
      class="fill-height">
      <v-list-item-avatar
        height="36"
        max-width="100"
        min-width="auto"
        width="auto"
        class="my-auto mx-0"
        tile>
        <img
          :src="companyLogo"
          :alt="$t('menu.companyNameTooltip',{0: companyName})"
          height="36"
          width="auto"
          class="object-fit-contain">
      </v-list-item-avatar>
      <v-list-item-content>
        <v-list-item-title
          v-if="$root.expand"
          class="font-weight-bold menu-text-color text-truncate ms-4">
          <v-card
            :title="$t('menu.companyNameTooltip',{0: companyName})"
            class="text-truncate transparent"
            min-width="50"
            flat>
            {{ companyName }}
          </v-card>
        </v-list-item-title>
      </v-list-item-content>
      <v-list-item-action v-if="$root.hoverDeferred && $root.stickyAllowed" class="d-flex flex-row ms-auto my-auto">
        <v-tooltip v-if="!$root.hidden && $root.allowHidden" bottom>
          <template #activator="{on, attrs}">
            <div
              v-on="on"
              v-bind="attrs">
              <v-btn
                :aria-label="$t('menu.collapse')"
                icon
                @click.stop.prevent="changeMenuStickiness('HIDDEN')"
                @mousedown.stop.prevent
                @mouseup.stop.prevent>
                <v-icon size="20">{{ arrowIconLeft }}</v-icon>
              </v-btn>
            </div>
          </template>
          <span>{{ $t('menu.collapse') }}</span>
        </v-tooltip>
        <v-tooltip v-if="!$root.icon && $root.allowIcon" bottom>
          <template #activator="{on, attrs}">
            <div
              v-on="on"
              v-bind="attrs">
              <v-btn
                :aria-label="$t('menu.reduce')"
                icon
                @click.stop.prevent="changeMenuStickiness('ICON')"
                @mousedown.stop.prevent
                @mouseup.stop.prevent>
                <img
                  :alt="$t('menu.reduce')"
                  src="/social/images/sidebar.svg"
                  class="icon-default-color"
                  height="20px"
                  width="20px">
              </v-btn>
            </div>
          </template>
          <span>{{ $t('menu.reduce') }}</span>
        </v-tooltip>
        <v-tooltip v-if="!$root.sticky && $root.allowSticky" bottom>
          <template #activator="{on, attrs}">
            <div
              v-on="on"
              v-bind="attrs">
              <v-btn
                :aria-label="$t('menu.expand')"
                icon
                @click.stop.prevent="changeMenuStickiness('STICKY')"
                @mousedown.stop.prevent
                @mouseup.stop.prevent>
                <v-icon size="20">{{ arrowIconRight }}</v-icon>
              </v-btn>
            </div>
          </template>
          <span>{{ $t('menu.expand') }}</span>
        </v-tooltip>
      </v-list-item-action>
    </v-list-item>
  </v-card>
</template>
<script>
export default {
  data: () => ({
    companyName: eXo.env.portal.companyName,
    companyLogo: eXo.env.portal.companyLogo,
    userName: eXo.env.portal.userName,
  }),
  computed: {
    arrowIconClass() {
      return this.$root.sticky && this.arrowIconLeft || this.arrowIconRight;
    },
    arrowIconLeft() {
      return this.$vuetify.rtl && 'fa-angle-double-right' || 'fa-angle-double-left';
    },
    arrowIconRight() {
      return this.$vuetify.rtl && 'fa-angle-double-left' || 'fa-angle-double-right';
    },
    defaultUserExternalPath() {
      return this.$root.defaultUserPath && Autolinker.parse(this.$root.defaultUserPath, {
        email: true,
      })?.[0]?.getUrl?.();
    },
    defaultUserPath() {
      return this.defaultUserExternalPath || this.$root.defaultUserPath;
    },
    defaultUserPathTarget() {
      return this.defaultUserExternalPath && '_blank' || '_self';
    },
  },
  methods: {
    changeMenuStickiness(mode) {
      this.$navigationSettingService.updateSidebarUserMode(mode);
      this.$root.mode = mode;
    },
  }
};
</script>