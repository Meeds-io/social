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
  <favorite-button
    :id="spaceId"
    :space-id="spaceId"
    :favorite="favorite"
    :absolute="absolute"
    :top="top"
    :right="right"
    :entity-type="entityType"
    :display-label="displayLabel"
    :extra-class="extraClass"
    :icon-size="iconSize"
    :small="false"
    type="space"
    type-label="space"
    @removed="removed"
    @remove-error="removeError"
    @added="added"
    @add-error="addError" />
</template>

<script>
export default {
  props: {
    spaceId: {
      type: String,
      default: '',
    },
    isFavorite: {
      type: Boolean,
      default: false,
    },
    entityType: {
      type: String,
      default: null,
    },
    absolute: {
      type: Boolean,
      default: false,
    },
    top: {
      type: Number,
      default: () => 0,
    },
    right: {
      type: Number,
      default: () => 0,
    },
    displayLabel: {
      type: Boolean,
      default: false,
    },
    extraClass: {
      type: String,
      default: () => '',
    },
    iconSize: {
      type: Number,
      default: () => 16,
    },
  },
  data: () => ({
    favorite: false
  }),
  created() {
    this.favorite = this.isFavorite === 'true';
  },
  methods: {
    removed() {
      this.displayAlert(this.$t('Favorite.tooltip.SuccessfullyDeletedFavorite', {0: this.$t('spaceList.alert.label')}));
      this.$emit('removed');
      document.dispatchEvent(new CustomEvent('space-favorite-removed', {detail: this.spaceId}));
    },
    removeError() {
      this.displayAlert(this.$t('Favorite.tooltip.ErrorDeletingFavorite', {0: this.$t('spaceList.alert.label')}), 'error');
    },
    added() {
      this.displayAlert(this.$t('Favorite.tooltip.SuccessfullyAddedAsFavorite', {0: this.$t('spaceList.alert.label')}));
      this.$emit('added');
      document.dispatchEvent(new CustomEvent('space-favorite-added', {detail: this.spaceId}));
    },
    addError() {
      this.displayAlert(this.$t('Favorite.tooltip.ErrorAddingAsFavorite', {0: this.$t('spaceList.alert.label')}), 'error');
    },
    displayAlert(message, type) {
      this.$root.$emit('alert-message', message, type || 'success');
    },
  },
};
</script>
