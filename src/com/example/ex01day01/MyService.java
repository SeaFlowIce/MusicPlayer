package com.example.ex01day01;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import com.example.DB.MyDataBase;
import com.example.Util.Constants;
import com.example.Util.MusicInfo;
import com.example.Util.MyApplication;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;
import android.widget.RemoteViews;

public class MyService extends Service {
	ArrayList<MusicInfo> list = new ArrayList<MusicInfo>();
	private MediaPlayer mediaPlayer;
	boolean isPrepared;
	private int musicIndex = -1;
	// String path = "http://169.254.231.0:8080/beishanglibai.mp3";
	public int modle = 0;// 保存播放模式
	private SQLiteDatabase mDB;
	private MyReceiver mReceiver;
	private MyBinder myBinder;
	private NotificationManager manager;
	private Notification notification;

	@SuppressWarnings("unused")
	@Override
	public void onCreate() {
		// 创建MediaPlay对象
		mediaPlayer = new MediaPlayer();
		// 创建数据库辅助类对象
		MyDataBase myDataBase = new MyDataBase(this);
		mDB = myDataBase.getReadableDatabase();

		mReceiver = new MyReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.NOTIFY_PLAY);// 播放暂停
		filter.addAction(Constants.NOTIFY_EXIT);//退出应用
		registerReceiver(mReceiver, filter);

