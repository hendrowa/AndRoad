// Created by plusminus on 6:15:04 PM - Mar 27, 2009
package org.androad.sys.ors.adt.ts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

import org.androad.util.constants.Constants;

import android.util.Log;

import com.att.research.rtree.RTree;
import com.att.research.spatialindex.IData;
import com.att.research.spatialindex.INode;
import com.att.research.spatialindex.ISpatialIndex;
import com.att.research.spatialindex.IVisitor;
import com.att.research.spatialindex.Point;
import com.att.research.storagemanager.MemoryStorageManager;
import com.att.research.storagemanager.PropertySet;

/**
 * 
 * @author Nicolas Gramlich
 *
 * @param <T>
 */
public class ListBackedSpatialIndexOrganizer<T extends OverlayItem> implements ISpatialDataOrganizer<T> {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private boolean mIndexBuilt = false;

	private final ISpatialIndex mSpatialIndex;

	protected final List<T> mFeatureList = new ArrayList<T>();

	// ===========================================================
	// Constructors
	// ===========================================================

	public ListBackedSpatialIndexOrganizer() {
		final MemoryStorageManager sm = new MemoryStorageManager();
		final PropertySet ps = new PropertySet();

		ps.setProperty("FillFactor", 0.7d);

		ps.setProperty("IndexCapacity", 20);
		ps.setProperty("LeafCapacity", 20);

		ps.setProperty("Dimension", 2);

		this.mSpatialIndex = new RTree(ps, sm);
	}

	public ListBackedSpatialIndexOrganizer(final List<T> pItems) {
		this();
		addAll(pItems);
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	@Override
	public boolean isIndexBuilt() {
		return this.mIndexBuilt;
	}

	@Override
	public List<T> getItems() {
		return this.mFeatureList;
	}

	@Override
	public void add(final T pItem) {
		if(this.mIndexBuilt) {
			throw new IllegalStateException("Trying to add after index was built.");
		}

		if(pItem != null) {
			this.mFeatureList.add(pItem);
		}
	}

	@Override
	public void addAll(final Collection<T> pItems){
		if(this.mIndexBuilt) {
			throw new IllegalStateException("Trying to add after index was built.");
		}

		if(pItems != null) {
			this.mFeatureList.addAll(pItems);
		}
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	@Override
	public void clearIndex(){
		this.mIndexBuilt = false;
		this.mFeatureList.clear();
	}

	@Override
	public void buildIndex(){
		this.mIndexBuilt = true;

		final double[] coords = new double[2];
		for(int i = 0; i < this.mFeatureList.size(); i++){
			Log.d(Constants.DEBUGTAG, "Inserting: " + i);
			final T ti = this.mFeatureList.get(i);

			coords[0] = ti.getPoint().getLatitudeE6() / 1E6;
			coords[1] = ti.getPoint().getLongitudeE6() / 1E6;
			this.mSpatialIndex.insertData(null, new Point(coords), i);
		}
	}

	@Override
	public List<T> getClosest(final GeoPoint pGeoPoint, final int pCount){
		if(!this.mIndexBuilt) {
			throw new IllegalStateException("Trying to query before index was built.");
		}

		final List<T> out = new ArrayList<T>();

		final double[] pointCoords = new double[]{pGeoPoint.getLatitudeE6() / 1E6, pGeoPoint.getLongitudeE6() / 1E6};
		this.mSpatialIndex.nearestNeighborQuery(pCount, new Point(pointCoords ), new IVisitor(){

			@Override
			public void visitData(final IData d) {
				out.add(ListBackedSpatialIndexOrganizer.this.mFeatureList.get(d.getIdentifier()));
			}

			@Override
			public void visitNode(final INode n) { }

		});

		return out;
	}

	@Override
	public List<T> getWithinBoundingBox(final BoundingBoxE6 boundingBoxE6, final int count) {
		throw new IllegalStateException("Wrong method!");
	}

	@Override
	public GetMode getGetMode() {
		return GetMode.CLOSEST;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
