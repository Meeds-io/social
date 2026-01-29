package io.meeds.oauth.filter;

import co.elastic.clients.elasticsearch.nodes.Http;
import io.meeds.oauth.spi.AccessTokenContext;
import io.meeds.oauth.spi.OAuthProviderType;
import io.meeds.oauth.spi.OAuthProviderTypeRegistry;
import io.meeds.oauth.spi.SocialNetworkService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserProfile;
import org.gatein.sso.agent.filter.api.AbstractSSOInterceptor;

import java.io.IOException;

public class OAuthLogoutFilter extends AbstractSSOInterceptor {

  protected final Log LOG = ExoLogger.getLogger(OAuthLogoutFilter.class);
  public static final String OAUTH_LOGOUT_ATTRIBUTE = "OAUTH_LOGOUT_IN_PROGRESS";


  public OAuthLogoutFilter() {
  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) servletRequest;
    HttpServletResponse response = (HttpServletResponse) servletResponse;
    String remoteUser = request.getRemoteUser();
    if (isPortalLogoutInProgress(request) && !StringUtils.isEmpty(remoteUser)) {
      //if (StringUtils.isBlank(getPortalLogoutURLFromSession(request))) {
        //request.getSession().setAttribute(OAUTH_LOGOUT_ATTRIBUTE, request.getRequestURI());
        try {
          OAuthProviderTypeRegistry oauthRegistry = getService(OAuthProviderTypeRegistry.class);
          for (OAuthProviderType oAuthProviderType : oauthRegistry.getEnabledOAuthProviders()) {
            LOG.debug("Logging out from OAuth provider: " + oAuthProviderType.getKey());
            if (oAuthProviderType.getOauthProviderProcessor().processLogout(request, response, oAuthProviderType)) {
              return;
            }
          }
        } catch (Exception e) {
          LOG.error("Unable to get OAuthProviderTypeRegistry during logout", e);
        }
      //}
    }
    filterChain.doFilter(servletRequest, servletResponse);

  }

  protected <T> T getService(Class<T> clazz) {
    return PortalContainer.getInstance().getComponentInstanceOfType(clazz);
  }
  public static boolean isPortalLogoutInProgress(HttpServletRequest request) {
    return request.getRequestURI().equals("/portal/logout") && request.getRemoteUser() != null;
  }

  @Override
  protected void initImpl() {

  }

  private static String getPortalLogoutURLFromSession(HttpServletRequest request) {
    return request.getSession().getAttribute(OAUTH_LOGOUT_ATTRIBUTE) == null ? null
                                                                             : request.getSession()
                                                                                      .getAttribute(OAUTH_LOGOUT_ATTRIBUTE)
                                                                                      .toString();
  }

  public static boolean isOAuthLogoutInProgress(HttpServletRequest request) {
    return request.getRemoteUser() != null && StringUtils.isNotBlank(getPortalLogoutURLFromSession(request))
        && !StringUtils.equals(getPortalLogoutURLFromSession(request), "DONE");
  }
}
