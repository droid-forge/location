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

package promise.location.activity;

import android.app.Activity;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import promise.location.GooglePlayServicesListener;
import promise.location.Logger;

public class GooglePlayServicesActivityProvider
    implements ActivityProvider,
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener,
    ResultCallback<Status> {
  public static final int RESULT_CODE = 10002;

  private static final String GMS_ID = "GMS";
  private static final String BROADCAST_INTENT_ACTION =
      GooglePlayServicesActivityProvider.class.getCanonicalName() + ".DETECTED_ACTIVITY";
  private static final String DETECTED_ACTIVITY_EXTRA_ID = "activity";
  private final GooglePlayServicesListener googlePlayServicesListener;
  private GoogleApiClient client;
  private Logger logger;
  private ActivityUpdatedListener listener;
  private ActivityStore activityStore;
  private Context context;
  private boolean shouldStart = false;
  private boolean stopped = false;
  private PendingIntent pendingIntent;
  private ActivityParams activityParams;
  private BroadcastReceiver activityReceiver =
      new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          if (BROADCAST_INTENT_ACTION.equals(intent.getAction())
              && intent.hasExtra(DETECTED_ACTIVITY_EXTRA_ID)) {
            logger.d("sending new activity");
            DetectedActivity detectedActivity =
                intent.getParcelableExtra(DETECTED_ACTIVITY_EXTRA_ID);
            notifyActivity(detectedActivity);
          }
        }
      };

  public GooglePlayServicesActivityProvider() {
    this(null);
  }

  public GooglePlayServicesActivityProvider(GooglePlayServicesListener playServicesListener) {
    googlePlayServicesListener = playServicesListener;
  }

  @Override
  public void init(@NonNull Context context, Logger logger) {
    this.context = context;
    this.logger = logger;

    activityStore = new ActivityStore();

    if (!shouldStart) {
      this.client =
          new GoogleApiClient.Builder(context)
              .addApi(ActivityRecognition.API)
              .addConnectionCallbacks(this)
              .addOnConnectionFailedListener(this)
              .build();

      client.connect();
    } else logger.d("already started");
  }

  @Override
  public void start(ActivityUpdatedListener listener, @NonNull ActivityParams params) {
    this.activityParams = params;
    this.listener = listener;

    IntentFilter intentFilter = new IntentFilter(BROADCAST_INTENT_ACTION);
    context.registerReceiver(activityReceiver, intentFilter);

    if (client.isConnected()) startUpdating(params);
    else if (stopped) {
      shouldStart = true;
      client.connect();
      stopped = false;
    } else {
      shouldStart = true;
      logger.d("still not connected - scheduled start when connection is ok");
    }
  }

  private void startUpdating(ActivityParams params) {
    if (client.isConnected()) {
      pendingIntent =
          PendingIntent.getService(
              context,
              0,
              new Intent(context, ActivityRecognitionService.class),
              PendingIntent.FLAG_UPDATE_CURRENT);
      ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
          client, params.getInterval(), pendingIntent)
          .setResultCallback(this);
    }
  }

  @Override
  public void stop() {
    logger.d("stop");
    if (client.isConnected()) {
      ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(client, pendingIntent);
      client.disconnect();
    }
    try {
      context.unregisterReceiver(activityReceiver);
    } catch (IllegalArgumentException e) {
      logger.d(
          "Silenced 'receiver not registered' stuff (calling stop more times than necessary did this)");
    }
    shouldStart = false;
    stopped = true;
  }

  @Override
  public DetectedActivity getLastActivity() {
    if (activityStore != null) return activityStore.get(GMS_ID);
    return null;
  }

  @Override
  public void onConnected(Bundle bundle) {
    logger.d("onConnected");
    if (shouldStart) startUpdating(activityParams);
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

  private void notifyActivity(final DetectedActivity detectedActivity) {
    if (listener != null) listener.onActivityUpdated(detectedActivity);
    if (activityStore != null) activityStore.put(GMS_ID, detectedActivity);
  }

  @Override
  public void onResult(@NonNull Status status) {
    if (status.isSuccess()) logger.d("Activity update request successful");
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

  public static class ActivityRecognitionService extends IntentService {

    public ActivityRecognitionService() {
      super(ActivityRecognitionService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
      if (ActivityRecognitionResult.hasResult(intent)) {
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
        DetectedActivity mostProbableActivity = result.getMostProbableActivity();

        // Broadcast an intent containing the activity
        Intent activityIntent = new Intent(BROADCAST_INTENT_ACTION);
        activityIntent.putExtra(DETECTED_ACTIVITY_EXTRA_ID, mostProbableActivity);
        sendBroadcast(activityIntent);
      }
    }
  }
}
