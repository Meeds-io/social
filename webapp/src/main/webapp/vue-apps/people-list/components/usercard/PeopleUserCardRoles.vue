<!--
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
 -->

<template>
  <div>
    <people-user-role
      v-if="isManager"
      :title="$t('peopleList.label.spaceManager')"
      :icon="'fas fa-user-cog'" />
    <people-user-role
      v-if="isRedactor"
      :title="$t('peopleList.label.spaceRedactor')"
      :icon="'fas fa-user-edit'" />
    <people-user-role
      v-if="isPublisher"
      :title="$t('peopleList.label.spacePublisher')"
      :icon="'fas fa-paper-plane'" />
    <people-user-role
      v-if="isGroupBound"
      :title="$t('peopleList.label.groupBound')"
      :icon="'fas fa-users'" />
  </div>
</template>
<script>
export default {
  props: {
    user: {
      type: Object,
      default: null
    },
  },
  computed: {
    isManager() {
      return this.user?.isManager;
    },
    isRedactor() {
      return this.user?.isSpaceRedactor;
    },
    isPublisher() {
      return this.user?.isSpacePublisher;
    },
    isGroupBound() {
      return this.user?.isGroupBound;
    }
  },
};
</script>
