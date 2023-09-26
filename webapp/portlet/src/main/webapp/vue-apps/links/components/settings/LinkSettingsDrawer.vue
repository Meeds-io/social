<template>
  <exo-drawer
    ref="drawer"
    :loading="loading || saving"
    class="linkSettingDrawer"
    right
    @closed="reset">
    <template slot="title">
      {{ $t('links.drawer.title') }}
    </template>
    <template v-if="settings" #content>
      <div class="d-flex flex-column pa-4">
        <div class="d-flex mb-2">
          <div class="d-flex align-center flex-grow-1 flex-shrink-1 text-truncate text-color">
            {{ $t('links.label.configureDisplay') }}
          </div>
          <v-tooltip bottom>
            <template #activator="{bind, on}">
              <v-btn
                class="flex-grow-0 flex-shrink-0"
                icon
                v-on="on"
                v-bind="bind"
                @click="$root.$emit('links-advanced-settings-drawer')">
                <v-icon size="20">fa-sliders-h</v-icon>
              </v-btn>
            </template>
            <span>{{ $t('links.label.openAdvancedSettings') }}</span>
          </v-tooltip>
        </div>

        <select
          id="linkSettingDisplayType"
          v-model="settings.type"
          class="flex-grow-1 ignore-vuetify-classes py-2 mb-4 height-auto width-auto text-truncate"
          @change="$emit('filter-select-change', select)">
          <option
            v-for="item in displayTypes"
            :key="item.value"
            :value="item.value">
            {{ item.label }}
          </option>
        </select>

        <links-display-preview
          :settings="settings"
          :links="links"
          class="mb-4" />

        <div class="d-flex mb-2">
          <div class="d-flex align-center flex-grow-1 flex-shrink-1 text-truncate text-color">
            {{ $t('links.label.addLinks') }}
          </div>
          <v-tooltip v-if="hasLinks" bottom>
            <template #activator="{bind, on}">
              <v-btn
                class="flex-grow-0 flex-shrink-0"
                icon
                v-on="on"
                v-bind="bind"
                @click="$root.$emit('links-form-drawer')">
                <v-icon size="20">fa-plus</v-icon>
              </v-btn>
            </template>
            <span>{{ $t('links.label.addLinks') }}</span>
          </v-tooltip>
        </div>

        <div v-if="hasLinks" class="d-flex flex-column">
          <v-scroll-y-transition
            v-for="(link, index) in links"
            :key="link.url"
            hide-on-leave>
            <div
              :key="link.url"
              class="mb-2">
              <links-input
                :link="link"
                :first="index === 0"
                :last="index === (links.length - 1)"
                @move-top="moveTop(index)"
                @move-down="moveDown(index)"
                @edit="edit(link, index)"
                @remove="remove(index)" />
            </div>
          </v-scroll-y-transition>
        </div>
        <v-btn
          v-else
          :title="$t('links.label.addLinksButton')"
          class="flex-grow-0 flex-shrink-0 mx-auto btn primary"
          @click="$root.$emit('links-form-drawer')">
          {{ $t('links.label.add') }}
        </v-btn>
      </div>
      <links-advanced-settings-drawer
        v-model="settings"
        :links="links" />
      <links-form-drawer
        @link-add="addLink"
        @link-edit="editLink" />
    </template>
    <template #footer>
      <div class="d-flex align-center justify-end">
        <v-btn
          class="btn me-2"
          @click="close()">
          {{ $t('links.label.cancel') }}
        </v-btn>
        <v-btn
          :loading="saving"
          class="btn primary"
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
    links: null,
    loading: false,
    saving: false,
  }),
  computed: {
    displayTypes() {
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
    hasLinks() {
      return this.links?.length;
    },
  },
  created() {
    this.$root.$on('links-settings-drawer', this.open);
  },
  methods: {
    open() {
      this.reset();
      this.loading = true;
      this.$nextTick().then(() => this.$refs.drawer.open());
      this.$linkService.getSettings(this.$root.name)
        .then(settings => {
          this.settings = settings;
          this.links = settings?.links || [];
        })
        .finally(() => this.loading = false);
    },
    reset() {
      this.settings = null;
      this.links = null;
      this.loading = false;
      this.saving = false;
    },
    close() {
      this.$refs.drawer.close();
    },
    save() {
      this.saving = true;
      const settings = {
        ...this.settings
      };
      this.links.forEach((link, index) => link.order = index);
      settings.links = this.links;
      this.$linkService.saveSettings(settings)  
        .then(() => {
          this.close();
          this.$root.$emit('links-refresh', settings);
          this.$root.$emit('alert-message', this.$t('links.label.savedSuccessfully'), 'success');
        })
        .catch(() => this.$root.$emit('alert-message', this.$t('links.label.errorSaving'), 'error'))
        .finally(() => this.saving = false);
    },
    moveTop(index) {
      const links = this.links.slice();
      const linkTmp = links[index - 1];
      links[index - 1] = links[index];
      links[index] = linkTmp;
      this.links = links;
    },
    moveDown(index) {
      const links = this.links.slice();
      const linkTmp = links[index + 1];
      links[index + 1] = links[index];
      links[index] = linkTmp;
      this.links = links;
    },
    edit(link, index) {
      this.$root.$emit('links-form-drawer', link, true, index);
    },
    remove(index) {
      this.links.splice(index, 1);
    },
    addLink(link) {
      this.links.push(link);
    },
    editLink(link, index) {
      this.links.splice(index, 1, link);
    },
  },
};
</script>