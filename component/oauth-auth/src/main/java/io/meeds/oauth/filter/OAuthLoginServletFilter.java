/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.oauth.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import javax.security.auth.login.LoginException;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.portal.rest.UserFieldValidator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.DisabledUserException;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileHandler;
import org.exoplatform.services.organization.UserStatus;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.Credential;
import org.exoplatform.services.security.PasswordCredential;
import org.exoplatform.services.security.UsernameCredential;
import org.exoplatform.web.security.AuthenticationRegistry;

import io.meeds.oauth.common.OAuthConstants;
import io.meeds.oauth.contant.OAuthConst;
import io.meeds.oauth.exception.OAuthException;
import io.meeds.oauth.exception.OAuthExceptionCode;
import io.meeds.oauth.service.OAuthRegistrationService;
import io.meeds.oauth.spi.OAuthPrincipal;
import io.meeds.oauth.spi.OAuthProviderType;
import io.meeds.oauth.utils.OAuthUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class OAuthLoginServletFilter extends OAuthAbstractFilter {

  private static final String            EMAIL_ADDRESS_PARAM               = "emailAddress";

  private static final String            USERNAME_PARAM                    = "username";

  public static final String             EMAIL_PARAM                       = "email";

  public static final String             LASTNAME_PARAM                    = "lastName";

  public static final String             FIRSTNAME_PARAM                   = "firstName";

  public static final String             PASSWORD_PARAM                    = "password";

  private static final String            INVITATION_CONFIRM_ERROR_PARAM    = "invitationConfirmError";

  private final Log                      log                               = ExoLogger.getLogger(OAuthLoginServletFilter.class);

  public static final String             CONTROLLER_PARAM_NAME             = "login_controller";

  public static final String             CANCEL_OAUTH                      = "oauth_cancel";

  public static final String             CONFIRM_ACCOUNT                   = "confirm_account";

  public static final String             REGISTER_NEW_ACCOUNT              = "register";

  public static final String             CONFIRM_REGISTER_ACCOUNT          = "submit_register";

  public static final String             SESSION_ATTR_REGISTER_NEW_ACCOUNT = "__oauth_create_new_account";

  private static final String            SESSION_ATTR_IS_ON_FLY_ERROR      = "isOnFlyError";

  private static final String            OAUTH_REGISTER_JSP_PATH           = "/WEB-INF/jsp/login/oauth_register.jsp";           // NOSONAR

  private static final String            OAUTH_INVITATION_JSP_PATH         = "/WEB-INF/jsp/login/oauth_invitation.jsp";         // NOSONAR

  public static final UserFieldValidator USERNAME_VALIDATOR                = new UserFieldValidator("userName", true, false);

  public static final UserFieldValidator PASSWORD_VALIDATOR                =
                                                            new UserFieldValidator(PASSWORD_PARAM, false, false, 8, 255);

  public static final UserFieldValidator LASTNAME_VALIDATOR                =
                                                            new UserFieldValidator(LASTNAME_PARAM, false, true);

  public static final UserFieldValidator FIRSTNAME_VALIDATOR               =
                                                             new UserFieldValidator(FIRSTNAME_PARAM, false, true);

  public static final UserFieldValidator EMAIL_VALIDATOR                   = new UserFieldValidator(EMAIL_PARAM, false, false);

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    // This is workaround to fix bug:
    // - error message is shown after user cancel his oauth login then do login
    // with username and password
    ((HttpServletRequest) request).getSession().removeAttribute(OAuthConstants.ATTRIBUTE_EXCEPTION_OAUTH);
    super.doFilter(request, response, chain);
  }

  @Override
  protected void executeFilter(HttpServletRequest request,
                               HttpServletResponse response,
                               FilterChain chain) throws IOException, ServletException {
    request.setCharacterEncoding("UTF-8");

    AuthenticationRegistry authenticationRegistry = getService(AuthenticationRegistry.class);
    ResourceBundleService resourceBundleService = getService(ResourceBundleService.class);
    Locale locale = request.getLocale();
    ResourceBundle resourceBundle = resourceBundleService.getResourceBundle(resourceBundleService.getSharedResourceBundleNames(),
                                                                            locale);
    MessageResolver messageResolver = new MessageResolver(resourceBundle);
    ServletContext context = getContext();

    Boolean isOnFlyError = (Boolean) request.getSession().getAttribute(OAuthConst.SESSION_KEY_ON_FLY_ERROR);
    if (isOnFlyError != null) {
      request.getSession().removeAttribute(OAuthConst.SESSION_KEY_ON_FLY_ERROR);
      request.setAttribute(SESSION_ATTR_IS_ON_FLY_ERROR, isOnFlyError);
    }

    User portalUser = (User) authenticationRegistry.getAttributeOfClient(request,
                                                                         OAuthConstants.ATTRIBUTE_AUTHENTICATED_PORTAL_USER);
    User detectedUser =
                      (User) authenticationRegistry.getAttributeOfClient(request,
                                                                         OAuthConst.ATTRIBUTE_AUTHENTICATED_PORTAL_USER_DETECTED);
    request.setAttribute("portalUser", portalUser);
    if (detectedUser != null) {
      request.setAttribute("detectedUser", detectedUser);
    }

    final String controller = request.getParameter(CONTROLLER_PARAM_NAME);
    if (CANCEL_OAUTH.equalsIgnoreCase(controller)) {
      cancelOauth(request, response, authenticationRegistry);
      return;
    } else if (CONFIRM_ACCOUNT.equalsIgnoreCase(controller)) {
      confirmExistingAccount(request, response, messageResolver, authenticationRegistry);
      return;
    } else if (CONFIRM_REGISTER_ACCOUNT.equalsIgnoreCase(controller)) {
      processCreateNewAccount(request, response, messageResolver, authenticationRegistry, portalUser);
      return;
    } else if (REGISTER_NEW_ACCOUNT.equalsIgnoreCase(controller)) {
      request.getSession(true).setAttribute(SESSION_ATTR_REGISTER_NEW_ACCOUNT, 1);
    }

    Integer createNewAccount = (Integer) request.getSession().getAttribute(SESSION_ATTR_REGISTER_NEW_ACCOUNT);
    if (detectedUser != null && createNewAccount == null) {
      RequestDispatcher invitation = context.getRequestDispatcher(OAUTH_INVITATION_JSP_PATH);
      if (invitation != null) {
        invitation.forward(request, response);
        return;
      }
    } else {
      request.setAttribute("SSO.Login.Status", "USER_ACCOUNT_NOT_FOUND");
      request.removeAttribute("portalUser");
      authenticationRegistry.removeAttributeOfClient(request, OAuthConstants.ATTRIBUTE_AUTHENTICATED_OAUTH_PRINCIPAL);
      authenticationRegistry.removeAttributeOfClient(request, OAuthConstants.ATTRIBUTE_AUTHENTICATED_PORTAL_USER);
      log.warn("User {} does not have an account and On the fly registration is disabled, could not login !",
               portalUser.getDisplayName());
    }

    chain.doFilter(request, response);
  }

  private void cancelOauth(HttpServletRequest req, HttpServletResponse res, AuthenticationRegistry authReg) throws IOException {
    req.getSession().removeAttribute(SESSION_ATTR_REGISTER_NEW_ACCOUNT);
    authReg.removeAttributeOfClient(req, OAuthConstants.ATTRIBUTE_AUTHENTICATED_OAUTH_PRINCIPAL);
    authReg.removeAttributeOfClient(req, OAuthConstants.ATTRIBUTE_AUTHENTICATED_PORTAL_USER);
    authReg.removeAttributeOfClient(req, OAuthConst.ATTRIBUTE_AUTHENTICATED_PORTAL_USER_DETECTED);

    // . Redirect to last URL
    String initialURL = OAuthUtils.getURLToRedirectAfterLinkAccount(req, req.getSession());
    if (initialURL == null) {
      initialURL = req.getServletPath();
    }
    res.sendRedirect(res.encodeRedirectURL(initialURL));
  }

  private void confirmExistingAccount(HttpServletRequest request,
                                      HttpServletResponse response,
                                      MessageResolver bundle,
                                      AuthenticationRegistry authenticationRegistry) throws IOException, ServletException {
    String username = getUserName(request, authenticationRegistry);
    if (StringUtils.isBlank(username)) {
      request.setAttribute(INVITATION_CONFIRM_ERROR_PARAM, bundle.resolve("UIOAuthInvitationForm.message.not-in-oauth-login"));
      getContext().getRequestDispatcher(OAUTH_INVITATION_JSP_PATH).forward(request, response);
      return;
    }

    String password = request.getParameter(PASSWORD_PARAM);
    Credential[] credentials =
                             new Credential[] {
                               new UsernameCredential(username), new PasswordCredential(password)
                             };
    Authenticator authenticator = getService(Authenticator.class);
    try {
      boolean isAuthenticated = StringUtils.isNotBlank(authenticator.validateUser(credentials));
      if (isAuthenticated) {
        OAuthRegistrationService oAuthRegistrationService = getService(OAuthRegistrationService.class);

        // Update authentication
        OAuthPrincipal<?> principal = getPrincipal(request, authenticationRegistry);
        OAuthProviderType<?> providerType = principal.getOauthProviderType();
        oAuthRegistrationService.updateUserProfileAttributes(username, providerType);

        // . Redirect to last URL
        authenticationRegistry.removeAttributeOfClient(request, OAuthConstants.ATTRIBUTE_AUTHENTICATED_PORTAL_USER);
        authenticationRegistry.removeAttributeOfClient(request, OAuthConst.ATTRIBUTE_AUTHENTICATED_PORTAL_USER_DETECTED);
        String initURL = OAuthUtils.getURLToRedirectAfterLinkAccount(request, request.getSession());
        if (initURL == null) {
          initURL = request.getServletPath();
        }
        response.sendRedirect(response.encodeRedirectURL(initURL));
      }
    } catch (LoginException ex) {
      Exception e = authenticator.getLastExceptionOnValidateUser();
      if (e instanceof DisabledUserException) {
        request.setAttribute(INVITATION_CONFIRM_ERROR_PARAM, bundle.resolve("UIOAuthInvitationForm.message.userDisabled"));
      } else {
        request.setAttribute(INVITATION_CONFIRM_ERROR_PARAM, bundle.resolve("UIOAuthInvitationForm.message.loginFailure"));
      }
    } catch (Exception ex) {
      request.setAttribute(INVITATION_CONFIRM_ERROR_PARAM, bundle.resolve("UIOAuthInvitationForm.message.loginUnknowException"));
      log.warn("Exception while checking password of user", ex);
    }

    getContext().getRequestDispatcher(OAUTH_INVITATION_JSP_PATH).forward(request, response);
  }

  private OAuthPrincipal<?> getPrincipal(HttpServletRequest request, AuthenticationRegistry authenticationRegistry) {
    return (OAuthPrincipal<?>) authenticationRegistry.getAttributeOfClient(request,
                                                                           OAuthConstants.ATTRIBUTE_AUTHENTICATED_OAUTH_PRINCIPAL);
  }

  private String getUserName(HttpServletRequest request, AuthenticationRegistry authenticationRegistry) {
    User user = (User) authenticationRegistry.getAttributeOfClient(request,
                                                                   OAuthConst.ATTRIBUTE_AUTHENTICATED_PORTAL_USER_DETECTED);
    return user == null ? null : user.getUserName();
  }

  private void processCreateNewAccount(HttpServletRequest req,
                                       HttpServletResponse res,
                                       MessageResolver bundle,
                                       AuthenticationRegistry authReg,
                                       User portalUser) throws IOException, ServletException {

    String username = req.getParameter(USERNAME_PARAM);
    String password = req.getParameter(PASSWORD_PARAM);
    String password2 = req.getParameter("password2");
    String firstName = req.getParameter(FIRSTNAME_PARAM);
    String lastName = req.getParameter(LASTNAME_PARAM);
    String displayName = req.getParameter("displayName");
    String email = req.getParameter(EMAIL_PARAM);

    portalUser.setUserName(username);
    portalUser.setPassword(password);
    portalUser.setDisplayName(displayName);
    portalUser.setFirstName(firstName);
    portalUser.setLastName(lastName);
    portalUser.setEmail(email);

    List<String> errors = new ArrayList<>();
    Set<String> errorFields = new HashSet<>();
    OrganizationService orgService = getService(OrganizationService.class);

    validateUser(portalUser,
                 password2,
                 orgService,
                 bundle,
                 req.getLocale(),
                 errors,
                 errorFields);

    if (errors.isEmpty()) {
      try {
        orgService.getUserHandler().createUser(portalUser, true);
        UserProfileHandler profileHandler = orgService.getUserProfileHandler();
        UserProfile newUserProfile = profileHandler.findUserProfileByName(portalUser.getUserName());
        if (newUserProfile == null) {
          newUserProfile = orgService.getUserProfileHandler().createUserProfileInstance(portalUser.getUserName());
        }
        OAuthPrincipal<?> oauthPrincipal = getPrincipal(req, authReg);
        newUserProfile.setAttribute(oauthPrincipal.getOauthProviderType().getUserNameAttrName(), oauthPrincipal.getUserName());

        try {
          profileHandler.saveUserProfile(newUserProfile, true);

          authReg.removeAttributeOfClient(req, OAuthConstants.ATTRIBUTE_AUTHENTICATED_PORTAL_USER);
          authReg.removeAttributeOfClient(req, OAuthConst.ATTRIBUTE_AUTHENTICATED_PORTAL_USER_DETECTED);

          // Successfully to register new account
          // Just refresh then oauth lifecycle will continue process
          res.sendRedirect(getContext().getContextPath());
          return;

        } catch (OAuthException gtnOAuthException) {
          // Show warning message if user with this facebookUsername (or
          // googleUsername) already exists
          // NOTE: It could happen only in case of parallel registration of same
          // oauth user from more browser windows
          if (gtnOAuthException.getExceptionCode() == OAuthExceptionCode.DUPLICATE_OAUTH_PROVIDER_USERNAME) {

            // Drop new user
            orgService.getUserHandler().removeUser(portalUser.getUserName(), true);

            // Clear previous message about successful creation of user because
            // we dropped him. Add message about duplicate oauth username
            errors.add(bundle.resolve("UIAccountSocial.msg.failed-registration",
                                      gtnOAuthException.getExceptionAttribute(OAuthConstants.EXCEPTION_OAUTH_PROVIDER_USERNAME),
                                      gtnOAuthException.getExceptionAttribute(OAuthConstants.EXCEPTION_OAUTH_PROVIDER_NAME)));
          } else {
            log.warn("Unknown oauth error", gtnOAuthException);
            errors.add(bundle.resolve("UIAccountSocial.msg.oauth-error"));
          }
        }
      } catch (Exception ex) {
        log.warn("Exception when create new account for user", ex);
        errors.add(bundle.resolve("UIAccountInputSet.msg.fail.create.user"));
      }
    }
    req.setAttribute("register_errors", errors);
    req.setAttribute("register_error_fields", errorFields);
    getContext().getRequestDispatcher(OAUTH_REGISTER_JSP_PATH).forward(req, res);
  }

  private void validateUser(User user,
                            String password2,
                            OrganizationService organizationService,
                            MessageResolver bundle,
                            Locale locale,
                            List<String> errorMessages,
                            Set<String> errorFields) {
    String username = user.getUserName();
    String firstName = user.getFirstName();
    String lastName = user.getLastName();
    String email = user.getEmail();
    String password = user.getPassword();

    validate(USERNAME_VALIDATOR,
             USERNAME_PARAM,
             username,
             locale,
             errorMessages,
             errorFields);
    if (!errorFields.contains(USERNAME_PARAM)) {
      try {
        if (organizationService.getUserHandler().findUserByName(username, UserStatus.ANY) != null) {
          errorFields.add(USERNAME_PARAM);
          errorMessages.add(bundle.resolve("UIAccountInputSet.msg.user-exist", username));
        }
      } catch (Exception ex) {
        log.warn("Can not check username exist or not for: " + username);
      }
    }
    validate(USERNAME_VALIDATOR,
             USERNAME_PARAM,
             username,
             locale,
             errorMessages,
             errorFields);
    validate(FIRSTNAME_VALIDATOR,
             FIRSTNAME_PARAM,
             firstName,
             locale,
             errorMessages,
             errorFields);
    validate(LASTNAME_VALIDATOR,
             LASTNAME_PARAM,
             lastName,
             locale,
             errorMessages,
             errorFields);
    validate(EMAIL_VALIDATOR,
             EMAIL_ADDRESS_PARAM,
             email,
             locale,
             errorMessages,
             errorFields);
    validate(PASSWORD_VALIDATOR,
             PASSWORD_PARAM,
             password,
             locale,
             errorMessages,
             errorFields);
    if (!errorFields.contains(PASSWORD_PARAM)
        && !password.equals(password2)) {
      errorMessages.add(bundle.resolve("UIAccountForm.msg.password-is-not-match"));
      errorFields.add("password2");
    }

    if (!errorFields.contains(EMAIL_ADDRESS_PARAM)) {
      try {
        Query query = new Query();
        query.setEmail(email);
        ListAccess<User> users = organizationService.getUserHandler().findUsersByQuery(query, UserStatus.ANY);
        if (users != null && users.getSize() > 0) {
          errorFields.add(EMAIL_ADDRESS_PARAM);
          errorMessages.add(bundle.resolve("UIAccountInputSet.msg.email-exist", email));
        }
      } catch (Exception ex) {
        log.warn("Can not check email exist or not for: " + email);
      }
    }
  }

  private void validate(UserFieldValidator validator,
                        String fieldName,
                        String value,
                        Locale locale,
                        List<String> errorMessages,
                        Set<String> errorFields) {
    String errorMessage = validator.validate(locale, value);
    if (StringUtils.isNotBlank(errorMessage)) {
      errorFields.add(fieldName);
      errorMessages.add(errorMessage);
    }
  }

  public static class MessageResolver {
    private final ResourceBundle bundle;

    public MessageResolver(ResourceBundle bundle) {
      this.bundle = bundle;
    }

    public String resolve(String key, Object... args) {
      try {
        String message = bundle.getString(key);
        if (message != null && args != null) {
          for (int i = 0; i < args.length; i++) {
            final Object messageArg = args[i];
            if (messageArg != null) {
              String arg = messageArg.toString();
              message = message.replace("{" + i + "}", arg);
            }
          }
        }
        return message;
      } catch (Exception ex) {
        return key;
      }
    }

    public ResourceBundle getBundle() {
      return bundle;
    }
  }
}
