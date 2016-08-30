package com.example.ex01day01;

import java.util.ArrayList;

import com.example.DB.MyDataBase;
import com.example.Util.Constants;
import com.example.Util.MusicInfo;
import com.example.Util.MyApplication;
import com.example.ex01day01.MyService.MyBinder;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.widget.TabHost.TabSpec;
import com.example.fragment.Fragment1;
import com.example.fragment.Fragment2;
import com.example.fragment.Fragment3;



public class MainActivity extends FragmentActivity implements OnClickListener {
	private MyBinder myBinder;
	private SQLiteDatabase mDB;
	ArrayList<MusicInfo> list = new ArrayList<MusicInfo>();
	private int musicIndex = -1;

	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			myBinder = (MyBinder) service;
			// 初始化播放状态
			if (myBinder.isPrepared()) {
				// 更新控件
				referUI();
				if (myBinder.isPlaying()) {
					mImagePlay.setImageResource(R.drawable.selector_pause);
				} else {
					mImagePlay.setImageResource(R.drawable.selector_play);
				}
			} else {// 显示退出应用前的播放记录
					// 查询缓存列表
				query();
				musicIndex = sp.getInt(Constants.SP_PLAYINGLIST, -1);
				if (list.size() > 0 && musicIndex != -1) {
					myBinder.setMusicList(list);
					MusicInfo info = list.get(musicIndex);
					myBinder.setPosition(musicIndex);
					mTvMusicName.setText(info.getMusicName());
					mTvSinger.setText(info.getSinger());
				} else {
					mBar.setEnabled(false);
					return;
				}

			}
			mBar.setEnabled(true);

		}
	};
	private SharedPreferences sp;
	private Editor mEdit;
	private ImageView mImagePlay;
	private ImageView mImageNext;
	private TextView mTvSinger;
	private TextView mTvMusicName;
	private SeekBar mBar;
	private ImageView mImageSinger;
	private MusicInfo musicInfo;
	private MyReceiver myReceiver;
	protected boolean isTouch;
	protected int progress;
	private MyApplication myApplication;
	

	// FragmentTabhost
	private FragmentTabHost mTabHost;
	//布局填充器
	private LayoutInflater mLayoutInflater;
	//Fragment数组界面
	@SuppressWarnings("rawtypes")
	private Class mFragmentArray[] = { Fragment1.class, Fragment2.class,
			Fragment3.class};
	//存放图片数组
	private int mImageArray[] = {R.drawable.image_listen_selector,
			R.drawable.image_watch_selector,R.drawable.image_sing_selector};
  // 选项卡文字
	private String mTextArray[] = { "听音乐", "看MTV", "唱KTV"};
	private ImageView mImagePlaylist;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//隐藏标题栏
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//关联布局
		setContentView(R.layout.activity_main);
		//添加启动的Activity
		myApplication = (MyApplication) getApplication();
		myApplication.addAcivity(this);
		//参数共享
		sp = getSharedPreferences(Constants.SP_PLAYINGLIST, 0);
		mEdit = sp.edit();
		//数据库存储
		MyDataBase myDataBase = new MyDataBase(this);
		mDB = myDataBase.getReadableDatabase();
		
		initView();
		initUI();

	}
	private void initUI() {

		mImagePlay = (ImageView) findViewById(R.id.img_main_play);
		mImageNext = (ImageView) findViewById(R.id.img_main_next);
		mTvMusicName = (TextView) findViewById(R.id.tv_mian_musicName);
		mTvSinger = (TextView) findViewById(R.id.tv_main_singer);
		mBar = (SeekBar) findViewById(R.id.sb_main);
		mImageSinger = (ImageView) findViewById(R.id.img_main_singer);
		mImagePlaylist = (ImageView) findViewById(R.id.img_main_playinglist);
		
		mImagePlay.setOnClickListener(this);
		mImageNext.setOnClickListener(this);
		mImagePlaylist.setOnClickListener(this);

		mBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// 停止触碰滑块
				isTouch = false;
				if (myBinder.isPrepared()) {
					// 拖拽停止后从当前位置播放
					myBinder.seekTo(progress);
				} else {
					//没有歌曲播放是拖拽进度条  播放该歌曲
					if (list.size() > 0) {
						myBinder.PlayMusic(musicIndex);
						mImagePlay.setImageResource(R.drawable.selector_pause);
					}
				}

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// 开始触碰滑块
				isTouch = true;
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {// 保存用户拖拽后的进度
					MainActivity.this.progress = progress;
				}
			}
		});
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

	@Override
	protected void onDestroy() {
		// 保存后台正在播放的列表
		ArrayList<MusicInfo> playList = myBinder.getPlayList();
		if (playList.size() > 0) {
			// 删除表格
			mDB.delete(Constants.TABLE_PLAYINGLIST, null, null);
			for (int i = 0; i < playList.size(); i++) {
				MusicInfo musicInfo = playList.get(i);
				ContentValues values = new ContentValues();
				values.put(Constants.SINGER, musicInfo.getSinger());
				values.put(Constants.MUSICNAME, musicInfo.getMusicName());
				values.put(Constants.MUSICURL, musicInfo.getMusicUrl());
				values.put(Constants.IMAGEURL, musicInfo.getImageUrl());
				values.put(Constants.MUSICLEN, musicInfo.getMusicLen());
				mDB.insert(Constants.TABLE_PLAYINGLIST, null, values);
			}
			// 保存退出应用前后台正在播放歌曲下标
			int musicIndex = myBinder.getMusicIndex();
			mEdit.putInt(Constants.SP_PLAYINGLIST, musicIndex);
			mEdit.commit();
		}
		//移除关闭的界面
		super.onDestroy();
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
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.img_main_play:// 播放 暂停
			play();
			break;
		case R.id.img_main_next:// 下一首
			if (myBinder.isPrepared()) {
				myBinder.playNext();
				mImagePlay.setImageResource(R.drawable.selector_pause);
			}
			break;
		case R.id.img_main_playinglist://跳转正在播放界面
			Intent intent = new Intent(this, PlayingListActivity.class);
			startActivity(intent);
			break;
		default:
			break;
		}
	}

	@SuppressWarnings("deprecation")
	public void referUI() {
		if (myBinder == null) {
			return;
		}
		musicInfo = myBinder.getMusicInfo();
		if (null != musicInfo) {
			mTvSinger.setText(musicInfo.getSinger());
			mTvMusicName.setText(musicInfo.getMusicName());
			mBar.setMax(musicInfo.getMusicLen() / 1000);

			Bitmap bimap = musicInfo.bimap;
			if (bimap == null) {
				Drawable drawable = getResources().getDrawable(R.drawable.kg_eq_pop_little);
				mImageSinger.setBackgroundDrawable(drawable);
			} else {
				mImageSinger.setImageBitmap(bimap);
			}
		}

		mBar.post(new Runnable() {
			@Override
			public void run() {
				// 获得音乐播放进度
				if (!isTouch) {// 停止触碰时更新进度
					int currentProgress = myBinder.getCurrentPosition();
					mBar.setProgress(currentProgress);
				}
				mBar.postDelayed(this, 1000);
			}
		});
	}

	private void play() {
		if (myBinder == null) {
			return;
		}
		// 判断当前歌曲是否第一次播放
		boolean prepared = myBinder.isPrepared();
		if (prepared) {// 已经准备完成 歌曲在播放
			// 获得音乐播放状态
			boolean playing = myBinder.isPlaying();
			if (playing) {// 播放状态
				myBinder.pause();// 暂停歌曲
				mImagePlay.setImageResource(R.drawable.selector_play);
			} else {// 暂停状态
				myBinder.start();// 播放歌曲
				mImagePlay.setImageResource(R.drawable.selector_pause);
			}
		} else {// 第一次播放 （歌曲还未准备）

			if (list.size() > 0) {
				myBinder.PlayMusic(musicIndex);
				mImagePlay.setImageResource(R.drawable.selector_pause);
			} else {
				mImagePlay.setImageResource(R.drawable.selector_play);
			}
		}
	}

	class MyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 接收服务中准备完成后发送的广播 更新控件
			referUI();
		}

	}

	//TODO
	//初始化组件
	private void initView() {
		mLayoutInflater = LayoutInflater.from(this);

		// 找到TabHost
		mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
		// 得到fragment的个数
		int count = mFragmentArray.length;
		for (int i = 0; i < count; i++) {
			// 给每个Tab按钮设置图标、文字和内容
			TabSpec tabSpec = mTabHost.newTabSpec(mTextArray[i])
					.setIndicator(getTabItemView(i));
			// 将Tab按钮添加进Tab选项卡中
			mTabHost.addTab(tabSpec, mFragmentArray[i], null);
			// 设置Tab按钮的背景
			mTabHost.getTabWidget().getChildAt(i)
					.setBackgroundResource(R.drawable.selector_tab_background);
		}
	}

	//给每个Tab按钮设置图标和文字
	@SuppressLint("InflateParams")
	private View getTabItemView(int index) {
		View view = mLayoutInflater.inflate(R.layout.tab_item_view, null);
		ImageView imageView = (ImageView) view.findViewById(R.id.imageview);
		imageView.setImageResource(mImageArray[index]);
		TextView textView = (TextView) view.findViewById(R.id.textview);
		textView.setText(mTextArray[index]);

		return view;
	}
	
}
