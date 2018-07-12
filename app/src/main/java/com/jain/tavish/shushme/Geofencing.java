package com.jain.tavish.shushme;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;

import java.util.ArrayList;
import java.util.List;

public class Geofencing implements ResultCallback<Status> {

    private GoogleApiClient googleApiClient;
    private List<Geofence> geofenceList;
    private PendingIntent mGeofencePendingIntent;
    private Context mContext;

    public Geofencing(Context mContext, GoogleApiClient googleApiClient) {
        this.mContext = mContext;
        this.googleApiClient = googleApiClient;
        geofenceList = new ArrayList<>();
        mGeofencePendingIntent = null;
    }

    public void registerAllGoefences(){
        if(googleApiClient ==  null || !googleApiClient.isConnected() || geofenceList == null || geofenceList.size() == 0){
            return;
        }
            try {
                LocationServices.GeofencingApi.addGeofences(
                        googleApiClient,
                        getGeofencingRequest(),
                        getGeofencePendingIntent()).setResultCallback(this);
            }catch (SecurityException e){
                Log.e("tavish", e.getMessage());
            }
    }

    public void  unRegisterAllGeofences(){
        if(googleApiClient == null || !googleApiClient.isConnected()){
            return;
        }

        try{
            LocationServices.GeofencingApi.removeGeofences(
                    googleApiClient,
                    getGeofencePendingIntent()
            ).setResultCallback(this);
        }catch (SecurityException e){
            Log.e("tavish", e.getMessage());
        }

    }

    public void updateGeofencesList(PlaceBuffer places){
        geofenceList = new ArrayList<>();

        for(Place place : places){
            String placeUID = place.getId();
            double placeLat = place.getLatLng().latitude;
            double placeLon = place.getLatLng().longitude;

            Geofence geofence = new Geofence.Builder()
                                            .setRequestId(placeUID)
                                            .setExpirationDuration(24 * 60 * 60 * 1000)
                                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                                            .setCircularRegion(placeLat, placeLon, 50)
                                            .build();

            geofenceList.add(geofence);

        }
    }

    public GeofencingRequest getGeofencingRequest(){
        GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                                                                   .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                                                                   .addGeofences(geofenceList)
                                                                   .build();
        return geofencingRequest;
    }

    public PendingIntent getGeofencePendingIntent(){
        if(mGeofencePendingIntent != null){
            return mGeofencePendingIntent;
        }else{
            Intent intent = new Intent(mContext, GeofenceBroadcastReceiver.class);
            mGeofencePendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            return mGeofencePendingIntent;
        }
    }

    @Override
    public void onResult(@NonNull Status status) {
        Log.e("tavish", String.format("Error adding/removing geofence : %s",  status.getStatus().toString()));
    }
}
