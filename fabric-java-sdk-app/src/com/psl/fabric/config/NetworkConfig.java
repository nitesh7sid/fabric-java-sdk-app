package com.psl.fabric.config;

import static java.lang.String.format;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.exception.EnrollmentException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.psl.fabric.util.*;

import java.io.*;
import java.nio.file.Paths;

import javax.json.JsonValue;

public class NetworkConfig {

	static String  channelConfigPath = "src/test/fixture/crypto-config/mychannel.tx";
	private static final String TEST_ADMIN_NAME = "admin";
	private static final String TEST_ADMIN_PW = "adminpw";
	static int GOSSIPWAITTIME = 5000;
	static int INVOKEWAITTIME = 100000;
	static int DEPLOYWAITTIME = 120000;
	static int PROPOSALWAITTIME = 120000;
	private static final HashMap<String, SampleOrg> sampleOrgs = new HashMap<>();
	private static final HashMap<String, SampleOrg> sampleOrgsChannels = new HashMap<>();

	public void initialConfig(String configPath) {

		JSONParser parser = new JSONParser();

		try {

			Object obj = parser.parse(new FileReader(
					"src/test/fixture/network-config.json"));
			JSONObject jsonObject = (JSONObject) obj;
			// System.out.println(jsonObject);

			JSONObject networkConfig = (JSONObject) jsonObject
					.get("network-config");
			System.out.println(networkConfig.keySet());

			String ordererUrl = "", ordererTlsCa = "", ordererDomainName = "";
			Iterator itr = networkConfig.keySet().iterator();

			while (itr.hasNext()) {
				String orgKey = (String) itr.next();
				JSONObject orgObject;
				System.out.println(orgKey);

				if (orgKey.indexOf("orderer") == 0) {
					JSONObject ordererObject = (JSONObject) networkConfig
							.get(orgKey);
					ordererUrl = (String) ordererObject.get("url");
					ordererTlsCa = (String) ordererObject.get("tls_cacerts");
					ordererDomainName = (String) ordererObject
							.get("server-hostname");

				}
				if (orgKey.indexOf("org") == 0) {
					
					orgObject = (JSONObject) networkConfig.get(orgKey);
					JSONObject adminObject= (JSONObject) orgObject.get("admin");
					String adminKeyPath = (String) adminObject.get("key");
					String adminCertPath = (String) adminObject.get("cert");
					
					String orgName = (String) orgObject.get("name");
					String orgMSP = (String) orgObject.get("mspid");
					String caUrl = (String) orgObject.get("ca");
					SampleOrg sampleOrg = new SampleOrg(orgName, orgMSP);
					sampleOrg.addOrdererLocation(ordererDomainName, ordererUrl);

					// add ca Client to sampleOrg

					if (orgObject.containsKey("peers")) {

						System.out.println("Found peer obj");
						JSONObject peerObject = (JSONObject) orgObject
								.get("peers");
						// Iterate over the peer object and get all the peer
						// defined for each org
						Iterator peerObjIterator = peerObject.keySet()
								.iterator();

						while (peerObjIterator.hasNext()) {

							String peerKey = (String) peerObjIterator.next();
							JSONObject peerInfoObject = (JSONObject) peerObject
									.get(peerKey);
							String peerUrl = (String) peerInfoObject
									.get("requests");
							String peerEventsUrl = (String) peerInfoObject
									.get("events");
							String peerDomainName = (String) peerInfoObject
									.get("server-hostname");
							String peerTlsCaCerts = (String) peerInfoObject
									.get("tls_cacerts");

							sampleOrg.addPeerLocation(peerDomainName, peerUrl);
							sampleOrg.addEventHubLocation(peerDomainName,
									peerEventsUrl);
							sampleOrg.setDomainName(peerDomainName.substring(
									peerDomainName.indexOf(".") + 1,
									peerDomainName.length()));
							sampleOrg.addOrdererLocation(ordererDomainName,
									ordererUrl);

							Properties caProperties = setProperties(
									peerTlsCaCerts, null);
							sampleOrg.setCAProperties(caProperties);
							sampleOrg.setCALocation(caUrl);

							Properties ordererProperties = setProperties(
									ordererTlsCa, ordererDomainName);
							sampleOrg.setOrdererProperties(ordererProperties);

							Properties peerProperties = setProperties(
									peerTlsCaCerts, peerDomainName);
							sampleOrg.setPeerProperties(peerDomainName,peerProperties);
							// add Peer in Set
							
							sampleOrgs.put(orgName, sampleOrg);
						}

						 sampleOrg.setCAClient(HFCAClient.createNewInstance(sampleOrg.getCALocation(),
								 sampleOrg.getCAProperties()));
						 
						setClient(sampleOrgs.get(orgName));
						setSampleStore(sampleOrgs.get(orgName));
						//setOrgAdmin
						setOrgAdmin(sampleOrgs.get(orgName),adminCertPath, adminKeyPath);
						setAdminUser(sampleOrgs.get(orgName));
						setOrgPeers(sampleOrgs.get(orgName));
					}
				}

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (CryptoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EnrollmentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (org.hyperledger.fabric_ca.sdk.exception.InvalidArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private Properties setProperties(String tlsCertPath, String hostNameOverRide) {

		File cf = new File("src/test/fixture" + tlsCertPath);
		if (!cf.exists() || !cf.isFile()) {
			throw new RuntimeException("TEST is missing cert file "
					+ cf.getAbsolutePath());
		}
		Properties properties = new Properties();
		properties.setProperty("pemFile", cf.getAbsolutePath());

		// only for CA there is no option for hostNameOverride.
		if (hostNameOverRide == null) {
			properties.setProperty("allowAllHostNames", "true");//
		} else {
			properties.setProperty("hostnameOverride", hostNameOverRide);
		}
		properties.setProperty("sslProvider", "openSSL");
		properties.setProperty("negotiationType", "TLS");
		return properties;
	}

	public int getTransactionWaitTime() {
		return INVOKEWAITTIME;
	}

	public int getDeployWaitTime() {
		return DEPLOYWAITTIME;
	}

	public int getGossipWaitTime() {
		return GOSSIPWAITTIME;
	}

	public long getProposalWaitTime() {
		return PROPOSALWAITTIME;
	}

	public SampleOrg getSampleOrg(String name) {
		return sampleOrgs.get(name);
	}

	public SampleOrg setSampleOrg(String name, SampleOrg org) {
		return sampleOrgs.put(name, org);

	}

	public void setClient (SampleOrg sampleOrg) throws CryptoException, InvalidArgumentException{
		
		  HFClient client = HFClient.createNewInstance();
		  client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
		  sampleOrg.setClient(client);	  
	}
	
	public void setSampleStore (SampleOrg sampleOrg){
		  //Persistence is not part of SDK. Sample file store is for demonstration purposes only!
        //   MUST be replaced with more robust application implementation  (Database, LDAP)
        
		File sampleStoreFile = new File(System.getProperty("java.io.tmpdir") + "/HFCSampletest"+sampleOrg.name+".properties");
        if (sampleStoreFile.exists()) { //For testing start fresh
            sampleStoreFile.delete();
        }

        SampleStore sampleStore = new SampleStore(sampleStoreFile);
        sampleOrg.setSampleStore(sampleStore);
	}
	
	public void setOrgAdmin(SampleOrg sampleOrg, String adminCertPath, String adminKeyPath) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, IOException{
		
		 SampleStore sampleStore = sampleOrg.getSampleStore();
		 final String sampleOrgName = sampleOrg.getName();
		 final String sampleOrgDomainName = sampleOrg.getDomainName();
		SampleUser peerOrgAdmin = sampleStore.getMember(sampleOrgName + "Admin", sampleOrgName, sampleOrg.getMSPID(),
                findFileSk(Paths.get("src/test/fixture/",adminKeyPath).toFile()),
                Paths.get("src/test/fixture/",adminCertPath,format("/Admin@%s-cert.pem", sampleOrgDomainName)).toFile());

        sampleOrg.setOrgAdmin(peerOrgAdmin); //A special user that can create channels, join peers and install chaincode
		
		
	}
	
	public SampleUser getOrgAdmin(String orgName){
		return sampleOrgs.get(orgName).getOrgAdmin();
	}
	
	private static File findFileSk(File directory) {

	        File[] matches = directory.listFiles((dir, name) -> name.endsWith("_sk"));

	        if (null == matches) {
	            throw new RuntimeException(format("Matches returned null does %s directory exist?", directory.getAbsoluteFile().getName()));
        }

	        if (matches.length != 1) {
	            throw new RuntimeException(format("Expected in %s only 1 sk file but found %d", directory.getAbsoluteFile().getName(), matches.length));
	        }

	        return matches[0];

	    }
		
	public void setAdminUser(SampleOrg sampleOrg) throws EnrollmentException, org.hyperledger.fabric_ca.sdk.exception.InvalidArgumentException{
		
		HFCAClient ca = sampleOrg.getCAClient();
		SampleStore sampleStore = sampleOrg.getSampleStore();
        final String orgName = sampleOrg.getName();
        final String mspid = sampleOrg.getMSPID();
        ca.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
        SampleUser admin = sampleStore.getMember(TEST_ADMIN_NAME, orgName);

        if (!admin.isEnrolled()) {  //Preregistered admin only needs to be enrolled with Fabric caClient.
            
        	System.out.println("Admin is not enrolled");
        	admin.setEnrollment(ca.enroll(admin.getName(), TEST_ADMIN_PW));
            admin.setMspId(mspid);
        }

        sampleOrg.setAdminUser(admin); // The admin of this org --*/
	}
	
	public SampleUser getAdminUser(String orgName){
		return sampleOrgs.get(orgName).getAdminUser();
	}
	
	public Collection<Peer> newPeers(String peers[],SampleOrg sampleOrg ) throws InvalidArgumentException{
		Collection<Peer> targets = new Vector<>();
		HFClient client = sampleOrg.getClient();
		for (String peerName:peers){
			Peer peer = client.newPeer(peerName,sampleOrg.getPeerLocation(peerName), sampleOrg.getPeerProperties(peerName));
			targets.add(peer);
		}
		
		return targets;
	}
	
	public Collection<Orderer> newOrderer(SampleOrg sampleOrg, Channel newChannel ) throws InvalidArgumentException{
		Collection<Orderer> targets = new Vector<>();
		HFClient client = sampleOrg.getClient();
		for (String ordererName:sampleOrg.getOrdererNames()){
		Orderer orderer = client.newOrderer(ordererName,
				sampleOrg.getOrdererLocation(ordererName), sampleOrg.getordererProperties());
		targets.add(orderer);
	}
		
		return targets;
	}
	
	public Channel getChannelInstance(Collection<Peer> peers, String channelName, HFClient client, SampleOrg sampleOrg) throws InvalidArgumentException, TransactionException{
		
		// add orderer, add peer, add eventhub and initialize the channel. for all the cases do a check if they already present or not
		client.setUserContext(sampleOrg.getOrgAdmin());
		Channel newChannel = client.newChannel(channelName);
		
		// set the orderer and peers in the channel object
		String orderName = sampleOrg.getOrdererNames().iterator().next();
		Properties ordererProperties = sampleOrg.getordererProperties();
		ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveTime",
				new Object[] { 5L, TimeUnit.MINUTES });
		ordererProperties.put(
				"grpc.NettyChannelBuilderOption.keepAliveTimeout",
				new Object[] { 8L, TimeUnit.SECONDS });
		Orderer orderer = client.newOrderer(orderName, sampleOrg.getOrdererLocation(orderName),ordererProperties);
		if (newChannel.getOrderers().size() == 0) {
			System.out.println("orderer org is not added");
			newChannel.addOrderer(orderer);
		}
		
		System.out.println("peer length"+peers.size());
		if (newChannel.getPeers().size() == 0) {
		
			for (Peer peer: peers){
    			newChannel.addPeer(peer);
    			
			}
		}
		for (String event:sampleOrg.getEventHubNames()){
		
			newChannel.addEventHub(client.newEventHub(event, sampleOrg.getEventHubLocation(event)));
		}
		if (!newChannel.isInitialized())
		{
			newChannel.initialize();
		}
		return newChannel;
 }

	public Collection<SampleOrg> getIntegrationTestsSampleOrgs() {
        return Collections.unmodifiableCollection(sampleOrgs.values());
    }
	
	public void setOrgPeers(SampleOrg sampleOrg) throws InvalidArgumentException{
		System.out.println("organization"+sampleOrg.getName());
		HFClient client = sampleOrg.getClient(); 
		client.setUserContext(sampleOrg.getOrgAdmin());
		for (String peerName:sampleOrg.getPeerNames()){
			System.out.println("Peer name in add peer set"+peerName);
			Peer peer = client.newPeer(peerName,sampleOrg.getPeerLocation(peerName), sampleOrg.getPeerProperties(peerName));
			sampleOrg.peers.add(peer);
		}	
	}
	/*public static void main(String[] args) throws InvalidArgumentException {
		NetworkConfig nc = new NetworkConfig();
		nc.initialConfig("");
		String cc_args[] = { "open", "key" };
		//nc.getSampleOrg("Org1");
		ChannelUtility util = new ChannelUtility();
		String peers1[]={"peer0.org1.example.com","peer1.org1.example.com"};
		String peers2[]={"peer0.org2.example.com","peer1.org2.example.com"};
		Collection<Peer> porg1 = nc.newPeers(peers1, nc.getSampleOrg("Org1"));
		Collection<Peer>targetPeer=new Vector<Peer>();
		Collection<Peer> porg2 = nc.newPeers(peers2, nc.getSampleOrg("Org2"));
		for (Peer peer:porg1){
			targetPeer.add(peer);
		}
		for (Peer peer:porg2){
			targetPeer.add(peer);
		}
		//util.createChannel("mychannel", channelConfigPath, nc.getSampleOrg("Org1"));
		//util.joinChannel("mychannel",peers, nc.getSampleOrg("Org1"));
	
		ChaincodeUtility cc = new ChaincodeUtility();
		//cc.installChaincode(peers1, "simple", "0", "simple", nc.getSampleOrg("Org1"));
		//cc.installChaincode(peers2, "simple", "0", "simple", nc.getSampleOrg("Org2"));
		//cc.instantiateChaincode("mychannel", "simple", "0", "simple", "init", cc_args, nc.getSampleOrg("Org1"));
		cc.invokeChaincode(targetPeer, "simple1", "1", "simple1", "open", cc_args, "mychannel", nc.getSampleOrg("Org1"));
		//cc.invokeChaincode(targetPeer, chaincodeName, chaincodeVersion, chaincodePath, functionName, cc_args, channelName, sampleOrg);
	}*/
}
