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

package promise.location.geofencing;

import com.google.android.gms.location.Geofence;

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
public class GeoFenceStoreTest {

  private static final double DELTA = 1e-7;

  private static final String TEST_GEOFENCE_ID = "test_geofence_1";

  private GeoFenceModel testGeofence;

  @Before
  public void setup() {
    testGeofence =
        new GeoFenceModel.Builder("a_test_geofence")
            .setExpiration(1234)
            .setLatitude(50.505050)
            .setLongitude(-40.4040)
            .setRadius(12.34f)
            .setLoiteringDelay(100)
            .setTransition(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build();
  }

  @Test
  public void test_geofencing_store_full_cycle() {
    Promise.init(RuntimeEnvironment.application);
    GeoFenceStore store = new GeoFenceStore();
    store.setPreferences(getPreferences());

    Assert.assertNull(store.get(TEST_GEOFENCE_ID));

    store.put(TEST_GEOFENCE_ID, testGeofence);
    GeoFenceModel geofenceModel = store.get(TEST_GEOFENCE_ID);
    Assert.assertEquals(geofenceModel.getLatitude(), testGeofence.getLatitude(), DELTA);
    Assert.assertEquals(geofenceModel.getLongitude(), testGeofence.getLongitude(), DELTA);
    Assert.assertEquals(geofenceModel.getExpiration(), testGeofence.getExpiration());
    Assert.assertEquals(geofenceModel.getRadius(), testGeofence.getRadius(), DELTA);
    Assert.assertEquals(geofenceModel.getTransition(), testGeofence.getTransition());
    Assert.assertEquals(geofenceModel.getLoiteringDelay(), testGeofence.getLoiteringDelay());

    store.remove(TEST_GEOFENCE_ID);
    Assert.assertNull(store.get(TEST_GEOFENCE_ID));
  }

  private Preferences getPreferences() {
    return new Preferences("test_prefs");
  }
}
