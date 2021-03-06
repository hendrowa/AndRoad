// Created by plusminus on 20:49:09 - 04.12.2008
package org.androad.osm.api.traces;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.osmdroid.contributor.util.RecordedGeoPoint;
import org.osmdroid.contributor.util.RecordedRouteGPXFormatter;
import org.osmdroid.tileprovider.util.StreamUtils;

import org.androad.osm.util.Util;
import org.androad.osm.util.constants.OSMConstants;

import android.util.Log;


public class GPXToFileWriter implements OSMConstants {
	// ===========================================================
	// Constants
	// ===========================================================

	protected static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd_EEEE_HH-mm-ss");

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	public static void writeToFileAsync(final ArrayList<RecordedGeoPoint> recordedGeoPoints){
		new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					// Ensure folder exists
					final String traceFolderPath = Util.getAndRoadExternalStoragePath() + SDCARD_SAVEDTRACES_PATH;
					new File(traceFolderPath).mkdirs();

					// Create file and ensure that needed folders exist.
					final String filename = SDF.format(new Date(System.currentTimeMillis())) + ".gpx";
					final File dest = new File(traceFolderPath + filename + ".zip");

					// Write Data
					final OutputStream out = new BufferedOutputStream(new FileOutputStream(dest),StreamUtils.IO_BUFFER_SIZE);
					final byte[] data = org.androad.osm.api.traces.util.Util.zipBytes(RecordedRouteGPXFormatter.create(recordedGeoPoints).getBytes(), filename);

					out.write(data);
					out.flush();
					out.close();
				} catch (final Exception e) {
					Log.e(OSMConstants.DEBUGTAG, "File-Writing-Error", e);
				}
			}
		}, "GPXToFileSaver-Thread").start();
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
