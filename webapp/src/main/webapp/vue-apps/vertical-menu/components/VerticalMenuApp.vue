<!--
  This file is part of the Meeds project (https://meeds.io/).
  Copyright (C) 2023 Meeds Association
  contact@meeds.io
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
    <v-main class="application-body">
      <v-card
        min-height="var(--100vh, 100vh)"
        color="transparent"
        class="d-flex flex-column"
        flat>
        <vertical-menu-content :extra-class="extraClass" class="flex-grow-1 flex-shrink-1" />
        <vertical-menu-bottom-bar class="flex-grow-0 flex-shrink-0" />
        <template v-if="$root.isMobile">
          <vertical-menu-button />
          <vertical-menu-drawer />
        </template>
      </v-card>
    </v-main>
  </v-app>
</template>
<script></script>
