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

package promise.dev4vin.promiselocation;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import promise.location.LocationUpdatedListener;
import promise.location.PromiseLocation;
import promise.dev4vin.promiselocation.util.MockLocationProvider;
import promise.location.LocationParams;
import promise.location.Logger;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(PromiseLocationRobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class LocationControlTest {

  private static final LocationParams DEFAULT_PARAMS = LocationParams.BEST_EFFORT;
  private static final boolean DEFAULT_SINGLE_UPDATE = false;

  private MockLocationProvider mockProvider;
  private LocationUpdatedListener locationUpdatedListener;

  @Before
  public void setup() {
    mockProvider = mock(MockLocationProvider.class);
    locationUpdatedListener = mock(LocationUpdatedListener.class);
  }

  @Test
  public void test_location_control_init() {
    Context context = RuntimeEnvironment.application.getApplicationContext();
    PromiseLocation promiseLocation =
        new PromiseLocation.Builder(context).logging(false).preInitialize(false).build();
    PromiseLocation.LocationControl locationControl = promiseLocation.location(mockProvider);
    verifyZeroInteractions(mockProvider);

    promiseLocation = new PromiseLocation.Builder(context).logging(false).build();
    locationControl = promiseLocation.location(mockProvider);
    verify(mockProvider).init(eq(context), any(Logger.class));
  }

  @Test
  public void test_location_control_start_defaults() {
    PromiseLocation.LocationControl locationControl = createLocationControl();

    locationControl.start(locationUpdatedListener);
    verify(mockProvider).start(locationUpdatedListener, DEFAULT_PARAMS, DEFAULT_SINGLE_UPDATE);
  }

  @Test
  public void test_location_control_start_only_once() {
    PromiseLocation.LocationControl locationControl = createLocationControl();
    locationControl.once();

    locationControl.start(locationUpdatedListener);
    verify(mockProvider).start(locationUpdatedListener, DEFAULT_PARAMS, true);
  }

  @Test
  public void test_location_control_start_continuous() {
    PromiseLocation.LocationControl locationControl = createLocationControl();
    locationControl.once();
    locationControl.continuous();
    locationControl.start(locationUpdatedListener);
    verify(mockProvider).start(locationUpdatedListener, DEFAULT_PARAMS, false);
  }

  @Test
  public void test_location_control_start_navigation() {
    PromiseLocation.LocationControl locationControl = createLocationControl();
    locationControl.config(LocationParams.NAVIGATION);

    locationControl.start(locationUpdatedListener);
    verify(mockProvider)
        .start(eq(locationUpdatedListener), eq(LocationParams.NAVIGATION), anyBoolean());
  }

  @Test
  public void test_location_control_get_last_location() {
    PromiseLocation.LocationControl locationControl = createLocationControl();
    locationControl.getLastLocation();

    verify(mockProvider).getLastLocation();
  }

  @Test
  public void test_location_control_stop() {
    PromiseLocation.LocationControl locationControl = createLocationControl();
    locationControl.stop();

    verify(mockProvider).stop();
  }

  private PromiseLocation.LocationControl createLocationControl() {
    Context context = RuntimeEnvironment.application.getApplicationContext();
    PromiseLocation promiseLocation =
        new PromiseLocation.Builder(context).logging(false).preInitialize(false).build();
    PromiseLocation.LocationControl locationControl = promiseLocation.location(mockProvider);
    return locationControl;
  }
}
