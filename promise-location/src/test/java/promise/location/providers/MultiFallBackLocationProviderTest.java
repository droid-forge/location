/*
 * Copyright 2017, Peter Vincent
 * Licensed under the Apache License, Version 2.0, Android Promise.
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package promise.location.providers;

import android.content.Context;
import android.location.Location;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.Collection;
import java.util.Iterator;

import promise.location.GooglePlayServicesLocationProvider;
import promise.location.LocationManagerProvider;
import promise.location.LocationUpdatedListener;
import promise.dev4vin.promiselocation.PromiseLocationRobolectricTestRunner;
import promise.location.LocationProvider;
import promise.location.LocationParams;
import promise.location.Logger;
import promise.location.MultiFallBackLocationProvider;
import promise.location.ServiceConnectionListener;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link MultiFallBackLocationProvider}
 *
 */
@RunWith(PromiseLocationRobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class MultiFallBackLocationProviderTest {

  @Test
  public void testDefaultBuilder() {
    MultiFallBackLocationProvider subject = new MultiFallBackLocationProvider.Builder().build();
    checkExpectedProviders(subject, LocationManagerProvider.class);
  }

  @Test
  public void testGoogleBuilder() {
    MultiFallBackLocationProvider subject =
        new MultiFallBackLocationProvider.Builder().withGooglePlayServicesProvider().build();
    checkExpectedProviders(subject, GooglePlayServicesLocationProvider.class);
  }

  @Test
  public void testMultiProviderBuilder() {
    MultiFallBackLocationProvider subject =
        new MultiFallBackLocationProvider.Builder()
            .withGooglePlayServicesProvider()
            .withDefaultProvider()
            .build();
    checkExpectedProviders(
        subject, GooglePlayServicesLocationProvider.class, LocationManagerProvider.class);
  }

  @Test
  public void testMultiProviderRun() {
    TestServiceProvider testServiceProvider = new TestServiceProvider();
    ServiceConnectionListener mockListener = mock(ServiceConnectionListener.class);
    testServiceProvider.setServiceListener(mockListener);
    LocationProvider backupProvider = mock(LocationProvider.class);
    MultiFallBackLocationProvider subject =
        new MultiFallBackLocationProvider.Builder()
            .withServiceProvider(testServiceProvider)
            .withProvider(backupProvider)
            .build();

    // Test initialization passes through to first provider
    subject.init(mock(Context.class), mock(Logger.class));
    assertEquals(1, testServiceProvider.getInitCount());

    // Test starting location updates passes through to first provider
    LocationUpdatedListener listenerMock = mock(LocationUpdatedListener.class);
    LocationParams paramsMock = mock(LocationParams.class);
    subject.start(listenerMock, paramsMock, false);
    assertEquals(1, testServiceProvider.getStartCount());

    // Test that falling back initializes and starts the backup provider
    testServiceProvider.simulateFailure();
    // Ensure that our 1st listener from the test service provider was invoked.
    verify(mockListener).onConnectionFailed();
    assertEquals(1, testServiceProvider.getStopCount());
    // Verify that the backup provider is initialized and started.
    verify(backupProvider).init(any(Context.class), any(Logger.class));
    verify(backupProvider).start(listenerMock, paramsMock, false);

    // Test that we're now using the fallback provider to stop.
    subject.stop();
    verify(backupProvider).stop();
    assertEquals(1, testServiceProvider.getStopCount());

    // Test that we're now using the fallback provider to get the last location
    Location mockLocation = mock(Location.class);
    when(backupProvider.getLastLocation()).thenReturn(mockLocation);
    assertEquals(mockLocation, subject.getLastLocation());
    assertEquals(0, testServiceProvider.getLastLocCount());
  }

  @SafeVarargs
  private final void checkExpectedProviders(
      MultiFallBackLocationProvider subject, Class<? extends LocationProvider>... expectedProviders) {
    Collection<LocationProvider> providers = subject.getProviders();
    assertEquals(expectedProviders.length, providers.size());
    Iterator<LocationProvider> providerIt = providers.iterator();
    for (Class<? extends LocationProvider> expected : expectedProviders) {
      if (!providerIt.hasNext()) {
        fail("providers list did not have expected value " + expected.getName());
      }
      LocationProvider actual = providerIt.next();
      assertTrue(
          "provider instance class "
              + actual.getClass().getName()
              + " does not "
              + "match expected value "
              + expected.getName(),
          actual.getClass().isAssignableFrom(expected));
    }
  }
}
