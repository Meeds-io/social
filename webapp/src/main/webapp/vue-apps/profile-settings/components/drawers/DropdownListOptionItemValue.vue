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
    {{ userLangOptionValue }}
    <v-col
      cols="8"
      class="d-flex py-1 px-0">
      <span class="my-auto">
        {{ displayedValue }}
      </span>
    </v-col>
    <v-col
      cols="4"
      class="text-end py-1 px-0">
      <div class="d-flex ms-auto width-fit-content">
        <translation-text-field
          :ref="`option${option?.id || option.uuid}`"
          :object-id="option.id"
          :object-type="objectType"
          :save="option?.id"
          :field-name="fieldName"
          :field-value="displayedValue"
          :drawer-title="$t('profileSettings.dropdownList.translation.title')"
          no-expand-icon
          back-icon
          button
          required
          @update:field-value="updateFieldValue"
          @input="translationUpdated" />
        <property-option-action-menu
          @edit="editOption"
          @delete="deleteOption" />
      </div>
    </v-col>
  </v-row>
</template>

<script>
export default {
  data() {
    return {
      userLocale: eXo.env.portal.language,
      objectType: 'propertySettingOption',
      fieldName: 'optionValue',
      defaultLangValue: null,
      optionObject: null
    };
  },
  props: {
    option: {
      type: Object,
      default: null
    }
  },
  computed: {
    displayedValue() {
      return this.optionObject?.translations?.[this.userLocale] || this.defaultLangValue
                                                                || this.optionObject.value;
    }
  },
  created() {
    this.getSavedTranslations();
    this.cloneOptionObject();
  },
  watch: {
    option() {
      this.cloneOptionObject();
    }
  },
  methods: {
    cloneOptionObject() {
      this.optionObject = structuredClone(this.option);
    },
    getSavedTranslations() {
      if (this.option?.id) {
        this.$translationService.getTranslations(this.objectType, this.option.id, this.fieldName).then(translations => {
          this.$emit('data-translations', this.option, translations);
        });
      }
    },
    translationUpdated(translations) {
      this.optionObject = {...this.optionObject, translations};
      this.$emit('translation-updated', this.option, translations);
    },
    updateFieldValue(value) {
      this.defaultLangValue = value;
    },
    editOption() {
      this.$refs?.[`option${this.option.id || this.option.uuid}`]?.openDrawer();
    },
    deleteOption() {
      //
    }
  }
};
</script>
