<%@ page import="io.meeds.social.portlet.SubspacesListPortlet" %>
<%@ page import="org.apache.commons.lang3.math.NumberUtils" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page import="javax.portlet.PortletPreferences" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<portlet:defineObjects />
<portlet:actionURL var="saveSettingsUrl" />
<portlet:resourceURL var="resourceUrl" />
<%
  PortletPreferences preferences = renderRequest.getPreferences();
  String headerTranslations = preferences.getValue(SubspacesListPortlet.HEADER_TRANSLATIONS_PREFERENCE, "{}");
  boolean showHiddenSubspaces = Boolean.parseBoolean(preferences.getValue(SubspacesListPortlet.SHOW_HIDDEN_SUBSPACES_PREFERENCE, "false"));
  // a preference imported through the layout editor bypasses processAction: clamp it so the widget always asks a usable limit
  int subspacesLimit = Math.min(SubspacesListPortlet.MAX_SUBSPACES_LIMIT,
                                Math.max(SubspacesListPortlet.MIN_SUBSPACES_LIMIT,
                                         NumberUtils.toInt(preferences.getValue(SubspacesListPortlet.SUBSPACES_LIMIT_PREFERENCE, null),
                                                           SubspacesListPortlet.DEFAULT_SUBSPACES_LIMIT)));

  String portletId = (String) request.getAttribute("portletStorageId");
  String appId = "subspacesList" + portletId;
  String headerTranslationsDomId = appId + "HeaderTranslations";
%>
<div class="VuetifyApp">
  <div data-app="true"
       class="v-application v-application--is-ltr theme--light"
       id="<%=appId%>">
    <textarea id="<%=headerTranslationsDomId%>" style="display:none;"><%=URLEncoder.encode(headerTranslations, StandardCharsets.UTF_8)%></textarea>
    <script type="text/javascript">
      require(['PORTLET/social/SubspacesList'], app => app.init({
        appId: '<%=appId%>',
        // a preference imported through the layout editor bypasses processAction: never let a bad value block the widget
        headerTranslations: (() => {
          try {
            const value = JSON.parse(decodeURIComponent(document.getElementById('<%=headerTranslationsDomId%>').value.replace(/\+/g, '%20')));
            return value && typeof value === 'object' && !Array.isArray(value) ? value : {};
          } catch (e) {
            return {};
          }
        })(),
        showHiddenSubspaces: <%=showHiddenSubspaces%>,
        subspacesLimit: <%=subspacesLimit%>,
        saveSettingsUrl: '<%=saveSettingsUrl%>',
        resourceUrl: '<%=resourceUrl%>'
      }));
    </script>
  </div>
</div>
