<%@page import="org.exoplatform.commons.api.notification.service.WebNotificationService"%>
<%@page import="org.exoplatform.container.ExoContainerContext"%>
<%
  WebNotificationService webNotificationService = ExoContainerContext.getService(WebNotificationService.class);
  int badge = webNotificationService.getNumberOnBadge(request.getRemoteUser());
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application v-application--is-ltr theme--light"
    id="NotificationPopoverPortlet">
    <div class="v-application--wrap">
      <div class="flex">
        <div class="layout">
          <button type="button" class="v-btn v-btn--flat v-btn--icon v-btn--round theme--light v-size--default">
            <span class="v-btn__content">
              <span class="v-badge v-badge--overlap theme--light">
                <i
                  aria-hidden="true"
                  class="v-icon notranslate icon-default-color fa fa-bell theme--light"
                  style="font-size: 20px;"></i>
              </span>
            </span>
          </button>
          <script type="text/javascript">
            require(['PORTLET/social/TopBarNotification'], app => app.init(<%=badge%>));
            eXo.env.portal.topbarDisplayedApps = eXo.env.portal.topbarDisplayedApps || [];
            if (!eXo.env.portal.topbarDisplayedApps.includes('notifications')) {
              eXo.env.portal.topbarDisplayedApps.push('notifications');
            }
          </script>
        </div>
      </div>
    </div>
  </div>
</div>

