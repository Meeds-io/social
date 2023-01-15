package org.exoplatform.social.service.rest;

import java.lang.reflect.*;
import java.security.Principal;
import java.util.Map;

import javax.ws.rs.core.SecurityContext;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.component.test.*;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.services.rest.impl.*;
import org.exoplatform.services.rest.tools.ResourceLauncher;
import org.exoplatform.services.test.mock.MockPrincipal;

@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.social.component.service-local-configuration.xml"),
})
public abstract class BaseRestServicesTestCase extends AbstractKernelTest {

  protected ProviderBinder   providers;

  protected ResourceBinder   binder;

  protected ResourceLauncher launcher;

  protected ExoContainer     container;

  public void setUp() throws Exception {
    begin();
    ExoContainer container = getContainer();
    this.container = container;

    binder = (ResourceBinder) container.getComponentInstanceOfType(ResourceBinder.class);
    RequestHandlerImpl requestHandler = (RequestHandlerImpl) container.getComponentInstanceOfType(RequestHandlerImpl.class);

    // reset default providers to be sure it is clean.
    ProviderBinder.setInstance(new ProviderBinder());
    providers = ProviderBinder.getInstance();

    binder.clear();

    ApplicationContextImpl.setCurrent(new ApplicationContextImpl(null, null, providers, null));

    launcher = new ResourceLauncher(requestHandler);
    registry(getComponentClass());
  }

  public void tearDown() throws Exception {
    unregistry(getComponentClass());
    end();
  }

  protected abstract Class<?> getComponentClass();

  protected <T> T getService(Class<T> clazz) {
    return clazz.cast(this.container.getComponentInstanceOfType(clazz));
  }

  private void registry(Class<?> resourceClass) throws Exception {
    binder.addResource(resourceClass, null);
  }

  private void unregistry(Class<?> resourceClass) {
    binder.removeResource(resourceClass);
  }

  protected <T> T createProxy(Class<T> type, final Map<String, Object> result) {
    Object o = Proxy.newProxyInstance(getClass().getClassLoader(),
                                      new Class<?>[] { type },
                                      new InvocationHandler() {
                                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                          if (method.getName().equals("equals")) {
                                            return proxy == args[0];
                                          }
                                          Object o = result.get(method.getName());
                                          return o instanceof Invoker ? ((Invoker) o).invoke(args) : o;
                                        }
                                      });
    return type.cast(o);
  }

  protected static interface Invoker {
    Object invoke(Object[] args);
  }

  protected static class MockSecurityContext implements SecurityContext {

    private final String username;

    public MockSecurityContext(String username) {
      this.username = username;
    }

    public Principal getUserPrincipal() {
      return new MockPrincipal(username);
    }

    public boolean isUserInRole(String role) {
      return false;
    }

    public boolean isSecure() {
      return false;
    }

    public String getAuthenticationScheme() {
      return null;
    }
  }

  protected static class MockListAccess<T> implements ListAccess<T> {
    private final T[] values;

    public MockListAccess(T[] values) {
      this.values = values;
    }

    public T[] load(int index, int length) throws Exception, IllegalArgumentException {
      return values;
    }

    public int getSize() throws Exception {
      return values.length;
    }
  }
}
