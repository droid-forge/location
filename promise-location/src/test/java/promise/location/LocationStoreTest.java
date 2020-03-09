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

package promise.location;

import android.location.Location;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import promise.commons.Promise;
import promise.commons.pref.Preferences;
import promise.dev4vin.promiselocation.PromiseLocationRobolectricTestRunner;

@RunWith(PromiseLocationRobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class LocationStoreTest {

  private static final double DELTA = 1e-7;

  private static final String TEST_LOCATION_ID = "test_location_1";
  private static final float ACCURACY = 1.234f;
  private static final double ALTITUDE = 12.34;
  private static final float BEARING = 123f;
  private static final float SPEED = 321f;
  private static final double LATITUDE = -50.123456;
  private static final double LONGITUDE = 9.8765432;
  private static final int TIME = 987654321;

  private final Location testLocation = new Location("test");

  @Before
  public void setup() {
    testLocation.setAccuracy(ACCURACY);
    testLocation.setAltitude(ALTITUDE);
    testLocation.setBearing(BEARING);
    testLocation.setLatitude(LATITUDE);
    testLocation.setLongitude(LONGITUDE);
    testLocation.setSpeed(SPEED);
    testLocation.setTime(TIME);
  }

  @Test
  public void test_location_store_full_cycle() {
    Promise.init(RuntimeEnvironment.application);
    LocationStore store = new LocationStore();
    store.setPreferences(getPreferences());

    Assert.assertNull(store.get(TEST_LOCATION_ID));

    store.put(TEST_LOCATION_ID, testLocation);
    Location storedLocation = store.get(TEST_LOCATION_ID);
    Assert.assertEquals(storedLocation.getAccuracy(), testLocation.getAccuracy(), DELTA);
    Assert.assertEquals(storedLocation.getAltitude(), testLocation.getAltitude(), DELTA);
    Assert.assertEquals(storedLocation.getBearing(), testLocation.getBearing(), DELTA);
    Assert.assertEquals(storedLocation.getLatitude(), testLocation.getLatitude(), DELTA);
    Assert.assertEquals(storedLocation.getLongitude(), testLocation.getLongitude(), DELTA);
    Assert.assertEquals(storedLocation.getSpeed(), testLocation.getSpeed(), DELTA);
    Assert.assertEquals(storedLocation.getTime(), testLocation.getTime());

    store.remove(TEST_LOCATION_ID);
    Assert.assertNull(store.get(TEST_LOCATION_ID));
  }

  private Preferences getPreferences() {
    return new Preferences("test_prefs");
  }
}
