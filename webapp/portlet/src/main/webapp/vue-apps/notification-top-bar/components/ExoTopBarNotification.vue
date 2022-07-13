<template>
  <v-app id="NotificationPopoverPortlet">
    <v-flex>
      <v-layout>
        <v-btn
          icon
          class="text-xs-center"
          :title="notificationIconTooltip"
          @click="openDrawer()">
          <v-badge
            :value="badge > 0"
            :content="badge"
            flat
            color="var(--allPagesBadgePrimaryColor, #d32a2a)"
            overlap>
            <v-icon class="icon-default-color" size="22">fa-bell</v-icon>
          </v-badge>
        </v-btn>
        <exo-drawer
          ref="drawerNotificationDrawer"
          class="notifDrawer"
          body-classes="hide-scroll"
          right>
          <template slot="title">
            {{ $t('UIIntranetNotificationsPortlet.title.notifications') }}
          </template>
          <template slot="titleIcons">
            <v-btn
              :title="$t('UIIntranetNotificationsPortlet.title.NotificationsSetting')"
              :href="settingsLink"
              icon>
              <v-icon class="uiSettingsIcon notifDrawerSettings" />
            </v-btn>
          </template>
          <template v-if="notificationsSize" slot="content">
            <div class="notifDrawerItems">
              <a
                v-for="(notif, i) in notifications"
                :key="i"
                :id="'notifItem-'+i"
                v-sanitized-html="notif.notification"
                class="notifDrawerItem"
                @mouseenter="applyActions(`notifItem-`+i)">
              </a>
            </div>
          </template>
          <template v-else-if="!loading" slot="content">
            <div class="noNoticationWrapper">
              <div class="noNotificationsContent">
                <i class="uiNoNotifIcon"></i>
                <p>{{ $t('UIIntranetNotificationsPortlet.label.NoNotifications') }}</p>
              </div>
            </div>
          </template>
          <template v-if="notificationsSize" slot="footer">
            <v-row class="notifFooterActions mx-0">
              <v-card 
                flat
                tile 
                class="d-flex flex justify-end mx-2">
                <v-btn 
                  text
                  small
                  class="text-uppercase caption markAllAsRead"
                  color="primary"
                  @click="markAllAsRead()">
                  {{ $t('UIIntranetNotificationsPortlet.label.MarkAllAsRead') }}
                </v-btn>
                <v-btn 
                  :href="allNotificationsLink"
                  class="text-uppercase caption primary--text seeAllNotif"
                  outlined
                  small>
                  {{ $t('UIIntranetNotificationsPortlet.label.seeAll') }}
                </v-btn>
              </v-card>
            </v-row>
          </template>
        </exo-drawer>
      </v-layout>
    </v-flex>
  </v-app>
</template>
<script>
const SLIDE_UP = 300;
const SLIDE_UP_MORE = 600;

