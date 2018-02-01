package com.psl.fabric.config;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric_ca.sdk.HFCAClient;

/*
 *  Copyright 2016, 2017 DTCC, Fujitsu Australia Software Technology, IBM - All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * Sample Organization Representation
 *
 * Keeps track which resources are defined for the Organization it represents.
 *
 */
public class SampleOrg {
    final String name;
    final String mspid;
    HFCAClient caClient;
    HFClient client;

    SampleStore sampleStore;
    Map<String, User> userMap = new HashMap<>();
    Map<String, String> peerLocations = new HashMap<>();
    Map<String, String> ordererLocations = new HashMap<>();
    Map<String, String> eventHubLocations = new HashMap<>();
    Set<Peer> peers = new HashSet<>();
    @Override
	public String toString() {
		return "SampleOrg [name=" + name + ", mspid=" + mspid + ", caClient="
				+ caClient + ", client=" + client + ", userMap=" + userMap
				+ ", peerLocations=" + peerLocations + ", ordererLocations="
				+ ordererLocations + ", eventHubLocations=" + eventHubLocations
				+ ", peers=" + peers + ", admin=" + adminUser + ", caLocation="
				+ caLocation + ", caProperties=" + caProperties
				+ ", peerProperties=" + peerProperties
				+ ", ordeererProperties=" + ordeererProperties + ", peerAdmin="
				+ orgAdmin + ", domainName=" + domainName + "]";
	}

	private SampleUser adminUser;
    private String caLocation;
    private Properties caProperties = null;
    private  Map<String, Properties> peerProperties = new HashMap<>();
    private Properties ordeererProperties = null;

    private SampleUser orgAdmin;


    private String domainName;

    public SampleOrg(String name, String mspid) {
        this.name = name;
        this.mspid = mspid;
    }

    public SampleUser getAdminUser() {
        return adminUser;
    }

    public void setAdminUser(SampleUser adminUser) {
        this.adminUser = adminUser;
    }

    public String getMSPID() {
        return mspid;
    }

    public String getCALocation() {
        return this.caLocation;
    }

    public void setCALocation(String caLocation) {
        this.caLocation = caLocation;
    }

    public void addPeerLocation(String name, String location) {

        peerLocations.put(name, location);
    }

    public void addOrdererLocation(String name, String location) {

        ordererLocations.put(name, location);
    }

    public void addEventHubLocation(String name, String location) {

        eventHubLocations.put(name, location);
    }

    public String getPeerLocation(String name) {
        return peerLocations.get(name);

    }

    public String getOrdererLocation(String name) {
        return ordererLocations.get(name);

    }

    public String getEventHubLocation(String name) {
        return eventHubLocations.get(name);

    }

    public Set<String> getPeerNames() {

        return Collections.unmodifiableSet(peerLocations.keySet());
    }


    public Set<String> getOrdererNames() {

        return Collections.unmodifiableSet(ordererLocations.keySet());
    }

    public Set<String> getEventHubNames() {

        return Collections.unmodifiableSet(eventHubLocations.keySet());
    }

    public HFCAClient getCAClient() {

        return caClient;
    }

    public void setCAClient(HFCAClient caClient) {

        this.caClient = caClient;
    }
    
    public HFClient getClient() {

        return client;
    }

    public void setClient(HFClient client) {

        this.client = client;
    }
    
    public String getName() {
        return name;
    }

    public void addUser(SampleUser user) {
        userMap.put(user.getName(), user);
    }

    public User getUser(String name) {
        return userMap.get(name);
    }

    public Collection<String> getOrdererLocations() {
        return Collections.unmodifiableCollection(ordererLocations.values());
    }

    public Collection<String> getEventHubLocations() {
        return Collections.unmodifiableCollection(eventHubLocations.values());
    }

    public Set<Peer> getPeers() {
        return Collections.unmodifiableSet(peers);
    }

    public void addPeer(Peer peer) {
        peers.add(peer);
    }

    public void setCAProperties(Properties caProperties) {
        this.caProperties = caProperties;
    }

    public Properties getCAProperties() {
        return caProperties;
    }

    public void setPeerProperties(String peerName, Properties peerProperties) {
        this.peerProperties.put(peerName, peerProperties);
    }

    public Properties getPeerProperties(String peerName) {
        return peerProperties.get(peerName);
    }
    public void setOrdererProperties(Properties ordererProperties) {
        this.ordeererProperties = ordererProperties;
    }

    public Properties getordererProperties() {
        return ordeererProperties;
    }

    public SampleUser getOrgAdmin() {
        return orgAdmin;
    }

    public void setOrgAdmin(SampleUser orgAdmin) {
        this.orgAdmin = orgAdmin;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getDomainName() {
        return domainName;
    }
    
    public SampleStore getSampleStore() {
        return sampleStore;
    }
    
    public void setSampleStore(SampleStore sampleStore) {
    	 this.sampleStore = sampleStore;
    }
    
}
