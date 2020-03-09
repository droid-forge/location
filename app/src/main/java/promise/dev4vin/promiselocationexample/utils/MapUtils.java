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

package promise.dev4vin.promiselocationexample.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import androidx.annotation.IdRes;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by dev4vin on 11/21/17.
 */
public class MapUtils {

  public static void drawRoute(GoogleMap googleMap, LatLng start, LatLng end) {
    String url = getUrl(start, end);
    FetchUrl FetchUrl = new FetchUrl(googleMap, false);
    FetchUrl.execute(url);
  }

  public static void track(GoogleMap googleMap, LatLng start, LatLng end) {
    String url = getUrl(start, end);
    FetchUrl FetchUrl = new FetchUrl(googleMap, true);
    FetchUrl.execute(url);
  }

  private static String getUrl(LatLng origin, LatLng dest) {
    // Origin of route
    String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
    // Destination of route
    String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
    // Sensor enabled
    String sensor = "sensor=false";
    // Building the parameters to the web service
    String parameters = str_origin + "&" + str_dest + "&" + sensor;
    // Output format
    String output = "json";
    // Building the url to the web service
    return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
  }


  public static void addMarkers(
      Activity activity, GoogleMap googleMap, MarkerOptions... options) {
    LatLngBounds.Builder builder = new LatLngBounds.Builder();
    for (MarkerOptions option : options) {
      googleMap.addMarker(option);
      builder.include(option.getPosition());
    }
    LatLngBounds bounds = builder.build();
    CameraUpdate factory = CameraUpdateFactory.newLatLngBounds(bounds, dipToPixels(activity, 11));
    googleMap.moveCamera(factory);
  }

  public static int dipToPixels(Context context, float dipValue) {
    DisplayMetrics metrics = context.getResources().getDisplayMetrics();
    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
  }

  public static void addMarkers(
      Activity activity, GoogleMap googleMap, List<MarkerOptions> options) {
    LatLngBounds.Builder builder = new LatLngBounds.Builder();
    for (MarkerOptions option : options) {
      googleMap.addMarker(option);
      builder.include(option.getPosition());
    }
    LatLngBounds bounds = builder.build();
    CameraUpdate factory =
        CameraUpdateFactory.newLatLngBounds(bounds, dipToPixels(activity, 11));
    googleMap.moveCamera(factory);
  }

  public static void showMap(FragmentManager manager, @IdRes int map_id, AcquireMap acquireMap) {
    SupportMapFragment fragment = SupportMapFragment.newInstance();
    FragmentTransaction transaction = manager.beginTransaction();
    transaction.add(map_id, fragment).commit();
    showMap(fragment, acquireMap);
  }

  public static void showMap(SupportMapFragment fragment, AcquireMap acquireMap) {
    fragment.getMapAsync(acquireMap::onAcquireMap);
  }


  private static String downloadUrl(String strUrl) throws IOException {
    String data = "";
    HttpURLConnection urlConnection;
    URL url = new URL(strUrl);
    urlConnection = (HttpURLConnection) url.openConnection();
    urlConnection.connect();
    try (InputStream iStream = urlConnection.getInputStream()) {
      BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = br.readLine()) != null) sb.append(line);
      data = sb.toString();
      br.close();
    } catch (Exception ignored) {
    } finally {
      urlConnection.disconnect();
    }
    return data;
  }

  public interface CurrentLocation {
    void onCurrentLocation(LatLng latLng);

    void onCurrentLocation(String name);
  }

  public interface AcquireMap {
    void onAcquireMap(GoogleMap map);
  }

  private static class FetchUrl extends AsyncTask<String, Void, String> {
    private GoogleMap googleMap;
    private boolean track;

    FetchUrl(GoogleMap googleMap, boolean track) {
      this.googleMap = googleMap;
      this.track = track;
    }

    @Override
    protected String doInBackground(String... url) {
      String data = "";
      try {
        data = downloadUrl(url[0]);
      } catch (Exception ignored) {
      }
      return data;
    }

    @Override
    protected void onPostExecute(String result) {
      super.onPostExecute(result);
      ParserTask parserTask = new ParserTask(googleMap, track);
      parserTask.execute(result);
    }
  }

  private static class ParserTask
      extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

    private GoogleMap googleMap;
    private boolean track;

    ParserTask(GoogleMap googleMap, boolean track) {
      this.googleMap = googleMap;
      this.track = track;
    }

    @Override
    protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
      JSONObject jObject;
      List<List<HashMap<String, String>>> routes = null;
      try {
        jObject = new JSONObject(jsonData[0]);
        DataParser parser = new DataParser();
        routes = parser.parse(jObject);
      } catch (Exception ignored) {
      }
      return routes;
    }

    // Executes in UI thread, after the parsing process
    @Override
    protected void onPostExecute(List<List<HashMap<String, String>>> result) {
      if (result == null || result.isEmpty()) return;
      ArrayList<LatLng> points;
      PolylineOptions lineOptions = null;
      for (int i = 0; i < result.size(); i++) {
        points = new ArrayList<>();
        lineOptions = new PolylineOptions();
        List<HashMap<String, String>> path = result.get(i);
        for (int j = 0; j < path.size(); j++) {
          HashMap<String, String> point = path.get(j);
          double lat = Double.parseDouble(point.get("lat"));
          double lng = Double.parseDouble(point.get("lng"));
          LatLng position = new LatLng(lat, lng);
          points.add(position);
        }
        lineOptions.addAll(points);
        lineOptions.width(10);
        if (track) lineOptions.color(Color.BLACK);
        else lineOptions.color(Color.RED);
      }
      googleMap.addPolyline(lineOptions);
    }
  }

  static class DataParser {
    List<List<HashMap<String, String>>> parse(JSONObject jObject) {
      List<List<HashMap<String, String>>> routes = new ArrayList<>();
      JSONArray jRoutes;
      JSONArray jLegs;
      JSONArray jSteps;
      try {
        jRoutes = jObject.getJSONArray("routes");
        /** Traversing all routes */
        for (int i = 0; i < jRoutes.length(); i++) {
          jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
          List path = new ArrayList<>();
          for (int j = 0; j < jLegs.length(); j++) {
            jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");
            for (int k = 0; k < jSteps.length(); k++) {
              String polyline;
              polyline =
                  (String)
                      ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
              List<LatLng> list = decodePoly(polyline);
              for (int l = 0; l < list.size(); l++) {
                HashMap<String, String> hm = new HashMap<>();
                hm.put("lat", Double.toString((list.get(l)).latitude));
                hm.put("lng", Double.toString((list.get(l)).longitude));
                path.add(hm);
              }
            }
            routes.add(path);
          }
        }

      } catch (JSONException ignored) {
      } catch (Exception ignored) {
      }
      return routes;
    }

    private List<LatLng> decodePoly(String encoded) {
      List<LatLng> poly = new ArrayList<>();
      int index = 0, len = encoded.length();
      int lat = 0, lng = 0;
      while (index < len) {
        int b, shift = 0, result = 0;
        do {
          b = encoded.charAt(index++) - 63;
          result |= (b & 0x1f) << shift;
          shift += 5;
        } while (b >= 0x20);
        int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
        lat += dlat;
        shift = 0;
        result = 0;
        do {
          b = encoded.charAt(index++) - 63;
          result |= (b & 0x1f) << shift;
          shift += 5;
        } while (b >= 0x20);
        int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
        lng += dlng;
        LatLng p = new LatLng((((double) lat / 1E5)), (((double) lng / 1E5)));
        poly.add(p);
      }
      return poly;
    }
  }
}
