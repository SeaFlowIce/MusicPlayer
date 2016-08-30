package com.example.ex01day01;

import java.util.ArrayList;

import com.example.DB.MyDataBase;
import com.example.Util.Constants;
import com.example.Util.MusicInfo;
import com.example.Util.MyApplication;
import com.example.ex01day01.MyService.MyBinder;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class PlayingListActivity extends Activity {
	private MyBinder myBinder;
	ArrayList<MusicInfo> list = new ArrayList<MusicInfo>();
	private int musicIndex = -1;// 后台正在播放的歌曲下标

	private ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			myBinder = (MyBinder) service;
			if (myBinder.isPrepared()) {
				// 获得后台正在播放的列表
				ArrayList<MusicInfo> playList = myBinder.getPlayList();
				musicIndex = myBinder.getMusicIndex();
				list.clear();// 清空容器
				list.addAll(playList);// 保存正在播放列表
				myAdapter.notifyDataSetChanged();
			} else {// 第一启动应用 还未播放歌曲 显示缓存列表
					// 获得缓存正在播放的歌曲下标
				musicIndex = sp.getInt(Constants.SP_PLAYINGLIST, -1);
				// 查询数据库
				query();
			}
			// 滑动到指定行
			mListView.setSelection(musicIndex);

		}
	};
	private ListView mListView;
	private Myadapter myAdapter;
	private MyReceiver myReceiver;
	private SQLiteDatabase mDB;
	private SharedPreferences sp;
	private MyApplication myApplication;

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		myApplication.removeAcivity(this);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_playing_list);
		// 添加启动的Activity
		myApplication = (MyApplication) getApplication();
		myApplication.addAcivity(this);
		sp = getSharedPreferences(Constants.SP_PLAYINGLIST, 0);

		MyDataBase myDataBase = new MyDataBase(this);
		mDB = myDataBase.getReadableDatabase();
		RelativeLayout layoutOutside = (RelativeLayout) findViewById(R.id.layout_outside);
		layoutOutside.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mListView = (ListView) findViewById(R.id.listView1);
		
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//获得正在播放的音乐地址
				String oldMusicUrl = myBinder.getMusicUrl();
				// 传递当前容器到服务中
				myBinder.setMusicList(list);
				//获得点击行的音乐地址
				String newMusicUrl = list.get(position).getMusicUrl();
				if (newMusicUrl .equals(oldMusicUrl)) {
					// 点击同一行
					startActivity(new Intent(PlayingListActivity.this,PlayActivity.class));
				}
			}	
		});
		myAdapter = new Myadapter();
		mListView.setAdapter(myAdapter);
	}

	protected void query() {
		list.clear();
		Cursor cursor = mDB.query(Constants.TABLE_PLAYINGLIST, null, null, null, null, null, null);
		boolean first = cursor.moveToFirst();
		while (first) {
			String singer = cursor.getString(cursor.getColumnIndex(Constants.SINGER));
			String musicName = cursor.getString(cursor.getColumnIndex(Constants.MUSICNAME));
			String musicUrl = cursor.getString(cursor.getColumnIndex(Constants.MUSICURL));
			String imageUrl = cursor.getString(cursor.getColumnIndex(Constants.IMAGEURL));
			int musicLen = cursor.getInt(cursor.getColumnIndex(Constants.MUSICLEN));
			list.add(new MusicInfo(singer, musicName, musicLen, musicUrl, imageUrl));
			first = cursor.moveToNext();
		}
		cursor.close();
		myAdapter.notifyDataSetChanged();

	}

	class Myadapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View layout = getLayoutInflater().inflate(R.layout.playinglist_item, null);
			TextView tvMusicName = (TextView) layout.findViewById(R.id.tv_playing_musicName);
			TextView tvSinger = (TextView) layout.findViewById(R.id.tv_playing_singer);
			TextView tvNum = (TextView) layout.findViewById(R.id.tv_playing_num);
			ImageView imgSinger = (ImageView) layout.findViewById(R.id.img_playing_singer);
			ImageView imgFavor = (ImageView) layout.findViewById(R.id.img_playing_favor);
			MusicInfo info = list.get(position);
			tvMusicName.setText(info.getMusicName());
			tvSinger.setText(info.getSinger());
			tvNum.setText((position + 1) + "");

			// 正在播放的行显示字体颜色 跟布局与其他行不同
			if (position == musicIndex) {// 后台正在播放的歌曲所在行
				int color = Color.parseColor("#40ABF7");
				tvMusicName.setTextColor(color);
				tvSinger.setTextColor(color);
				imgFavor.setVisibility(View.VISIBLE);
				imgSinger.setVisibility(View.VISIBLE);
				tvNum.setVisibility(View.INVISIBLE);

				Bitmap bimap = info.bimap;
				if (bimap != null) {
					imgSinger.setImageBitmap(bimap);
				} else {
					imgSinger.setImageResource(R.drawable.ic_lanuch);
				}
			} else {
				imgSinger.setVisibility(View.INVISIBLE);
				imgFavor.setVisibility(View.INVISIBLE);
				tvNum.setVisibility(View.VISIBLE);
			}

			return layout;
		}

	}

	@Override
	protected void onStart() {
		Intent service = new Intent(this, MyService.class);
		startService(service);
		bindService(service, conn, Context.BIND_AUTO_CREATE);
		myReceiver = new MyReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.BROADCAST_REFRESHUI);
		registerReceiver(myReceiver, filter);
		super.onStart();
	}

	@Override
	protected void onStop() {
		unbindService(conn);
		unregisterReceiver(myReceiver);
		super.onStop();
	}

	class MyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 播放新的歌曲时 刷新正在播放界面
			musicIndex = myBinder.getMusicIndex();
			// 刷新界面
			myAdapter.notifyDataSetChanged();
			// 滑动到指定行
			mListView.setSelection(musicIndex);
		}

	}
}
