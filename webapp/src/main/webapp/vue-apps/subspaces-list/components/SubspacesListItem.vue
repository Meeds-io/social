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
      :space="displayedSpace"
      :popover="!hiddenToViewer"
      link-hidden-space
      link-style />
  </div>
</template>
<script>
export default {
  props: {
    /**
     * One sub-space of the resource envelope: {id, displayName, prettyName,
     * avatarUrl, visibility, isMember, isInvited}. A HIDDEN sub-space reaches a
     * non-member only because the server decided to list it (invited user, or
     * 'show hidden subspaces' on), so it is rendered as a normal link. Where
     * /s/{id} lands is the platform's decision, not this widget's: the space
     * access page (join, request to join, invitation) for an OPEN or
     * VALIDATION space or an invited viewer; the space's public site when it
     * has one the viewer may see; "page not found" for a CLOSED hidden space
     * the viewer is not invited to (SpacePermanentLinkHandler).
     */
    space: {
      type: Object,
      default: () => null,
    },
    /**
     * How many rows the portlet must look through to serve this row's avatar,
     * for a list longer than the widget's own — the 'see more' drawer passes
     * the number of rows it received. Left unset by the widget, whose rows the
     * portlet looks through by default.
     */
    avatarListingLimit: {
      type: Number,
      default: () => 0,
    },
  },
  computed: {
    /**
     * The hover popover fetches the space over REST, which a viewer with no
     * relationship to a HIDDEN space is refused: keep it off for those rows
     * only. An invited user is served exactly like a member (the platform
     * rule of canListSpace), so their row keeps both the popover and the
     * standard avatar URL; an administrator likewise (same rule as
     * <space-avatar>'s canAccessSpace).
     *
     * @returns {boolean} whether the space is hidden to this viewer
     */
    hiddenToViewer() {
      return this.space?.visibility === 'hidden'
        && !this.space?.isMember
        && !this.space?.isInvited
        && !eXo.env.portal.isAdministrator;
    },
    /**
     * The space as handed to <space-avatar>. For a row hidden to the viewer,
     * the avatar is served by the portlet (which lists the space to them)
     * instead of the space avatar endpoint, which refuses a non-member of a
     * hidden closed space.
     *
     * @returns {object} the space, with the avatar URL the viewer can load
     */
    displayedSpace() {
      if (!this.hiddenToViewer || !this.space) {
        return this.space;
      }
      const avatarUrl = this.$subspacesListService.getSubspaceAvatarUrl(
        this.$root.settings?.avatarResourceUrl,
        this.space.id,
        this.avatarListingLimit);
      // no portlet-served URL: keep the standard one rather than lose the row
      return avatarUrl && {...this.space, avatarUrl} || this.space;
    },
  },
};
</script>
