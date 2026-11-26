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

import {autoTextColor, isHexColor, opaqueColor, sameColor} from '../../styling-inputs/js/ColorContrast.js';

// the theme key of each row of the text input, after the area's prefix, the text input's model key of its color and
// its text level (portal ThemeValueResolver#levelOf)
const TEXT_ROWS = Object.freeze({
  title: {suffix: 'TextTitleColor', model: 'textTitleColor', level: 'neutral'},
  header: {suffix: 'TextHeaderColor', model: 'textHeaderColor', level: 'grey'},
  text: {suffix: 'TextColor', model: 'textColor', level: 'neutral'},
  subtitle: {suffix: 'TextSubtitleColor', model: 'textSubtitleColor', level: 'grey'},
  icon: {suffix: 'IconColor', model: 'iconColor', level: 'grey'},
});

const TEXT_KEY = /(Text.*Color|IconColor)$/;

// the application's body text and icons have no value of their own in the template, which takes them from the
// platform's text color and grey: these are the keys the server makes Auto, on the application background
const APP_AUTO_KEYS = Object.freeze({TextColor: 'textColor', IconColor: 'greyColorLighten1'});

// the backgrounds a text row is read on when the area has none (portal ThemeColorCalculator)
const DEFAULT_BACKGROUNDS = Object.freeze({
  light: {drawer: '#FFFFFF', other: '#FFFFFF'},
  dark: {drawer: '#252B31', other: '#1E2328'},
});

/**
 * What an area drawer of the Theme Edition shares: the version toggle, the
 * contrast ratios and restore actions of its colors and the restart from the
 * edited theme when the version changes. The drawer defines
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
    // version's grey (greyColorLighten1), which an icon with no color of its own takes, else its default (never the
    // administration page's own, which follows the administrator's mode)
    versionIconColor(key) {
      const version = this.themeEditSession?.version || 'light';
      const resolved = this.themeEditSession?.resolved?.[version]?.[key];
      if (resolved && resolved !== 'initial') {
        return resolved;
      }
      return this.versionColor('greyColorLighten1') || (version === 'dark' ? '#9BA3A8' : '#6B6B6B');
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
    // the background the area's text rows are read on, as the server reads it: the color the drawer shows, else the
    // version's resolved one, else the version's default, its alpha ignored
    contrastBackground(area, color) {
      const version = this.themeEditSession?.version || 'light';
      const background = [color, this.versionColor(`${area}BackgroundColor`)].find(isHexColor)
        || DEFAULT_BACKGROUNDS[version][area === 'drawer' && 'drawer' || 'other'];
      return opaqueColor(background);
    },
    // the color each text row left on Auto takes on the background the drawer shows, keyed as the text input's rows,
    // as the server calculates it at Save: a row the server resolved to its Auto color, which nothing stores or
    // configures and the drawer did not change; the other rows have none and show their own color. The application's
    // body text and icons are Auto through the keys they are taken from while they have no value of their own
    autoTextColors(area, textProperties, background) {
      const session = this.themeEditSession;
      const version = session?.version;
      const resolved = session?.resolved?.[version];
      if (!resolved) {
        return null;
      }
      const resolvedBackground = this.contrastBackground(area, null);
      const shownBackground = this.contrastBackground(area, background);
      return Object.fromEntries(Object.entries(TEXT_ROWS).map(([row, {suffix, model, level}]) => {
        const key = `${area}${suffix}`;
        const value = textProperties?.[model];
        const autoKey = area === 'app' && !isHexColor(resolved[key]) && APP_AUTO_KEYS[suffix] || key;
        const auto = !this.isEditedColor(key, value)
          && [key, autoKey].every(k => session.configSettings?.[version]?.[k] == null
            && session.settings?.[version]?.[k] == null)
          && sameColor(resolved[autoKey], autoTextColor(level, resolvedBackground));
        return [row, auto && autoTextColor(level, shownBackground) || null];
      }));
    },
    // the label of the restore action of a color, null when it has none: a color has one once it is edited, in the
    // drawer or stored, and only where it has a calculated value to go back to (every dark color, a text or icon color
    // in light); a value set on this page over a configured one goes back to the configured value
    restoreLabel(key, value) {
      const session = this.themeEditSession;
      const version = session?.version;
      if (!version || (version !== 'dark' && !TEXT_KEY.test(key))) {
        return null;
      }
      if (!this.isEditedColor(key, value)) {
        return null;
      }
      if (session.configSettings?.[version]?.[key] != null) {
        return this.$t('generalSettings.version.backToConfigured');
      }
      return this.$t(version === 'dark' && 'generalSettings.version.backToAutomatic' || 'generalSettings.version.auto');
    },
    // a color the edited version stores, or which the drawer changed: one the drawer keeps from where it started, or
    // which is the resolved value, is no edit (a drawer reopened after a save starts from the values before it)
    isEditedColor(key, value) {
      const session = this.themeEditSession;
      const version = session?.version;
      return session?.settings?.[version]?.[key] != null
        || isHexColor(value) && !sameColor(value, session?.baseline?.[key]) && !sameColor(value, session?.resolved?.[version]?.[key]);
    },
    // the restore labels of the text input's rows, keyed as its rows, from its model
    textRestoreLabels(area, textProperties) {
      return Object.fromEntries(Object.entries(TEXT_ROWS)
        .map(([row, {suffix, model}]) => [row, this.restoreLabel(`${area}${suffix}`, textProperties?.[model])]));
    },
    restoreTextRow(area, row) {
      this.restoreColor(`${area}${TEXT_ROWS[row].suffix}`);
    },
    // the drawer's unapplied edits go to the session, then the color goes back to its calculated or configured value
    // and the drawer restarts from the session
    restoreColor(key) {
      this.$root.$emit('theme-edit-commit');
      this.$root.$emit('theme-edit-revert', key);
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
