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
  <tr>
    <!-- description -->
    <td align="left">
      {{ operation.group.name }}
    </td>
    <td
      v-if="!$root.isMobile"
      align="center">
      <date-format
        v-if="operation.startDate && operation.startDate !== 'null'"
        :value="operation.startDate"
        :format="fullDateFormat" />
      <template v-else>-</template>
    </td>
    <td
      v-if="!$root.isMobile"
      align="center">
      <date-format
        v-if="operation.endDate && operation.endDate !== 'null'"
        :value="operation.endDate"
        :format="fullDateFormat" />
      <v-icon
        v-else
        :title="$t('social.spaces.administration.manageSpaces.operationInProgress')"
        color="primary"
        size="20">
        fa-spinner
      </v-icon>
    </td>
    <td
      v-if="!$root.isMobile"
      align="center">
      {{ $t(`social.spaces.administration.manageSpaces.operationType.${operation.operationType}`) }}
    </td>
    <td align="center">
      <number-format v-if="operation.addedUsers && operation.addedUsers !== '0'" :value="operation.addedUsers" />
      <template v-else>-</template>
    </td>
    <td align="center">
      <number-format v-if="operation.removedUsers && operation.removedUsers !== '0'" :value="operation.removedUsers" />
      <template v-else>-</template>
    </td>
    <td align="center">
      <v-btn
        :title="$t('social.spaces.administration.manageSpaces.downloadReportTooltip')"
        :loading="downloading"
        icon
        @click="download">
        <v-icon size="20">fa-download</v-icon>
      </v-btn>
    </td>
  </tr>
</template>
<script>
export default {
  props: {
    operation: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    downloading: false,
    fullDateFormat: {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    },
  }),
  methods: {
    async download() {
      this.downloading = true;
      try {
        await this.$spacesAdministrationServices.getReport(
          this.operation.space.id,
          this.operation.operationType,
          this.operation.group.id,
          this.operation.bindingId);
      } finally {
        this.downloading = false;
      }
    },
  },
};
</script>