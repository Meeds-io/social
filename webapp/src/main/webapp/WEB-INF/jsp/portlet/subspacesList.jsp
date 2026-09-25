<%@ page import="io.meeds.social.portlet.SubspacesListPortlet" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page import="javax.portlet.PortletPreferences" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<portlet:defineObjects />
<portlet:actionURL var="saveSettingsUrl" />
<portlet:resourceURL var="resourceUrl" />
<portlet:resourceURL var="avatarResourceUrl" id="<%=SubspacesListPortlet.AVATAR_RESOURCE_ID%>" />
<%
  PortletPreferences preferences = renderRequest.getPreferences();
  String headerTranslations = preferences.getValue(SubspacesListPortlet.HEADER_TRANSLATIONS_PREFERENCE, "{}");
  boolean showHiddenSubspaces = Boolean.parseBoolean(preferences.getValue(SubspacesListPortlet.SHOW_HIDDEN_SUBSPACES_PREFERENCE, "false"));
  // a preference imported through the layout editor bypasses processAction: the portlet's own reader clamps it,
  // and is shared here so that the JSP and the resource envelope cannot answer two different limits
  int subspacesLimit = SubspacesListPortlet.storedLimit(preferences);
  // the portlet's own decision, shared here as storedLimit is: outside a parent space the widget is not
  // booted at all, so no loading state is rendered on a space that has nothing to list
  boolean parentSpace = SubspacesListPortlet.isParentSpaceContext();

  String portletId = (String) request.getAttribute("portletStorageId");
  String appId = "subspacesList" + portletId;
  String headerTranslationsDomId = appId + "HeaderTranslations";
%>
<%-- The bootstrap below is emitted even when the widget will not boot. Rendering nothing instead
     looks like a cleanup and is a regression: an empty fragment makes the legacy renderer hide the
     window itself (UIPortlet.gtmpl), but a page rendered by layout's page-layout app transplants only
     the .PORTLET-FRAGMENT child and leaves that class behind on the discarded div, so the cell would
     stay visible there. main.js hides the application on both. --%>
<div class="VuetifyApp">
  <div data-app="true"
       class="v-application v-application--is-ltr theme--light"
       id="<%=appId%>">
    <textarea id="<%=headerTranslationsDomId%>" style="display:none;"><%=URLEncoder.encode(headerTranslations, StandardCharsets.UTF_8)%></textarea>
    <script type="text/javascript">
      require(['PORTLET/social/SubspacesList'], app => app.init({
        appId: '<%=appId%>',
        <%-- Never a // comment inside this script: the layout renderer strips newlines before
             injecting it (commonLayoutComponents re()/ie()), so a line comment swallows the rest
             of the script and the require() call never parses. Use a JSP comment instead. --%>
        <%-- A preference imported through the layout editor bypasses processAction: never let a
             bad value block the widget. --%>
        headerTranslations: (() => {
          try {
            const value = JSON.parse(decodeURIComponent(document.getElementById('<%=headerTranslationsDomId%>').value.replace(/\+/g, '%20')));
            return value && typeof value === 'object' && !Array.isArray(value) ? value : {};
          } catch (e) {
            return {};
          }
        })(),
        parentSpace: <%=parentSpace%>,
        showHiddenSubspaces: <%=showHiddenSubspaces%>,
        subspacesLimit: <%=subspacesLimit%>,
        saveSettingsUrl: '<%=saveSettingsUrl%>',
        resourceUrl: '<%=resourceUrl%>',
        avatarResourceUrl: '<%=avatarResourceUrl%>'
      }));
    </script>
  </div>
</div>
