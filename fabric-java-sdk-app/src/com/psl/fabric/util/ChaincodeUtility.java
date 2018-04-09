package com.psl.fabric.util;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.ChaincodeEndorsementPolicy;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.InstallProposalRequest;
import org.hyperledger.fabric.sdk.InstantiateProposalRequest;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.QueryByChaincodeRequest;
import org.hyperledger.fabric.sdk.SDKUtils;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.hyperledger.fabric.sdk.BlockEvent.TransactionEvent;
import org.hyperledger.fabric.sdk.exception.ChaincodeEndorsementPolicyParseException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionEventException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import com.psl.fabric.config.*;


public class ChaincodeUtility {

	public void installChaincode(String[] peers, String chaincodeName,
			String chaincodeVersion, String chaincodePath, SampleOrg sampleOrg) {

		NetworkConfig nconfig = new NetworkConfig();
		HFClient client = sampleOrg.getClient();
		SampleUser orgAdmin = sampleOrg.getOrgAdmin();
		final ChaincodeID chaincodeID;
		Collection<ProposalResponse> responses;
		Collection<ProposalResponse> successful = new LinkedList<>();
		Collection<ProposalResponse> failed = new LinkedList<>();

		chaincodeID = ChaincodeID.newBuilder().setName(chaincodeName)
				.setVersion(chaincodeVersion).setPath(chaincodePath).build();
		try {
			client.setUserContext(orgAdmin);
			InstallProposalRequest installProposalRequest = client
					.newInstallProposalRequest();
			installProposalRequest.setChaincodeID(chaincodeID);
			// GOPATH..READ FROM CONFIG.JSON
			installProposalRequest.setChaincodeSourceLocation(new File(
					nconfig.GOPATH));
			installProposalRequest.setChaincodeVersion(chaincodeVersion);
			responses = client.sendInstallProposal(installProposalRequest,
					nconfig.newPeers(peers, sampleOrg));

			for (ProposalResponse response : responses) {
				if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
					System.out
							.println("Successful install proposal response Txid: %s from peer %s"
									+ response.getTransactionID()
									+ " "
									+ response.getPeer().getName());
					successful.add(response);
				} else {
					failed.add(response);
				}
			}

			Collection<Set<ProposalResponse>> proposalConsistencySets = SDKUtils
					.getProposalConsistencySets(responses);
			if (proposalConsistencySets.size() != 1) {
				System.out
						.println("Expected only one set of consistent proposal responses but got "
								+ proposalConsistencySets.size());
				throw new AssertionError(
						format("Expected only one set of consistent proposal responses but got%s",
								proposalConsistencySets.size()));

			}

			if (failed.size() > 0) {
				ProposalResponse first = failed.iterator().next();
				throw new AssertionError(
						"Failed to send install Proposal or receive valid response. Response null or status is not 200. exiting...'");

			} else {
				System.out.println("Successfully installed");
			}

		} catch (InvalidArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProposalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void instantiateChaincode(String channelName, String chaincodeName,
			String chaincodeVersion, String chaincodePath, String functionName,
			String args[], SampleOrg sampleOrg) {

		// the channel object should have peers from all the orgs whom the
		// client wishes to get endorsement from.
		NetworkConfig nconfig = new NetworkConfig();
		int INVOKEWAITTIME = nconfig.getTransactionWaitTime();
		int DEPLOYWAITTIME = nconfig.getDeployWaitTime();
		Long PROPOSALWAITTIME = nconfig.getProposalWaitTime();
		HFClient client = sampleOrg.getClient();
		SampleUser orgAdmin = sampleOrg.getOrgAdmin();
		final ChaincodeID chaincodeID;
		Collection<ProposalResponse> responses;
		Collection<ProposalResponse> successful = new LinkedList<>();
		Collection<ProposalResponse> failed = new LinkedList<>();
		Collection<Peer> targetPeer = new Vector<>();
		chaincodeID = ChaincodeID.newBuilder().setName(chaincodeName)
				.setVersion(chaincodeVersion).setPath(chaincodePath).build();

		for (SampleOrg org : nconfig.getIntegrationTestsSampleOrgs()) {

			System.out.println("ORGS are"+org.getName());
			for (Peer p : org.getPeers()) {
				targetPeer.add(p);
			}
		}
				
		try {
			client.setUserContext(orgAdmin);
			Channel newChannel = nconfig.getChannelInstance(targetPeer,
					channelName, client, sampleOrg);
			newChannel.setTransactionWaitTime(INVOKEWAITTIME);
			newChannel.setDeployWaitTime(DEPLOYWAITTIME);

			System.out.println("Channel peers:"+ newChannel.getPeers());
			InstantiateProposalRequest instantiateProposalRequest = client
					.newInstantiationProposalRequest();
			instantiateProposalRequest.setProposalWaitTime(PROPOSALWAITTIME);
			instantiateProposalRequest.setChaincodeID(chaincodeID);
			instantiateProposalRequest.setFcn(functionName);
			instantiateProposalRequest.setArgs(args);
			Map<String, byte[]> tm2 = new HashMap<>();
			tm2.put("HyperLedgerFabric",
					"TransactionProposalRequest:JavaSDK".getBytes(UTF_8));
			tm2.put("method", "TransactionProposalRequest".getBytes(UTF_8));
			tm2.put("result", ":)".getBytes(UTF_8)); // / This should be
														// returned see
														// chaincode.
			instantiateProposalRequest.setTransientMap(tm2);
			ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();

			chaincodeEndorsementPolicy
					.fromYamlFile(new File(
							"src/test/fixture/sample_chaincode_endorsement_policies/chaincodeendorsementpolicy.yaml"));

			instantiateProposalRequest
					.setChaincodeEndorsementPolicy(chaincodeEndorsementPolicy);

			responses = newChannel.sendInstantiationProposal(
					instantiateProposalRequest, newChannel.getPeers());

			for (ProposalResponse response : responses) {
				if (response.isVerified()
						&& response.getStatus() == ProposalResponse.Status.SUCCESS) {
					successful.add(response);
					System.out
							.println("Succesful instantiate proposal response Txid: %s from peer %s"
									+ response.getTransactionID()
									+ " "
									+ response.getPeer().getName());
				} else {
					failed.add(response);
				}
			}
			if (failed.size() > 0) {
				ProposalResponse first = failed.iterator().next();
				System.out.println("Not enough endorsers for instantiate :"
						+ successful.size() + "endorser failed with "
						+ first.getMessage() + ". Was verified:"
						+ first.isVerified());
			}

			System.out.println("Send it to the orderer");

			CompletableFuture<TransactionEvent> f = newChannel.sendTransaction(
					successful, newChannel.getOrderers());
			f.get(600, TimeUnit.SECONDS).getTransactionID();

			f.exceptionally(e -> {
				if (e instanceof TransactionEventException) {
					BlockEvent.TransactionEvent te = ((TransactionEventException) e)
							.getTransactionEvent();
					if (te != null) {
						System.out
								.println("Transaction with txid %s failed. %s"
										+ te.getTransactionID() + " "
										+ e.getMessage());
					}
				}
				System.out.println("Test failed with %s exception %s"
						+ e.getClass().getName() + " " + e.getMessage());
				return null;
			});

		} catch (InvalidArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ExecutionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TimeoutException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ProposalException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ChaincodeEndorsementPolicyParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransactionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	public void invokeChaincode(Collection<Peer> targetPeer,
			String chaincodeName, String chaincodeVersion,
			String chaincodePath, String functionName, String args[],
			String channelName, SampleOrg sampleOrg) {

		NetworkConfig nconfig = new NetworkConfig();
		int INVOKEWAITTIME = nconfig.getTransactionWaitTime();
		int DEPLOYWAITTIME = nconfig.getDeployWaitTime();
		Long PROPOSALWAITTIME = nconfig.getProposalWaitTime();
		HFClient client = sampleOrg.getClient();
		SampleUser orgAdmin = sampleOrg.getOrgAdmin();
		final ChaincodeID chaincodeID;
		chaincodeID = ChaincodeID.newBuilder().setName(chaincodeName)
				.setVersion(chaincodeVersion).setPath(chaincodePath).build();
		Collection<ProposalResponse> responses;
		Collection<ProposalResponse> successful = new LinkedList<>();
		Collection<ProposalResponse> failed = new LinkedList<>();

		try {
			client.setUserContext(orgAdmin);

			Channel newChannel = nconfig.getChannelInstance(targetPeer,
					channelName, client, sampleOrg);

			newChannel.setTransactionWaitTime(INVOKEWAITTIME);
			// ////////////
			// / Send transaction proposal to all peers
			TransactionProposalRequest transactionProposalRequest = client
					.newTransactionProposalRequest();
			transactionProposalRequest.setChaincodeID(chaincodeID);
			transactionProposalRequest.setFcn(functionName);
			transactionProposalRequest.setProposalWaitTime(PROPOSALWAITTIME);
			transactionProposalRequest.setArgs(args);

			// is transient map compulsory ?? only for instantiate
			Map<String, byte[]> tm2 = new HashMap<>();
			tm2.put("HyperLedgerFabric",
					"TransactionProposalRequest:JavaSDK".getBytes(UTF_8));

			// transactionProposalRequest.setTransientMap(tm2);

			Collection<ProposalResponse> transactionPropResp = newChannel
					.sendTransactionProposal(transactionProposalRequest,
							newChannel.getPeers());

			for (ProposalResponse response : transactionPropResp) {
				if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
					System.out
							.println("Successful transaction proposal response Txid: %s from peer %s"
									+ response.getTransactionID()
									+ " "
									+ response.getPeer().getName());
					successful.add(response);
				} else {
					failed.add(response);
				}
			}

			// Check that all the proposals are consistent with each other. We
			// should have only one set
			// where all the proposals above are consistent.
			// call SDK level fucntion to check the consistency of proposal
			// responses
			Collection<Set<ProposalResponse>> proposalConsistencySets = SDKUtils
					.getProposalConsistencySets(transactionPropResp);
			if (proposalConsistencySets.size() != 1) {
				System.out
						.println("Expected only one set of consistent proposal responses but got "
								+ proposalConsistencySets.size());
			}

			System.out.println("Received " + transactionPropResp.size() + " "
					+ "transaction proposal responses. Successful "
					+ successful.size() + " Failed " + failed.size());

			if (failed.size() > 0) {
				ProposalResponse firstTransactionProposalResponse = failed
						.iterator().next();
				System.out.println("Not enough endorsers for open:"
						+ failed.size() + " endorser error: "
						+ firstTransactionProposalResponse.getMessage()
						+ ". Was verified: "
						+ firstTransactionProposalResponse.isVerified());
			} else {
				System.out
						.println("Successfully received transaction proposal responses.");
				ProposalResponse resp = transactionPropResp.iterator().next();
				byte[] x = resp.getChaincodeActionResponsePayload(); // CC
																		// response

				String resultAsString = null;
				if (x != null) {
					resultAsString = new String(x, "UTF-8");
				}
				System.out.println("Chaincode Response Status is: "
						+ resp.getChaincodeActionResponseStatus()); 
				System.out.println("Sending chaincode transaction to orderer.");
				System.out.println(newChannel.getEventHubs().iterator().next()
						.isConnected());
				CompletableFuture<TransactionEvent> f = newChannel
						.sendTransaction(successful, newChannel.getOrderers());
				System.out.println(f.get(600, TimeUnit.SECONDS)
						.getTransactionID());
				f.exceptionally(e -> {
					if (e instanceof TransactionEventException) {
						BlockEvent.TransactionEvent te = ((TransactionEventException) e)
								.getTransactionEvent();
						if (te != null) {
							System.out
									.println("Transaction with txid %s failed. %s"
											+ te.getTransactionID()
											+ " "
											+ e.getMessage());
						}
					}
					System.out.println("Test failed with %s exception %s"
							+ e.getClass().getName() + " " + e.getMessage());
					return null;
				});
			}
		} catch (InvalidArgumentException | TransactionException
				| ProposalException | UnsupportedEncodingException
				| InterruptedException | ExecutionException | TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void queryChaincode(Collection<Peer> targetPeer,
			String chaincodeName, String chaincodeVersion,
			String chaincodePath, String functionName, String args[],
			String channelName, SampleOrg sampleOrg) {

		NetworkConfig nconfig = new NetworkConfig();
		int INVOKEWAITTIME = nconfig.getTransactionWaitTime();
		// int DEPLOYWAITTIME = nconfig.getDeployWaitTime();
		Long PROPOSALWAITTIME = nconfig.getProposalWaitTime();
		HFClient client = sampleOrg.getClient();
		SampleUser orgAdmin = sampleOrg.getOrgAdmin();
		final ChaincodeID chaincodeID;
		Collection<ProposalResponse> responses;
		Collection<ProposalResponse> successful = new LinkedList<>();
		Collection<ProposalResponse> failed = new LinkedList<>();
		chaincodeID = ChaincodeID.newBuilder().setName(chaincodeName)
				.setVersion(chaincodeVersion).setPath(chaincodePath).build();
		try {
			client.setUserContext(orgAdmin);

			Channel newChannel = nconfig.getChannelInstance(targetPeer,
					channelName, client, sampleOrg);
			newChannel.setTransactionWaitTime(INVOKEWAITTIME);
			// /////////////
			// / Send transaction proposal to all peers
			QueryByChaincodeRequest queryByChaincodeRequest = client
					.newQueryProposalRequest();
			queryByChaincodeRequest.setArgs(args);
			queryByChaincodeRequest.setFcn(functionName);
			queryByChaincodeRequest.setChaincodeID(chaincodeID);
			Collection<ProposalResponse> queryProposals = newChannel
					.queryByChaincode(queryByChaincodeRequest,
							newChannel.getPeers());
			for (ProposalResponse proposalResponse : queryProposals) {
				if (!proposalResponse.isVerified()
						|| proposalResponse.getStatus() != ProposalResponse.Status.SUCCESS) {
					System.out.println("Failed query proposal from peer "
							+ proposalResponse.getPeer().getName()
							+ " status: " + proposalResponse.getStatus()
							+ ". Messages: " + proposalResponse.getMessage()
							+ ". Was verified : "
							+ proposalResponse.isVerified());
				} else {
					String payload = proposalResponse.getProposalResponse()
							.getResponse().getPayload().toStringUtf8();
					System.out.println("PAYLOAD"+payload);
				}
			}
		} catch (InvalidArgumentException | TransactionException
				| ProposalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
