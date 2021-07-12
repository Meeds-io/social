<%@ page import="org.exoplatform.commons.api.settings.ExoFeatureService"%>
<%@ page import="org.exoplatform.commons.utils.CommonsUtils"%>
<%@ page import="org.exoplatform.services.security.ConversationState"%>

<%
  ExoFeatureService featureService = CommonsUtils.getService(ExoFeatureService.class);
	boolean isEnabled = featureService.isFeatureActiveForUser("UserSettingMobile",
	                      ConversationState.getCurrent().getIdentity().getUserId());;
%>
<% if(isEnabled) { %>

<div class="VuetifyApp">
    <div id="UserSettingMobile">
        <script type="text/javascript">
          require(['PORTLET/social-portlet/UserSettingMobile'], app => app.init());
        </script>
    </div>
</div>
<%}%>
