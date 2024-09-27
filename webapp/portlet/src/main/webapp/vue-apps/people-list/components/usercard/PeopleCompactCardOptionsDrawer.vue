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
  <exo-drawer
    id="peopleCompactCardBottomDrawer"
    ref="userOptionsDrawer"
    :bottom="true">
    <template slot="content">
      <v-list dense>
        <v-list-item-group>
          <span
            v-for="(extension, i) in profileActionExtensions"
            :key="i">
            <v-list-item
              v-if="!extension.init"
              @click="triggerExtension(extension)">
              <v-list-item-title class="align-center d-flex">
                <v-card
                  class="d-flex align-center justify-center transparent ms-4 me-2"
                  height="25"
                  width="25"
                  flat>
                  <v-icon
                    class="itemIconSize"
                    size="20">
                    {{ extension.class }}
                  </v-icon>
                </v-card>
                <span class="ms-3">
                  {{ extension.title || $t(extension.titleKey) }}
                </span>
              </v-list-item-title>
            </v-list-item>
            <v-list-item
              v-else
              :class="`${extension.appClass} ${extension.typeClass}`"
              :ref="extension.id" />
          </span>
        </v-list-item-group>
        <v-divider
          v-if="spaceMembersExtensions.length"
          class="mx-6 my-1" />
        <v-list-item-group>
          <v-list-item
            v-for="(extension, i) in spaceMembersExtensions"
            :key="i"
            @click="triggerExtension(extension)">
            <v-list-item-title class="align-center d-flex">
              <v-card
                class="d-flex align-center justify-center transparent ms-4 me-2"
                height="25"
                width="25"
                flat>
                <v-icon
                  class="itemIconSize"
                  size="20">
                  {{ extension.class }}
                </v-icon>
              </v-card>
              <span class="ms-3">
                {{ extension.title || $t(extension.titleKey) }}
              </span>
            </v-list-item-title>
          </v-list-item>
        </v-list-item-group>
      </v-list>
      <exo-drawer />
    </template>
  </exo-drawer>
</template>
<script>

export default {
  data() {
    return {
      user: null,
      profileActionExtensions: [],
      spaceMembersExtensions: []
    };
  },
  created() {
    this.$root.$on('open-people-compact-card-options-drawer', this.open);
  },
  methods: {
    triggerExtension(extension) {
      extension.click(this.user);
      this.$refs.userOptionsDrawer.close();
    },
    open(user, profileActions, spaceMembersActions) {
      this.user = user;
      this.profileActionExtensions = profileActions;
      this.spaceMembersExtensions = spaceMembersActions;
      this.$refs.userOptionsDrawer.open();
      this.$nextTick().then(() => {
        this.initExtensions();
      });
    },
    initExtensions() {
      this.profileActionExtensions.forEach((extension) => {
        if (extension.init) {
          let container = this.$refs[extension.id];
          if (container && container.length > 0) {
            container = container[0].$el;
            container.innerHTML='';
            extension.init(container, this.user.username);
          }
        }
      });
    }
  }
};
</script>
