package io.meeds.social.handler;

import io.meeds.spring.web.localization.HttpRequestLocaleWrapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.exoplatform.portal.branding.BrandingService;
import org.exoplatform.portal.resource.SkinService;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.application.JspBasedWebHandler;
import org.exoplatform.web.application.javascript.JavascriptConfigService;
import org.exoplatform.web.login.LoginUtils;
import org.exoplatform.web.security.security.CookieTokenService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

public class spaceCreationInstantiationHandler extends JspBasedWebHandler {

    public static final String INITIAL_URI_PARAM                   = "initialURI";

    public static final String SPACE_PARAM                         = "space";

    public static final String ANONYMOUS_USERNAME                  = "anonymousUsername";

    public static final String ALREADY_AUTHENTICATED_MESSAGE_PARAM = "authenticated";

    @Autowired
    private CookieTokenService tokenService;

    @Override
    public boolean execute(ControllerContext controllerContext) throws Exception {
      HttpServletRequest request = new HttpRequestLocaleWrapper(controllerContext.getRequest());
      HttpServletResponse response = controllerContext.getResponse();
        Map<String, Object> parameters = new HashMap<>();
        if (request.getRemoteUser() != null) {
            String token = getTokenCookie(request);
            // get space data

        }

            String initialUri = request.getParameter(INITIAL_URI_PARAM);
            String space = request.getParameter(SPACE_PARAM);
            String cookieToken = tokenService.createToken(ANONYMOUS_USERNAME);
            Cookie cookie = new Cookie("spaceCreationCookie", cookieToken);
            cookie.setPath("/");
            cookie.setMaxAge(300);
            cookie.setHttpOnly(true);
            cookie.setSecure(request.isSecure());
            response.addCookie(cookie);
            //save space data in cache
            response.sendRedirect("/portal/login?initialURI=%2Fportal%2Fcs");
      return true;
    }

    private String getTokenCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("spaceCreationCookie".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
