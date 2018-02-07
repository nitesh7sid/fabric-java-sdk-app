package com.psl.app;

 class CreateChannelRequestData {

	 @Override
	public String toString() {
		return "CreateChannelRequestData [channelName=" + channelName
				+ ", channelPath=" + channelPath + ", orgName=" + orgName + "]";
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
		return orgName;
	}
	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}
	String channelName;
	 String channelPath;
	 String orgName;
}
