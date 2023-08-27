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
  <exo-drawer
    ref="drawer"
    allow-expand
    right
    disable-pull-to-refresh>
    <template #title>
      <div class="d-flex flex-no-wrap align-center overflow-hidden">
        <v-icon size="18" class="me-2">fa-question-circle</v-icon>
        <div :title="title" class="text-truncate">{{ title }}</div>
      </div>
    </template>
    <template #content>
      <v-card class="pa-4" flat>
        <template v-if="value === 'whatIsExternalUser'">
          <p>
            {{ $t('generalSettings.access.whatIsExternalUser.paragraph1') }}
          </p>
          <p>
            {{ $t('generalSettings.access.whatIsExternalUser.paragraph2') }}
          </p>
          <p>
            {{ $t('generalSettings.access.whatIsExternalUser.paragraph3') }}
          </p>
        </template>
        <template v-else-if="value === 'whatIsSpaceHost'">
          <p>
            {{ $t('generalSettings.access.whatIsSpaceHost.paragraph1') }}
          </p>
          <p>
            {{ $t('generalSettings.access.whatIsSpaceHost.paragraph2') }}
          </p>
          <p>
            {{ $t('generalSettings.access.whatIsSpaceHost.paragraph3') }}
          </p>
        </template>
        <template v-else-if="value === 'whatIsRegisteredUser'">
          <p>
            {{ $t('generalSettings.access.whatIsRegisteredUser.paragraph1') }}
          </p>
          <p>
            {{ $t('generalSettings.access.whatIsRegisteredUser.paragraph2') }}
          </p>
          <p>
            {{ $t('generalSettings.access.whatIsRegisteredUser.paragraph3') }}
          </p>
          <p>
            {{ $t('generalSettings.access.whatIsRegisteredUser.paragraph4') }}
          </p>
        </template>
        <template v-else-if="value === 'whatIsDefaultSpace'">
          <p>
            {{ $t('generalSettings.access.whatIsDefaultSpace.paragraph1') }}
          </p>
          <p>
            {{ $t('generalSettings.access.whatIsDefaultSpace.paragraph2') }}
          </p>
          <p>
            {{ $t('generalSettings.access.whatIsDefaultSpace.paragraph3') }}
          </p>
        </template>
        <template v-else-if="value === 'whatIsMandatorySpace'">
          <p>
            {{ $t('generalSettings.access.whatIsMandatorySpace.paragraph1') }}
          </p>
          <p>
            {{ $t('generalSettings.access.whatIsMandatorySpace.paragraph2') }}
          </p>
          <p>
            {{ $t('generalSettings.access.whatIsMandatorySpace.paragraph3') }}
          </p>
        </template>
      </v-card>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  props: {
    value: {
      type: String,
      default: null,
    },
  },
  computed: {
    title() {
      return this.$t(`generalSettings.access.${this.value}`);
    },
  },
  methods: {
    open() {
      this.$refs.drawer.open();
    },
  }
};
</script>
