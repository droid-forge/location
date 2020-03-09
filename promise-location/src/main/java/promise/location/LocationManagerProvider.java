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

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;

import androidx.core.app.ActivityCompat;

public class LocationManagerProvider implements LocationProvider, LocationListener {
  private static final String LOCATION_MANAGER_PROVIDER_ID = "LMP_ID";

  private LocationManager locationManager;
  private LocationUpdatedListener listener;
  private LocationStore locationStore;
  private Logger logger;
  private Context mContext;

  @Override
  public void init(Context context, Logger logger) {
    this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

    this.logger = logger;
    mContext = context;
    locationStore = new LocationStore();
  }

  @Override
  public void start(
      LocationUpdatedListener listener, LocationParams params, boolean singleUpdate) {
    this.listener = listener;
    if (listener == null) {
      logger.d("Listener is null, you sure about this?");
    }

    Criteria criteria = getProvider(params);

    if (singleUpdate) {
      if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
          != PackageManager.PERMISSION_GRANTED
          && ActivityCompat.checkSelfPermission(
          mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
          != PackageManager.PERMISSION_GRANTED) {
        logger.i(
            "Permission check failed. Please handle it in your app before setting up location");
        // TODO: Consider calling ActivityCompat#requestPermissions here to request the
        // missing permissions, and then overriding onRequestPermissionsResult
        // to handle the case where the user grants the permission. See the documentation
        // for ActivityCompat#requestPermissions for more details.

        return;
      }
      locationManager.requestSingleUpdate(criteria, this, Looper.getMainLooper());
    } else {
      locationManager.requestLocationUpdates(
          params.getInterval(), params.getDistance(), criteria, this, Looper.getMainLooper());
    }
  }

  @Override
  public void stop() {
    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
      // TODO: Consider calling
      //    ActivityCompat#requestPermissions
      // here to request the missing permissions, and then overriding
      //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
      //                                          int[] grantResults)
      // to handle the case where the user grants the permission. See the documentation
      // for ActivityCompat#requestPermissions for more details.
      return;
    }
    locationManager.removeUpdates(this);
  }

  @Override
  public Location getLastLocation() {

    if (locationManager != null) {
      if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
          != PackageManager.PERMISSION_GRANTED
          && ActivityCompat.checkSelfPermission(
          mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
          != PackageManager.PERMISSION_GRANTED) {
        // TODO: Consider calling
        //    ActivityCompat#requestPermissions
        // here to request the missing permissions, and then overriding
        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
        //                                          int[] grantResults)
        // to handle the case where the user grants the permission. See the documentation
        // for ActivityCompat#requestPermissions for more details.
        return null;
      }
      Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
      if (location != null) {
        return location;
      }
    }

    Location location = locationStore.get(LOCATION_MANAGER_PROVIDER_ID);
    return location;
  }

  private Criteria getProvider(LocationParams params) {
    final LocationAccuracy accuracy = params.getAccuracy();
    final Criteria criteria = new Criteria();
    switch (accuracy) {
      case HIGH:
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setBearingAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setSpeedAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        break;
      case MEDIUM:
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_MEDIUM);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_MEDIUM);
        criteria.setBearingAccuracy(Criteria.ACCURACY_MEDIUM);
        criteria.setSpeedAccuracy(Criteria.ACCURACY_MEDIUM);
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
        break;
      case LOW:
      case LOWEST:
      default:
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_LOW);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_LOW);
        criteria.setBearingAccuracy(Criteria.ACCURACY_LOW);
        criteria.setSpeedAccuracy(Criteria.ACCURACY_LOW);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
    }
    return criteria;
  }

  @Override
  public void onLocationChanged(Location location) {
    logger.d("onLocationChanged", location);
    if (listener != null) {
      listener.onLocationUpdated(location);
    }
    if (locationStore != null) {
      logger.d("Stored in SharedPreferences");
      locationStore.put(LOCATION_MANAGER_PROVIDER_ID, location);
    }
  }

  @Override
  public void onStatusChanged(String provider, int status, Bundle extras) {
  }

  @Override
  public void onProviderEnabled(String provider) {
  }

  @Override
  public void onProviderDisabled(String provider) {
  }
}
