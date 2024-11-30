<!--

 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
 
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
  <v-app class="full-height">
    <div class="d-flex flex-row full-height">
      <top-bar-logo-company-name v-if="$root.displayCompany" class="ms-4" />
      <div v-if="$root.displayCompany && $root.displaySite" class="ms-4 d-flex align-center">
        <v-icon size="16">
          {{ $vuetify.rtl && 'fa-chevron-left' || 'fa-chevron-right' }}
        </v-icon>
      </div>
      <top-bar-logo-site-name v-if="$root.displaySite" class="ms-4" />
    </div>
  </v-app>
</template>
<script></script>