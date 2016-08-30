package com.example.Util;

public interface Constants {

	//广播 过滤条件  更新UI控件
	public static final String BROADCAST_REFRESHUI="refershUI";
	//key值
	public static final String SINGER="singer";
	public static final String MUSICNAME="musicName";
	public static final String MUSICLEN="musicLen";
	public static final String MUSICURL="musicUrl";
	public static final String IMAGEURL="imageUrl";
	public static final String JSONKEY="jsonkey";
	//播放模式
	public static final int MODLE_ALL=0;
	public static final int MODLE_RANDOM=1;
	public static final int MODLE_SINGLE=2;
	//数据库表名
	public static final String TABLE_RECENTLY="recently";//最近播放表名
	public static final String TABLE_FAVOR="favor";//最近播放表名
	public static final String TABLE_PLAYINGLIST="playlingList";//最近播放表名
	public static final String RECNET_FAVOR="recent_favor";//最近播放表名
	public static final String SP_PLAYINGLIST="playinglist";//最近播放表名
	
	//通知
	public static final int NOTIFY_ID=2;
	public static final String NOTIFY_PLAY="notifyPlay";//最近播放表名
	public static final String NOTIFY_EXIT="notifyExit";//最近播放表名
	
	
	
	
	
	
	
}
