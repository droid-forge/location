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

package promise.dev4vin.promiselocation.util;

import android.content.Context;
import android.location.Location;

import promise.location.LocationUpdatedListener;
import promise.location.LocationProvider;
import promise.location.LocationParams;
import promise.location.Logger;

public class MockLocationProvider implements LocationProvider {

  private LocationUpdatedListener listener;

  @Override
  public void init(Context context, Logger logger) {
  }

  @Override
  public void start(
      LocationUpdatedListener listener, LocationParams params, boolean singleUpdate) {
    this.listener = listener;
  }

  @Override
  public void stop() {
  }

  @Override
  public Location getLastLocation() {
    return null;
  }

  public void fakeEmitLocation(Location location) {
    listener.onLocationUpdated(location);
  }
}
