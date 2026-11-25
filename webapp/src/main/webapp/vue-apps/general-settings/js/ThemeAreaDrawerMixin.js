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

/**
 * What an area drawer of the Theme Edition shares: the version toggle, the
 * Automatic rows of its colors and the restart from the edited theme when the
 * version changes. The drawer defines `themeAreaKeys` (its color keys),
 * `commitEdits()` (sends its edits without closing) and `init()`; its Save calls `saveAndClose()`.
 */
export default {
  inject: {
    themeEditSession: {
      default: null,
    },
  },
  data: () => ({
    // true once the drawer's Save went through: a close without it gives the session its draft back
    areaSaved: false,
  }),
  computed: {
    // a row (Customize, Back to automatic) or the other version's edit changed the session since the drawer opened:
    // Save keeps it even when the drawer's own model did not change
    draftChanged() {
      return !!this.themeEditSession?.draftChanged;
    },
  },
  watch: {
    drawer() {
      if (this.drawer) {
        this.areaSaved = false;
        this.$root.$emit('theme-edit-area-opened');
      } else if (!this.areaSaved) {
        this.$root.$emit('theme-edit-area-cancelled');
      }
    },
  },
  created() {
    this.$root.$on('theme-edit-reloaded', this.restartFromTheme);
    this.$root.$on('theme-edit-commit', this.commitOpenEdits);
  },
  beforeDestroy() {
    this.$root.$off('theme-edit-reloaded', this.restartFromTheme);
    this.$root.$off('theme-edit-commit', this.commitOpenEdits);
  },
  methods: {
    // the icon color an unset icon of the area shows in the edited version: the server's resolved value, else the
    // version's Grey text color (never the administration page's own, which follows the administrator's mode)
    versionIconColor(key) {
      const version = this.themeEditSession?.version || 'light';
      const resolved = this.themeEditSession?.resolved?.[version]?.[key];
      if (resolved && resolved !== 'initial') {
        return resolved;
      }
      return version === 'dark' ? '#9BA3A8' : '#6B6B6B';
    },
    // the colors the area's unset text rows show in the edited version, keyed as the text input's rows: the server's
    // resolved value of each key; the application body text has no value of its own in the template, which derives it
    // from the platform's text color
    versionTextColors(area) {
      return {
        title: this.versionColor(`${area}TextTitleColor`),
        header: this.versionColor(`${area}TextHeaderColor`),
        text: this.versionColor(`${area}TextColor`) || (area === 'app' && this.versionColor('textColor')) || null,
        subtitle: this.versionColor(`${area}TextSubtitleColor`),
      };
    },
    versionColor(key) {
      const version = this.themeEditSession?.version || 'light';
      const resolved = this.themeEditSession?.resolved?.[version]?.[key];
      return resolved && resolved !== 'initial' ? resolved : null;
    },
    // a row of the open drawer is about to reload the session: the drawer's unapplied edits go first
    commitOpenEdits() {
      if (this.drawer) {
        this.commitEdits();
      }
    },
    // Save: the edits go to the draft and the drawer closes, keeping them
    saveAndClose() {
      this.commitEdits();
      this.areaSaved = true;
      this.close();
    },
    changeVersion(version) {
      if (version !== this.themeEditSession.version) {
        this.commitEdits();
        this.$root.$emit('theme-edit-version', version);
      }
    },
    restartFromTheme() {
      if (this.drawer) {
        this.init();
      }
    },
  },
};
