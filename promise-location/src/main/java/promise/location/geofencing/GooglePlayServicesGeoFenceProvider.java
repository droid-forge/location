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

package promise.location.geofencing;

import android.Manifest;
import android.app.Activity;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import promise.location.GooglePlayServicesListener;
import promise.location.Logger;

public class GooglePlayServicesGeoFenceProvider
    implements GeoFenceProvider,
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener,
    ResultCallback<Status> {

  public static final int RESULT_CODE = 10003;

  public static final String BROADCAST_INTENT_ACTION =
      GooglePlayServicesGeoFenceProvider.class.getCanonicalName() + ".GEOFENCE_TRANSITION";
  public static final String GEOFENCES_EXTRA_ID = "geofences";
  public static final String TRANSITION_EXTRA_ID = "transition";
  public static final String LOCATION_EXTRA_ID = "location";

  private final List<Geofence> geofencesToAdd =
      Collections.synchronizedList(new ArrayList<Geofence>());
  private final List<String> geofencesToRemove =
      Collections.synchronizedList(new ArrayList<String>());
  private final GooglePlayServicesListener googlePlayServicesListener;
  private GoogleApiClient client;
  private Logger logger;
  private GeoFencingTransitionListener listener;
  private GeoFenceStore geoFenceStore;
  private Context context;
  private PendingIntent pendingIntent;
  private boolean stopped = false;
  private BroadcastReceiver geofencingReceiver =
      new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          if (BROADCAST_INTENT_ACTION.equals(intent.getAction())
              && intent.hasExtra(GEOFENCES_EXTRA_ID)) {
            logger.d("Received geofencing event");
            final int transitionType = intent.getIntExtra(TRANSITION_EXTRA_ID, -1);
            final List<String> geofencingIds = intent.getStringArrayListExtra(GEOFENCES_EXTRA_ID);
            for (final String geofenceId : geofencingIds) {
              // Get GeofenceModel
              GeoFenceModel geofenceModel = geoFenceStore.get(geofenceId);
              if (geofenceModel != null) listener.onGeoFenceTransition(
                  new TransitionGeoFence(geofenceModel, transitionType));
              else logger.w(
                  "Tried to retrieve geofence " + geofenceId + " but it was not in the store");
            }
          }
        }
      };

  public GooglePlayServicesGeoFenceProvider() {
    this(null);
  }

  public GooglePlayServicesGeoFenceProvider(GooglePlayServicesListener playServicesListener) {
    googlePlayServicesListener = playServicesListener;
  }

  @Override
  public void init(@NonNull Context context, Logger logger) {
    this.context = context;
    this.logger = logger;

    geoFenceStore = new GeoFenceStore();

    this.client =
        new GoogleApiClient.Builder(context)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build();

    client.connect();

    pendingIntent =
        PendingIntent.getService(
            context,
            0,
            new Intent(context, GeoFenceService.class),
            PendingIntent.FLAG_UPDATE_CURRENT);
  }

  @Override
  public void addGeoFence(GeoFenceModel geofence) {
    List<GeoFenceModel> wrapperList = new ArrayList<>();
    wrapperList.add(geofence);
    addGeoFences(wrapperList);
  }

  @Override
  public void addGeoFences(List<GeoFenceModel> geofenceList) {
    List<Geofence> convertedGeofences = new ArrayList<>();
    for (GeoFenceModel geofenceModel : geofenceList) {
      geoFenceStore.put(geofenceModel.getRequestId(), geofenceModel);
      convertedGeofences.add(geofenceModel.toGeoFence());
    }

    if (client.isConnected()) {
      if (geofencesToAdd.size() > 0) {
        convertedGeofences.addAll(geofencesToAdd);
        geofencesToAdd.clear();
      }
      if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
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
      LocationServices.GeofencingApi.addGeofences(client, convertedGeofences, pendingIntent);

    } else for (GeoFenceModel geofenceModel : geofenceList)
      geofencesToAdd.add(geofenceModel.toGeoFence());
  }

  @Override
  public void removeGeoFence(String geofenceId) {
    List<String> wrapperList = new ArrayList<>();
    wrapperList.add(geofenceId);
    removeGeoFences(wrapperList);
  }

  @Override
  public void removeGeoFences(List<String> geofenceIds) {
    for (String id : geofenceIds) geoFenceStore.remove(id);
    if (client.isConnected()) {
      if (geofencesToRemove.size() > 0) {
        geofenceIds.addAll(geofencesToRemove);
        geofencesToRemove.clear();
      }
      LocationServices.GeofencingApi.removeGeofences(client, geofenceIds);
    } else geofencesToRemove.addAll(geofenceIds);
  }

  @Override
  public void start(GeoFencingTransitionListener listener) {
    this.listener = listener;

    IntentFilter intentFilter = new IntentFilter(BROADCAST_INTENT_ACTION);
    context.registerReceiver(geofencingReceiver, intentFilter);

    if (!client.isConnected())
      logger.d("still not connected - scheduled start when connection is ok");
    else if (stopped) {
      client.connect();
      stopped = false;
    }
  }

  @Override
  public void stop() {
    logger.d("stop");
    if (client.isConnected()) client.disconnect();
    try {
      context.unregisterReceiver(geofencingReceiver);
    } catch (IllegalArgumentException e) {
      logger.d(
          "Silenced 'receiver not registered' stuff (calling stop more times than necessary did this)");
    }
    stopped = true;
  }

  @Override
  public void onConnected(Bundle bundle) {
    logger.d("onConnected");

    // TODO wait until the connection is done and retry
    if (client.isConnected()) {
      if (geofencesToAdd.size() > 0) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
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
        LocationServices.GeofencingApi.addGeofences(client, geofencesToAdd, pendingIntent);
        geofencesToAdd.clear();
      }

      if (geofencesToRemove.size() > 0) {
        LocationServices.GeofencingApi.removeGeofences(client, geofencesToRemove);
        geofencesToRemove.clear();
      }
    }
    if (googlePlayServicesListener != null) googlePlayServicesListener.onConnected(bundle);
  }

  @Override
  public void onConnectionSuspended(int i) {
    logger.d("onConnectionSuspended " + i);
    if (googlePlayServicesListener != null) googlePlayServicesListener.onConnectionSuspended(i);
  }

  @Override
  public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    logger.d("onConnectionFailed");
    if (googlePlayServicesListener != null)
      googlePlayServicesListener.onConnectionFailed(connectionResult);
  }

  @Override
  public void onResult(@NonNull Status status) {
    if (status.isSuccess()) logger.d("Geofencing update request successful");
    else // No recovery. Weep softly or inform the user.
      if (status.hasResolution() && context instanceof Activity) {
        logger.w(
            "Unable to register, but we can solve this - will startActivityForResult expecting result code "
                + RESULT_CODE
                + " (if received, please try again)");
        try {
          status.startResolutionForResult((Activity) context, RESULT_CODE);
        } catch (IntentSender.SendIntentException e) {
          logger.e(e, "problem with startResolutionForResult");
        }
      } else logger.e("Registering failed: " + status.getStatusMessage());
  }

  public static class GeoFenceService extends IntentService {

    public GeoFenceService() {
      super(GeoFenceService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
      GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
      if (geofencingEvent != null && !geofencingEvent.hasError()) {
        int transition = geofencingEvent.getGeofenceTransition();

        // Broadcast an intent containing the geofencing info
        Intent geofenceIntent = new Intent(BROADCAST_INTENT_ACTION);
        geofenceIntent.putExtra(TRANSITION_EXTRA_ID, transition);
        geofenceIntent.putExtra(LOCATION_EXTRA_ID, geofencingEvent.getTriggeringLocation());
        ArrayList<String> geofencingIds = new ArrayList<>();
        for (Geofence geofence : geofencingEvent.getTriggeringGeofences())
          geofencingIds.add(geofence.getRequestId());
        geofenceIntent.putStringArrayListExtra(GEOFENCES_EXTRA_ID, geofencingIds);
        sendBroadcast(geofenceIntent);
      }
    }
  }
}
