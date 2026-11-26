/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

import {toEditorBranding, fromEditorResult, restoreAreaKeys, toResolvePayload, sameSettings} from './ThemeSettingsMapper.js';

// the keys each area's restore clears, in both versions
const AREA_RESTORES = Object.freeze({
  'reset-theme-colors': /^(primaryColor|secondaryColor)$/,
  'reset-font-family': /^fontFamily$/,
  'reset-page-styling': /^(page|app|borderRadius)/,
  'reset-top-bar-styling': /^topBar/,
  'reset-sidebar-styling': /^sideBar/,
  'reset-drawer-styling': /^drawer/,
});

const STYLE_UPDATES = Object.freeze([
  'update-branding-theme-colors',
  'update-branding-font-family',
  'update-top-bar-styling-properties',
  'update-sidebar-styling-properties',
  'update-drawer-styling-properties',
  'update-page-styling-properties',
]);

/**
 * The edition session of one theme (Theme Edition drawer): a draft of the
 * theme, the version the area drawers edit and the values the server
 * resolves for both versions. The area drawers read it through the injected
 * `themeEditSession` and talk to it through $root events.
 * The host holds `branding` (the style the drawers start from), its `stored`
 * themes and the styling model of ThemeStylingMixin.
 */
export default {
  provide() {
    return {
      themeEditSession: this.session,
    };
  },
  data: () => ({
    draft: null,
    resolveTimer: null,
    resolveToken: 0,
    // shared with the area drawers: what a row needs to show its state
    session: {
      version: 'light',
      settings: null,
      // the configured values of a theme bound to the configuration: a configured dark value is no Automatic one
      configSettings: null,
      resolved: null,
      // the values the area drawers start from: a value a drawer keeps is no edit, as the draft reads it (fromEditorResult)
      baseline: null,
      modesOffered: null,
      // the draft (settings and pending images) differs from the one the open area drawer started from
      draftChanged: false,
    },
  }),
  created() {
    this.restoreHandlers = {};
    STYLE_UPDATES.forEach(event => this.$root.$on(event, this.styleChanged));
    Object.keys(AREA_RESTORES).forEach(event => {
      this.restoreHandlers[event] = () => this.restoreArea(AREA_RESTORES[event]);
      this.$root.$on(event, this.restoreHandlers[event]);
    });
    this.$root.$on('theme-edit-version', this.changeEditedVersion);
    this.$root.$on('theme-edit-revert', this.revert);
    this.$root.$on('theme-edit-area-opened', this.snapshotDraft);
    this.$root.$on('theme-edit-area-cancelled', this.restoreSnapshot);
  },
  beforeDestroy() {
    STYLE_UPDATES.forEach(event => this.$root.$off(event, this.styleChanged));
    Object.keys(this.restoreHandlers).forEach(event => this.$root.$off(event, this.restoreHandlers[event]));
    this.$root.$off('theme-edit-version', this.changeEditedVersion);
    this.$root.$off('theme-edit-revert', this.revert);
    this.$root.$off('theme-edit-area-opened', this.snapshotDraft);
    this.$root.$off('theme-edit-area-cancelled', this.restoreSnapshot);
    window.clearTimeout(this.resolveTimer);
  },
  methods: {
    startSession(theme, modesOffered) {
      this.draft = Object.assign({}, theme, {settings: JSON.parse(JSON.stringify(theme.settings))});
      this.session.version = 'light';
      this.session.modesOffered = modesOffered;
      this.session.settings = this.draft.settings;
      this.session.configSettings = this.draft.configSettings || null;
      this.session.resolved = null;
      this.resolveToken++;
      return this.resolveNow().then(() => this.reloadStyling());
    },
    // an area drawer opens: the draft as it is, to give it back when the drawer is cancelled
    snapshotDraft() {
      this.areaSnapshot = this.draft && {
        settings: JSON.parse(JSON.stringify(this.draft.settings)),
        pendingImages: Object.assign({}, this.draft.pendingImages),
      };
      this.session.draftChanged = false;
    },
    // the draft's maps are rebuilt in another key order than the server sent them: compared by value
    refreshDraftChanged() {
      this.session.draftChanged = !!this.areaSnapshot
        && (!sameSettings(this.draft?.settings, this.areaSnapshot.settings)
          || this.imagesKey(this.draft?.pendingImages) !== this.imagesKey(this.areaSnapshot.pendingImages));
    },
    imagesKey(images) {
      return JSON.stringify(Object.keys(images || {}).sort((a, b) => a.localeCompare(b)).map(key => [key, images[key]]));
    },
    // an area drawer is cancelled or closed without Save: what its version toggle or a row of the theme edition
    // committed meanwhile goes back to the draft of its opening, and the preview follows
    restoreSnapshot() {
      const snapshot = this.areaSnapshot;
      this.areaSnapshot = null;
      this.session.draftChanged = false;
      if (snapshot && this.draft) {
        window.clearTimeout(this.resolveTimer);
        this.draft = Object.assign({}, this.draft, {
          settings: JSON.parse(JSON.stringify(snapshot.settings)),
          pendingImages: snapshot.pendingImages,
        });
        this.session.settings = this.draft.settings;
        this.reloadStyling();
        this.refreshStickyTopBar();
        return this.resolveNow().then(() => this.reloadStyling());
      }
    },
    // restarts the area drawers from the draft, for the edited version
    reloadStyling() {
      const version = this.session.version;
      this.branding = toEditorBranding(this.draft, this.$root.defaultBrandingThemeStyle, version, this.session.resolved?.[version]);
      this.session.baseline = this.branding.themeStyle;
      this.initStyling();
      this.refreshDraftChanged();
      // the drawers read their props, which the render after this change updates
      this.$nextTick(() => this.$root.$emit('theme-edit-reloaded'));
    },
    // writes what the drawers edited into the draft
    syncDraft() {
      const result = fromEditorResult(this.draft,
        this.exportStyling(),
        this.$root.defaultBrandingThemeStyle,
        this.session.version,
        this.branding.themeStyle);
      this.draft = Object.assign({}, this.draft, result);
      this.session.settings = this.draft.settings;
      this.refreshDraftChanged();
    },
    resolveNow() {
      window.clearTimeout(this.resolveTimer);
      const stored = this.stored.find(item => item.id === this.draft.id)?.settings;
      const token = ++this.resolveToken;
      // an answer to an older draft is dropped
      return this.$themeService.resolve(toResolvePayload(this.draft, stored))
        .then(resolved => token === this.resolveToken && (this.session.resolved = resolved))
        .catch(() => token === this.resolveToken && (this.session.resolved = null));
    },
    // the resolved values (the Auto colors and the restore state of the rows) follow the edits, without restarting the
    // drawers
    styleChanged() {
      this.syncDraft();
      window.clearTimeout(this.resolveTimer);
      this.resolveTimer = window.setTimeout(this.resolveNow, 500);
    },
    changeEditedVersion(version) {
      this.syncDraft();
      this.session.version = version;
      return this.resolveNow().then(() => this.reloadStyling());
    },
    revert(key) {
      this.syncDraft();
      delete this.draft.settings[this.session.version][key];
      return this.resolveNow().then(() => this.reloadStyling());
    },
    // the area drawers already reset their model: the draft gives the area its defaults back, in both versions
    restoreArea(pattern) {
      this.draft.settings = restoreAreaKeys(this.draft, pattern);
      this.session.settings = this.draft.settings;
      // at once: the drawers' own reset wrote the platform defaults, which a quick Apply must not export
      this.reloadStyling();
      return this.resolveNow().then(() => this.reloadStyling());
    },
  },
};
