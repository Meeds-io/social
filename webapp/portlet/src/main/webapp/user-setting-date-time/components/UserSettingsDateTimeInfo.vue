<template>
  <div>
    <v-list-item two-line>
      <v-list-item-content>
        <v-list-item-title class="text-color">
          <div
            :class="skeleton && 'skeleton-background skeleton-border-radius skeleton-text-width skeleton-text-height my-2'">
            {{ skeleton && '&nbsp;' || $t('UserSettings.dateTime.timeZone') }}
          </div>
        </v-list-item-title>
        <v-list-item-subtitle v-if="skeleton || timezoneLabel" class="text-sub-title font-italic">
          <div
            :class="skeleton && 'skeleton-background skeleton-border-radius skeleton-text-width-small skeleton-text-height-fine my-2'">
            {{ skeleton && '&nbsp;' || timezoneLabel }}
          </div>
        </v-list-item-subtitle>
      </v-list-item-content>
    </v-list-item>
    <v-divider class="mx-4"/>
    <v-list-item two-line>
      <v-list-item-content>
        <v-list-item-title class="text-color">
          <div
            :class="skeleton && 'skeleton-background skeleton-border-radius skeleton-text-width skeleton-text-height my-2'">
            {{ skeleton && '&nbsp;' || $t('UserSettings.dateTime.dateFormat') }}
          </div>
        </v-list-item-title>
        <v-list-item-subtitle v-if="skeleton || dateFormatLabel" class="text-sub-title font-italic">
          <div
            :class="skeleton && 'skeleton-background skeleton-border-radius skeleton-text-width-small skeleton-text-height-fine my-2'">
            {{ skeleton && '&nbsp;' || dateFormatLabel }}
          </div>
        </v-list-item-subtitle>
      </v-list-item-content>
    </v-list-item>
    <v-divider class="mx-4"/>
    <v-list-item two-line>
      <v-list-item-content>
        <v-list-item-title class="text-color">
          <div
            :class="skeleton && 'skeleton-background skeleton-border-radius skeleton-text-width skeleton-text-height my-2'">
            {{ skeleton && '&nbsp;' || $t('UserSettings.dateTime.timeFormat') }}
          </div>
        </v-list-item-title>
        <v-list-item-subtitle v-if="skeleton || timeFormatLabel" class="text-sub-title font-italic">
          <div
            :class="skeleton && 'skeleton-background skeleton-border-radius skeleton-text-width-small skeleton-text-height-fine my-2'">
            {{ skeleton && '&nbsp;' || timeFormatLabel }}
          </div>
        </v-list-item-subtitle>
      </v-list-item-content>
    </v-list-item>
  </div>
</template>

<script>
export default {
  props: {
    setting: {
      type: Object,
      default: null,
    },
    skeleton: {
      type: Boolean,
      default: null,
    },
    timezoneLabel: {
      type: String,
      default: null,
    },
    dateFormatLabel: {

      type: String,
      default: null,
    },
    timeFormatLabel: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    saving: false,
  }),
  /*computed: {
    label() {
      return this.setting && this.setting.channelLabels && this.setting.channelLabels[this.channel];
    },
    description() {
      return this.setting && this.setting.channelDescriptions && this.setting.channelDescriptions[this.channel];
    },
  },*/
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

