package com.lmn.Arbiter_Android.DatabaseHelpers.Schemas;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.lmn.Arbiter_Android.ListItems.Layer;
import com.lmn.Arbiter_Android.Loaders.LayersListLoader;

public class LayersHelper implements ArbiterDatabaseHelper<Layer, ArrayList<Layer>>, BaseColumns{
	public static final String LAYERS_TABLE_NAME = "layers";
	public static final String LAYER_TITLE = "layer_title";
	// Feature type with prefix ex. geonode:roads
	public static final String FEATURE_TYPE = "feature_type"; 
	public static final String SERVER_ID = "server_id";
	public static final String LAYER_SRS = "srs";
	public static final String BOUNDING_BOX = "bbox";
	
	public void createTable(SQLiteDatabase db){
		String sql = "CREATE TABLE " + LAYERS_TABLE_NAME + " (" +
					_ID +
					" INTEGER PRIMARY KEY AUTOINCREMENT, " +
					LAYER_TITLE + " TEXT, " +
					FEATURE_TYPE + " TEXT, " +
					LAYER_SRS + " TEXT, " +
					BOUNDING_BOX + " TEXT, " +
					SERVER_ID + " INTEGER, " + 
					" FOREIGN KEY (" + SERVER_ID + ") REFERENCES " + 
					ServersHelper.SERVERS_TABLE_NAME + " (" + ServersHelper._ID + "));";
		
		db.execSQL(sql);
	}
	
	public Layer[] getAll(SQLiteDatabase db){
		// Projection - columns to get back
		String[] columns = {
			LAYERS_TABLE_NAME + "." + _ID, // 0
			FEATURE_TYPE, // 1
			SERVER_ID, // 2
			ServersHelper.SERVER_NAME, // 3
			LAYER_TITLE, // 4
			LAYER_SRS, // 5
			BOUNDING_BOX // 6
		};
		
		// How to sort the results
		String orderBy = LAYER_TITLE + " COLLATE NOCASE";
				
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		
		builder.setTables(LAYERS_TABLE_NAME + " INNER JOIN " + 
				ServersHelper.SERVERS_TABLE_NAME + " ON " + LAYERS_TABLE_NAME + "." + 
				SERVER_ID + " = " + ServersHelper.SERVERS_TABLE_NAME + "." + ServersHelper._ID);
		
		Cursor cursor = builder.query(db, columns, null, null, null, null, orderBy);
		
		Log.w("LAYERS HELPER", "GET LAYER COUNT: " + cursor.getCount());
		
		Layer[] layers = new Layer[cursor.getCount()];
		
		int i = 0;
		
		//Traverse the cursors to populate the projects array
		for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			layers[i] = new Layer(cursor.getInt(0),
					cursor.getString(1), cursor.getInt(2), cursor.getString(3),
					cursor.getString(4), cursor.getString(5), cursor.getString(6));
			i++;
		}
		
		cursor.close();
		return layers;
	}
	
	public void insert(SQLiteDatabase db, Context context, ArrayList<Layer> newLayers){
		
		db.beginTransaction();
		try {
			ContentValues values;
			
			for(int i = 0; i < newLayers.size(); i++){
				values = new ContentValues();
				values.put(LayersHelper.LAYER_TITLE, newLayers.get(i).getLayerTitle());
				values.put(LayersHelper.SERVER_ID, newLayers.get(i).getServerId());
				values.put(LayersHelper.FEATURE_TYPE, newLayers.get(i).getFeatureType());
				values.put(LayersHelper.BOUNDING_BOX, newLayers.get(i).getLayerBBOX());
				values.put(LayersHelper.LAYER_SRS, newLayers.get(i).getLayerSRS());
				
				
				db.insert(LAYERS_TABLE_NAME, null, values);
				Log.w("LAYERSHELPER", "LAYERS HELPER INSERT");
			}
			
			db.setTransactionSuccessful();
			
			LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(LayersListLoader.LAYERS_LIST_UPDATED));
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
	}

	@Override
	public void delete(SQLiteDatabase db, Context context, ArrayList<Layer> list) {
		Log.w("LAYERSHELPER", "LAYERSHELPER delete");
		db.beginTransaction();
		
		try {
			String whereClause = _ID + "=?";
			String[] whereArgs;
			String id;
			
			for(int i = 0; i < list.size(); i++){
				whereArgs = new String[1];
				id = Integer.valueOf(list.get(i).getLayerId()).toString();
				Log.w("LAYERSHELPER", "LAYER DELETE ID: " + id);
				whereArgs[0] = id;
				db.delete(LAYERS_TABLE_NAME, whereClause, whereArgs);
			}
			
			db.setTransactionSuccessful();
			
			LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(LayersListLoader.LAYERS_LIST_UPDATED));
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
		
	}
	
	
}
