package au.com.origin.snapshots.junit5;

import au.com.origin.snapshots.*;
import au.com.origin.snapshots.exceptions.SnapshotMatchException;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;

import java.lang.reflect.Field;

public class SnapshotExtension implements AfterAllCallback, BeforeAllCallback, SnapshotConfigInjector, ParameterResolver {

  private SnapshotVerifier snapshotVerifier;

  @Override
  public void beforeAll(ExtensionContext context) {
    // don't fail if a test is run alone from the IDE for example
    boolean failOnOrphans = shouldFailOnOrphans(context);
    Class<?> testClass = context.getTestClass()
        .orElseThrow(() -> new SnapshotMatchException("Unable to locate Test class"));
    this.snapshotVerifier = new SnapshotVerifier(getSnapshotConfig(), testClass, failOnOrphans);
  }

  @Override
  public void afterAll(ExtensionContext context) {
    this.snapshotVerifier.validateSnapshots();
  }

  @Override
  public SnapshotConfig getSnapshotConfig() {
    return new PropertyResolvingSnapshotConfig();
  }


  /**
   * FIXME This is a hack until I find the correct way to determine if a test run is individual or as part of a class
   *
   * @param context
   * @return
   */
  private boolean shouldFailOnOrphans(ExtensionContext context) {
    try {
      Field field = context.getClass().getSuperclass().getDeclaredField("testDescriptor");
      field.setAccessible(true);
      ClassTestDescriptor classTestDescriptor = (ClassTestDescriptor) field.get(context);
      return classTestDescriptor.getChildren().size() > 1;
    } catch (NoSuchFieldException | IllegalAccessException | ClassCastException e) {
      e.printStackTrace();
      System.err.println(
          "FAILED: (Java Snapshot Testing) Unable to get JUnit5 ClassTestDescriptor!\n" +
              "Ensure you are using Junit5 >= 5.3.2\n" +
              "This may be due to JUnit5 changing their private api as we use reflection to access it\n" +
              "Log a support ticket https://github.com/origin-energy/java-snapshot-testing/issues and supply your JUnit5 version\n" +
              "Setting failOnOrphans=true as this is the safest option." +
              "This means that running a test alone (say from the IDE) will fail the snapshot, you need to run the entire class.");
      return true;
    }
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return parameterContext.getParameter().getType() == Expect.class;
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return new Expect(snapshotVerifier, extensionContext.getTestMethod().orElseThrow(() -> new RuntimeException("getTestMethod() is missing")));
  }

}
