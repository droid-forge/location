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

package promise.location.geocoding;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import promise.location.LocationAddress;
import promise.location.Logger;

public class AndroidGeoCodingProvider implements GeoCodingProvider {
  private static final String BROADCAST_DIRECT_GEOCODING_ACTION =
      AndroidGeoCodingProvider.class.getCanonicalName() + ".DIRECT_GEOCODE_ACTION";
  private static final String BROADCAST_REVERSE_GEOCODING_ACTION =
      AndroidGeoCodingProvider.class.getCanonicalName() + ".REVERSE_GEOCODE_ACTION";
  private static final String DIRECT_GEOCODING_ID = "direct";
  private static final String REVERSE_GEOCODING_ID = "reverse";
  private static final String LOCALE_ID = "locale";
  private static final String NAME_ID = "name";
  private static final String LOCATION_ID = "location";
  private static final String RESULT_ID = "result";

  private Locale locale;
  private GeoCodingListener geocodingListener;
  private ReverseGeoCodingListener reverseGeocodingListener;
  private HashMap<String, Integer> fromNameList;
  private HashMap<Location, Integer> fromLocationList;
  private Context context;
  private Logger logger;
  private BroadcastReceiver directReceiver =
      new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          if (BROADCAST_DIRECT_GEOCODING_ACTION.equals(intent.getAction())) {
            logger.d("sending new direct geocoding response");
            if (geocodingListener != null) {
              final String name = intent.getStringExtra(NAME_ID);
              final ArrayList<LocationAddress> results =
                  intent.getParcelableArrayListExtra(RESULT_ID);
              geocodingListener.onLocationResolved(name, results);
            }
          }
        }
      };
  private BroadcastReceiver reverseReceiver =
      new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          if (BROADCAST_REVERSE_GEOCODING_ACTION.equals(intent.getAction())) {
            logger.d("sending new reverse geocoding response");
            if (reverseGeocodingListener != null) {
              final Location location = intent.getParcelableExtra(LOCATION_ID);
              Object results1 = intent.getSerializableExtra(RESULT_ID);
              final ArrayList<Address> results = (ArrayList<Address>) results1;
              if (results == null || results.isEmpty()) return;
              reverseGeocodingListener.onAddressResolved(location, results);
            }
          }
        }
      };

  public AndroidGeoCodingProvider() {
    this(Locale.getDefault());
  }

  public AndroidGeoCodingProvider(Locale locale) {
    // This should be super weird
    if (locale == null) throw new RuntimeException("Locale is null");
    this.locale = locale;
    fromNameList = new HashMap<>();
    fromLocationList = new HashMap<>();
    if (!Geocoder.isPresent()) throw new RuntimeException(
        "Android Geocoder not present. Please check if Geocoder.isPresent() before invoking the search");
  }

  @Override
  public void init(Context context, Logger logger) {
    this.logger = logger;
    this.context = context;
  }

  @Override
  public void addName(String name, int maxResults) {
    fromNameList.put(name, maxResults);
  }

  @Override
  public void addLocation(Location location, int maxResults) {
    fromLocationList.put(location, maxResults);
  }

  @Override
  public void start(
      GeoCodingListener geocodingListener, ReverseGeoCodingListener reverseGeocodingListener) {
    this.geocodingListener = geocodingListener;
    this.reverseGeocodingListener = reverseGeocodingListener;

    if (fromNameList.isEmpty() && fromLocationList.isEmpty())
      logger.w("No direct geocoding or reverse geocoding points added");
    else {
      // Registering receivers for both possibilities
      final IntentFilter directFilter = new IntentFilter(BROADCAST_DIRECT_GEOCODING_ACTION);
      final IntentFilter reverseFilter = new IntentFilter(BROADCAST_REVERSE_GEOCODING_ACTION);

      // Launch service for processing the geocoder stuff in a background thread
      final Intent serviceIntent = new Intent(context, AndroidGeocodingService.class);
      serviceIntent.putExtra(LOCALE_ID, locale);
      if (!fromNameList.isEmpty()) {
        context.registerReceiver(directReceiver, directFilter);
        serviceIntent.putExtra(DIRECT_GEOCODING_ID, fromNameList);
      }
      if (!fromLocationList.isEmpty()) {
        context.registerReceiver(reverseReceiver, reverseFilter);
        serviceIntent.putExtra(REVERSE_GEOCODING_ID, fromLocationList);
      }
      context.startService(serviceIntent);

      // Clear hashmaps so they don't stay added for next invocations
      fromNameList.clear();
      fromLocationList.clear();
    }
  }

  @Override
  public void stop() {
    try {
      context.unregisterReceiver(directReceiver);
    } catch (IllegalArgumentException e) {
      logger.d(
          "Silenced 'receiver not registered' stuff (calling stop more times than necessary did this)");
    }

    try {
      context.unregisterReceiver(reverseReceiver);
    } catch (IllegalArgumentException e) {
      logger.d(
          "Silenced 'receiver not registered' stuff (calling stop more times than necessary did this)");
    }
  }

  public static class AndroidGeocodingService extends IntentService {

    private Geocoder geocoder;

    public AndroidGeocodingService() {
      super(AndroidGeocodingService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
      final Locale locale = (Locale) intent.getSerializableExtra(LOCALE_ID);

      if (locale == null) geocoder = new Geocoder(this);
      else geocoder = new Geocoder(this, locale);

      if (intent.hasExtra(DIRECT_GEOCODING_ID)) {
        final HashMap<String, Integer> nameList =
            (HashMap<String, Integer>) intent.getSerializableExtra(DIRECT_GEOCODING_ID);
        for (String name : nameList.keySet()) {
          int maxResults = nameList.get(name);
          final ArrayList<LocationAddress> response = addressFromName(name, maxResults);
          sendDirectGeocodingBroadcast(name, response);
        }
      }

      if (intent.hasExtra(REVERSE_GEOCODING_ID)) {
        final HashMap<Location, Integer> locationList =
            (HashMap<Location, Integer>) intent.getSerializableExtra(REVERSE_GEOCODING_ID);
        for (Location location : locationList.keySet()) {
          int maxResults = locationList.get(location);
          final ArrayList<Address> response = addressFromLocation(location, maxResults);
          sendReverseGeocodingBroadcast(location, response);
        }
      }
    }

    private void sendDirectGeocodingBroadcast(String name, ArrayList<LocationAddress> results) {
      final Intent directIntent = new Intent(BROADCAST_DIRECT_GEOCODING_ACTION);
      directIntent.putExtra(NAME_ID, name);
      directIntent.putExtra(RESULT_ID, results);
      sendBroadcast(directIntent);
    }

    private void sendReverseGeocodingBroadcast(Location location, ArrayList<Address> results) {
      final Intent reverseIntent = new Intent(BROADCAST_REVERSE_GEOCODING_ACTION);
      reverseIntent.putExtra(LOCATION_ID, location);
      reverseIntent.putExtra(RESULT_ID, results);
      sendBroadcast(reverseIntent);
    }

    private ArrayList<Address> addressFromLocation(Location location, int maxResults) {
      try {
        return new ArrayList<>(
            geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), maxResults));
      } catch (IOException ignored) {
      }
      return new ArrayList<>();
    }

    private ArrayList<LocationAddress> addressFromName(String name, int maxResults) {
      try {
        final List<Address> addresses = geocoder.getFromLocationName(name, maxResults);
        final ArrayList<LocationAddress> result = new ArrayList<>();
        for (Address address : addresses) result.add(new LocationAddress(address));
        return result;
      } catch (IOException ignored) {
      }
      return new ArrayList<>();
    }
  }
}
