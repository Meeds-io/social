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
  <v-app>
    <v-hover v-slot="{ hover }">
      <widget-wrapper
        :title="$t('social.space.description.title')"
        extra-class="application-body">
        <template v-if="$root.canEdit" #action>
          <v-btn
            v-show="hover"
            :title="$t('social.space.description.editTooltip')"
            :href="administrationUrl"
            height="27"
            width="27"
            min-width="auto"
            class="pa-0"
            icon
            text
            small>
            <v-icon
              size="18"
              color="primary">
              fa-external-link-alt
            </v-icon>
          </v-btn>
        </template>
        <template #default>
          <span
            v-if="$root.spaceDescription"
            v-sanitized-html="$root.spaceDescription"
            id="spaceDescription"
            class="text-color"></span>
        </template>
      </widget-wrapper>
    </v-hover>
  </v-app>
</template>
<script>
export default {
  computed: {
    administrationUrl() {
      return `${eXo.env.portal.context}/s/${this.$root.spaceId}/settings#overview`;
    },
  },
  mounted() {
    this.$root.$applicationLoaded();
  },
};
</script>
