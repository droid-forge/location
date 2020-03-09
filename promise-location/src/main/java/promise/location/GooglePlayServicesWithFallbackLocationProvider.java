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

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;

public class GooglePlayServicesWithFallbackLocationProvider
    implements LocationProvider, GooglePlayServicesListener {

  private Logger logger;
  private LocationUpdatedListener listener;
  private boolean shouldStart = false;
  private Context context;
  private LocationParams params;
  private boolean singleUpdate = false;

  private LocationProvider provider;

  public GooglePlayServicesWithFallbackLocationProvider(Context context) {
    if (NetworkUtils.getConnectivityStatus(context) == NetworkUtils.TYPE_NOT_CONNECTED)
      provider = new LocationManagerProvider();
    else provider = new GooglePlayServicesLocationProvider();
  }

  @Override
  public void init(Context context, Logger logger) {
    this.logger = logger;
    this.context = context;

    logger.d("Currently selected provider = " + provider.getClass().getSimpleName());
    provider.init(context, logger);
  }

  @Override
  public void start(
      LocationUpdatedListener listener, LocationParams params, boolean singleUpdate) {
    shouldStart = true;
    this.listener = listener;
    this.params = params;
    this.singleUpdate = singleUpdate;
    provider.start(listener, params, singleUpdate);
  }

  @Override
  public void stop() {
    provider.stop();
    shouldStart = false;
  }

  @Override
  public Location getLastLocation() {
    return provider.getLastLocation();
  }

  @Override
  public void onConnected(Bundle bundle) {
    // Nothing to do here
  }

  @Override
  public void onConnectionSuspended(int i) {
    fallbackToLocationManager();
  }

  @Override
  public void onConnectionFailed(ConnectionResult connectionResult) {
    fallbackToLocationManager();
  }

  private void fallbackToLocationManager() {
    logger.d("FusedLocationProvider not working, falling back and using LocationManager");
    provider = new LocationManagerProvider();
    provider.init(context, logger);
    if (shouldStart) provider.start(listener, params, singleUpdate);
  }
}
