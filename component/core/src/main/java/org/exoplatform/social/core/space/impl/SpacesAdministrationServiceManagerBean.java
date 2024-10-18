package org.exoplatform.social.core.space.impl;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.management.annotations.Impact;
import org.exoplatform.management.annotations.ImpactType;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.annotations.ManagedName;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.management.rest.annotations.RESTEndpoint;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.space.SpacesAdministrationService;

@Managed
@ManagedDescription("Social Spaces Administration Service manager bean")
@NameTemplate({ @Property(key = "service", value = "social"), @Property(key = "view", value = "SpacesAdministrationService") })
@RESTEndpoint(path = "spacesadministrationservice")
public class SpacesAdministrationServiceManagerBean {
  private SpacesAdministrationService spacesAdministrationService;

  public SpacesAdministrationServiceManagerBean(SpacesAdministrationServiceImpl spacesAdministrationServiceImpl) {
    this.spacesAdministrationService = spacesAdministrationServiceImpl;
  }

  /**
   * Gets the list of permission expressions of space super managers.
   * See {@link SpacesAdministrationService#getSpacesAdministratorsMemberships()}
   * 
   * @return {@link List} of type {@link String}
   */
  @Managed
  @ManagedDescription("Get Spaces administrators")
  @Impact(ImpactType.READ)
  public List<String> getSpaceManager() {
    return spacesAdministrationService.getSpacesAdministratorsMemberships()
                       .stream()
                       .map(MembershipEntry::toString)
                       .toList();
  }

  /**
   * Adds a membership in spaces administrators
   *
   * @param permissionExpression permission expression of type {@link String}
   * 
   */
  @Managed
  @ManagedDescription("Add Spaces administrators membership")
  @Impact(ImpactType.WRITE)
  public void addSpaceManager(@ManagedDescription("Spaces super manger role") @ManagedName("permissionExpression") String permissionExpression) {
    List<MembershipEntry> superManagersMemberships = new ArrayList<>(spacesAdministrationService.getSpacesAdministratorsMemberships());
    superManagersMemberships.add(MembershipEntry.parse(permissionExpression));
    spacesAdministrationService.updateSpacesAdministratorsMemberships(superManagersMemberships);
  }

  /**
   * Removes a membership from spaces administrators
   * 
   * @param permissionExpression permission expression of type {@link String}
   * 
   */
  @Managed
  @ManagedDescription("Remove Spaces administrators membership")
  @Impact(ImpactType.WRITE)
  public void removeSpaceManager(@ManagedDescription("Spaces super manger memberships") @ManagedName("permissionExpression") String permissionExpression) {
    List<MembershipEntry> superManagersMemberships = spacesAdministrationService.getSpacesAdministratorsMemberships();
    List<MembershipEntry> updatedMemberships = superManagersMemberships.stream()
            .filter(m -> !m.toString().equals(permissionExpression))
            .toList();
    spacesAdministrationService.updateSpacesAdministratorsMemberships(updatedMemberships);
  }
}