		// 设置歌曲播放完成的监听事件
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			// 歌曲播放完成后调用
			@Override
			public void onCompletion(MediaPlayer mp) {
				if (Constants.MODLE_SINGLE != modle) {
					// 不是单曲循环模式时自动下一首
					// 自动下一首
					// setNextIndex();
					// Log.e("onCompletion", "onCompletion");
					if (musicIndex > list.size() - 1) {
						musicIndex = 0;
					}
					musicIndex++;
					// Log.e("onCompletion", "onCompletion");
				}
				// 播放该首歌曲
				PlayMusic();
				// Log.e("onCompletion", "onCompletion==="+musicIndex);
			}
		});

		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent intent) {
		myBinder = new MyBinder();
		return myBinder;
	}

	public void PlayMusic() {
		try {
			mediaPlayer.reset();// 进入空闲状态
			mediaPlayer.setDataSource(list.get(musicIndex).getMusicUrl());// 进入初始化状态
			Log.e("PlayMusic", "musicIndex==" + musicIndex);
			mediaPlayer.prepareAsync();// 进入准备状态
			mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
				// 设置准备完成监听事件
				@Override
				public void onPrepared(MediaPlayer mp) {
					// 准备完成后播放歌曲
					mediaPlayer.start();
					// 保存是否准备完成（第一次播放）
					isPrepared = true;
					// 添加最近播放歌曲到数据库 时机
					// 删除数据
					deleteRecent();
					// 添加数据
					addRecent();

					// 发送通知
					sendNotify();
					// 准备完成后更新控件 发送广播通知界面更新控件
					Intent intent = new Intent();
					intent.setAction(Constants.BROADCAST_REFRESHUI);
					sendBroadcast(intent);
				}
			});
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@SuppressLint("NewApi")
	protected void sendNotify() {
		manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

		RemoteViews views = new RemoteViews(getPackageName(), R.layout.layout_notify);
		Intent MainIntent = new Intent(this, MainActivity.class);
		PendingIntent intent = PendingIntent.getActivity(this, 0, MainIntent, 0);
		builder.setSmallIcon(R.drawable.kugou_small).setTicker("酷狗音乐").setContentIntent(intent).setOngoing(true)
				.setContent(views);
		notification = builder.build();
		// 状态栏上显示大的图标
		notification.bigContentView = views;
		// 设置音乐基本信息
		MusicInfo musicInfo = list.get(musicIndex);
		Bitmap bitmap = musicInfo.bimap;
		if (bitmap != null) {
			notification.bigContentView.setImageViewBitmap(R.id.img_notify_singer, bitmap);
		} else {
			notification.bigContentView.setImageViewResource(R.id.img_notify_singer, R.drawable.kg_eq_pop_little);
		}
		notification.bigContentView.setTextViewText(R.id.tv_notify_musicName, musicInfo.getMusicName());
		notification.bigContentView.setTextViewText(R.id.tv_notify_singer, musicInfo.getSinger());

		// 设置通知上控件的延迟点击事件
		// 广播的过滤条件
		Intent playIntent = new Intent(Constants.NOTIFY_PLAY);
		// 控件的延时意图
		PendingIntent pendingPlayIntent = PendingIntent.getBroadcast(this, 0, playIntent, 0);
		// 设置通知上控件的延迟点击事件
		notification.bigContentView.setOnClickPendingIntent(R.id.img_notify_play, pendingPlayIntent);
		
		Intent exitIntent=new Intent(Constants.NOTIFY_EXIT);
		PendingIntent pendingExitIntent = PendingIntent.getBroadcast(this, 0, exitIntent, 0);
		//退出
		notification.bigContentView.setOnClickPendingIntent(R.id.img_notify_exit, pendingExitIntent);

		
		
		
		manager.notify(Constants.NOTIFY_ID, notification);

	}

	// 删除相同数据
	protected void deleteRecent() {
		mDB.delete(Constants.TABLE_RECENTLY, "musicUrl=?", new String[] { list.get(musicIndex).getMusicUrl() });
	}

	// 添加数据
	protected void addRecent() {
		ContentValues values = new ContentValues();
		MusicInfo musicInfo = list.get(musicIndex);
		values.put(Constants.SINGER, musicInfo.getSinger());
		values.put(Constants.MUSICNAME, musicInfo.getMusicName());
		values.put(Constants.MUSICURL, musicInfo.getMusicUrl());
		values.put(Constants.IMAGEURL, musicInfo.getImageUrl());
		values.put(Constants.MUSICLEN, musicInfo.getMusicLen());
		mDB.insert(Constants.TABLE_RECENTLY, null, values);
	}

	class MyBinder extends Binder {
		// 第一播放音乐
		public void PlayMusic(int position) {
			MyService.this.musicIndex = position;
			MyService.this.PlayMusic();
		}

		// 获得音乐的播放状态
		public boolean isPlaying() {
			return mediaPlayer.isPlaying();
		}

		// 暂停音乐
		@SuppressLint("NewApi")
		public void pause() {
			mediaPlayer.pause();
			// 更新通知栏上图片暂停
			notification.bigContentView.setImageViewResource(R.id.img_notify_play, R.drawable.selector_play);
			manager.notify(Constants.NOTIFY_ID, notification);
		}

		// 播放音乐
		@SuppressLint("NewApi")
		public void start() {
			mediaPlayer.start();
			// 更新通知栏上图片暂停
			notification.bigContentView.setImageViewResource(R.id.img_notify_play, R.drawable.selector_pause);
			manager.notify(Constants.NOTIFY_ID, notification);
		}

		// 获得音乐的准备状态（是否第一播放）
		public boolean isPrepared() {
			return isPrepared;
		}

		// 获得当前音乐播放进度
		public int getCurrentPosition() {
			if (mediaPlayer!=null) {
				return mediaPlayer.getCurrentPosition() / 1000;
			}else {
				return 0;
			}
		}

		// 从拖拽处播放音乐
		public void seekTo(int progress) {
			mediaPlayer.seekTo(progress * 1000);
		}

		// 获得正在播放的列表
		public void setMusicList(ArrayList<MusicInfo> musicList) {
			// 清空容器
			list.clear();
			// 保存列表内容
			list.addAll(musicList);
		}

		// 获得正在播放的歌曲下标
		public int getMusicIndex() {
			return musicIndex;
		}

		// 获得正在播放的歌曲信息
		public MusicInfo getMusicInfo() {
			if (list != null) {
				return list.get(musicIndex);
			} else {
				return null;
			}
		}

		// 播放下一首
		public void playNext() {
			setNextIndex();
			MyService.this.PlayMusic();
		}

		// 播放上一首
		public void playPre() {
			setPreIndex();
			MyService.this.PlayMusic();
		}

		// 设置播放模式
		public void setModle(int modle) {
			MyService.this.modle = modle;
		}

		// 设置播放模式
		public int getModle() {
			return MyService.this.modle;
		}

		// 获得后台正在播放的音乐地址
		public String getMusicUrl() {
			if (isPrepared) {
				return list.get(musicIndex).getMusicUrl();
			} else {
				return null;
			}
		}

		// 设置点击行的行号
		public void setPosition(int position) {
			MyService.this.musicIndex = position;
		}

		// 获得后台正在播放的列表
		public ArrayList<MusicInfo> getPlayList() {

			return list;
		}

	}

	// 下一首下标操作
	private void setNextIndex() {
		switch (modle) {
		case Constants.MODLE_ALL:// 顺序播放
			musicIndex++;
			if (musicIndex > list.size() - 1) {
				musicIndex = 0;
			}
			Log.e("MODLE_ALL", "musicIndex==" + musicIndex);
			break;
		case Constants.MODLE_RANDOM:// 随机播放
			Random random = new Random();
			musicIndex = random.nextInt(list.size());
			Log.e("MODLE_RANDOM", "musicIndex==" + musicIndex);
			break;
		case Constants.MODLE_SINGLE:// 单曲循环
			musicIndex++;
			if (musicIndex > list.size() - 1) {
				musicIndex = 0;
			}
			break;

		default:
			break;
		}
	}

	// 上一首下标操作
	private void setPreIndex() {

		switch (modle) {
		case Constants.MODLE_ALL:// 顺序播放
			musicIndex--;
			if (musicIndex < 0) {
				musicIndex = list.size() - 1;
			}
			break;
		case Constants.MODLE_RANDOM:// 随机播放
			Random random = new Random();
			musicIndex = random.nextInt(list.size());
			break;
		case Constants.MODLE_SINGLE:// 单曲循环
			musicIndex++;
			if (musicIndex > list.size() - 1) {
				musicIndex = 0;
			}
			break;

		}
	}

	class MyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 根据过滤条件进行判断
			String action = intent.getAction();
			switch (action) {
			case Constants.NOTIFY_PLAY:// 播放暂停
				notifyPlay();
				break;
			case Constants.NOTIFY_EXIT://退出应用
				notiyExit();
				break;

			default:
				break;
			}
		}

	}

	@SuppressLint("NewApi")
	public void notifyPlay() {
		// 判断是否播放歌曲
		if (mediaPlayer.isPlaying()) {
			myBinder.pause();
			notification.bigContentView.setImageViewResource(R.id.img_notify_play, R.drawable.selector_play);
		} else {
			myBinder.start();
			notification.bigContentView.setImageViewResource(R.id.img_notify_play, R.drawable.selector_pause);
		}
		// 更新通知
		manager.notify(Constants.NOTIFY_ID, notification);
	}

	public void notiyExit() {
		manager.cancel(Constants.NOTIFY_ID);//取消通知
		mediaPlayer.release();//释放mediaPlayer
		mediaPlayer=null;
		unregisterReceiver(mReceiver);
		MyService.this.stopSelf();//停止服务
//		System.exit(0);
		//关闭所有的启动的Activity
		MyApplication application = (MyApplication) getApplication();
		application.closeAllAcivity();
	}

}
