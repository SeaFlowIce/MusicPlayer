package com.example.ex01day01;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.Util.Constants;
import com.example.Util.MusicInfo;
import com.example.Util.MyApplication;
import com.example.ex01day01.MyService.MyBinder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class OnlineActivity extends Activity
{
	// 保存音乐信息
	ArrayList<MusicInfo> list = new ArrayList<MusicInfo>();
	// 保存下载过的Json文件路径
	String SaveUrl = Environment.getExternalStorageDirectory().getAbsolutePath() + "/json.text";
	// 下载路径
	// String downUrl = "http://169.254.231.0:8080/musicList/online.txt";
	String downUrl = "http://o9dupqosi.bkt.clouddn.com/online.txt";
	private MyAdapter myAdapter;
	private SharedPreferences sp;
	private MyBinder myBinder;
	private ServiceConnection conn = new ServiceConnection()
	{

		@Override
		public void onServiceDisconnected(ComponentName name)
		{
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			myBinder = (MyBinder) service;

		}
	};
	private MyApplication myApplication;

	@Override
	protected void onDestroy()
	{

		super.onDestroy();
		myApplication.removeAcivity(this);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_online);
		// 添加启动的Activity
		myApplication = (MyApplication) getApplication();
		myApplication.addAcivity(this);
		initUI();
	}

	private void initUI()
	{
		ListView mListView = (ListView) findViewById(R.id.listView1);
		myAdapter = new MyAdapter();
		initData();
		mListView.setAdapter(myAdapter);

		// listview的行点击
		mListView.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				// 获得正在播放的音乐地址
				String oldMusicUrl = myBinder.getMusicUrl();
				// 传递当前容器到服务中
				myBinder.setMusicList(list);
				// 获得点击行的音乐地址
				String newMusicUrl = list.get(position).getMusicUrl();
				// 获得该首歌的准备状态 是否第一次播放
				if (myBinder.isPrepared())
				{
					// 传递点击行的行号到服务中
					myBinder.setPosition(position);
					// 判断点击行的音乐地址跟服务中播放的歌曲地址是否一致
					if (newMusicUrl.equals(oldMusicUrl))
					{
						// 点击同一行 根据改行歌曲播放状态判断是否播放或暂停
						// 获得该首歌的播放状态
						if (myBinder.isPlaying())
						{
							myBinder.pause();
						} else
						{
							myBinder.start();
						}
					} else
					{// 点击其他行歌曲 播放其他行歌曲
						myBinder.PlayMusic(position);
					}
				} else
				{
					// 点击播放歌曲
					myBinder.PlayMusic(position);
				}

			}
		});

	}

	private void initData()
	{
		// 获得是否下载的状态
		sp = getSharedPreferences("json", 0);
		boolean isDownLoad = sp.getBoolean(Constants.JSONKEY, false);
		if (isDownLoad)
		{// 已经下载过
			// 读取文件
			readJson();
		} else
		{
			// 启动异步任务下载Json文件 完成后解析
			new JsonTask().execute(downUrl);
		}
	}

	@SuppressWarnings("resource")
	private void readJson()
	{
		ByteArrayBuffer arrayBuffer = new ByteArrayBuffer(500);
		try
		{

			FileInputStream fis = new FileInputStream(SaveUrl);
			byte[] buffer = new byte[1024];
			int len = 0;
			while (-1 != (len = fis.read(buffer)))
			{
				arrayBuffer.append(buffer, 0, len);
			}

			String str = new String(arrayBuffer.toByteArray());
			// 解析
			parseJson(str);
		} catch (FileNotFoundException e)
		{

			e.printStackTrace();
		} catch (IOException e)
		{

			e.printStackTrace();
		}

	}

	@SuppressLint("ViewHolder")
	class MyAdapter extends BaseAdapter
	{

		@Override
		public int getCount()
		{

			return list.size();
		}

		@Override
		public Object getItem(int position)
		{

			return null;
		}

		@Override
		public long getItemId(int position)
		{

			return 0;
		}

		@SuppressLint("InflateParams")
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View layout = getLayoutInflater().inflate(R.layout.list_item_online, null);
			ImageView imgSinger = (ImageView) layout.findViewById(R.id.img_singer);
			TextView tvSinger = (TextView) layout.findViewById(R.id.tv_singer);
			TextView tvMusicName = (TextView) layout.findViewById(R.id.tv_musicName);
			MusicInfo musicInfo = list.get(position);
			tvSinger.setText(musicInfo.getSinger());
			tvMusicName.setText(musicInfo.getMusicName());
			new ImageTask(imgSinger, position).execute(musicInfo.getImageUrl());
			return layout;
		}

	}

	class ImageTask extends AsyncTask<String, Void, Bitmap>
	{

		private ImageView imgSinger;
		private int position;

		public ImageTask(ImageView imgSinger, int position)
		{
			this.imgSinger = imgSinger;
			this.position = position;
		}

		@Override
		protected Bitmap doInBackground(String... params)
		{
			Bitmap bitmap = null;
			try
			{
				URL url = new URL(params[0]);
				InputStream is = url.openStream();
				bitmap = BitmapFactory.decodeStream(is);
			} catch (MalformedURLException e)
			{

				e.printStackTrace();
			} catch (IOException e)
			{

				e.printStackTrace();
			}
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap result)
		{
			// 更新图片
			imgSinger.setImageBitmap(result);
			// 保存下载的图片
			list.get(position).bimap = result;

		}

	}

	class JsonTask extends AsyncTask<String, Void, String>
	{

		@Override
		protected String doInBackground(String... params)
		{
			String str = "";
			InputStream is = null;
			FileOutputStream fos = null;
			try
			{
				ByteArrayBuffer arrayBuffer = new ByteArrayBuffer(500);
				URL url = new URL(params[0]);
				is = url.openStream();
				byte[] buffer = new byte[1024];
				int len = 0;
				fos = new FileOutputStream(SaveUrl);
				while (-1 != (len = is.read(buffer)))
				{
					arrayBuffer.append(buffer, 0, len);
					fos.write(buffer, 0, len);
				}
				fos.flush();
				str = new String(arrayBuffer.toByteArray());
			} catch (MalformedURLException e)
			{
				str = "下载失败";
				e.printStackTrace();
			} catch (IOException e)
			{
				str = "下载失败";
				e.printStackTrace();
			} finally
			{
				if (is != null)
				{
					try
					{
						is.close();
					} catch (IOException e)
					{
						e.printStackTrace();
					}
				}
				if (fos != null)
				{
					try
					{
						fos.close();
					} catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}

			return str;
		}

		@Override
		protected void onPostExecute(String result)
		{
			if ("下载失败".equals(result))
			{
				return;
			}
			// 保存下载状态 json文件已经下载
			Editor edit = sp.edit();
			edit.putBoolean(Constants.JSONKEY, true);
			edit.commit();
			// 解析数据
			parseJson(result);

		}
	}

	public void parseJson(String result)
	{

		try
		{
			JSONArray jsonArray = new JSONArray(result);
			for (int i = 0; i < jsonArray.length(); i++)
			{
				JSONObject object = (JSONObject) jsonArray.get(i);
				String singer = object.getString(Constants.SINGER);
				String musicName = object.getString(Constants.MUSICNAME);
				int musicLen = object.getInt(Constants.MUSICLEN);
				String musicUrl = object.getString(Constants.MUSICURL);
				String imageUrl = object.getString(Constants.IMAGEURL);
				list.add(new MusicInfo(singer, musicName, musicLen, musicUrl, imageUrl));
			}
			// 通知Listview更新
			myAdapter.notifyDataSetChanged();

		} catch (JSONException e)
		{
			e.printStackTrace();
		}

	}

	@Override
	protected void onStart()
	{
		Intent service = new Intent(this, MyService.class);
		startService(service);
		bindService(service, conn, Context.BIND_AUTO_CREATE);
		super.onStart();
	}

	@Override
	protected void onStop()
	{
		unbindService(conn);
		super.onStop();
	}

}
