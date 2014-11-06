package com.gmail.snowmanam2.icecastplayer;

import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;


public class IcecastXmlParser {
	public static final String ns = null;
	
	public void parse (InputStream in, IcecastDatabase db) throws XmlPullParserException, IOException {
		try {
			XmlPullParser parser = Xml.newPullParser();
			db.beginTransaction();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            readDirectory(parser, db);
		} finally {
			in.close();
			db.endTransaction();
		}
	}
	
	private void readDirectory(XmlPullParser parser, IcecastDatabase db) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "directory");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			
			String name = parser.getName();
			
			if (name.equals("entry")) {
				Station station = readEntry (parser);
				db.addStation(station);
			}
		}
	}
	
	private Station readEntry (XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "entry");
		
		String serverName = "";
		String listenUrl = "";
		String genre = "";
		
		while (parser.next() != XmlPullParser.END_TAG) { 
			
			String name = parser.getName();
			if (name == null) {
				continue;
			} else if (name.equals("server_name")) {
				serverName = readServerName (parser);
			} else if (name.equals("listen_url")) {
				listenUrl = readListenUrl (parser);
			} else if (name.equals("genre")) {
				genre = readGenre (parser);
			} else {
				skip (parser);
			}
		}
		
		return new Station (serverName, listenUrl, genre);
		
	}
	
	private String readServerName (XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "server_name");
		String text = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "server_name");
		return text;
	}
	
	private String readListenUrl (XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "listen_url");
		String text = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "listen_url");
		return text;
	}
	
	private String readGenre (XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "genre");
		String text = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "genre");
		return text;
	}
	
	private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
	    String result = "";
	    if (parser.next() == XmlPullParser.TEXT) {
	        result = parser.getText();
	        parser.nextTag();
	    }
	    return result;
	}
	
	private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
	    if (parser.getEventType() != XmlPullParser.START_TAG) {
	        throw new IllegalStateException();
	    }
	    int depth = 1;
	    while (depth != 0) {
	        switch (parser.next()) {
	        case XmlPullParser.END_TAG:
	            depth--;
	            break;
	        case XmlPullParser.START_TAG:
	            depth++;
	            break;
	        }
	    }
	 }
}