package com.psl.app;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RequestData {

	public RequestData() {
		       super();
		  }

	//class RegistrationRequest{
		private String username;
		private String userorg;
		private String useraffiliation;
		
		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getOrg() {
			return userorg;
		}

		public void setOrg(String org) {
			this.userorg = org;
		}

		public String getUseraffiliation() {
			return useraffiliation;
		}

		public void setUseraffiliation(String useraffiliation) {
			this.useraffiliation = useraffiliation;
		}

		@Override
		public String toString() {
			return "RegistrationRequest [userName=" + username + ", userorg="
					+ userorg + ", userAffiliation=" + useraffiliation + "]";
		}
		
	//}
}
