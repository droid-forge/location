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

package promise.location.geofencing.model;

import com.google.android.gms.location.Geofence;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import promise.dev4vin.promiselocation.PromiseLocationRobolectricTestRunner;
import promise.location.geofencing.GeoFenceModel;

@RunWith(PromiseLocationRobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class GeoFenceModelTest {

  private static final double DELTA = 1e-7;
  private static final String GEOFENCE_ID = "id1";
  private static final int EXPIRATION = 1234;
  private static final double LATITUDE = 50.123456;
  private static final double LONGITUDE = -30.65312;
  private static final int LOITERING_DELAY = 100;
  private static final int RADIUS = 444;
  private static final int TRANSITION = Geofence.GEOFENCE_TRANSITION_EXIT;

  @Test
  public void test_geofence_model_creation() {
    final GeoFenceModel model =
        new GeoFenceModel.Builder(GEOFENCE_ID)
            .setExpiration(EXPIRATION)
            .setLatitude(LATITUDE)
            .setLongitude(LONGITUDE)
            .setRadius(RADIUS)
            .setTransition(TRANSITION)
            .setLoiteringDelay(LOITERING_DELAY)
            .build();

    Assert.assertEquals(model.getRequestId(), GEOFENCE_ID);
    Assert.assertEquals(model.getExpiration(), EXPIRATION);
    Assert.assertEquals(model.getLatitude(), LATITUDE, DELTA);
    Assert.assertEquals(model.getLongitude(), LONGITUDE, DELTA);
    Assert.assertEquals(model.getRadius(), RADIUS, DELTA);
    Assert.assertEquals(model.getTransition(), TRANSITION);
    Assert.assertEquals(model.getLoiteringDelay(), LOITERING_DELAY);
  }

  @Test
  public void test_geofence_model_to_geofence() {
    final GeoFenceModel model =
        new GeoFenceModel.Builder(GEOFENCE_ID)
            .setExpiration(EXPIRATION)
            .setLatitude(LATITUDE)
            .setLongitude(LONGITUDE)
            .setRadius(RADIUS)
            .setLoiteringDelay(LOITERING_DELAY)
            .setTransition(TRANSITION)
            .build();

    Geofence geofence = model.toGeoFence();

    Assert.assertNotNull(geofence);
    Assert.assertEquals(geofence.getRequestId(), GEOFENCE_ID);
  }
}
