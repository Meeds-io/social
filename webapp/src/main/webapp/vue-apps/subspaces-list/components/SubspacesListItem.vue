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
  <div class="d-flex align-center py-2">
    <space-avatar
      :space="space"
      link-style />
  </div>
</template>
<script>
export default {
  props: {
    /**
     * One sub-space of the resource envelope: {id, displayName, prettyName,
     * avatarUrl, visibility, isMember}. The shared <space-avatar> reads
     * visibility and isMember itself to decide how a HIDDEN space is rendered
     * to a viewer who is not a member of it.
     */
    space: {
      type: Object,
      default: () => null,
    },
  },
};
</script>
