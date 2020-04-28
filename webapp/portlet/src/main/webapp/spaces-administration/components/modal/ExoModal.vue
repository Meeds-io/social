<!--
This file is part of the Meeds project (https://meeds.io/).
Copyright (C) 2020 Meeds Association
contact@meeds.io
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
  <div class="modal-mask uiPopupWrapper">
    <div :class="modalClass" class="uiPopup modal-content">
      <div class="popupHeader">
        <span class="PopupTitle popupTitle">{{ title }}</span>
        <a v-if="displayClose" class="uiIconClose pull-right" @click="closeModal"></a>
        <v-btn
          v-else
          icon
          class="closeBindingModal"
          @click="closeModal">
          <v-icon
            large
            color="#a8b3c5"
            class="closeIcon">
            close
          </v-icon>
        </v-btn>
      </div>
      <div class="PopupContent popupContent">
        <slot></slot>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    displayClose: {
      type: Boolean,
      default: true
    },
    title: {
      type: String,
      default: ''
    },
    modalClass: {
      type: String,
      default: ''
    }
  },
  methods: {
    closeModal() {
      // Emit the click event of close icon
      this.$emit('modal-closed');
    }
  }
};
</script>


