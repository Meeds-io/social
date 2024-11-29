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
  <form ref="form" class="px-5">
    <div class="d-flex flex-column">
      <div class="text-header">
        {{ $t('spacesList.label.accessTitle') }}
      </div>
      <v-radio-group
        v-model="spaceSubscription"
        class="mt-2 mx-0"
        mandatory
        inset>
        <v-radio
          value="open"
          class="mt-0 mb-8">
          <template #label>
            <div class="d-flex flex-column">
              <div class="text-body">
                {{ $t('spacesList.label.open') }}
              </div>
              <div class="text-subtitle position-absolute line-height-normal mt-5">
                {{ $t('spacesList.description.open') }}
              </div>
            </div>
          </template>
        </v-radio>
        <v-radio
          value="validation"
          class="mt-0 mb-8">
          <template #label>
            <div class="d-flex flex-column">
              <div class="text-body">
                {{ $t('spacesList.label.validation') }}
              </div>
              <div class="text-subtitle position-absolute line-height-normal mt-5">
                {{ $t('spacesList.description.validation') }}
              </div>
            </div>
          </template>
        </v-radio>
        <v-radio
          value="closed"
          class="mt-0 mb-6">
          <template #label>
            <div class="d-flex flex-column">
              <div class="text-body">
                {{ $t('spacesList.label.closed') }}
              </div>
              <div class="text-subtitle position-absolute line-height-normal mt-5">
                {{ $t('spacesList.description.closed') }}
              </div>
            </div>
          </template>
        </v-radio>
      </v-radio-group>
    </div>
    <div class="d-flex flex-column">
      <div class="text-header">
        {{ $t('spacesList.label.visibilityTitle') }}
      </div>
      <v-radio-group
        v-model="spaceVisibility"
        class="mt-2 mx-0"
        mandatory
        inset>
        <v-radio
          value="private"
          class="mb-6">
          <template #label>
            <div class="d-flex flex-column">
              <div class="text-body">
                {{ $t('spacesList.label.private') }}
              </div>
              <div class="text-subtitle position-absolute line-height-normal mt-5">
                {{ $t(`spacesList.description.private`) }}
              </div>
            </div>
          </template>
        </v-radio>
        <v-radio
          value="hidden"
          class="mt-0 mb-8">
          <template #label>
            <div class="d-flex flex-column">
              <div class="text-body">
                {{ $t('spacesList.label.hidden') }}
              </div>
              <div class="text-subtitle position-absolute line-height-normal mt-5">
                {{ $t('spacesList.description.hidden') }}
              </div>
            </div>
          </template>
        </v-radio>
      </v-radio-group>
    </div>
  </form>
</template>
<script>
export default {
  props: {
    subscription: {
      type: Object,
      default: null,
    },
    visibility: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    spaceSubscription: null,
    spaceVisibility: null,
  }),
  watch: {
    subscription() {
      this.spaceSubscription = this.subscription;
    },
    visibility() {
      this.spaceVisibility = this.visibility;
    },
    spaceSubscription() {
      this.$emit('subscription', this.spaceSubscription);
    },
    spaceVisibility() {
      this.$emit('visibility', this.spaceVisibility);
    },
  },
  created() {
    this.spaceSubscription = this.subscription;
    this.spaceVisibility = this.visibility;
  }
};
</script>