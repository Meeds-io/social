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
  <exo-drawer
    ref="drawer"
    v-model="drawer"
    :right="!$vuetify.rtl"
    :loading="loading"
    class="z-index-snackbar"
    go-back-button>
    <template v-if="drawer" #title>
      {{ $t('contentLink.drawer.search.title', {0: $t(plugin.titleKey)}) }}
    </template>
    <template v-if="drawer" #content>
      <div class="ma-5">
        {{ $t('contentLink.drawer.search.label', {0: $t(plugin.titleKey)}) }}
        <v-text-field
          v-model="keyword"
          :placeholder="$t('contentLink.drawer.search.placeholder')"
          class="pa-0 mt-2"
          autocomplete="off"
          hide-details
          outlined
          dense />
        <v-list v-if="links" dense>
          <v-list-item
            v-for="link in links"
            :key="link.objectId"
            dense
            @click="select(link)">
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
        </v-list>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    drawer: false,
    loading: false,
    plugin: null,
    links: null,
    keyword: null,
    typing: false,
    startSearchAfterInMilliseconds: 600,
    endTypingKeywordTimeout: 50,
    startTypingKeywordTimeout: 0,
  }),
  computed: {
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
    this.$root.$on('content-link-search-drawer', this.open);
  },
  beforeDestroy() {
    this.$root.$off('content-link-search-drawer', this.open);
  },
  methods: {
    open(plugin) {
      this.plugin = plugin;
      this.keyword = null;
      this.links = null;
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
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
        this.links = await this.$contentLinkService.searchLinks(this.plugin.objectType, this.keyword, 0, 10);
      } finally {
        this.loading = false;
      }
    },
    select(link) {
      this.$emit('select', link);
      this.close();
    },
  },
};
</script>