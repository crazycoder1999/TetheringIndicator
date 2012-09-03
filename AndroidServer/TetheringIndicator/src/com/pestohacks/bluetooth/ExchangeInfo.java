package com.pestohacks.bluetooth;

//All the code is GPL 3.0 and comes from http://pestohacks.blogspot.com
//Made by crazycoder1999

import java.util.StringTokenizer;

public class ExchangeInfo {
	public final String net3G = "3G";
	public final String net2G = "2G";
	public final String noBtConnection = "NC";
	public final String noNetwork = "NN";
	public final String netErr = "EE";

	String connectionType = "";
	int notificationCount;
	
	public void set3G() {
		connectionType = net3G;
	}
	
	public void setConnectionError() {
		connectionType = netErr;
	}
	
	public void set2G() {
		connectionType = net2G;
	}
	
	public void setNoBtConnection() {
		connectionType = noBtConnection;
	}
	
	public void setNoNetwork() {
		connectionType = noNetwork; //no net.
	}
	
	public boolean isNoNetwork() {
		return connectionType.equals(noNetwork);
	}
	
	public boolean is2G(){
		return connectionType.equals(net2G);
	}
	
	public boolean is3G(){
		return connectionType.equals(net3G);
	}
	
	public boolean isNoBtConnection() {
		return connectionType.equals(noBtConnection);
	}
	
	public void setNotificationCount(int count) {
		this.notificationCount = count;
	}
	
	public int getNotificationCount() {
		return this.notificationCount;
	}
	
	public void setByAnother(ExchangeInfo ec) {
		setNotificationCount(ec.getNotificationCount());
		setConnectionType(ec.getConnectionType());
	}
	
	public void setConnectionType(String connection){
		this.connectionType = connection;
	}
	
	public String getConnectionType() {
		return this.connectionType;
	}
	
	public ExchangeInfo(String connectionType,int notificationCount) {
		this.connectionType = connectionType;
		this.notificationCount = notificationCount;
	}
	
	public boolean isEqual(ExchangeInfo cc){
		return this.notificationCount == cc.getNotificationCount() && this.connectionType.equals(cc.getConnectionType());
	}

	/*
	 * lat lng zoom
	 */
	public String toPacket() {
		return connectionType+";"+notificationCount+";";
	}
	
	public static ExchangeInfo getPacket(String toStrip) {
		String connType = "";
		int notifyCount = 0;
		StringTokenizer strToken = new StringTokenizer(toStrip,";");
		if( strToken.countTokens() == 2) {
			connType = strToken.nextToken();
			notifyCount = Integer.parseInt(strToken.nextToken());			
		}
		
		ExchangeInfo excInfo = new ExchangeInfo(connType,notifyCount);
		return excInfo;
	 }
	
	public ExchangeInfo() {
		connectionType = "";
		notificationCount = 0;
	}
	
	public String toString() {
		return ": " + connectionType + " : " + notificationCount ;
	}
	
	
}
