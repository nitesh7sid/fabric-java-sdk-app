package com.psl.app;

import java.util.Arrays;

 class InstallChaincodeRequestData {

	 @Override
	public String toString() {
		return "InstallChaincodeRequestData [chaincodeName=" + chaincodeName
				+ ", chaincodeVersion=" + chaincodeVersion + ", chaincodePath="
				+ chaincodePath + ", peers=" + Arrays.toString(peers)
				+ ", userOrg=" + userOrg + "]";
	}
	public String getChaincodeName() {
		return chaincodeName;
	}
	public void setChaincodeName(String chaincodeName) {
		this.chaincodeName = chaincodeName;
	}
	public String getChaincodeVersion() {
		return chaincodeVersion;
	}
	public void setChaincodeVersion(String chaincodeVersion) {
		this.chaincodeVersion = chaincodeVersion;
	}
	public String getChaincodePath() {
		return chaincodePath;
	}
	public void setChaincodePath(String chaincodePath) {
		this.chaincodePath = chaincodePath;
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
	String chaincodeName;
	 String chaincodeVersion;
	 String chaincodePath;
	 String peers[];
	 String userOrg;
}
