package xyz.selfenrichment.robertotomas.popularmovies.lib;
// Created by RobertoTomás on 0001, 1, 4, 2016.

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xyz.selfenrichment.robertotomas.popularmovies.service.AbstractService;

/**
 * Since we are using multiple serviceintents in the details page it is nice to delegate setting
 * them up and attaching the listening callback in a third party like this one.
 */
public class RoutingDirector {
    private Context mContext;
    private Map<String, RoutingCallback> mRoutes;
    private BroadcastReceiver mBroadcastReceiver;
    private List<Intent> mIntents;

    public RoutingDirector(Context c){
        mContext = c;
        mRoutes = new HashMap<String, RoutingCallback>();
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                RoutingCallback callback = mRoutes.get(intent.getStringExtra(AbstractService.INTENT_TYPE));
                callback.callback(intent);
            }
        };
        mIntents = new ArrayList<Intent>();
    }

    public void initService(String route, Class service, String intentExtra, String id, RoutingCallback routingCallback) {
        putRoute(route, routingCallback);
        LocalBroadcastManager.getInstance(mContext)
                .registerReceiver(mBroadcastReceiver, new IntentFilter(route));
        Intent intent = new Intent(mContext, service);
        intent.putExtra(AbstractService.INTENT_TYPE, route);
        intent.putExtra(intentExtra, id);
        mIntents.add(intent);
    }

    public void startServices() {
        for(Intent i: mIntents) {
            mContext.startService(i);
        }
    }

    public void putRoute(String route, RoutingCallback callback){
        mRoutes.put(route, callback);
    }
    public void removeRoute(String route) {
        mRoutes.remove(route);
    }

    public interface RoutingCallback {
        public void callback(Intent i);
    }
}
