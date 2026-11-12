<!--
   * This file is part of the Meeds project (https://meeds.io/).
   *
   * Copyright (C) 2023 Meeds Association
   * contact@meeds.io
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
  <v-bottom-sheet
    v-model="showChildren"
    inset
    content-class="topBar-navigation-drop-menu topBar-navigation-bottom-drop-menu"
    hide-overlay>
    <v-sheet
      v-if="showChildren"
      class="transparent"
      @keydown.native="onListKeydown">
      <div
        v-if="navigationObject.previous">
        <v-btn
          class="mt-2 ms-1"
          icon
          @click.stop.prevent="prev">
          <v-icon
            size="15">
            {{ $vuetify.rtl && 'fa-arrow-right' || 'fa-arrow-left' }}
          </v-icon>
        </v-btn>
      </div>
      <v-list
        ref="list"
        class="mt-n3 transparent"
        dense>
        <v-list-item-group>
          <template v-for="nav in navigationObject">
            <v-list-item
              v-if="nav.pageKey || nav.children?.length || nav.pageLink"
              :key="nav.id"
              :href="nav.nodeUri || $navigationUtils.getNavigationNodeUri(baseSiteUri, nav)"
              :target="nav.nodeTarget || $navigationUtils.getNavigationNodeTarget(nav)"
              :rel="nav.nodeRel || $navigationUtils.getNavigationNodeRel(nav)"
              :link="!!nav.pageKey"
              :aria-current="isCurrent(nav) ? 'page' : null"
              @click="checkLink(nav, $event)">
              <v-list-item-content>
                <!-- the current page is shown by more than a color: bold and underlined -->
                <v-list-item-title
                  :class="isCurrent(nav) && 'font-weight-bold text-decoration-underline'"
                  class="text-body"
                  v-text="nav.label" />
              </v-list-item-content>
              <v-list-item-icon
                v-if="nav.children?.length"
                class="full-height">
                <v-btn
                  icon
                  @click.stop.prevent="next(nav)">
                  <v-icon size="18">
                    {{ $vuetify.rtl && 'fa-angle-left' || 'fa-angle-right' }}
                  </v-icon>
                </v-btn>
              </v-list-item-icon>
            </v-list-item>
          </template>
        </v-list-item-group>
      </v-list>
    </v-sheet>
  </v-bottom-sheet>
</template>

<script>
export default {
  props: {
    navigation: {
      type: Object,
      default: null
    },
    baseSiteUri: {
      type: String,
      default: null
    },
    showMenu: {
      type: Boolean,
      default: false
    },
    parentNavigationUri: {
      type: String,
      default: null
    },
    selectedPath: {
      type: String,
      default: null
    },
  },
  data: () => ({
    navigationObject: null,
    showChildren: false,
  }),
  computed: {
  },
  watch: {
    // immediate: the sheet's content is created at its first opening, already shown
    showMenu: {
      immediate: true,
      handler(value) {
        this.showChildren = value;
        if (value) {
          this.navigationObject = Object.assign({}, this.navigation);
          // however the sheet is opened, its first entry takes the focus
          this.focusEntry(0);
        }
      },
    },
  },
  created() {
    if (!this.showMenu) {
      this.navigationObject = Object.assign({}, this.navigation);
    }
  },
  methods: {
    isCurrent(nav) {
      return !!this.selectedPath
        && (nav.nodeUri || this.$navigationUtils.getNavigationNodeUri(this.baseSiteUri, nav)) === this.selectedPath;
    },
    next(navigation) {
      const previous = this.navigationObject;
      this.navigationObject = navigation.children ;
      this.navigationObject.previous = previous;
      this.focusEntry(0);
    },
    prev() {
      this.navigationObject = this.navigationObject.previous;
      this.focusEntry(0);
    },
    entries() {
      const list = this.$refs.list?.$el;
      return list ? [...list.querySelectorAll('.v-list-item')].filter(entry => entry.tabIndex >= 0) : [];
    },
    // the sheet is shown, and focuses its own container, a frame or more after it opens:
    // an entry takes the focus after that, and again until it keeps it one frame
    focusEntry(index, attempt = 0) {
      window.requestAnimationFrame(() => {
        const entries = this.entries();
        const entry = entries[index < 0 ? entries.length - 1 : index];
        entry?.focus?.();
        window.requestAnimationFrame(() => {
          if ((!entry || document.activeElement !== entry) && attempt < 10) {
            this.focusEntry(index, attempt + 1);
          }
        });
      });
    },
    onListKeydown(event) {
      const entries = this.entries();
      const index = entries.indexOf(document.activeElement);
      if (event.key === 'Escape') {
        event.preventDefault();
        event.stopPropagation();
        this.$emit('close');
        return;
      }
      if (index < 0) {
        // from the sheet's back button or an entry's chevron, the arrow keys go to the first entry
        if (['ArrowDown', 'ArrowUp', 'Home', 'End'].includes(event.key) && entries.length) {
          event.preventDefault();
          entries[event.key === 'End' || event.key === 'ArrowUp' ? entries.length - 1 : 0].focus();
        }
        return;
      }
      const target = {
        ArrowDown: (index + 1) % entries.length,
        ArrowUp: (index - 1 + entries.length) % entries.length,
        Home: 0,
        End: entries.length - 1,
      }[event.key];
      if (typeof target === 'number') {
        event.preventDefault();
        entries[target].focus();
      }
    },
    checkLink(navigation, e) {
      if (!navigation.pageKey) {
        e.preventDefault();
        e.stopPropagation();
        if (navigation.children) {
          this.next(navigation);
        }
      } else {
        this.$emit('update-navigation-state', `${this.parentNavigationUri}`);
      }
      if (navigation?.nodeUri?.includes?.('#')) {
        if (navigation?.nodeTarget === '_blank') {
          window.open(navigation.nodeUri);
        } else {
          window.location.href = navigation.nodeUri;
        }
      }
    }
  }
};
</script>