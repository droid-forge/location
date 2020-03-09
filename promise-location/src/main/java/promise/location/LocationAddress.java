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

import android.location.Address;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

public class LocationAddress implements Parcelable {
  public static final Parcelable.Creator CREATOR =
      new Parcelable.Creator() {
        public LocationAddress createFromParcel(Parcel in) {
          return new LocationAddress(in);
        }

        public LocationAddress[] newArray(int size) {
          return new LocationAddress[size];
        }
      };
  private Location location;
  private Address address;

  public LocationAddress(Address address) {
    this.address = address;
    location = new Location(LocationAddress.class.getCanonicalName());
    location.setLatitude(address.getLatitude());
    location.setLongitude(address.getLongitude());
  }

  public LocationAddress(Parcel in) {
    this.location = in.readParcelable(Location.class.getClassLoader());
    this.address = in.readParcelable(Address.class.getClassLoader());
  }

  public Location getLocation() {
    return location;
  }

  public Address getAddress() {
    return address;
  }

  public String getFormattedAddress() {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
      builder.append(address.getAddressLine(i));
    }
    return builder.toString();
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelable(this.location, flags);
    dest.writeParcelable(this.address, flags);
  }
}
