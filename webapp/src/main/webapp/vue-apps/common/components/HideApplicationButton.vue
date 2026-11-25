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
  <!-- A guest has no choice to save: the button is for authenticated users only -->
  <v-btn
    v-if="authenticated"
    :title="label"
    :aria-label="label"
    :loading="hiding"
    class="hide-application-button"
    small
    icon
    @click="hide">
    <v-icon size="18">fa-times</v-icon>
  </v-btn>
</template>
<script>
const FOCUSABLE_SELECTOR = 'a[href], button:not([disabled]), input:not([disabled]):not([type="hidden"]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])';

export default {
  props: {
    // Application type, as its HiddenApplicationPlugin declares it
    type: {
      type: String,
      default: null,
    },
    // Application window name
    name: {
      type: String,
      default: null,
    },
    // Accessible name and tooltip of the button
    label: {
      type: String,
      default: null,
    },
    // Message shown when the application could not be hidden
    errorLabel: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    hiding: false,
    authenticated: !!eXo?.env?.portal?.userName,
  }),
  methods: {
    hide() {
      // Computed before the application leaves the page, which removes the
      // button and its position in the document
      const nextFocusable = this.getNextFocusableElement();
      this.hiding = true;
      return this.$hiddenApplicationService.hide(this.type, this.name)
        .then(() => {
          this.$emit('hidden');
          return this.$nextTick();
        })
        .then(() => nextFocusable?.focus?.())
        .catch(() => this.$root.$emit('alert-message', this.errorLabel, 'error'))
        .finally(() => this.hiding = false);
    },
    getNextFocusableElement() {
      const application = this.$el?.closest?.('.PORTLET-FRAGMENT')?.parentElement || this.$el;
      return Array.from(document.querySelectorAll(FOCUSABLE_SELECTOR))
        .find(element => !application.contains(element)
          && (application.compareDocumentPosition(element) & Node.DOCUMENT_POSITION_FOLLOWING)
          && element.offsetParent !== null);
    },
  },
};
</script>