export default {
  data () {
    return {
      loading: false,
      drawerNotification: false,
      notifications: [],
      badge: 0,
      notificationsSize: 0,
      settingsLink: `${eXo.env.portal.context}/${eXo.env.portal.portalName}/settings`,
      allNotificationsLink: `${eXo.env.portal.context}/${eXo.env.portal.portalName}/allNotifications`,
      notificationIconTooltip: this.$t('UIIntranetNotificationsPortlet.label.tooltip'),
    };
  },
  watch: {
    badge() {
      return this.badge;
    },
    loading() {
      if (!this.loading && this.$refs.drawerNotificationDrawer) {
        this.$refs.drawerNotificationDrawer.endLoading();
      }
    },
  },
  created() {
    document.addEventListener('cometdNotifEvent', this.notificationUpdated);
    this.getNotifications();
  },
  mounted() {
    if (this.$refs.drawerNotificationDrawer) {
      if (this.loading) {
        this.$refs.drawerNotificationDrawer.startLoading();
      } else {
        this.$refs.drawerNotificationDrawer.endLoading();
      }
    }
    this.openDrawer();
    this.$root.$applicationLoaded();
  },
  methods: {
    getNotifications() {
      this.loading = true;
      return this.$notificationService.getNotifications()
        .then((data) => {
          this.notifications = data.notifications;
          this.badge = data.badge;
          this.notificationsSize = this.notifications.length;
          return this.$nextTick();
        })
        .finally(() => this.loading = false);
    },
    markAllAsRead() {
      return this.$notificationService.updateNotification(null, 'markAllAsRead')
        .then(() => {
          $('.notifDrawerItems').find('li').each(function() {
            if ($(this).hasClass('unread')) {
              $(this).removeClass('unread').addClass('read');
            }
          });
        })
        .finally(() => this.$root.$emit('application-loaded'));
    },
    openDrawer() {
      this.$refs.drawerNotificationDrawer.open();
      return this.$notificationService.updateNotification(null, 'resetNew')
        .then(() => this.badge = 0);
    },
    closeDrawer() {
      this.$refs.drawerNotificationDrawer.close();
      this.$nextTick().then(() => this.$root.$emit('application-loaded'));
    },
    navigateTo(pagelink) {
      location.href=`${ eXo.env.portal.context }/${ eXo.env.portal.portalName }/${ pagelink }` ;
    },
    applyActions(item) {
      $(`#${item}`).find('li').each(function () {
        let dataLink = $(this).find('.contentSmall:first').data('link');
        if (typeof dataLink === 'undefined') {
          dataLink = $(this).find('.media').children('a').attr('href');
          
        }
        if (dataLink.includes('/portal/g/') || dataLink.includes('/portal/rest/')){
          dataLink.replace(/\/portal\//,`${eXo.env.portal.context}/`);
        } else {
          dataLink.replace(/\/portal\/([a-zA-Z0-9_-]+)\//, `${eXo.env.portal.context}/${eXo.env.portal.portalName}/`);
        }
        const linkId = dataLink.split(`${eXo.env.portal.context}/`);
        const dataId = $(this).data('id').toString();
        const dataDetails = $(this).data('details') && $(this).data('details').toString() ? $(this).data('details').toString() : null;
        if (linkId != null && linkId.length >1 ) {
          if (linkId[0].includes('/view_full_activity/')) {
            const id = linkId[0].split('/view_full_activity/')[1];
            $(this).parent().attr('href', `${eXo.env.portal.context}/${eXo.env.portal.portalName}/activity?id=${id}`);
          } else {
            $(this).parent().attr('href', `${eXo.env.portal.context}/${linkId[1]}`);
          }
        } else if (dataDetails !== 'notification-details-drawer') {
          $(this).parent().attr('href', dataLink.replace(/^\/rest\//,`${eXo.env.portal.context}/rest/`));
        }

        // ------------- Open details drawer

        $(this).find('.open-details-drawer').off('click').on('click', function(evt) {
          evt.preventDefault();
          evt.stopPropagation();
          const notificationDetails = $(this).data('notification-details');
          document.dispatchEvent(new CustomEvent('open-notification-details-drawer', {detail: notificationDetails}));
          if ($(this).closest('[data-details]').hasClass('unread')) {
            $(this).closest('[data-details]').removeClass('unread').addClass('read');
          }
          Vue.prototype.$notificationService.updateNotification(dataId, 'markAsRead');
        });

        // ----------------- Mark as read
        $(this).on('click', function() {
          if ($(this).hasClass('unread')) {
            $(this).removeClass('unread').addClass('read');
          }
          Vue.prototype.$notificationService.updateNotification(dataId, 'markAsRead');
        });

        // ------------- hide notif
        $(this).find('.remove-item').off('click')
          .on('click', function(evt) {
            evt.preventDefault();
            evt.stopPropagation();
            Vue.prototype.$notificationService.updateNotification(dataId,'hide');
            $(this).parents('li:first').slideUp(SLIDE_UP);
          });

        // ------------- Accept request

        $(this).find('.action-item').off('click')
          .on('click', function(evt) {
            evt.stopPropagation();
            let restURl = $(this).data('rest');
            if (restURl.indexOf('?') >= 0 ) {
              restURl += '&';
            } 
            else {
              restURl += '?';
            }
            restURl += `portal:csrf=${eXo.env.portal.csrfToken}`;
            if (restURl && restURl.length > 0) {
              $.ajax(restURl).done(function () {
                $(document).trigger('exo-invitation-updated');
              });
            }
            Vue.prototype.$notificationService.updateNotification(dataId,'hide');
            $(this).parents('li:first').slideUp(SLIDE_UP_MORE);
          });

        // ------------- Refuse request

        $(this).find('.cancel-item').off('click')
          .on('click', function(evt) {
            evt.stopPropagation();
            let restCancelURl = $(this).data('rest');
            if (restCancelURl.indexOf('?') >= 0 ) {
              restCancelURl += '&';
            }
            else {
              restCancelURl += '?';
            }
            restCancelURl += `portal:csrf=${eXo.env.portal.csrfToken}`;
            if (restCancelURl && restCancelURl.length > 0) {
              $.ajax(restCancelURl).done(function () {
                $(document).trigger('exo-invitation-updated');
              });
            }
            Vue.prototype.$notificationService.updateNotification(dataId,'hide');
            $(this).parents('li:first').slideUp(SLIDE_UP_MORE);
          });
      });
    },
    notificationUpdated(event) {
      if (event && event.detail) {
        this.badge = event.detail.data.numberOnBadge;
        this.getNotifications();
      }
    },
  }
};
</script>
