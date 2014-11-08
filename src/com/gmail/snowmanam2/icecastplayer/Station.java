package com.gmail.snowmanam2.icecastplayer;


public class Station {
	private String stationName;
	private String listenUrl;
	private String genre;
	
	public Station (){}
	
	public Station (String stationName, String listenUrl, String genre) {
		this.stationName = stationName;
		this.listenUrl = listenUrl;
		this.genre = genre;
	}
	
	public String getStationName () {
		return stationName;
	}
	
	public void setStationName (String name) {
		this.stationName = name;
	}
	
	public String getListenUrl () {
		return listenUrl;
	}
	
	public void setListenUrl (String url) {
		this.listenUrl = url;
	}
	
	public String getGenre () {
		return genre;
	}
	
	public void setGenre (String genre) {
		this.genre = genre;
	}
}