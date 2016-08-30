package com.example.ex01day01;

import com.example.DB.MyDataBase;
import com.example.Util.Constants;
import com.example.Util.MusicInfo;
import com.example.Util.MyApplication;
import com.example.Util.TimeUtil;
import com.example.ex01day01.MyService.MyBinder;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class PlayActivity extends Activity implements OnClickListener {

	private MyBinder myBinder;
	// int len = 210;
	int modle = 0;
	// 保存播放模式
	int[] imgModle = new int[] { R.drawable.ic_player_mode_all_default, R.drawable.ic_player_queue_mode_random_default,
			R.drawable.ic_player_queue_mode_single_default };
	String[] toastModle = new String[] { "顺序播放", "随机播放", "单曲循环" };
	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			myBinder = (MyBinder) service;
			// 获得服务中的播放模式
			modle = myBinder.getModle();
			mImageModle.setImageResource(imgModle[modle]);

			// 初始化播放状态
			if (myBinder.isPrepared()) {
				// 更新控件
				referUI();

				if (myBinder.isPlaying()) {
					mImagePlay.setImageResource(R.drawable.selector_pause);
				} else {
					mImagePlay.setImageResource(R.drawable.selector_play);
				}
			}

		}
	};
	private ImageView mImagePlay;
	private SeekBar mBar;
	private MyReceiver myReceiver;
	private TextView mTvCurrent;
	private TextView mTvTotal;
	protected boolean isTouch;
	protected int progress;
	private TextView mTvSinger;
	private TextView mTvMusicName;
	private ImageView mImageSinger;
	private ImageView mImageNext;
	private ImageView mImagePre;
	private ImageView mImageModle;
	private RelativeLayout mPlaylayout;
	private ImageView mImageBack;
	private ImageView mImageFavor;
	private SQLiteDatabase mDB;
	private MusicInfo musicInfo;
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
		setContentView(R.layout.activity_play);
		// 添加启动的Activity
		myApplication = (MyApplication) getApplication();
		myApplication.addAcivity(this);
		MyDataBase myDataBase = new MyDataBase(this);
		mDB = myDataBase.getReadableDatabase();
		initUI();
	}

	private void initUI() {
		mPlaylayout = (RelativeLayout) findViewById(R.id.play_layout);
		mTvCurrent = (TextView) findViewById(R.id.tv_currenttime);
		mTvSinger = (TextView) findViewById(R.id.tv_play_singer);
		mTvMusicName = (TextView) findViewById(R.id.tv_play_musicName);
		mImageFavor = (ImageView) findViewById(R.id.img_favor);
		mImageBack = (ImageView) findViewById(R.id.img_back);
		mImagePlay = (ImageView) findViewById(R.id.imag_play);
		mImagePre = (ImageView) findViewById(R.id.img_pre);
		mImageNext = (ImageView) findViewById(R.id.img_next);
		mImageModle = (ImageView) findViewById(R.id.img_modle);
		mImageFavor.setOnClickListener(this);
		mImageBack.setOnClickListener(this);
		mImageNext.setOnClickListener(this);
		mImagePre.setOnClickListener(this);
		mImagePlay.setOnClickListener(this);
		mImageModle.setOnClickListener(this);
		mTvTotal = (TextView) findViewById(R.id.tv_totaltime);
		mBar = (SeekBar) findViewById(R.id.seekBar1);
		// mTvTotal.setText(TimeUtil.timeFont(len));
		// mBar.setMax(len);

		mBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// 停止触碰滑块
				isTouch = false;
				// 拖拽停止后从当前位置播放
				myBinder.seekTo(progress);

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// 开始触碰滑块
				isTouch = true;
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				mTvCurrent.setText(TimeUtil.timeFont(progress));
				if (fromUser) {// 保存用户拖拽后的进度
					PlayActivity.this.progress = progress;
				}
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
				// 准备播放
			// myBinder.PlayMusic();
			mImagePlay.setImageResource(R.drawable.selector_pause);
		}
	}

	// 控件更新
	public void referUI() {
		if (myBinder == null) {
			return;
		}
		musicInfo = myBinder.getMusicInfo();
		if (null != musicInfo) {
			mTvSinger.setText(musicInfo.getSinger());
			mTvMusicName.setText(musicInfo.getMusicName());
			mBar.setMax(musicInfo.getMusicLen() / 1000);
			mTvTotal.setText(TimeUtil.timeFont(musicInfo.getMusicLen() / 1000));

			Bitmap bimap = musicInfo.bimap;
			if (bimap == null) {
				// mImageSinger.setImageResource(R.drawable.ic_launcher);
				// int color=Color.parseColor("#0C6FC0");
				// mPlaylayout.setBackgroundColor(color);
				Drawable drawable = getResources().getDrawable(R.drawable.kg_eq_pop_little);
				mPlaylayout.setBackgroundDrawable(drawable);
			} else {
				// mImageSinger.setImageBitmap(bimap);
				BitmapDrawable bitmapdraw = new BitmapDrawable(bimap);
				mPlaylayout.setBackgroundDrawable(bitmapdraw);
			}

			// 初始化收藏状态
			if (isFavor()) {
				mImageFavor.setImageResource(R.drawable.guess_user_favourite_heart_icon);
			} else {
				mImageFavor.setImageResource(R.drawable.btn_favorite_default);
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

	@Override
	protected void onStart() {
		Intent service = new Intent(this, MyService.class);
		startService(service);
		bindService(service, conn, Context.BIND_AUTO_CREATE);
		// 注册广播
		myReceiver = new MyReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.BROADCAST_REFRESHUI);
		registerReceiver(myReceiver, filter);
		super.onStart();
	}

	@Override
	protected void onStop() {
		unbindService(conn);
		// 注销广播
		unregisterReceiver(myReceiver);
		super.onStop();
	}

	class MyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 接收服务中准备完成后发送的广播 更新控件
			referUI();
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.imag_play:// 播放歌曲
			play();
			break;
		case R.id.img_next:// 下一首
			if (myBinder.isPrepared()) {
				myBinder.playNext();
			}
			break;
		case R.id.img_pre:// 上一首
			if (myBinder.isPrepared()) {
				myBinder.playPre();
			}
			break;
		case R.id.img_modle:// 设置模式
			setModle();
			break;
		case R.id.img_back:// 返回
			finish();
			break;
		case R.id.img_favor:// 收藏
			// 判断该首歌曲是否收藏
			// 有条件查询
			boolean isFavor = isFavor();
			if (isFavor) {// 收藏 点击取消收藏
				deleteFavor();
				mImageFavor.setImageResource(R.drawable.btn_favorite_default);
			} else {// 未收藏 点击收藏
				addFavor();
				mImageFavor.setImageResource(R.drawable.guess_user_favourite_heart_icon);
			}

			break;
		default:
			break;
		}

	}

	// 取消收藏
	protected void deleteFavor() {
		mDB.delete(Constants.TABLE_FAVOR, "musicUrl=?", new String[] { musicInfo.getMusicUrl() });
	}

	// 收藏数据
	protected void addFavor() {
		ContentValues values = new ContentValues();
		values.put(Constants.SINGER, musicInfo.getSinger());
		values.put(Constants.MUSICNAME, musicInfo.getMusicName());
		values.put(Constants.MUSICURL, musicInfo.getMusicUrl());
		values.put(Constants.IMAGEURL, musicInfo.getImageUrl());
		values.put(Constants.MUSICLEN, musicInfo.getMusicLen());
		mDB.insert(Constants.TABLE_FAVOR, null, values);
	}

	// 有条件查询 是否收藏
	private boolean isFavor() {
		Cursor cursor = mDB.query(Constants.TABLE_FAVOR, null, "musicUrl=?", new String[] { musicInfo.getMusicUrl() },
				null, null, null);
		return cursor.moveToFirst();
	}

	private void setModle() {
		modle++;
		modle %= 3;
		mImageModle.setImageResource(imgModle[modle]);
		Toast.makeText(this, toastModle[modle], Toast.LENGTH_SHORT).show();
		// 传递播放模式到服务中
		myBinder.setModle(modle);
	}
}
