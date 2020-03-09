# Android Promise Location  [![](https://jitpack.io/v/android-promise/location.svg)](https://jitpack.io/#android-promise/location)
Detect the current device location. Geofence a given location, Detect movement activities.
 Geocode back location to the name of the location

### Features
* Find the current location of device
* Find Name of the location with specific coordinates
* Activity recognition
* Geofencing

## Setup
##### build.gradle
```

allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

android {
    ...
    compileOptions {
            sourceCompatibility JavaVersion.VERSION_1_8
            targetCompatibility JavaVersion.VERSION_1_8
        }
}

dependencies {
     ...
     implementation 'com.github.android-promise:location:TAG'
     implementation 'com.github.android-promise:commons:1.0'
}
```

### Initialization
Initialize Promise in your main application file, entry point

##### App.java
```java
  ...
  @Override
  public void onCreate() {
    super.onCreate();
    Promise.init(this);
    ...
  }
  ...
```

#### Sample Location of current location

```java
    @Override
  public void onMapReady(final GoogleMap googleMap) {
    this.googleMap = googleMap;
    PromiseLocation.with(this).location()
        .config(LocationParams.BEST_EFFORT).once().start(location -> runOnUiThread(() -> {
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
```
- 
## New features on the way
watch this repo to stay updated

# Developed By
* Peter Vincent - <dev4vin@gmail.com>
# Donations
If you'd like to support this library development, you could buy me coffee here:
* [![Become a Patreon]("https://c6.patreon.com/becomePatronButton.bundle.js")](https://www.patreon.com/bePatron?u=31165349)

Thank you very much in advance!

#### Pull requests / Issues / Improvement requests
Feel free to contribute and ask!<br/>

# License

    Copyright 2018 Peter Vincent

    Licensed under the Apache License, Version 2.0 Android Promise;
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

