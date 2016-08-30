package com.example.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDataBase extends SQLiteOpenHelper{

	public MyDataBase(Context context) {
		super(context, "MusicDB", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String recentleTable="CREATE TABLE recently(id INTEGER PRIMARY KEY AUTOINCREMENT,singer VARCHAR(20),musicName VARCHAR(20),musicUrl VARCHAR(20),imageUrl VARCHAR(20),musicLen INTEGER)";
		db.execSQL(recentleTable);
		String favorTable="CREATE TABLE favor(id INTEGER PRIMARY KEY AUTOINCREMENT,singer VARCHAR(20),musicName VARCHAR(20),musicUrl VARCHAR(20),imageUrl VARCHAR(20),musicLen INTEGER)";
		db.execSQL(favorTable);
		String playingTable="CREATE TABLE playlingList(id INTEGER PRIMARY KEY AUTOINCREMENT,singer VARCHAR(20),musicName VARCHAR(20),musicUrl VARCHAR(20),imageUrl VARCHAR(20),musicLen INTEGER)";
		db.execSQL(playingTable);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

}
