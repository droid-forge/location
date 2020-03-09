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

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

public class LocationState {
  @SuppressLint("StaticFieldLeak")
  private static LocationState instance;

  private Context context;
  private LocationManager locationManager;

  private LocationState(Context context) {
    this.context = context;
    this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
  }

  public static LocationState with(Context context) {
    if (instance == null) {
      instance = new LocationState(context.getApplicationContext());
    }
    return instance;
  }

  public boolean locationServicesEnabled() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      int locationMode = Settings.Secure.LOCATION_MODE_OFF;

      try {
        locationMode =
            Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

      } catch (Settings.SettingNotFoundException ignored) {

      }

      return locationMode != Settings.Secure.LOCATION_MODE_OFF;

    } else {
      String locationProviders =
          Settings.Secure.getString(
              context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
      return !TextUtils.isEmpty(locationProviders);
    }
  }

  public boolean isAnyProviderAvailable() {
    return isGpsAvailable() || isNetworkAvailable();
  }


  public boolean isGpsAvailable() {
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
  }

  public boolean isNetworkAvailable() {
    return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
  }

  public boolean isPassiveAvailable() {
    return locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);
  }

  @Deprecated
  public boolean isMockSettingEnabled() {
    return !("0"
        .equals(
            Settings.Secure.getString(
                context.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION)));
  }
}
