package com.psl.app;

import java.util.Arrays;

 class CreateChannelRequestData {

	 @Override
	public String toString() {
		return "CreateChannelRequestData [channelName=" + channelName
				+ ", channelPath=" + channelPath + ", userOrg=" + userOrg + "]";
	}
	public String getChannelName() {
		return channelName;
	}
	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}
	public String getChannelPath() {
		return channelPath;
	}
	public void setChannelPath(String channelPath) {
		this.channelPath = channelPath;
	}
	public String getOrgName() {
		return userOrg;
	}
	public void setOrgName(String userOrg) {
		this.userOrg = userOrg;
	}
	String channelName;
	 String channelPath;
	 String userOrg;
}

 class JoinChannelRequestData {
	
	 @Override
	public String toString() {
		return "JoinChannelRequestData [peers=" + Arrays.toString(peers)
				 + ", userOrg=" + userOrg + "]";
	}
	public String[] getPeers() {
		return peers;
	}
	public void setPeers(String[] peers) {
		this.peers = peers;
	}

	public String getUserOrg() {
		return userOrg;
	}
	public void setUserOrg(String userOrg) {
		this.userOrg = userOrg;
	}
	
	
	 String peers[];
	 String userOrg;
 }