<!--
  This file is part of the Meeds project (https://meeds.io/).
  Copyright (C) 2022 Meeds Association
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
  <div class="pinnedActivity card-border-radius">
    <div
      class="my-auto flex-grow-1 py-2 ps-4 pe-1">
      <v-icon
        color="grey"
        :size="16">
        fa-thumbtack
      </v-icon>
      <span class="ms-2">{{ $t('UIActivity.label.pinnedActivity') }}</span>
    </div>
    <activity-stream-activity
      :key="activity.id"
      :activity="activity"
      :activity-types="activityTypes"
      :activity-actions="activityActions"
      :comment-types="commentTypes"
      :comment-actions="commentActions"
      :is-activity-detail="isActivityDetail"
      @loaded="$emit('loaded')" />
  </div>
</template>

<script>
export default {
  props: {
    activity: {
      type: Object,
      default: null,
    },
    activityTypes: {
      type: Object,
      default: null,
    },
    activityActions: {
      type: Object,
      default: null,
    },
    commentTypes: {
      type: Object,
      default: null,
    },
    commentActions: {
      type: Object,
      default: null,
    },
    isActivityDetail: {
      type: Boolean,
      default: false,
    }
  }
};
</script>