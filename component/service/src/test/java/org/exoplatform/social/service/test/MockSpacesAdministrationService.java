package org.exoplatform.social.service.test;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.space.SpacesAdministrationService;

import java.util.ArrayList;
import java.util.List;

public class MockSpacesAdministrationService implements SpacesAdministrationService {

  private List<MembershipEntry> spacesAdministrators = new ArrayList<>();

  private List<MembershipEntry> spacesCreators       = new ArrayList<>();

  @Override
  public List<MembershipEntry> getSpacesAdministratorsMemberships() {
    return spacesAdministrators;
  }

  @Override
  public void updateSpacesAdministratorsMemberships(List<MembershipEntry> permissionsExpressions) {
    if (permissionsExpressions != null) {
      spacesAdministrators = new ArrayList<>(permissionsExpressions);
    } else {
      spacesAdministrators.clear();
    }
  }

  @Override
  public List<MembershipEntry> getSpacesCreatorsMemberships() {
    return spacesCreators;
  }

  @Override
  public void updateSpacesCreatorsMemberships(List<MembershipEntry> permissionsExpressions) {
    if (permissionsExpressions != null) {
      spacesCreators = new ArrayList<>(permissionsExpressions);
    } else {
      spacesCreators.clear();
    }
  }

  @Override
  public boolean canCreateSpace(String username) {
    return StringUtils.isNotBlank(username)
           && !IdentityConstants.ANONIM.equals(username)
           && !IdentityConstants.SYSTEM.equals(username);
  }

  @Override
  public boolean isSuperManager(String username) {
    UserACL userAcl = ExoContainerContext.getService(UserACL.class);
    if (StringUtils.isBlank(username)
        || IdentityConstants.ANONIM.equals(username)
        || IdentityConstants.SYSTEM.equals(username)) {
      return false;
    } else if (username.equals(userAcl.getSuperUser())) {
      return true;
    }
    org.exoplatform.services.security.Identity identity = userAcl.getUserIdentity(username);
    return identity != null && (identity.isMemberOf(userAcl.getAdminGroups())
                                || getSpacesAdministratorsMemberships().stream()
                                                                       .anyMatch(identity::isMemberOf));
  }

}
