package com.barefoot.pocketshake;

import java.util.List;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.barefoot.pocketshake.storage.EarthQuakeDataWrapper;
import com.barefoot.pocketshake.ui.QuakeItemizedMapOverlay;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class QuakeMapView extends MapActivity {

	private List<Overlay> mapOverlays;
	private EarthQuakeDataWrapper dbWrapper;
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.map);
	    dbWrapper = new EarthQuakeDataWrapper(this);
	    
	    MapView mapView = (MapView) findViewById(R.id.mapview);
	    mapView.setBuiltInZoomControls(true);
	    mapOverlays = mapView.getOverlays();

	    String[] currentQuakeDetails = dbWrapper.getStringRepresentationForQuakes();
	    
	    updateOverlaysList(currentQuakeDetails);
	}
	
	private void updateOverlaysList(String[] currentQuakeDetails) {
		Drawable drawable = this.getResources().getDrawable(R.drawable.quake);
		QuakeItemizedMapOverlay itemizedoverlay = new QuakeItemizedMapOverlay(drawable);
		createOverlayItem(itemizedoverlay, currentQuakeDetails);
		mapOverlays.add(itemizedoverlay);
	}
	
	private void createOverlayItem(QuakeItemizedMapOverlay itemizedoverlay, String[] currentQuakeDetails) {
		for (String currentQuake : currentQuakeDetails) {
			String[] details = currentQuake.split("::");
			Double longitude = Double.parseDouble(details[2]) * 10E6;
			Double latitude = Double.parseDouble(details[3]) * 10E6;
			GeoPoint point = new GeoPoint(longitude.intValue(), latitude.intValue());
			OverlayItem overlayitem = new OverlayItem(point, details[0] + " Richters", details[1]);
			itemizedoverlay.addOverlay(overlayitem);
		}
	}
}