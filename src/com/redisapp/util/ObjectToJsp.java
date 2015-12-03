package com.redisapp.util;

/**
 * A class to store cache hit/miss information
 */
public  class ObjectToJsp{
	String content;
	int hitormiss;
	
	public ObjectToJsp() {
		content="";
		hitormiss = 0;
	}
	
	public String getContent() {
		return content;
	}
	
	public int getHitorMiss() {
		return hitormiss;
	}

}
