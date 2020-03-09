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

package promise.dev4vin.promiselocationexample;

import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import promise.dev4vin.promiselocationexample.utils.MapUtils;
import promise.location.LocationParams;
import promise.location.PromiseLocation;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

  private GoogleMap googleMap;

  private List<Location> locations;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_maps);
    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
        .findFragmentById(R.id.map);
    /*assert mapFragment != null;*/
    mapFragment.getMapAsync(this);
    locations = new ArrayList<>();
  }

  /**
   * Manipulates the map once available.
   * This callback is triggered when the map is ready to be used.
   * This is where we can add markers or lines, add listeners or move the camera. In this case,
   * we just add a marker near Sydney, Australia.
   * If Google Play services is not installed on the device, the user will be prompted to install
   * it inside the SupportMapFragment. This method will only be triggered once the user has
   * installed Google Play services and returned to the app.
   */
  @Override
  public void onMapReady(final GoogleMap googleMap) {
    this.googleMap = googleMap;
    PromiseLocation.with(this).location()
        .config(LocationParams.BEST_EFFORT).continuous().start(location -> runOnUiThread(() -> {
      locations.add(location);
      googleMap.clear();
      MapUtils.addMarkers(this, this.googleMap, locations.stream().map(loc -> new MarkerOptions()
          .position(new LatLng(loc.getLatitude(), loc.getLongitude()))
          .title("Accuracy: " + loc.getAccuracy() + " Provider: " + loc.getProvider())).collect(Collectors.toList()));
      /*LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
      CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 15);
      googleMap.addMarker(new MarkerOptions().position(latLng).title("Current location"));
      googleMap.moveCamera(update);*/
      Toast.makeText(MapsActivity.this, "Updated location", Toast.LENGTH_SHORT).show();
    }));
  }
}
