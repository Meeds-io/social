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
  <a
    :id="id"
    :href="notifLink"
    class="notifDrawerItem notif-item-html"
    @click="markAsRead">
    <dynamic-html-element :child="bodyElement" />
  </a>
</template>
<script>
const SLIDE_UP = 300;
const SLIDE_UP_MORE = 600;

export default {
  props: {
    id: {
      type: String,
      default: null,
    },
    content: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    notifLink: null,
  }),
  computed: {
    bodyElement() {
      const template = DOMPurify.sanitize(this.content || '', {
        CUSTOM_ELEMENT_HANDLING: {
          tagNameCheck: () => true,
          attributeNameCheck: () => true,
        },
      });
      return {template};
    },
  },
  mounted() {
    this.applyActions();    
  },
  methods: {
    markAsRead() {
      return this.$notificationService.markRead(this.id);
    },
    applyActions() {
      const self = this;
      $(this.$el).find('li').each(function () {
        let dataLink = $(this).find('.contentSmall:first').data('link');
        if (!dataLink) {
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
            self.notifLink = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/activity?id=${id}`;
          } else {
            self.notifLink = `${eXo.env.portal.context}/${linkId[1]}`;
          }
        } else if (dataDetails !== 'notification-details-drawer') {
          self.notifLink = dataLink.replace(/^\/rest\//,`${eXo.env.portal.context}/rest/`);
        }

        // ------------- Open details drawer
        $(this).find('.open-details-drawer').off('click').on('click', function(evt) {
          evt.preventDefault();
          const notificationDetails = $(this).data('notification-details');
          document.dispatchEvent(new CustomEvent('open-notification-details-drawer', {detail: notificationDetails}));
          if ($(this).closest('[data-details]').hasClass('unread')) {
            $(this).closest('[data-details]').removeClass('unread').addClass('read');
          }
          Vue.prototype.$notificationService.markRead(dataId);
        });
        const selfs = self;
        // ----------------- Mark as read
        $(selfs.$el).on('click', function() {
          if ($(this).hasClass('unread')) {
            $(this).removeClass('unread').addClass('read');
          }
        });

        // ------------- hide notif
        $(selfs.$el).on('click', '.remove-item', function(evt) {
          evt.preventDefault();
          evt.stopPropagation();
          Vue.prototype.$notificationService.hideNotification(dataId);
          $(this).parents('li:first').slideUp(SLIDE_UP);
        });

        // ------------- Accept request
        $(selfs.$el).on('click', '.action-item', function(evt) {
          evt.preventDefault();
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
            }).then((resp) => {
              if (resp.body) {
                Vue.prototype.$notificationService.resetBadge();
              }
            });
          }
        });

        // ------------- Refuse request
        $(selfs.$el).on('click', '.cancel-item', function(evt) {
          evt.preventDefault();
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
          Vue.prototype.$notificationService.hideNotification(dataId);
          $(this).parents('li:first').slideUp(SLIDE_UP_MORE);
        });
      });
    },
  },
};
</script>
