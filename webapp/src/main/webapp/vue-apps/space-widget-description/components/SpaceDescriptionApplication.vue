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
    <v-hover v-model="hover">
      <widget-wrapper
        extra-class="application-body"
        :title="!emptyDescription && $t('social.space.description.title')">
        <template
          v-if="$root.isManager && !emptyDescription"
          #action>
          <v-btn
            v-show="hover"
            class="pa-0"
            height="27"
            :href="administrationUrl"
            icon
            min-width="auto"
            small
            text
            :title="$t('social.space.description.editTooltip')"
            width="27">
            <v-icon
              color="primary"
              size="18">
              fa-external-link-alt
            </v-icon>
          </v-btn>
        </template>
        <template #default>
          <div
            v-if="emptyDescription"
            class="d-flex flex-column align-center justify-center my-12">
            <v-icon
              color="tertiary"
              size="54">
              fa-align-left
            </v-icon>
            <div class="my-2">
              {{ $t('social.space.description.noDescription') }}
            </div>
            <v-btn
              color="primary"
              elevation="0"
              :href="administrationUrl"
              :title="$t('social.space.description.editTooltip')">
              {{ $t('social.space.description.addDescription') }}
            </v-btn>
          </div>
          <span
            v-else
            id="spaceDescription"
            key="spaceDescription"
            ref="spaceDescription"
            v-sanitized-html="$root.spaceDescription"
            class="text-color"></span>
        </template>
        <template
          v-if="publicSiteUrl"
          #footer>
          <v-card
            flat
            :href="publicSiteUrl"
            :title="$t('social.space.description.visitPublicSite.tooltip')"
            width="100%">
            <v-divider />
            <div class="d-flex align-center">
              <v-icon
                class="ma-4"
                size="48">
                fa-globe
              </v-icon>
              {{ $t('social.space.description.visitPublicSite') }}
            </div>
          </v-card>
        </template>
      </widget-wrapper>
    </v-hover>
  </v-app>
</template>
<script>
  export default {
    data: () => ({
      hover: false,
    }),
    computed: {
      administrationUrl () {
        return `${eXo.env.portal.context}/s/${this.$root.spaceId}/settings#overview`;
      },
      publicSiteUrl () {
        return this.$root.publicSiteName
          && eXo.env.portal.portalName !== this.$root.publicSiteName
          && `${eXo.env.portal.context}/${this.$root.publicSiteName}`;
      },
      emptyDescription () {
        return !this.$root.spaceDescription
          || !this.$utils.htmlToText(this.$root.spaceDescription).length;
      },
    },
    mounted () {
      this.$root.$applicationLoaded();
    },
  };
</script>
