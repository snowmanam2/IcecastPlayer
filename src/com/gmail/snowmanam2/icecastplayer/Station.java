package com.gmail.snowmanam2.icecastplayer;


public class Station {
	private String stationName;
	private String listenUrl;
	private String genre;
	
	public Station (String stationName, String listenUrl, String genre) {
		this.stationName = stationName;
		this.listenUrl = listenUrl;
		this.genre = genre;
	}
	
	public String getStationName () {
		return stationName;
	}
	
	public String getListenUrl () {
		return listenUrl;
	}
	
	public String getGenre () {
		return genre;
	}
}