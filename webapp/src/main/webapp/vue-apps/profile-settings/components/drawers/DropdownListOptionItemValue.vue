<!--
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2025 Meeds Association contact@meeds.io
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
  <v-row class="border-bottom-color">
    <v-col
      class="d-flex py-1 px-0"
      cols="8">
      <span class="my-auto">
        {{ displayedValue }}
      </span>
    </v-col>
    <v-col
      class="text-end py-1 px-0"
      cols="4">
      <div class="d-flex ms-auto width-fit-content">
        <translation-text-field
          :ref="`option${option?.id || option.uuid}`"
          back-icon
          button
          :drawer-title="$t('profileSettings.dropdownList.translation.title')"
          :field-name="fieldName"
          :field-value="displayedValue"
          no-expand-icon
          :object-id="option.id"
          :object-type="objectType"
          required
          :save="option?.id"
          @input="translationUpdated"
          @update:field-value="updateFieldValue" />
        <property-option-action-menu
          @delete="deleteOption"
          @edit="editOption" />
      </div>
    </v-col>
  </v-row>
</template>

<script>
  export default {
    props: {
      option: {
        type: Object,
        default: null,
      },
    },
    data () {
      return {
        userLocale: eXo.env.portal.language,
        objectType: 'propertySettingOption',
        fieldName: 'optionValue',
        defaultLangValue: null,
        optionObject: null,
      };
    },
    computed: {
      displayedValue () {
        return this.optionObject?.translations?.[this.userLocale] || this.defaultLangValue
          || this.optionObject.value;
      },
    },
    watch: {
      option () {
        this.cloneOptionObject();
      },
    },
    created () {
      this.getSavedTranslations();
      this.cloneOptionObject();
    },
    methods: {
      cloneOptionObject () {
        this.optionObject = structuredClone(this.option);
      },
      getSavedTranslations () {
        if (this.option?.id) {
          eXo.$translationService.getTranslations(this.objectType, this.option.id, this.fieldName).then(translations => {
            this.$emit('data-translations', this.option, translations);
          });
        }
      },
      translationUpdated (translations) {
        this.optionObject = { ...this.optionObject, translations };
        this.$emit('translation-updated', this.option, translations);
      },
      updateFieldValue (value) {
        this.defaultLangValue = value;
      },
      editOption () {
        this.$refs?.[`option${this.option.id || this.option.uuid}`]?.openDrawer();
      },
      deleteOption () {
        this.$emit('delete-option', this.option);
      },
    },
  };
</script>
