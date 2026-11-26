<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io

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
  <div class="brandingIdentity mb-6">
    <div class="d-flex align-center">
      <div class="text-header">
        {{ $t('generalSettings.branding.title') }}
      </div>
      <v-spacer />
      <v-tooltip bottom>
        <template #activator="{ on, attrs }">
          <v-btn
            v-bind="attrs"
            v-on="on"
            :aria-label="$t('generalSettings.edit.button.tooltip')"
            class="brandingEditButton"
            icon
            @click="$emit('edit')">
            <v-icon size="18" class="icon-default-color">fa-edit</v-icon>
          </v-btn>
        </template>
        {{ $t('generalSettings.edit.button.tooltip') }}
      </v-tooltip>
    </div>
  </div>
</template>
