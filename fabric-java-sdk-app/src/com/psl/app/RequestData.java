package com.psl.app;

import com.owlike.genson.annotation.JsonProperty;


 class RegistrationRequestData {
	 //@JsonProperty
	String userAffiliation;
	String userName;
	String userOrg;

	public String getuserOrg() {
		return userOrg;
	}

	public void setUserOrg(String userOrg) {
		this.userOrg = userOrg;
	}

	public String getUserOrg() {
		return userOrg;
	}

	public String getUserAffiliatiaon() {
		return userAffiliation;
	}

	public void setUserAffiliatiaon(String userAffiliation) {
		this.userAffiliation = userAffiliation;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Override
	public String toString() {
		return "Track [userAffiliation=" + userAffiliation + ", userName="
				+ userName + ", userOrg=" + userOrg + "]";
	}
		
}
 
 class EnrollRequestData{
	 	String enrollmentSecret;
		String userName;
		String userOrg;
		public String getEnrollmentSecret() {
			return enrollmentSecret;
		}
		public void setEnrollmentSecret(String enrollmentSecret) {
			this.enrollmentSecret = enrollmentSecret;
		}
		public String getUserName() {
			return userName;
		}
		public void setUserName(String userName) {
			this.userName = userName;
		}
		public String getUserOrg() {
			return userOrg;
		}
		@Override
		public String toString() {
			return "EnrollRequest [enrollmentSecret=" + enrollmentSecret
					+ ", userName=" + userName + ", userOrg=" + userOrg + "]";
		}
		public void setUserOrg(String userOrg) {
			this.userOrg = userOrg;
		}
 }