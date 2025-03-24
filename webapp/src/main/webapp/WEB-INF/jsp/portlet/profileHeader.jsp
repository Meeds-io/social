<%@ page import="org.exoplatform.social.core.manager.IdentityManager"%>
<%@ page import="org.exoplatform.commons.utils.CommonsUtils"%>
<%@ page import="org.exoplatform.social.webui.Utils"%>
<%@ page import="javax.portlet.PortletPreferences" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<portlet:defineObjects/>
<portlet:actionURL var="saveSettingsUrl" />
<%
  IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
  int maxUploadSize = identityManager.getImageUploadLimit();
  String profileOwnerId = Utils.getOwnerIdentityId();
  PortletPreferences preferences = renderRequest.getPreferences();
  String bannerMaxHeight = preferences.getValue("bannerMaxHeight", "175");
  String bannerHeight = preferences.getValue("bannerHeight", "auto");
  String avatarMaxSize = preferences.getValue("avatarMaxSize", "160");
  String avatarMinSize = preferences.getValue("avatarMinSize", "44");
  String displayOption = preferences.getValue("displayOption", "name");
  Boolean useActions = Boolean.parseBoolean(preferences.getValue("useActions", "false"));
  Boolean containerBasedBreakPoints = Boolean.parseBoolean(preferences.getValue("containerBasedBreakpoints", "true"));
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application v-application--is-ltr theme--light profileHeaderOwner"
    id="ProfileHeader">
    <v-cacheable-dom-app cache-id="ProfileHeader_<%=profileOwnerId%>"></v-cacheable-dom-app>
    <script type="text/javascript">
      require(['PORTLET/social/ProfileHeader'], app => app.init({
        maxUploadSize: <%= maxUploadSize %>,
        bannerMaxHeight: <%= bannerMaxHeight %>,
        bannerHeight: '<%= bannerHeight %>',
        avatarMaxSize: <%= avatarMaxSize %>,
        avatarMinSize: <%= avatarMinSize %>,
        displayOption: '<%= displayOption %>',
        useActions: <%= useActions %>,
        containerBasedBreakpoints: <%= containerBasedBreakPoints %>,
        saveSettingsUrl: '<%= saveSettingsUrl %>'
      }));
    </script>
  </div>
</div>
