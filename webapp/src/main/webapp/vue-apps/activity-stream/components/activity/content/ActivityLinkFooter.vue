<!--
 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2026 Meeds Association contact@meeds.io
 
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
  <div
    class="d-flex mt-auto">
    <div class="justify-start my-auto me-auto width-fit-content">
      <extension-registry-components
        name="ActivityLinkBottomLeft"
        type="activity-link-extension"
        :params="{
          activity
        }"
        parent-element="span"
        element="span" />
    </div>
    <div class="justify-end ms-auto width-fit-content">
      <extension-registry-components
        name="ActivityLinkBottomRight"
        type="activity-link-extension"
        :params="{
          activity
        }"
        element-class="ms-5"
        parent-element="span"
        element="span" />
      <span
        v-if="activityViews"
        :title="activityViewsTooltip"
        class="ms-5">
        <v-icon
          size="16"
          class="icon-default-color">
          fas fa-eye
        </v-icon>
        <span class="ms-1 text-subtitle text-color">
          {{ activityViewsCount }}
        </span>
      </span>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    activity: {
      type: Object,
      default: null
    },
    activityTypeExtension: {
      type: Object,
      default: null
    },
    isMobile: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    activityViewsTooltip() {
      return this.activityViews?.tooltip && this.$t(this.activityViews.tooltip, {0: this.activityViews?.originalViewsCount});
    },
    activityViews() {
      return this.activityTypeExtension?.getActivityViews?.(this.activity);
    },
    activityViewsCount() {
      return this.activityViews?.viewsCount;
    },
  }
};
</script>
