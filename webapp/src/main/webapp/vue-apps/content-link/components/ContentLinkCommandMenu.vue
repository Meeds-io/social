<!--
  This file is part of the Meeds project (https://meeds.io/).

  Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io

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
  <v-menu
    ref="menu"
    v-model="menu"
    :left="!$vuetify.rtl"
    :right="$vuetify.rtl"
    :nudge-top="top"
    :nudge-left="left"
    :min-width="width"
    :max-width="width"
    :width="width"
    :close-on-click="false"
    :close-on-content-click="false"
    id="contentLinkCommandMenu"
    z-index="2000"
    absolute
    offset-y
    bottom>
    <v-list
      v-if="isCommandFiltering"
      class="pa-0"
      dense>
      <v-list-item-group v-model="selectedItemIndex">
        <v-list-item
          v-for="(p, index) of filteredPlugins"
          :key="index"
          :value="index"
          class="ps-0"
          dense
          @mousedown.stop.prevent="0"
          @click.stop.prevent="selectCommand(p)">
          <v-list-item-icon class="my-auto mx-2">
            <v-card
              class="d-flex align-center justify-center"
              color="transparent"
              min-width="40"
              flat>
              <v-icon size="24">{{ $t(p.icon) }}</v-icon>
            </v-card>
          </v-list-item-icon>
          <v-list-item-content class="pa-0 my-2 border-box-sizing">
            <v-list-item-title class="pa-0 tex-truncate my-auto line-height-normal">{{ $t(p.titleKey) }}</v-list-item-title>
            <v-list-item-subtitle class="pa-0 ma-0 line-height-normal">
              <div class="text-subtitle line-height-normal">/{{ $t(p.command) }}</div>
            </v-list-item-subtitle>
          </v-list-item-content>
        </v-list-item>
      </v-list-item-group>
    </v-list>
    <v-card
      v-else
      :loading="loading"
      class="border-box-sizing"
      flat>
      <div v-if="links?.length" class="mt-2 pa-2">
        <span class="font-weight-bold">
          {{ $t('contentLink.drawer.search.label', {0: $t(plugin.titleKey)}) }}
        </span>
        <v-list v-if="links?.length" dense>
          <v-list-item-group v-model="selectedItemIndex">
            <v-list-item
              v-for="(link, index) in links"
              :key="link.objectId"
              :value="index"
              dense
              @mousedown.stop.prevent="0"
              @click.prevent.stop="selectItem(link)">
              <v-list-item-content class="text-truncate">
                <v-list-item-title :title="link.title" class="text-truncate">{{ link.title }}</v-list-item-title>
              </v-list-item-content>
              <v-list-item-action>
                <v-tooltip bottom>
                  <template #activator="{on, attrs}">
                    <v-btn
                      v-on="on"
                      v-bind="attrs"
                      :aria-label="buttonLinkTooltip"
                      color="success"
                      small
                      icon>
                      <v-icon size="16">fa-check</v-icon>
                    </v-btn>
                  </template>
                  <span>{{ buttonSelectTooltip }}</span>
                </v-tooltip>
              </v-list-item-action>
              <v-list-item-action>
                <v-tooltip bottom>
                  <template #activator="{on, attrs}">
                    <v-btn
                      v-on="on"
                      v-bind="attrs"
                      :aria-label="buttonLinkTooltip"
                      :href="link.uri"
                      target="_blank"
                      small
                      icon
                      @click.stop="0">
                      <v-icon size="16">fa-eye</v-icon>
                    </v-btn>
                  </template>
                  <span>{{ buttonLinkTooltip }}</span>
                </v-tooltip>
              </v-list-item-action>
            </v-list-item>
          </v-list-item-group>
        </v-list>
      </div>
      <div v-else-if="!keyword?.length" class="pa-2">
        {{ $t('contentLink.drawer.search.placeholder') }}
      </div>
      <div v-else-if="loading" class="pa-2">
        {{ $t('contentLink.drawer.search.searching') }}
      </div>
      <div v-else class="pa-2">
        {{ $t('contentLink.drawer.search.noResults') }}
      </div>
    </v-card>
  </v-menu>
</template>
<script>
export default {
  data: () => ({
    menu: false,
    top: 0,
    left: 0,
    query: null,
    textWatcher: null,
    range: null,
    loading: false,
    plugin: null,
    links: null,
    keyword: null,
    typing: false,
    selectedItemIndex: 0,
    startSearchAfterInMilliseconds: 600,
    endTypingKeywordTimeout: 50,
    startTypingKeywordTimeout: 0,
    width: 280,
  }),
  computed: {
    filteredPlugins() {
      return this.query?.length ? this.$root.plugins?.filter?.(p => p.command?.startsWith(this.query)) : this.$root.plugins || [];
    },
    isCommandFiltering() {
      return !this.query?.includes?.(':');
    },
    isItemFiltering() {
      return this.query?.includes?.(':');
    },
    buttonSelectTooltip() {
      return this.$t('contentLink.drawer.selectLink', {
        0: this.$t(this.plugin.titleKey)
      });
    },
    buttonLinkTooltip() {
      return this.$t('contentLink.drawer.openLink', {
        0: this.$t(this.plugin.titleKey)
      });
    },
  },
  watch: {
    menu() {
      if (this.menu) {
        document.addEventListener('mousedown', this.handleMousedown);
        document.addEventListener('custom-link-item-select', this.selectCurrentItem);
        document.addEventListener('custom-link-item-select-up', this.selectItemTop);
        document.addEventListener('custom-link-item-select-down', this.selectItemBottom);
      } else {
        document.removeEventListener('mousedown', this.handleMousedown);
        document.removeEventListener('custom-link-item-select', this.selectCurrentItem);
        document.removeEventListener('custom-link-item-select-up', this.selectItemTop);
        document.removeEventListener('custom-link-item-select-down', this.selectItemBottom);
      }
    },
    async isCommandFiltering() {
      if (this.isCommandFiltering) {
        if (!this.plugin) {
          const commandParts = this.query?.split?.(':');
          if (commandParts?.length) {
            const command = commandParts[0];
            this.plugin = this.$root.plugins.find(p => p.command === command);
          }
        }
        await this.$nextTick();
        this.$refs?.input?.focus?.();
      }
    },
    typing()  {
      if (!this.loading && this.typing) {
        this.loading = true;
      }
    },
    keyword() {
      if (!this.keyword?.length) {
        this.typing = false;
        this.loading = false;
        this.links = null;
      } else {
        this.startTypingKeywordTimeout = Date.now() + this.startSearchAfterInMilliseconds;
        if (!this.typing) {
          this.typing = true;
          this.waitForEndTyping();
        }
      }
    },
  },
  created() {
    document.addEventListener('content-link-menu-open', this.openMenu);
    document.addEventListener('content-link-menu-close', this.unWatch);
  },
  beforeDestroy() {
    document.removeEventListener('content-link-menu-open', this.openMenu);
    document.removeEventListener('content-link-menu-close', this.unWatch);
  },
  methods: {
    selectCommand(plugin) {
      if (plugin?.insert) {
        plugin.insert(this.$root.editor, this.range, this.textWatcher);
        this.closeMenu();
      } else if (this.menu && plugin) {
        const query = this.query || '';
        const textToInsert = query?.length ? plugin.command.replace(query, '') : plugin.command;
        this.$root.editor.insertText(`${textToInsert}:`);
        this.plugin = plugin;
        this.query = `${textToInsert}:`;
      }
    },
    async selectItem(link) {
      if (this.menu && link) {
        const range = this.range;
        this.menu = false;
        await this.$nextTick();
        range.startOffset--;
        range.deleteContents();
        this.$emit('select', link);
      }
    },
    selectCurrentItem() {
      if (this.isCommandFiltering) {
        this.selectCommand(this.filteredPlugins?.[this.selectedItemIndex]);
      } else if (this.links?.length) {
        this.selectItem(this.links?.[this.selectedItemIndex]);
      }
    },
    selectItemTop() {
      if (this.isCommandFiltering) {
        this.selectedItemIndex = Math.min(Math.max(0, this.selectedItemIndex - 1), this.filteredPlugins.length - 1);
      } else if (this.links?.length) {
        this.selectedItemIndex = Math.min(Math.max(0, this.selectedItemIndex - 1), this.links.length - 1);
      }
    },
    selectItemBottom() {
      if (this.isCommandFiltering) {
        this.selectedItemIndex = Math.max(0, Math.min(this.selectedItemIndex + 1, this.filteredPlugins.length - 1));
      } else if (this.links?.length) {
        this.selectedItemIndex = Math.max(0, Math.min(this.selectedItemIndex + 1, this.links.length - 1));
      }
    },
    waitForEndTyping() {
      window.setTimeout(() => {
        if (Date.now() > this.startTypingKeywordTimeout) {
          this.typing = false;
          this.search();
        } else {
          this.waitForEndTyping();
        }
      }, this.endTypingKeywordTimeout);
    },
    async search() {
      this.loading = true;
      try {
        if (this.keyword?.length && this.plugin?.objectType) {
          this.links = await this.$contentLinkService.searchLinks(this.plugin?.objectType, this.normaliserEspaces(String(this.keyword || '')), 0, 10);
        } else {
          this.links = [];
        }
        await this.$nextTick();
        this.selectedItemIndex = 0;
      } finally {
        this.loading = false;
      }
    },
    async openMenu(event) {
      const {
        editor,
        textWatcher,
        command,
        position,
        range
      } = event.detail;
      this.query = command;
      this.$root.editor = editor;
      this.textWatcher = textWatcher;
      this.range = range;
      await this.$nextTick();
      if (this.filteredPlugins?.length || this.isItemFiltering) {
        this.selectedItemIndex = this.selectedItemIndex || 0;
        this.left = -Math.min(parseInt(position.left) + this.width, window.innerWidth);
        this.top = -parseInt(position.top) - 20;
        if (this.isItemFiltering) {
          const commandParts = command.split(':');
          this.keyword = commandParts[1];
          const plugin = this.$root.plugins.find(p => p.command === commandParts[0]);
          if (plugin?.insert) {
            this.selectCommand(plugin);
          } else if (plugin) {
            this.plugin = plugin;
          } else {
            this.plugin = null;
            this.unWatch();
            return;
          }
        }
        this.menu = true;
      } else {
        this.unWatch();
      }
    },
    handleMousedown(event) {
      if (!event.target.closest('#contentLinkCommandMenu')) {
        this.unWatch();
      }
    },
    normaliserEspaces(texte) {
      return texte.replace(/\u00A0/g, ' ')
        .replace(/\u2007/g, ' ')
        .replace(/\u202F/g, ' ')
        .replace(/\s+/g, ' ')
        .trim();
    },
    unWatch() {
      if (this.textWatcher) {
        this.textWatcher.unmatch();
        this.textWatcher = null;
      }
      this.closeMenu();
    },
    closeMenu() {
      window.setTimeout(() => this.menu = false, 200);
      this.selectedItemIndex = 0;
      this.query = null;
      this.keyword = null;
      this.textWatcher = null;
      this.range = null;
      this.loading = false;
      this.plugin = null;
      this.links = null;
      this.keyword = null;
      this.typing = false;
    },
  },
};
</script>
