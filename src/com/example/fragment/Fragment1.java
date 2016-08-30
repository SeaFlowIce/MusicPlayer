package com.example.fragment;

import com.example.Util.Constants;
import com.example.ex01day01.OnlineActivity;
import com.example.ex01day01.R;
import com.example.ex01day01.RecentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class Fragment1 extends Fragment implements OnClickListener{

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v=inflater.inflate(R.layout.fragment1, null);
		TextView tv_favor = (TextView) v.findViewById(R.id.tV1);
		TextView tv_recent = (TextView) v.findViewById(R.id.tV4);
		TextView tv_online = (TextView) v.findViewById(R.id.tV2);
		tv_favor.setOnClickListener(this);
		tv_recent.setOnClickListener(this);
		tv_online.setOnClickListener(this);
		return v;	
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
		case R.id.tV1:
			// 跳转到我喜欢列表
			Intent intent = new Intent(getActivity(), RecentActivity.class);
			// 传递我喜欢表名
			intent.putExtra(Constants.RECNET_FAVOR, Constants.TABLE_FAVOR);
			startActivity(intent);
			break;
		case R.id.tV4:
			// 跳转到最近播放列表
			Intent intent0 = new Intent(getActivity(), RecentActivity.class);
			// 传递最近播放表名
			intent0.putExtra(Constants.RECNET_FAVOR, Constants.TABLE_RECENTLY);
			startActivity(intent0);
			break;
        case R.id.tV2:
        	// 跳转到在线列表
    		startActivity(new Intent(getActivity(), OnlineActivity.class));
			break;

		default:
			break;
		}
	}	
	
}
