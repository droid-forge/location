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

import androidx.annotation.VisibleForTesting;
import androidx.collection.ArrayMap;

import promise.commons.pref.Preferences;

public class LocationStore implements Store<Location> {

  private static final String PROVIDER = "LocationStore";

  private static final String PREFERENCES_FILE = "promise_location";
  private static final String PREFIX_ID = LocationStore.class.getCanonicalName() + ".key";
  private static final String PROVIDER_ID = "provider";
  private static final String LATITUDE_ID = "lat";
  private static final String LONGITUDE_ID = "lon";
  private static final String ACCURACY_ID = "acc";
  private static final String ALTITUDE_ID = "alt";
  private static final String SPEED_ID = "spd";
  private static final String TIME_ID = "tm";
  private static final String BEARING_ID = "bearing";

  private Preferences preferences;

  public LocationStore() {
    preferences = new Preferences(PREFERENCES_FILE);
  }

  @VisibleForTesting
  public void setPreferences(Preferences preferences) {
    this.preferences = preferences;
  }

  @Override
  public void put(final String id, final Location location) {
    preferences.save(new ArrayMap<String, Object>() {{
      put(getFieldKey(id, PROVIDER_ID), location.getProvider());
      put(getFieldKey(id, LATITUDE_ID), Double.doubleToLongBits(location.getLatitude()));
      put(getFieldKey(id, LONGITUDE_ID), Double.doubleToLongBits(location.getLongitude()));
      put(getFieldKey(id, ACCURACY_ID), location.getAccuracy());
      put(getFieldKey(id, ALTITUDE_ID), Double.doubleToLongBits(location.getAltitude()));
      put(getFieldKey(id, SPEED_ID), location.getSpeed());
      put(getFieldKey(id, TIME_ID), location.getTime());
      put(getFieldKey(id, BEARING_ID), location.getBearing());
    }});
  }

  @Override
  public Location get(String id) {
    if (preferences != null) {
      Location location = new Location(!preferences.getString(PROVIDER_ID).equals(PROVIDER) ? PROVIDER : preferences.getString(PROVIDER_ID));
      location.setLatitude(
          Double.longBitsToDouble(preferences.getLong(getFieldKey(id, LATITUDE_ID))));
      location.setLongitude(
          Double.longBitsToDouble(preferences.getLong(getFieldKey(id, LONGITUDE_ID))));
      location.setAccuracy((float) preferences.getDouble(getFieldKey(id, ACCURACY_ID)));
      location.setAltitude(
          Double.longBitsToDouble(preferences.getLong(getFieldKey(id, ALTITUDE_ID))));
      location.setSpeed((float) preferences.getDouble(getFieldKey(id, SPEED_ID)));
      location.setTime(preferences.getLong(getFieldKey(id, TIME_ID)));
      location.setBearing((float) preferences.getDouble(getFieldKey(id, BEARING_ID)));
      return location;
    } else {
      return null;
    }
  }

  @Override
  public void remove(String id) {
    preferences.clear(getFieldKey(id, PROVIDER_ID));
    preferences.clear(getFieldKey(id, LATITUDE_ID));
    preferences.clear(getFieldKey(id, LONGITUDE_ID));
    preferences.clear(getFieldKey(id, ACCURACY_ID));
    preferences.clear(getFieldKey(id, ALTITUDE_ID));
    preferences.clear(getFieldKey(id, SPEED_ID));
    preferences.clear(getFieldKey(id, TIME_ID));
    preferences.clear(getFieldKey(id, BEARING_ID));
  }

  private String getFieldKey(String id, String field) {
    return PREFIX_ID + "_" + id + "_" + field;
  }
}
