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
  <v-card
    elevation="2"
    class="pa-2 card-border-radius"
    max-width="250"
    min-width="250"
    id="identity-popover"
    @mouseenter="$root.$emit('popover-hovered')"
    @mouseleave="$root.$emit('popover-not-hovered')">
    <v-list-item class="px-0">
      <v-list-item-content class="py-0">
        <v-list-item-title>
          <exo-user-avatar
            :identity="identity"
            :size="45"
            :popover="false"
            bold-title
            link-style>
            <template v-if="position" slot="subTitle">
              <span class="caption text-bold">
                {{ position }}
              </span>
            </template>
          </exo-user-avatar>
        </v-list-item-title>
      </v-list-item-content>
    </v-list-item>
    <div v-if="!isCurrentUser" class="d-flex justify-end">
      <extension-registry-components
        :params="params"
        class="d-flex"
        name="UserPopover"
        type="user-popover-action"
        parent-element="div"
        element="div"
        element-class="mx-auto ma-lg-0" />
      <div
        v-for="extension in enabledExtensionComponents"
        :key="extension.key"
        :class="`${extension.appClass} ${extension.typeClass}`"
        :ref="extension.key">
      </div>
    </div>
  </v-card>
</template>
<script>
export default {
  props: {
    identity: {
      type: Object,
      default: null,
    },
  },
  data() {
    return {
      externalExtensions: [],
    };
  },
  computed: {
    params() {
      return {
        identityType: 'USER_TIPTIP',
        identityId: this.identity && this.identity.username,
        identityEnabled: this.identity && this.identity.enabled,
        identityDeleted: this.identity && this.identity.deleted,
      };
    },
    enabledExtensionComponents() {
      return this.externalExtensions.filter(extension => extension.enabled);
    },
    username() {
      return this.identity?.username;
    },
    position() {
      return this.identity?.position;
    },
    isCurrentUser() {
      return eXo.env.portal.userName === this.username;
    },
  },
  watch: {
    username: {
      immediate: true,
      handler(newVal, oldVal) {
        if (newVal !== oldVal) {
          this.refreshExtensions();
        }
      },
    },
  },
  methods: {
    refreshExtensions() {
      this.externalExtensions = [];
      this.$nextTick(() => {
        this.externalExtensions = extensionRegistry.loadExtensions('user-profile-popover', 'action') || [];
        this.$nextTick().then(() => this.externalExtensions.forEach(this.initExtensionAction));
      });
      // workaround for menu v-select absolute content that must be displayed inside the menu
      $('.profile-popover-menu').css('height', 'auto');
    },
    initExtensionAction(extension) {
      if (extension.enabled) {
        let container = this.$refs[extension.key];
        if (container?.length) {
          if (container[0]?.childNodes?.length) {
            while (container[0].firstChild) {
              container[0].firstChild.remove();
            }
          }
          container = container[0];
          extension.init(container, this.username);
        }
      }
    },
  }
};
</script>
