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
  <span>
    <span v-html="labelContent"></span>
    <help-drawer
      v-if="helpDrawer"
      :title-key="drawerTitle || tooltip"
      :content-key="drawerContent"
      @closed="helpDrawer = false">
      <div v-if="$slots.helpContent" slot="content">
        <slot name="helpContent"></slot>
      </div>
    </help-drawer>
    <help-tooltip
      v-if="helpTooltip"
      :label="tooltip"
      :attach="helpTooltipElement" />
  </span>
</template>
<script>
export default {
  props: {
    /* Optional attribute, will use 'label' attribute to generate the id if null */
    id: {
      type: String,
      default: null,
    },
    /* Mandatory attributes */
    label: {
      type: String,
      default: null,
    },
    tooltip: {
      type: String,
      default: null,
    },
    /* Either use 'drawerTitle' label Key or 'tooltip' label Key */
    drawerTitle: {
      type: String,
      default: null,
    },
    /* Either use 'drawerContent' label Key or 'helpContent' Slot */
    drawerContent: {
      type: String,
      default: null,
    },
    /* Optional attributes */
    labelClass: {
      type: String,
      default: () => 'text-color',
    },
    labelParams: {
      type: Object,
      default: null,
    },
    labelHelpLinkIndexStart: {
      type: Number,
      default: () => 0,
    },
    labelHelpLinkIndexEnd: {
      type: Number,
      default: () => 1,
    },
  },
  data: () => ({
    helpItemId: null,
    helpDrawer: false,
    helpTooltip: false,
    helpTooltipElement: null,
  }),
  computed: {
    helpParams() {
      const params = this.labelParams && {...this.labelParams} || {};
      params[this.labelHelpLinkIndexEnd] = '</a>';
      params[this.labelHelpLinkIndexStart] = `<a href="javascript:void(0)"
        class="${this.labelClass || ''} text-decoration-underline"
        onmouseover="document.dispatchEvent(new CustomEvent('hub-access-help-tooltip-open-${this.helpItemId}', {detail: {element: event.target}}))"
        onmouseout="document.dispatchEvent(new CustomEvent('hub-access-help-tooltip-close-${this.helpItemId}', {detail: {element: event.target}}))"
        onclick="window.event.cancelBubble = true;document.dispatchEvent(new CustomEvent('hub-access-help-${this.helpItemId}'))">
      `;
      return params;
    },
    labelContent() {
      return this.$t(this.label, this.helpParams);
    },
  },
  created() {
    this.helpItemId = this.id || this.label.replace(/[^A-Za-z0-9]/g, '') || parseInt(Math.random() * 100000);
    document.addEventListener(`hub-access-help-${this.helpItemId}`, this.openHelpDrawer);
    document.addEventListener(`hub-access-help-tooltip-open-${this.helpItemId}`, this.openHelpTooltip);
    document.addEventListener(`hub-access-help-tooltip-close-${this.helpItemId}`, this.closeHelpTooltip);
  },
  beforeDestroy() {
    document.removeEventListener(`hub-access-help-${this.helpItemId}`, this.openHelpDrawer);
    document.removeEventListener(`hub-access-help-tooltip-open-${this.helpItemId}`, this.openHelpTooltip);
    document.removeEventListener(`hub-access-help-tooltip-close-${this.helpItemId}`, this.closeHelpTooltip);
  },
  methods: {
    openHelpDrawer() {
      this.helpDrawer = true;
    },
    openHelpTooltip(event) {
      this.helpTooltipElement = event?.detail?.element;
      this.$nextTick().then(() => this.helpTooltip = true);
    },
    closeHelpTooltip() {
      this.helpTooltip = false;
      this.$nextTick().then(() => this.helpTooltipElement = null);
    },
  }
};
</script>
