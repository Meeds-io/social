<template>
  <div>
    <v-list-item two-line>
      <v-list-item-content>
        <v-list-item-title class="text-color">
          <div :class="skeleton && 'skeleton-background skeleton-border-radius skeleton-text-width skeleton-text-height my-2'">
            {{ skeleton && '&nbsp;' || label }}
          </div>
        </v-list-item-title>
        <v-list-item-subtitle v-if="skeleton || description" class="text-sub-title">
          <div :class="skeleton && 'skeleton-background skeleton-border-radius skeleton-text-width-small skeleton-text-height-fine my-2'">
            {{ skeleton && '&nbsp;' || description }}
          </div>
        </v-list-item-subtitle>
      </v-list-item-content>
      <v-list-item-action>
        <v-switch
          v-model="active"
          :class="skeleton && 'skeleton-background skeleton-child-hidden skeleton-border-radius-rounded'"
          :inset="skeleton"
          :loading="saving"
          @change="save"/>
      </v-list-item-action>
    </v-list-item>
    <v-divider class="mx-4" />
  </div>
</template>

<script>
export default {
  props: {
    channel: {
      type: String,
      default: null,
    },
    active: {
      type: Boolean,
      default: null,
    },
    settings: {
      type: Object,
      default: null,
    },
    skeleton: {
      type: Boolean,
      default: null,
    },
  },
  data: () => ({
    saving: false,
  }),
  computed: {
    label() {
      return this.settings && this.settings.channelLabels && this.settings.channelLabels[this.channel];
    },
    description() {
      return this.settings && this.settings.channelDescriptions && this.settings.channelDescriptions[this.channel];
    },
  },
  methods: {
    save() {
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/notifications/settings/${eXo.env.portal.userName}/channel/${this.channel}`, {
        method: 'PATCH',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `enable=${this.active}`
      }).then(resp => {
        if (resp && resp.ok) {
          this.$root.$emit('refresh');
        }
      })
        .finally(() => {
          this.saving = false;
        });
    },
  },
};
</script>

