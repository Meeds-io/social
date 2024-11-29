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
  <exo-drawer
    id="spaceInvitationDrawer"
    ref="drawer"
    v-model="drawer"
    go-back-button
    right>
    <template #title>
      {{ $t('spacesList.title.usersToInvite') }}
    </template>
    <template v-if="drawer" #content>
      <space-form-invite-users-input
        :value="invitedMembers"
        class="pa-5"
        @input="invitedMembers = $event" />
    </template>
    <template #footer>
      <div class="d-flex">
        <v-spacer />
        <v-btn
          class="btn me-2"
          @click="close">
          {{ $t('spacesList.label.cancel') }}
        </v-btn>
        <v-btn
          class="btn btn-primary"
          @click.prevent.stop="apply">
          {{ $t('spacesList.button.add') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  props: {
    value: {
      type: Array,
      default: null,
    },
  },
  data: () => ({
    drawer: false,
    invitedMembers: [],
  }),
  computed: {
    suggesterLabels() {
      return {
        placeholder: this.$t('SpaceSettings.inviteMembers.placeholder'),
        noDataLabel: this.$t('SpaceSettings.inviteMembers.noResults'),
      };
    },
  },
  created() {
    this.$root.$on('space-form-invite-member', this.open);
  },
  beforeDestroy() {
    this.$root.$off('space-form-invite-member', this.open);
  },
  methods: {
    open() {
      this.invitedMembers = this.value || [];
      this.$refs.drawer.open();
    },
    apply() {
      this.$emit('input', this.invitedMembers);
      this.close();
    },
    close() {
      this.$refs.drawer.close();
    },
  },
};
</script>
