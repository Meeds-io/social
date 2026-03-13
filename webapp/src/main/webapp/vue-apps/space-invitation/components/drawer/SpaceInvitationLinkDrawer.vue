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
  <exo-drawer
    id="SpaceInvitationLinkDrawer"
    ref="SpaceInvitationLinkDrawer"
    :go-back-button="goBackButton"
    :right="!$vuetify.rtl"
    allow-expand>
    <template slot="title">
      {{ $t('spaceLinkInvitationDrawer.title.invitationLink') }}
    </template>
    <template slot="content">
      <div class="pa-4 d-flex flex-column">
        <label for="invitationLink">
          <span class="text-header">
            {{ $t('spaceLinkInvitationDrawer.share.link.label') }}
          </span>
          <v-text-field
            :value="invitationLink"
            name="invitationLink"
            class="pt-0 mt-3"
            readonly
            outlined
            dense>
            <template #append>
              <v-btn
                min-width="18"
                min-height="18"
                width="24"
                height="24"
                class="pa-0 my-auto"
                icon
                @click="copyLink">
                <v-icon size="18">
                  fas fa-copy
                </v-icon>
              </v-btn>
            </template>
          </v-text-field>
        </label>
      </div>
    </template>
  </exo-drawer>
</template>
<script>

export default {
  data() {
    return {
      invitationLink: null,
    };
  },
  props: {
    spaceId: {
      type: Number,
      default: null,
    },
    goBackButton: {
      type: Boolean,
      default: false,
    }
  },
  created() {
    this.$root.$on('space-settings-invite-link', this.open);
    this.generateInvitationLink();
  },
  watch: {
    spaceId() {
      this.generateInvitationLink();
    }
  },
  methods: {
    open() {
      this.$refs.SpaceInvitationLinkDrawer.open();
    },
    generateInvitationLink() {
      if (!this.spaceId) {
        return;
      }
      this.$spaceService.generateInvitationToken(this.spaceId).then(invitationLink => {
        this.invitationLink = invitationLink;
      });
    },
    copyLink() {
      navigator.clipboard.writeText(this.invitationLink).then(() => {
        this.$root.$emit('alert-message', this.$t('spaceLinkInvitationDrawer.invitation.link.copy.success'), 'success');
      }).catch(() => {
        this.$root.$emit('alert-message', this.$t('spaceLinkInvitationDrawer.invitation.link.copy.success'), 'error');
      });
    }
  }
};
</script>