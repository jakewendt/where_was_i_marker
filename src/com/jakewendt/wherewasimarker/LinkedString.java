package com.jakewendt.wherewasimarker;

import java.lang.String;
import android.speech.tts.TextToSpeech;
import android.widget.TextView;


public class LinkedString {
	private java.lang.String s;
	private TextToSpeech tts;
	private TextView     tv;
	
	public LinkedString( ){
		
	}
	public LinkedString( TextToSpeech text_to_speech ){
		tts = text_to_speech;
	}
	public LinkedString( TextToSpeech text_to_speech, TextView text_view ){
		tts = text_to_speech;
		tv  = text_view;
	}
	public String message (String new_message){
		s = new_message;
		if( tts != null ){
			tts.speak(s,
				TextToSpeech.QUEUE_FLUSH,  // Drop all pending entries in the playback queue.
				null);
		}
		if( tv != null ){
			tv.setText(s);
		}
		return s;
	}
	public String message (){
		return s;
	}
/*	public LinkedString(String s) { 
		this.s = s; 
	}
	*/
	/**
	 * Returns this string.
	 *
	 * @return this string.
	 */
	@Override
	public String toString() {
		return this.s;
	}

}