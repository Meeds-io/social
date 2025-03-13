<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io

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
  <exo-drawer
    ref="drawer"
    v-model="drawer"
    :allow-expand="!$root.isMobile"
    class="linkSettingDrawer"
    :confirm-close="modified"
    :confirm-close-labels="confirmCloseLabels"
    eager
    :loading="loading || saving"
    :right="!$vuetify.rtl"
    @closed="reset"
    @expand-updated="expanded = $event"
    @opened="stepper = 1">
    <template #title>
      {{ $t('links.drawer.title') }}
    </template>
    <template
      v-if="settings"
      #content>
      <v-stepper
        v-model="stepper"
        class="ma-0 pa-4 d-flex"
        :class="expanded && 'flex-row' || 'flex-column'"
        flat
        vertical>
        <div
          class="flex-shrink-0"
          :class="{
            'col-6': expanded,
            'flex-grow-1': expanded || stepper === 1,
            'flex-grow-0': !expanded && stepper !== 1,
          }">
          <v-stepper-step
            class="ma-0 pa-0 position-relative"
            :editable="!expanded"
            :step="1"
            width="100%">
            <div class="d-flex">
              <div class="d-flex align-center flex-grow-1 flex-shrink-1 text-truncate text-header">
                {{ $t('links.label.addLinks') }}
              </div>
              <v-btn
                v-if="(stepper === 1 || expanded) && hasLinks"
                class="position-absolute t-0 mt-n1"
                :class="{
                  'r-0': !$vuetify.rtl,
                  'l-0': $vuetify.rtl,
                }"
                icon
                :title="$t('links.label.addLinks')"
                @click="$root.$emit('links-form-drawer')">
                <v-icon size="20">
                  fa-plus
                </v-icon>
              </v-btn>
            </div>
          </v-stepper-step>
          <v-slide-y-transition>
            <div v-show="expanded || stepper === 1">
              <div
                v-if="hasLinks"
                class="d-flex flex-column mt-8">
                <v-scroll-y-transition
                  v-for="(link, index) in links"
                  :key="`${link.id}-${link.url}-${index}`"
                  hide-on-leave>
                  <div
                    :key="`${link.id}-${link.url}-${index}`"
                    class="mb-2">
                    <links-input
                      :first="index === 0"
                      :last="index === (links.length - 1)"
                      :link="link"
                      @edit="edit(link, index)"
                      @move-down="moveDown(index)"
                      @move-top="moveTop(index)"
                      @remove="remove(index)" />
                  </div>
                </v-scroll-y-transition>
              </div>
              <div
                v-else
                class="d-flex flex-grow-1 full-height full-width align-center justify-center">
                <v-btn
                  border
                  class="flex-grow-0 flex-shrink-0 mx-auto my-8 primary"
                  outlined
                  :title="$t('links.label.addLinksButton')"
                  @click="$root.$emit('links-form-drawer')">
                  {{ $t('links.label.add') }}
                </v-btn>
              </div>
            </div>
          </v-slide-y-transition>
        </div>
        <div
          class="flex-grow-0 flex-shrink-0"
          :class="{
            'col-6': expanded,
            'mt-8': !expanded && stepper < 2,
            'mt-4': !expanded && stepper === 2,
          }">
          <v-stepper-step
            class="ma-0 pa-0 position-relative"
            :editable="!expanded && !disabledSecondStep"
            :step="2">
            <div class="d-flex align-center flex-grow-1 flex-shrink-1 text-truncate text-header">
              {{ $t('links.label.configureDisplay') }}
            </div>
          </v-stepper-step>
          <v-slide-y-transition>
            <div
              v-show="expanded || stepper > 1"
              class="mt-4">
              <div class="d-flex flex-column">
                <v-form
                  ref="form"
                  autocomplete="off"
                  @submit.prevent.stop="0">
                  <div class="d-flex flex-column">
                    <select
                      id="linkSettingDisplayType"
                      v-model="settings.type"
                      class="flex-grow-1 ignore-vuetify-classes py-2 mb-4 height-auto width-auto text-truncate">
                      <option
                        v-for="item in displayTypes"
                        :key="item.value"
                        :value="item.value">
                        {{ item.label }}
                      </option>
                    </select>
                    <links-display-preview
                      class="pa-4 mb-4"
                      :links="links"
                      :settings="settings" />
                    <div class="mb-2">
                      <div class="d-flex mb-2">
                        <div class="d-flex align-center flex-grow-1 flex-shrink-1 text-truncate text-color">
                          {{ $t('links.label.addHeader') }}
                        </div>
                        <v-switch
                          v-model="showHeader"
                          class="my-0 me-n2"
                          dense
                          hide-details />
                      </div>
                      <div
                        v-if="showHeader && settings?.header"
                        class="d-flex mb-2">
                        <translation-text-field
                          id="linksHeader"
                          ref="linksHeader"
                          v-model="header"
                          autofocus
                          back-icon
                          class="width-auto flex-grow-1"
                          drawer-title="links.label.headerTranslation"
                          :maxlength="maxHeaderLength"
                          no-expand-icon
                          :placeholder="$t('links.label.headerPlaceHolder')"
                          required
                          :rules="rules.header" />
                      </div>
                    </div>
                    <div class="d-flex mb-4">
                      <div class="d-flex align-center flex-grow-1 flex-shrink-1 text-truncate text-color">
                        {{ $t('links.label.showIcon') }}
                      </div>
                      <v-switch
                        v-model="settings.showIcon"
                        class="my-0 me-n2"
                        dense
                        hide-details />
                    </div>
                    <div
                      v-if="settings.showIcon"
                      class="d-flex mb-4">
                      <div class="d-flex align-center flex-grow-1 flex-shrink-1 text-truncate text-color">
                        {{ $t('links.label.iconsSize') }}
                      </div>
                      <number-input
                        v-model="settings.iconSize"
                        class="ms-auto my-n2"
                        :max="60"
                        :min="12"
                        :step="1" />
                    </div>
                    <div class="d-flex mb-4">
                      <div class="d-flex align-center flex-grow-1 flex-shrink-1 text-truncate text-color">
                        {{ $t('links.label.showName') }}
                      </div>
                      <v-switch
                        v-model="settings.showName"
                        class="my-0 me-n2"
                        dense
                        hide-details />
                    </div>
                    <div
                      v-if="settings?.type === 'COLUMN'"
                      class="d-flex mb-4">
                      <div class="d-flex align-center flex-grow-1 flex-shrink-1 text-truncate text-color">
                        {{ $t('links.label.showDescription') }}
                      </div>
                      <v-switch
                        v-model="settings.showDescription"
                        class="my-0 me-n2"
                        dense
                        hide-details />
                    </div>
                    <div class="mb-2">
                      <div class="d-flex mb-2">
                        <div class="d-flex align-center flex-grow-1 flex-shrink-1 text-truncate text-color">
                          {{ $t('links.label.addSeeMore') }}
                        </div>
                        <v-switch
                          v-model="seeMore"
                          class="my-0 me-n2"
                          dense
                          hide-details />
                      </div>
                      <div
                        v-if="seeMore"
                        class="mb-2">
                        <v-text-field
                          id="seeMoreInput"
                          ref="seeMoreInput"
                          v-model="settings.seeMore"
                          class="border-box-sizing width-auto pt-0"
                          dense
                          name="seeMoreInput"
                          outlined
                          :placeholder="$t('links.label.enterUrl')"
                          :rules="rules.seeMore"
                          type="text" />
                      </div>
                    </div>
                    <div class="d-flex flex-column mb-4">
                      <div class="d-flex align-center flex-grow-1 flex-shrink-1 text-truncate text-color mb-2">
                        {{ $t('links.label.align') }}
                      </div>
                      <div class="d-flex">
                        <div class="col-6 pa-0">
                          <v-radio-group v-model="settings.hAlign">
                            <v-radio
                              class="ma-0 pa-0"
                              value="START">
                              <template #label>
                                <span class="text-body">{{ $t('links.label.left') }}</span>
                              </template>
                            </v-radio>
                            <v-radio
                              class="ma-0 pa-0"
                              value="CENTER">
                              <template #label>
                                <span class="text-body">{{ $t('links.label.center') }}</span>
                              </template>
                            </v-radio>
                            <v-radio
                              class="ma-0 pa-0"
                              value="END">
                              <template #label>
                                <span class="text-body">{{ $t('links.label.right') }}</span>
                              </template>
                            </v-radio>
                          </v-radio-group>
                        </div>
                        <div class="col-6 pa-0">
                          <v-radio-group v-model="settings.vAlign">
                            <v-radio
                              class="ma-0 pa-0"
                              value="START">
                              <template #label>
                                <span class="text-body">{{ $t('links.label.top') }}</span>
                              </template>
                            </v-radio>
                            <v-radio
                              class="ma-0 pa-0"
                              value="CENTER">
                              <template #label>
                                <span class="text-body">{{ $t('links.label.middle') }}</span>
                              </template>
                            </v-radio>
                            <v-radio
                              class="ma-0 pa-0"
                              value="END">
                              <template #label>
                                <span class="text-body">{{ $t('links.label.bottom') }}</span>
                              </template>
                            </v-radio>
                          </v-radio-group>
                        </div>
                      </div>
                    </div>
                  </div>
                </v-form>
              </div>
            </div>
          </v-slide-y-transition>
        </div>
      </v-stepper>

      <links-form-drawer
        @link-add="addLink"
        @link-edit="editLink" />
    </template>
    <template #footer>
      <div class="d-flex align-center">
        <v-btn
          v-if="!expanded && stepper > 1"
          class="btn me-2 hidden-xs-only"
          :disabled="saving"
          :title="$t('links.label.previous')"
          @click="stepper--">
          {{ $t('links.label.previous') }}
        </v-btn>
        <v-btn
          class="btn ms-auto me-2"
          :title="$t('links.label.cancel')"
          @click="close()">
          {{ $t('links.label.cancel') }}
        </v-btn>
        <v-btn
          v-if="!expanded && stepper === 1"
          class="btn primary"
          :disabled="disabledSecondStep"
          :loading="saving"
          @click="stepper++">
          {{ $t('links.label.next') }}
        </v-btn>
        <v-btn
          v-else-if="expanded || stepper > 1"
          class="btn primary"
          :disabled="disabled"
          :loading="saving"
          @click="save()">
          {{ $t('links.label.confirm') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
  export default {
    data: () => ({
      settings: null,
      originalSettings: null,
      links: null,
      loading: false,
      saving: false,
      drawer: false,
      expanded: false,
      stepper: 1,
      showHeader: false,
      seeMore: false,
      valid: false,
      maxHeaderLength: 50,
      settingsHeader: null,
    }),
    computed: {
      displayTypes () {
        return [{
          value: 'ROW',
          label: this.$t('links.label.configureDisplay.row'),
        }, {
          value: 'COLUMN',
          label: this.$t('links.label.configureDisplay.column'),
        }, {
          value: 'CARD',
          label: this.$t('links.label.configureDisplay.cards'),
        }];
      },
      hasLinks () {
        return this.links?.length;
      },
      hadLinks () {
        return this.originalSettings?.links?.length;
      },
      disabledSecondStep () {
        return !this.hasLinks && !this.hadLinks;
      },
      modified () {
        return this.originalSettings && JSON.stringify(this.originalSettings) !== JSON.stringify(this.settings && { ...this.settings, links: this.links } || {});
      },
      disabled () {
        return !this.valid || !this.modified || (this.showHeader && !this.settings?.header?.[this.$root.defaultLanguage]?.length);
      },
      confirmCloseLabels () {
        return {
          title: this.$t('links.title.confirmCloseModification'),
          message: this.$t('links.message.confirmCloseModification'),
          ok: this.$t('confirm.yes'),
          cancel: this.$t('confirm.no'),
        };
      },
      rules () {
        return {
          header: [
            v => !!v?.length || ' ',
            v => (v && v.length < this.maxHeaderLength) || this.$t('links.input.exceedsMaxLength', {
              0: this.maxHeaderLength,
            }),
          ],
          seeMore: [
            v => !!v?.length || ' ',
            v => {
              try {
                return !!eXo.$utils.toLinkUrl(v, {
                  urls: true,
                  email: true,
                  phone: true,
                })?.length || this.$t('links.input.invalidLink');
              } catch (e) {
                return this.$t('links.input.invalidLink');
              }
            },
          ],
        };
      },
      showIcon () {
        return this.settings?.showIcon || false;
      },
      header () {
        return this.settings?.header || '';
      },
    },
    watch: {
      showHeader () {
        if (this.loading) {
          return;
        }
        if (this.showHeader) {
          if (!this.settings.header?.[this.$root.defaultLanguage]) {
            this.settings.header = this.settingsHeader || {};
            if (!this.settingsHeader) {
              this.settings.header[this.$root.defaultLanguage] = '';
            }
            this.settingsHeader = null;
          }
        } else {
          this.settingsHeader = this.settings.header;
          this.settings.header = null;
        }
        this.refreshValidation();
      },
      seeMore () {
        if (!this.seeMore) {
          this.settings.seeMore = null;
        }
        this.refreshValidation();
      },
      settings: {
        deep: true,
        handler () {
          this.refreshValidation();
        },
      },
      showIcon () {
        if (this.settings && !this.settings.iconSize) {
          this.settings.iconSize = 34;
        }
      },
      expanded () {
        if (this.expanded) {
          this.stepper = 2;
        } else {
          this.stepper = 1;
        }
      },
      header () {
        if (this.header) {
          if (this.$root.language) {
            this.header[this.$root.language] = this.$t(this.settings?.header?.[this.$root.language]);
          } else {
            this.header[this.$root.defaultLanguage] = this.$t(this.settings?.header?.[this.$root.defaultLanguage]);
          }
        }
      },
    },
    created () {
      this.$root.$on('links-settings-drawer', this.open);
    },
    beforeUnmount () {
      this.$root.$off('links-settings-drawer', this.open);
    },
    mounted () {
      document.querySelector('#vuetify-apps').appendChild(this.$el);
    },
    methods: {
      open (openForm) {
        this.reset();
        this.loading = true;
        this.$nextTick().then(() => this.$refs?.drawer?.open?.());
        eXo.$linkService.getSettings(this.$root.name)
          .then(settings => {
            if (settings.showIcon !== false) {
              settings.showIcon = true;
            }
            if (settings.showIcon !== false) {
              settings.showIcon = true;
            }
            this.settings = settings;
            this.links = settings?.links || [];
            this.links.forEach(link => {
              if (!link.name?.[this.$root.defaultLanguage]) {
                link.name[this.$root.defaultLanguage] = link.name['en'] || '';
              }
              if (!link.description?.[this.$root.defaultLanguage]) {
                link.description[this.$root.defaultLanguage] = link.description['en'] || '';
              }
            });
            this.showHeader = !!this.settings?.header?.[this.$root.defaultLanguage]?.length;
            this.seeMore = !!this.settings?.seeMore?.length;
            if (!this.showHeader) {
              this.settings.header = null;
            }
            this.originalSettings = settings && JSON.parse(JSON.stringify({ ...settings, links: this.links }));
          })
          .finally(() => {
            this.loading = false;
            if (openForm) {
              window.setTimeout(() => {
                this.$nextTick().then(() => {
                  this.$root.$emit('links-form-drawer');
                });
              }, 200);
            }
          });
      },
      reset () {
        this.settings = null;
        this.originalSettings = null;
        this.links = null;
        this.loading = false;
        this.saving = false;
        this.valid = false;
      },
      close () {
        this.originalSettings = null;
        return this.$nextTick().then(() => this.$refs.drawer.close());
      },
      refreshValidation () {
        this.$nextTick().then(() => {
          if (this.$refs.form) {
            this.valid = this.$refs.form.validate();
            if (!this.modified) {
              this.$refs.form.resetValidation();
            }
          }
        });
      },
      save () {
        this.saving = true;
        const settings = {
          ...this.settings,
        };
        this.links.forEach((link, index) => link.order = index);
        settings.links = this.links && JSON.parse(JSON.stringify(this.links)) || [];
        settings.links.forEach(l => delete l.iconSrc);
        eXo.$linkService.saveSettings(settings)
          .then(() => {
            this.$root.$emit('links-refresh');
            this.close();
            this.$root.$emit('alert-message', this.$t('links.label.savedSuccessfully'), 'success');
          })
          .catch(() => this.$root.$emit('alert-message', this.$t('links.label.errorSaving'), 'error'))
          .finally(() => this.saving = false);
      },
      moveTop (index) {
        const links = this.links.slice();
        const linkTmp = links[index - 1];
        links[index - 1] = links[index];
        links[index] = linkTmp;
        this.links = links;
      },
      moveDown (index) {
        const links = this.links.slice();
        const linkTmp = links[index + 1];
        links[index + 1] = links[index];
        links[index] = linkTmp;
        this.links = links;
      },
      edit (link, index) {
        this.$root.$emit('links-form-drawer', link, true, index);
      },
      remove (index) {
        this.links.splice(index, 1);
      },
      addLink (link) {
        this.links.push(link);
      },
      editLink (link, index) {
        this.links.splice(index, 1, link);
        this.links = this.links.slice();
      },
    },
  };
</script>
