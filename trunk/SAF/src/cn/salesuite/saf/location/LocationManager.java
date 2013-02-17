package cn.salesuite.saf.location;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.support.v4.app.Fragment;
import android.util.Log;

public class LocationManager implements LocationHelper.LocationListener{
	
	public static final String GPS_PROVIDER = "gps";
	public static final String NETWORK_PROVIDER = "network";
	public static final String WIFI_PROVIDER = "wifi";
	public static final String CELLID_PROVIDER = "cellid";
	
	public static final int GPS_STARTED = 1;
	public static final int NETWORK_STARTED = 2;
	public static final int GPS_STOPPED = 3;
	public static final int GPS_LOCATING = 4;
	
	private static LocationManager lmInstance = null;
	private static LocationHelper locationHelper = null;
	private ArrayList<ILocation> activities = new ArrayList<ILocation>();
	private Location lastLocation = null;
	
	public static final String SKYHOOK_HANDLER = "Skyhook";
	public static final String GOOGLE_HANDLER = "Google";
	private String locationHandler = SKYHOOK_HANDLER;
	
	
	public LocationManager(){}
	
	public static LocationManager getInstance(){
		
		if(lmInstance==null){
			lmInstance = new LocationManager();
//			locationHelper = new LocationHelper();	
//			locationHelper.registerLocationListener(lmInstance);
		}
		
		return lmInstance;
	}
	
	public void register(ILocation object){
		Activity act = null;

		if (object instanceof Activity) {
			act = (Activity)object;
		} else if (object instanceof Fragment) {
			act = ((Fragment)object).getActivity();
		} else {
			return;
		}
		
		if (act==null) {
			return;
		}
		
	    if(locationHelper==null){
	    	Context context = act.getApplicationContext();
			locationHelper = new LocationHelper(context,locationHandler);
			locationHelper.registerLocationListener(lmInstance);
		}
		activities.add(object);
		if(locationHelper.getContext() == null){
			Context context = act.getApplicationContext();
			locationHelper.setContext(context);
			if(locationHelper.status == LocationHelper.STOPPED){
				locationHelper.start();
			}
		}
	}
	
	public void unregister(ILocation object){
		Activity act = null;
		if (object instanceof Activity) {
			act = (Activity)object;
		} else if (object instanceof Fragment) {
			act = ((Fragment)object).getActivity();
		} else {
			return;
		}
		
		if (act==null) {
			return;
		}
		
		if(activities.contains(act)){
			activities.remove(act);
		}
		if(activities.size()==0){
		    locationHelper.stop();
			locationHelper.unregisterLocationListener(lmInstance);
		}
	}
	
	public void onLocationChanged(Location location){
		Log.d("LocationManager:onLocationChanged", location.getLatitude()+" "+location.getLongitude());
		Position pos = new Position(location.getLatitude(), location.getLongitude());
		//if(!location.getProvider().equals(LocationManager.NETWORK_PROVIDER)){
    		pos = LocationUtil.doShift(pos);
    	//}
		location.setLatitude(pos.getLat());
		location.setLongitude(pos.getLon());
		lastLocation = location;
		for(ILocation activity : activities){
			activity.onLocationChanged(location);
		}
	}
	
	public boolean isProviderEnabled(String provider){
		return locationHelper.isProviderEnabled(provider);
	}
	
	public Location getLastKnownLocation(){
		if(locationHelper==null){
			return null;
		}
		if(locationHelper.isQualified(lastLocation)){
			return lastLocation;
		}else{
			return null;
		}
	}	
	
	public Location getLastRawLocation(){
		if(locationHelper==null){
			return null;
		}
		
		return lastLocation;		
	}	
	
    public void destroy(){
    	if (locationHelper!=null) locationHelper.destroy();
		locationHelper = null;
		lmInstance = null;
	}
    
    public void pause(){
    	if (locationHelper!=null){
    		locationHelper.stop();
    	}
    }
    
    public void resume(){
    	if (locationHelper!=null && locationHelper.status == LocationHelper.STOPPED){
    		locationHelper.start();
    	}
    }
    
    public void push(){
    	locationHelper.start();
    }
    
    public int getStatus(){
    	return LocationHelper.status;
    }
    
    public void setLocationHandler(String handler){
    	this.locationHandler = handler;
    }
    
    public String getLocationHandler(){
    	return this.locationHandler;
    }
    
    public static LocationHelper getLocationHelper() {
		return locationHelper;
	}
}