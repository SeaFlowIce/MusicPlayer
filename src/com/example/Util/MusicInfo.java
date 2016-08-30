package com.example.Util;

import android.graphics.Bitmap;

public class MusicInfo {

	public Bitmap bimap;
	String singer;
	String musicName;
	int musicLen;
	String musicUrl;
	String imageUrl;
	public MusicInfo(String singer, String musicName, int musicLen, String musicUrl, String imageUrl) {
		super();
		this.singer = singer;
		this.musicName = musicName;
		this.musicLen = musicLen;
		this.musicUrl = musicUrl;
		this.imageUrl = imageUrl;
	}
	public String getSinger() {
		return singer;
	}
	public void setSinger(String singer) {
		this.singer = singer;
	}
	public String getMusicName() {
		return musicName;
	}
	public void setMusicName(String musicName) {
		this.musicName = musicName;
	}
	public int getMusicLen() {
		return musicLen;
	}
	public void setMusicLen(int musicLen) {
		this.musicLen = musicLen;
	}
	public String getMusicUrl() {
		return musicUrl;
	}
	public void setMusicUrl(String musicUrl) {
		this.musicUrl = musicUrl;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	@Override
	public String toString() {
		return "MusicInfo [singer=" + singer + ", musicName=" + musicName + ", musicLen=" + musicLen + ", musicUrl="
				+ musicUrl + ", imageUrl=" + imageUrl + "]";
	}
	
	
	
	
	
	
}
