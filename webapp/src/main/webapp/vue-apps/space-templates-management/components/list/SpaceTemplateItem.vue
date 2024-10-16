<template>
  <tr>
    <!-- name -->
    <td
      :width="$root.isMobile && '100%' || 'auto'"
      class="px-0">
      <div class="d-flex align-center">
        <v-icon>{{ icon }}</v-icon>
        <div v-sanitized-html="name" class="text-truncate"></div>
      </div>
    </td>
    <!-- description -->
    <td
      v-if="!$root.isMobile"
      v-sanitized-html="description"></td>
    <td
      v-if="!$root.isMobile"
      class="text-truncate text-center"
      width="120px">
      {{ spacesCount }}
    </td>
    <td
      v-if="!$root.isMobile"
      class="text-center"
      width="50px">
      <v-switch
        v-model="enabled"
        :loading="loading"
        :aria-label="enabled && $t('spaceTemplate.label.disableTemplate') || $t('spaceTemplate.label.enableTemplate')"
        class="mt-0 mx-auto"
        @click="changeStatus" />
    </td>
    <td
      class="text-center"
      width="50px">
      <space-templates-management-item-menu :space-template="spaceTemplate" />
    </td>
  </tr>
</template>
<script>
export default {
  props: {
    spaceTemplate: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    menu: false,
    hoverMenu: false,
  }),
  computed: {
    spaceTemplateId() {
      return this.spaceTemplate?.id;
    },
    enabled() {
      return this.spaceTemplate?.enabled;
    },
    name() {
      return this.$te(this.spaceTemplate?.name) ? this.$t(this.spaceTemplate?.name) : this.spaceTemplate?.name;
    },
    description() {
      return this.$te(this.spaceTemplate?.description) ? this.$t(this.spaceTemplate?.description) : this.spaceTemplate?.description;
    },
    icon() {
      return this.spaceTemplate?.icon;
    },
    spacesCount() {
      return this.$root.spacesCountByTemplates?.[this.spaceTemplate?.id] || 0;
    },
  },
  watch: {
    hoverMenu() {
      if (!this.hoverMenu) {
        window.setTimeout(() => {
          if (!this.hoverMenu) {
            this.menu = false;
          }
        }, 200);
      }
    },
  },
  methods: {
    changeStatus() {
      this.$root.$emit('close-alert-message');
      this.loading = true;
      this.$spaceTemplateService.getSpaceTemplate(this.spaceTemplate.id)
        .then(spaceTemplate => {
          spaceTemplate.enabled = !this.enabled;
          return this.$spaceTemplateService.updateSpaceTemplate(spaceTemplate)
            .then(() => {
              this.$root.$emit(`space-templates-${this.enabled && 'disabled' || 'enabled'}`, spaceTemplate);
            });
        })
        .then(() => {
          this.$root.$emit('alert-message', this.$t('spaceTemplate.status.update.success'), 'success');
        })
        .catch(() => this.$root.$emit('alert-message', this.$t('spaceTemplate.status.update.error'), 'error'))
        .finally(() => this.loading = false);
    },
  },
};
</script>