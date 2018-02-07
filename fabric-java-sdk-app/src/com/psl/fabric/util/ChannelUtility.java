package com.psl.fabric.util;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

import org.hyperledger.fabric.protos.common.Common.Block;
import org.hyperledger.fabric.sdk.BlockInfo;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.ChannelConfiguration;
import org.hyperledger.fabric.sdk.EventHub;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;

import com.psl.fabric.config.*;

public class ChannelUtility {

	// TODO: make these as private functions and add a public function which just
	// calls these private fnc.
	private static String createChannelHelper(String channelName, String channelConfigPath,
			SampleOrg sampleOrg) throws Exception {

		HFClient client = sampleOrg.getClient();
		SampleUser orgAdmin = sampleOrg.getOrgAdmin();
			client.setUserContext(orgAdmin);

			String orderName = sampleOrg.getOrdererNames().iterator().next();
			Properties ordererProperties = sampleOrg.getordererProperties();

			// example of setting keepAlive to avoid timeouts on inactive http2
			// connections.
			// Under 5 minutes would require changes to server side to accept
			// faster ping rates.
			ordererProperties.put(
					"grpc.NettyChannelBuilderOption.keepAliveTime",
					new Object[] { 5L, TimeUnit.MINUTES });
			ordererProperties.put(
					"grpc.NettyChannelBuilderOption.keepAliveTimeout",
					new Object[] { 8L, TimeUnit.SECONDS });

			Orderer orderer = client.newOrderer(orderName,
					sampleOrg.getOrdererLocation(orderName), ordererProperties);

			ChannelConfiguration channelConfiguration = new ChannelConfiguration(
					new File(channelConfigPath));

			// Create channel that has only one signer that is this orgs peer
			// admin. If channel creation policy needed more signature they
			// would need to be added too.
			Channel newChannel = client.newChannel(channelName, orderer,
					channelConfiguration, client
							.getChannelConfigurationSignature(
									channelConfiguration, orgAdmin));

			newChannel.addOrderer(orderer);

			System.out.println("channel created:" + newChannel.getName());
			return newChannel.getName();
	}

	public static String createChannel(String channelName, String channelConfigPath,
			String orgName) throws Exception{
		NetworkConfig config = new NetworkConfig();
		SampleOrg sampleOrg = config.getSampleOrg(orgName);
		return createChannelHelper(channelName, config.pathPrefix+channelConfigPath, sampleOrg);
	}
	
	private static void joinChannelHelper(String channelName, String peers[],
			SampleOrg sampleOrg) {

		// add eventHub to get Join channel confirmation
		HFClient client = sampleOrg.getClient();
		SampleUser orgAdmin = sampleOrg.getOrgAdmin();
		String orderName = sampleOrg.getOrdererNames().iterator().next();
		Properties ordererProperties = sampleOrg.getordererProperties();

		// example of setting keepAlive to avoid timeouts on inactive http2
		// connections.
		// Under 5 minutes would require changes to server side to accept
		// faster ping rates.
		ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveTime",
				new Object[] { 5L, TimeUnit.MINUTES });
		ordererProperties.put(
				"grpc.NettyChannelBuilderOption.keepAliveTimeout",
				new Object[] { 8L, TimeUnit.SECONDS });

		try {
			client.setUserContext(orgAdmin);
			// check if orderer is already present or not
			Orderer orderer = client.newOrderer(orderName,
					sampleOrg.getOrdererLocation(orderName), ordererProperties);
			Channel newChannel = client.newChannel(channelName);

			if (newChannel.getOrderers().size() == 0) {
				System.out.println("orderer org is not added");
				newChannel.addOrderer(orderer);
			}
			for (String peerName : peers) {
				Properties peerProperties = sampleOrg
						.getPeerProperties(peerName); // test properties for
														// peer.. if any.
				if (peerProperties == null) {
					peerProperties = new Properties();
				}
				// Example of setting specific options on grpc's
				// NettyChannelBuilder
				peerProperties.put(
						"grpc.NettyChannelBuilderOption.maxInboundMessageSize",
						9000000);
				Peer peer = client.newPeer(peerName,
						sampleOrg.getPeerLocation(peerName), peerProperties);
				newChannel.joinPeer(peer);
				// sampleOrg.addPeer(peer);--? if Set<Peer>not used, remove from
				// SampleOrg

			}

			if (verifyJoinChannel(client, newChannel)) {
				System.out.println("Peers joined successfully");
				// return here
			}

		} catch (InvalidArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProposalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AssertionError e) {
			// peer nor joined channel

		}
	}

	public static void joinChannel(String channelName, String peers[],
			String orgName){
		NetworkConfig config = new NetworkConfig();
		SampleOrg sampleOrg = config.getSampleOrg(orgName);
		joinChannelHelper(channelName, peers, sampleOrg);
	}
	
	private static boolean verifyJoinChannel(HFClient client, Channel newChannel)
			throws InvalidArgumentException, ProposalException {

		System.out.println("peer length" + newChannel.getPeers().size());
		for (Peer peer : newChannel.getPeers()) {
			Set<String> channels = client.queryChannels(peer);
			if (!channels.contains(newChannel.getName())) {
				throw new AssertionError(format(
						"Peer %s does not appear to belong to channel %s",
						peer.getName(), newChannel.getName()));
			}

		}
		return true;
	}

	private String registerBlockListener(HFClient client, Channel newChannel,
			SampleOrg sampleOrg) throws InvalidArgumentException,
			TransactionException {
		for (String eventHubName : sampleOrg.getEventHubNames()) {

			final Properties eventHubProperties = sampleOrg
					.getPeerProperties(eventHubName);
			eventHubProperties.put(
					"grpc.NettyChannelBuilderOption.keepAliveTime",
					new Object[] { 5L, TimeUnit.MINUTES });
			eventHubProperties.put(
					"grpc.NettyChannelBuilderOption.keepAliveTimeout",
					new Object[] { 8L, TimeUnit.SECONDS });
			EventHub eventHub;
			eventHub = client.newEventHub(eventHubName,
					sampleOrg.getEventHubLocation(eventHubName),
					eventHubProperties);
			newChannel.addEventHub(eventHub);
		}

		newChannel.initialize();
		return newChannel.registerBlockListener(blockEvent -> {
			int i = 0;
			for (BlockInfo.EnvelopeInfo envelopeInfo : blockEvent
					.getEnvelopeInfos()) {
				++i;
				final String channelId = envelopeInfo.getChannelId();
				if (channelId == newChannel.getName()) {
					System.out.println("channel is present");
					// return true;
			}
		}
	})	;

	}
}