<template>
  <v-app id="NotificationPopoverPortlet">
    <v-flex>
      <v-layout>
        <v-btn
          icon
          class="text-xs-center"
          @click="openDrawer()">
          <v-badge
            :value="badge > 0"
            :content="badge"
            flat
            color="var(--allPagesBadgePrimaryColor, #ff5335)"
            overlap>
            <v-icon
              class="grey-color">
              notifications
            </v-icon>
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
              <div
                v-for="(notif, i) in notifications"
                :key="i"
                :id="'notifItem-'+i"
                class="notifDrawerItem"
                @mouseenter="applyActions(`notifItem-`+i)"
                v-html="notif.notification">
              </div>
            </div>
          </template>
          <template v-else slot="content">
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
import * as notificationlAPI from '../js/notificationsAPI';

const SLIDE_UP = 300;
const SLIDE_UP_MORE = 600;

export default {
  data () {
    return {
      drawerNotification: false,
      notifications: [],
      badge: 0,
      notificationsSize: 0,
      settingsLink: `${eXo.env.portal.context}/${eXo.env.portal.portalName}/settings`,
      allNotificationsLink: `${eXo.env.portal.context}/${eXo.env.portal.portalName}/allNotifications`,
    };
  },
  watch: {
    badge() {
      return this.badge;
    },
  },
  created() {
    this.getNotifications().finally(() => this.$root.$emit('application-loaded'));
    notificationlAPI.getUserToken().then(
      (data) => {
        notificationlAPI.initCometd(data);
      }
    );
    document.addEventListener('cometdNotifEvent', this.notificationUpdated);
  },
  mounted() {
    this.$root.$emit('application-loaded');
  },
  methods: {
    getNotifications() {
      return notificationlAPI.getNotifications()
        .then((data) => {
          this.notifications = data.notifications;
          this.badge = data.badge;
          this.notificationsSize = this.notifications.length;
          return this.$nextTick();
        });
    },
    markAllAsRead() {
      return notificationlAPI.updateNotification(null, 'markAllAsRead')
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
      return notificationlAPI.updateNotification(null, 'resetNew')
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

        // ----------------- Mark as read

        $(this).on('click', function(evt) {
          evt.stopPropagation();

          if ($(this).hasClass('unread')) {
            $(this).removeClass('unread').addClass('read');
          }

          notificationlAPI.updateNotification(dataId, 'markAsRead')
            .finally(() => {
              if (linkId != null && linkId.length >1 ) {
                if (linkId[0].includes('/view_full_activity/')) {
                  const id = linkId[0].split('/view_full_activity/')[1];
                  location.href = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/activity?id=${id}`;
                } else {
                  location.href = `${eXo.env.portal.context}/${linkId[1]}`;
                }
              } else {
                location.href = dataLink.replace(/^\/rest\//,`${eXo.env.portal.context}/rest/`);
              }
            });
        });

        // ------------- hide notif
        $(this).find('.remove-item').off('click')
          .on('click', function(evt) {
            evt.stopPropagation();
            notificationlAPI.updateNotification(dataId,'hide');
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
            notificationlAPI.updateNotification(dataId,'hide');
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
            notificationlAPI.updateNotification(dataId,'hide');
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
