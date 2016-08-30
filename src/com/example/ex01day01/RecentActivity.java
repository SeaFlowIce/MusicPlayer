package com.example.ex01day01;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class RecentActivity extends Activity implements OnClickListener {
	// 保存删除的歌曲
	HashMap<Integer, MusicInfo> deletMap = new HashMap<Integer, MusicInfo>();
	ArrayList<MusicInfo> list = new ArrayList<MusicInfo>();
	private Myadapter myAdapter;
	private SQLiteDatabase mDB;
	private MyReceiver myReceiver;
	private MyBinder myBinder;
	boolean isCheckAll;
	int showPosition = -1;// 保存操作的行号
	int showImageId = R.drawable.kg_group_arrow_up;// 保存操作行的图片
	int showVisible = View.GONE;// 保存操作行的显示状态
	private ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

	@Override public void onServiceConnected(ComponentName name,IBinder service){myBinder=(MyBinder)service;

	}};
	private String tableName;
	private TextView mTvIsAll;
	private RelativeLayout mLayoutCheck;
	private RelativeLayout mLayoutDefult;
	private LinearLayout mLayoutDelete;
	private CheckBox mCbAll;
	private OnCheckedChangeListener listener;
	private TextView mTvDelete;
	private String musicUrl2;
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
		setContentView(R.layout.activity_recent);
		//添加启动的Activity
				myApplication = (MyApplication) getApplication();
				myApplication.addAcivity(this);
		initUI();
		initData();}

	private void initUI() {

		mTvIsAll = (TextView) findViewById(R.id.tv_isAll);
		mTvDelete = (TextView) findViewById(R.id.tv_delete);
		mLayoutCheck = (RelativeLayout) findViewById(R.id.layout_check);
		mLayoutDefult = (RelativeLayout) findViewById(R.id.layout_defult);
		mLayoutDelete = (LinearLayout) findViewById(R.id.layout_delete);
		mCbAll = (CheckBox) findViewById(R.id.cb_layout_checkall);
		mTvIsAll.setOnClickListener(this);
		mTvDelete.setOnClickListener(this);
		ListView mListView = (ListView) findViewById(R.id.listView1);
		myAdapter = new Myadapter();
		mListView.setAdapter(myAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// 获得后台正在播放的歌曲地址
				String oldMusicUrl = myBinder.getMusicUrl();
				// 传递当前容器到服务中
				myBinder.setMusicList(list);
				// 获得点击行的歌曲地址
				String newMusicUrl = list.get(position).getMusicUrl();
				// 获得该首歌的准备状态 是否第一次播放
				if (myBinder.isPrepared()) {
					// 传递点击行的行号到服务中
					myBinder.setPosition(position);
					// 于当前点击大行号进行比较
					if (newMusicUrl.equals(oldMusicUrl)) {
						// 点击同一行 根据改行歌曲播放状态判断是否播放或暂停
						// 获得该首歌的播放状态
						if (myBinder.isPlaying()) {
							myBinder.pause();
						} else {
							myBinder.start();
						}
					} else {// 点击其他行歌曲 播放其他行歌曲
						myBinder.PlayMusic(position);
					}
				} else {
					// 点击播放歌曲
					myBinder.PlayMusic(position);
				}

			}
		});

		listener = new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// 保存是否全选
				isCheckAll = isChecked;
				if (isChecked) {// 全选 添加到删除的容器中
					for (int i = 0; i < list.size(); i++) {
						deletMap.put(i, list.get(i));
					}
				} else {// 取消全选 删除的容器中移除数据
					deletMap.clear();
				}
				myAdapter.notifyDataSetChanged();
			}
		};
		mCbAll.setOnCheckedChangeListener(listener);
	}

	private void initData() {

		// 查询数据库
		MyDataBase myDataBase = new MyDataBase(this);
		mDB = myDataBase.getReadableDatabase();
		Intent intent = getIntent();
		tableName = intent.getStringExtra(Constants.RECNET_FAVOR);
		// 查询数据库
		query();
	}

	private void query() {
		list.clear();// 清除内容
		String order = null;
		if (Constants.TABLE_RECENTLY.equals(tableName)) {
			order = "id DESC";
		}
		Cursor cursor = mDB.query(tableName, null, null, null, null, null, order);
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
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View layout = getLayoutInflater().inflate(R.layout.list_item_recent, null);
			TextView tvSinger = (TextView) layout.findViewById(R.id.tv_recent_singer);
			TextView tvMusicName = (TextView) layout.findViewById(R.id.tv_recent_musicName);
			CheckBox cbItem = (CheckBox) layout.findViewById(R.id.cb_item);
			final LinearLayout layoutShow = (LinearLayout) layout.findViewById(R.id.linearlayout_show);
			final ImageView imgShow = (ImageView) layout.findViewById(R.id.img_show);
			// 设置CheckBox的显示隐藏
			String string = mTvIsAll.getText().toString();
			if ("多选".equals(string)) {
				cbItem.setVisibility(View.GONE);
			} else {
				cbItem.setVisibility(View.VISIBLE);
			}
			Log.e("isCheckAll", "isCheckAll" + isCheckAll + position);
			// 判断当前行是否被选中
			MusicInfo deletInfo = deletMap.get(position);
			if (deletInfo != null) {
				cbItem.setChecked(true);
			} else {
				cbItem.setChecked(false);
			}

			cbItem.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					// listivew checkbox单行选中 添加到deleteMap中
					if (isChecked) {
						deletMap.put(position, list.get(position));
					} else {
						deletMap.remove(position);
					}

					// 取消外层布局CheckBox状态改变监听事件
					mCbAll.setOnCheckedChangeListener(null);

					// 判断list 跟DeleteMap长度是否一致 是否全选
					if (list.size() == deletMap.size()) {// 全选
						mCbAll.setChecked(true);
					} else {// 外层布局 checkbox取消全选
						mCbAll.setChecked(false);
					}
					// 设置状态改变监听事件
					mCbAll.setOnCheckedChangeListener(listener);

				}
			});

			imgShow.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// 保存操作的行号
					showPosition = position;
					// 获得控件的显示隐藏状态
					int visibility = layoutShow.getVisibility();
					if (View.VISIBLE == visibility) {
						// 保存控件的状态
						showImageId = R.drawable.kg_group_arrow_up;
						showVisible = View.GONE;
					} else if (View.GONE == visibility) {
						showImageId = R.drawable.kg_group_arrow_down;
						showVisible = View.VISIBLE;
					}
					// 刷新界面更新
					myAdapter.notifyDataSetChanged();
				}
			});

			// 初始化状态 判断当前行是否是操作行
			if (position == showPosition) {// 操作的是当前行
				// 设置操作行的状态
				layoutShow.setVisibility(showVisible);
				imgShow.setImageResource(showImageId);
			} else {// 不是操作行 显示默认状态
				layoutShow.setVisibility(View.GONE);
				imgShow.setImageResource(R.drawable.kg_group_arrow_up);
			}

			MusicInfo musicInfo = list.get(position);
			tvSinger.setText(musicInfo.getSinger());
			tvMusicName.setText(musicInfo.getMusicName());
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
			if (Constants.TABLE_RECENTLY.equals(tableName)) {
				// 播放下一首时 重新查询数据 刷新Lisrview
				query();
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_isAll:// 多选 文本切换 布局显示隐藏
			String strAll = mTvIsAll.getText().toString();
			if ("多选".equals(strAll)) {
				mTvIsAll.setText("取消");
				mLayoutCheck.setVisibility(View.VISIBLE);
				mLayoutDelete.setVisibility(View.VISIBLE);
				mLayoutDefult.setVisibility(View.GONE);
			} else {
				mTvIsAll.setText("多选");
				mLayoutCheck.setVisibility(View.GONE);
				mLayoutDelete.setVisibility(View.GONE);
				mLayoutDefult.setVisibility(View.VISIBLE);
			}
			// 刷新界面
			myAdapter.notifyDataSetChanged();
			break;
		case R.id.tv_delete:// 删除选中行数据

			// Set<Integer> keySet = deletMap.keySet();
			// Iterator<Integer> iterator = keySet.iterator();
			// while (iterator.hasNext()) {
			// Integer next = iterator.next();
			// mDB.delete(tableName, "musicUrl=?",
			// new String[] { deletMap.get(next).getMusicUrl() });
			// }

			Set<Entry<Integer, MusicInfo>> entrySet = deletMap.entrySet();
			Iterator<Entry<Integer, MusicInfo>> iterator = entrySet.iterator();
			while (iterator.hasNext()) {
				Entry<Integer, MusicInfo> next = iterator.next();
				MusicInfo value = next.getValue();
				mDB.delete(tableName, "musicUrl=?", new String[] { value.getMusicUrl() });
			}

			// for (int i = 0; i < list.size(); i++) {
			// String musicUrl = list.get(i).getMusicUrl();
			// MusicInfo info = null;
			// info = deletMap.get(i);
			// if (info!=null) {
			// musicUrl2 = info.getMusicUrl();
			// }
			// if (musicUrl.equals(musicUrl2)) {
			// mDB.delete(tableName, "musicUrl=?",
			// new String[] { musicUrl2 });
			// }
			// }
			deletMap.clear();
			query();
			mCbAll.setChecked(false);
			break;

		default:
			break;
		}
	}
}
