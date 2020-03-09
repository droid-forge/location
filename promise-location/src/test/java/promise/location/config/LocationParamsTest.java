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

package promise.location.config;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import promise.dev4vin.promiselocation.PromiseLocationRobolectricTestRunner;
import promise.location.LocationAccuracy;
import promise.location.LocationParams;


@RunWith(PromiseLocationRobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class LocationParamsTest {

  private static final LocationAccuracy ACCURACY = LocationAccuracy.HIGH;
  private static final long INTERVAL = 1000;
  private static final float DISTANCE = 1000f;
  private static final double DELTA = 1e-7;

  @Test
  public void test_location_params_builder() {
    LocationParams locationParams =
        new LocationParams.Builder()
            .setAccuracy(ACCURACY)
            .setInterval(INTERVAL)
            .setDistance(DISTANCE)
            .build();

    Assert.assertEquals(locationParams.getAccuracy(), ACCURACY);
    Assert.assertEquals(locationParams.getDistance(), DISTANCE, DELTA);
    Assert.assertEquals(locationParams.getInterval(), INTERVAL);
  }
}
