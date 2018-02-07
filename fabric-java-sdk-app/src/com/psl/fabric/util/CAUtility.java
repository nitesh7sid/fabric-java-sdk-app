package com.psl.fabric.util;

import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.hyperledger.fabric_ca.sdk.exception.EnrollmentException;
import org.hyperledger.fabric_ca.sdk.exception.RegistrationException;

import com.psl.fabric.config.NetworkConfig;
import com.psl.fabric.config.SampleOrg;
import com.psl.fabric.config.SampleStore;
import com.psl.fabric.config.SampleUser;


public class CAUtility {

	public static SampleUser enrollUser(String userName, String enrollSecret,
			String orgName) throws EnrollmentException,
			org.hyperledger.fabric_ca.sdk.exception.InvalidArgumentException {

		NetworkConfig config = new NetworkConfig();
		SampleOrg sampleOrg = config.getSampleOrg(orgName);
		HFCAClient ca_client = sampleOrg.getCAClient();
		SampleUser orgAdmin = sampleOrg.getOrgAdmin();
		SampleStore sampleStore = sampleOrg.getSampleStore();
		SampleUser user = sampleStore.getMember(userName, orgName);
		// Preregistered admin only needs to be enrolled with Fabric caClient.

		if (!user.isEnrolled()) {
			String enrollmentSecret = user.getEnrollmentSecret();
			System.out.println("user is not enrolled" + enrollmentSecret);
			user.setEnrollment(ca_client.enroll(user.getName(), enrollSecret));
			user.setMspId(sampleOrg.getMSPID());
		}

		return user;
	}
	
	public static String registerUser(String userName, String orgName,
			String affiliation
		) throws Exception {
		System.out.println(orgName);
		NetworkConfig config = new NetworkConfig();
		SampleOrg sampleOrg = config.getSampleOrg(orgName);
		//System.out.println("getDeployWaitTime"+config.getIntegrationTestsSampleOrgs());
		SampleUser admin, user;
		String enrollmentSecret = "";
		HFCAClient ca_client = sampleOrg.getCAClient();
		SampleUser orgAdmin = sampleOrg.getOrgAdmin();
		SampleStore sampleStore = sampleOrg.getSampleStore();
		user = sampleStore.getMember(userName, orgName);
		admin = enrollUser(config.TEST_ADMIN_NAME,config.TEST_ADMIN_PW, orgName);

		user = sampleStore.getMember(userName, orgName);

		if (!user.isRegistered()) {
			// users need to be registered AND enrolled
			System.out.println("user is not registered");
			RegistrationRequest rr = new RegistrationRequest(user.getName(),
					affiliation);
			enrollmentSecret = ca_client.register(rr, admin);
			System.out.println("Enrollment secret" + enrollmentSecret);
			user.setEnrollmentSecret(enrollmentSecret);
		}

		return enrollmentSecret;
	}
	
}
