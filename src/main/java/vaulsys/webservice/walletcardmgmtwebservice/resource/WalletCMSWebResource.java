package vaulsys.webservice.walletcardmgmtwebservice.resource;



import vaulsys.message.MessageManager;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.util.WebServiceUtil;
import vaulsys.webservice.walletcardmgmtwebservice.component.WSOperation;
import vaulsys.webservice.walletcardmgmtwebservice.entity.WalletCMSWsEntity;
import vaulsys.webservice.walletcardmgmtwebservice.webservice.WalletCMSWSServer;
import vaulsys.wfe.process.MainWSProcess;
import org.apache.log4j.Logger;
import vaulsys.wfe.process.SimpleMainWSProcess;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Path("/NayaPayWalletCMSGateway/webresources/nayapay")
public class WalletCMSWebResource {

	private static final Logger logger = Logger.getLogger(WalletCMSWebResource.class);

	//m.rehman: trying to get ip address of a client
	@Context
	private HttpServletRequest request;

	@GET
	@Path("/ping")
	public String getServerTime() {
		System.out.println("Wallet CMS Service is running ==> ping");
		logger.info("Wallet CMS Service is running ==> ping");
		return "received ping on "+new Date().toString();
	}

	@Path("/checkcnic")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity CheckCnic(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource CheckCnic Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("CheckCnic");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//create thread here
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			//MessageManager.threadPool.execute(process);
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing CheckCnic call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending CheckCnic reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/createwallet")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity CreateWallet(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource CreateWallet Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("CreateWallet");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//create thread here
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			//MessageManager.threadPool.execute(process);
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing CreateWallet call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending CreateWallet reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/createwalletpin")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity CreateWalletPIN(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource CreateWalletPIN Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("CreateWalletPIN");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//create thread here
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			//MessageManager.threadPool.execute(process);
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing CreateWalletPIN call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending CreateWalletPIN reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	// Asim Shahzad, Date : 9th March 2021, Tracking ID : VP-NAP-202103112 / VC-NAP-202103112
	@Path("/createwalletpinwithsecretquestions")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity CreateWalletPINWithSecretQuestions(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource CreateWalletPINWithSecretQuestions Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("CreateWalletPINWithSecretQuestions");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//create thread here
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			//MessageManager.threadPool.execute(process);
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing CreateWalletPINWithSecretQuestions call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending CreateWalletPINWithSecretQuestions reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}
	//=================================================================================================================

	@Path("/deleteprovisionalwallet")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity DeleteProvisionalWallet(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource DeleteProvisionalWallet Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("DeleteProvisionalWallet");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//create thread here
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			//MessageManager.threadPool.execute(process);
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing DeleteProvisionalWallet call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending DeleteProvisionalWallet reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/activateprovisionalwallet")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity ActivateProvisionalWallet(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource ActivateProvisionalWallet Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("ActivateProvisionalWallet");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//create thread here
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			//MessageManager.threadPool.execute(process);
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing ActivateProvisionalWallet call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending ActivateProvisionalWallet reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/customerenablewalletaccount") //enable-disbale wallet account
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity CustomerEnableWalletAccount(WalletCMSWsEntity wsobj) throws Exception {
		try {
			logger.info("WebResource CustomerEnableWalletAccount Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("CustomerEnableWalletAccount");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing CustomerEnableWalletAccount call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending CustomerEnableWalletAccount reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/createwalletlevelone")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity CreateWalletLevelOne(WalletCMSWsEntity wsobj) throws Exception{
		try {
			logger.info("WebResource CreateWalletLevelOne Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("CreateWalletLevelOne");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing createwalletlevelone call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending CreateWalletlevelone reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
		//return npwm;
	}

	@Path("/changewalletpin")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity ChangeWalletPin(WalletCMSWsEntity wsobj) {

		try {
			logger.info("WebResource ChangeWalletPin Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("ChangeWalletPin");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//Execute Thread Here...!
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();


		} catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing ChangeWalletPin call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending ChangeWalletPin reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/verifywalletpin")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity VerifyWalletPin(WalletCMSWsEntity wsobj) {

		try {
			logger.info("WebResource VerifyWalletPin Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("VerifyWalletPin");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//Execute Thread Here...!
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();


		} catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing ChangeWalletPin call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending ChangeWalletPin reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/debitcardrequest")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity RequestDebitCard(WalletCMSWsEntity wsobj) throws Exception {
		try {
			logger.info("WebResource DebitCardRequest Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("DebitCardRequest");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing debitcardrequest call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending debitcardrequest reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/enabledebitcard")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity EnableDebitCard(WalletCMSWsEntity wsobj) throws Exception {
		try {
			logger.info("WebResource EnableDebitCard Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("EnableDebitCard");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing EnableDebitCard call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending EnableDebitCard reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/supportportalenabledebitcard")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity SupportPortalEnableDebitCard(WalletCMSWsEntity wsobj) throws Exception {
		try {
			logger.info("WebResource SupportPortalEnableDebitCard Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("SupportPortalEnableDebitCard");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing SupportPortalEnableDebitCard call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending SupportPortalEnableDebitCard reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/supportportalblockdebitcard")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity SupportPortalBlockDebitCard(WalletCMSWsEntity wsobj) throws Exception {
		try {
			logger.info("WebResource SupportPortalBlockDebitCard Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("SupportPortalBlockDebitCard");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing SupportPortalBlockDebitCard call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending SupportPortalBlockDebitCard reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/changedebitcardpin")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity ChangeDebitCardPin(WalletCMSWsEntity wsobj) throws Exception {
		try {
			logger.info("WebResource ChangeDebitCardPin Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("ChangeDebitCardPin");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing ChangeDebitCardPin call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending ChangeDebitCardPin reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/linkbankaccountinquiry")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity LinkBankAccountInquiry(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource LinkBankAccountInquiry Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}

			wsobj.setServicename("LinkBankAccountInquiry");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing LinkBankAccountInquiry call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending LinkBankAccountInquiry reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/linkbankaccount")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity LinkBankAccount(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource LinkBankAccount Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}

			wsobj.setServicename("LinkBankAccount");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing LinkBankAccount call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending LinkBankAccount reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/unlinkbankaccountinquiry")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity UnLinkBankAccountInquiry(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource UnLinkBankAccountInquiry Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}

			wsobj.setServicename("UnLinkBankAccountInquiry");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing UnLinkBankAccountInquiry call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending UnLinkBankAccountInquiry reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/unlinkbankaccount")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity UnLinkBankAccount(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource UnLinkBankAccount Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}

			wsobj.setServicename("UnLinkBankAccount");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing UnLinkBankAccount call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending UnLinkBankAccount reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/linkbankaccountotp")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity LinkBankAccountOTP(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource LinkBankAccountOTP Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}

			wsobj.setServicename("LinkBankAccountOTP"); //Raza segregating from Normal LinkBankAccount as it will require OTP to confirm
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing LinkBankAccountOTP call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending LinkBankAccountOTP reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/verifylinkaccountotp")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity VerifyLinkAccountOTP(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource VerifyLinkAccountOTP Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}

			wsobj.setServicename("VerifyLinkAccountOTP"); //Raza segregating from Normal LinkBankAccount as it will require OTP to confirm
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing VerifyLinkAccountOTP call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending VerifyLinkAccountOTP reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/confirmlinkbankaccountotp")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity ConfirmLinkBankAccountOTP(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource ConfirmLinkBankAccountOTP Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}

			wsobj.setServicename("ConfirmLinkBankAccountOTP"); //Raza segregating from Normal LinkBankAccount as it will require OTP to confirm
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing ConfirmLinkBankAccountOTP call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending ConfirmLinkBankAccountOTP reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/linkdebitcardaccount")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity LinkDebitCardAccount(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource LinkDebitCardAccount Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}

			wsobj.setServicename("LinkBankAccount"); //Raza it has no impact on WMS, as it will be same as LinkAccount
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing LinkDebitCardAccount call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending LinkDebitCardAccount reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/unlinkdebitcardaccount")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity UnLinkDebitCardAccount(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource UnLinkDebitCardAccount Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}

			wsobj.setServicename("UnLinkBankAccount"); //Raza it has no impact on WMS, as it will be same as LinkAccount
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing UnLinkDebitCardAccount call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending UnLinkDebitCardAccount reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/updatelinkedaccountalias")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity UpdateLinkedAccountAlias(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource UpdateLinkedAccountAlias Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}

			wsobj.setServicename("UpdateLinkedAccountAlias");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing UpdateLinkedAccountAlias call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending UpdateLinkedAccountAlias reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/setprimarylinkedaccount")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity SetPrimaryLinkedAccount(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource SetPrimaryLinkedAccount Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}

			wsobj.setServicename("SetPrimaryLinkedAccount");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing SetPrimaryLinkedAccount call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending SetPrimaryLinkedAccount reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/getusertoken")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity GetUserToken(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource GetUserToken Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}

			wsobj.setServicename("GetUserToken");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing GetUserToken call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending GetUserToken reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/getuserwallet")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity GetUserWallet(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource GetUserWallet Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}

			wsobj.setServicename("GetUserWallet");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing GetUserWallet call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending GetUserWallet reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/getuserdebitcard")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity GetUserdebitCard(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource GetUserdebitCard Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}

			wsobj.setServicename("GetUserdebitCard");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing GetUserdebitCard call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending GetUserdebitCard reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/getuserlinkedaccountlist")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity GetUserLinkedAccountList(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource GetUserLinkedAccountList Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}

			wsobj.setServicename("GetUserLinkedAccountList");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing GetUserLinkedAccountList call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending GetUserLinkedAccountList reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/getuserlinkedaccountlistwithouttoken")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity GetUserLinkedAccountListWithoutToken(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource GetUserLinkedAccountListWithoutToken Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}

			wsobj.setServicename("GetUserLinkedAccountListWithoutToken");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing GetUserLinkedAccountListWithoutToken call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending GetUserLinkedAccountListWithoutToken reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/supportportalgetusertransactionlist")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity SupportPortalGetUserTransactionList(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource SupportPortalGetUserTransactionList Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}

			wsobj.setServicename("SupportPortalGetUserTransactionList");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing SupportPortalGetUserTransactionList call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending SupportPortalGetUserTransactionList reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/getusertransaction")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity GetUserTransaction(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource GetUserTransaction Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}

			wsobj.setServicename("GetUserTransaction");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing GetUserTransaction call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending GetUserTransaction reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/getusertransactionforchat")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity GetUserTransactionforChat(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource GetUserTransactionforChat Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}

			wsobj.setServicename("GetUserTransactionforChat");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing GetUserTransactionforChat call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending GetUserTransactionforChat reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/getusertransactionlist")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity GetUserTransactionList(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource GetUserTransactionList Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}

			wsobj.setServicename("GetUserTransactionList");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing GetUserTransactionList call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending GetUserTransactionList reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/loadwalletinquiry")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity LoadWalletInquiry(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource LoadWalletInquiry Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("LoadWalletInquiry");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing LoadWalletInquiry call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending LoadWalletInquiry reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/loadwallet")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity LoadWallet(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource LoadWallet Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("LoadWallet");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing LoadWallet call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending LoadWallet reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/unloadwalletinquiry")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity UnloadWalletInquiry(WalletCMSWsEntity wsobj) throws Exception {

		try {
			logger.info("WebResource UnloadWalletInquiry Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("UnloadWalletInquiry");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing UnloadWalletInquiry call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending UnloadWalletInquiry reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/unloadwallet")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity UnloadWallet(WalletCMSWsEntity wsobj) throws Exception {

		try {
			logger.info("WebResource UnloadWallet Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("UnloadWallet");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing UnloadWallet call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending UnloadWallet reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/wallettransaction")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity WalletTransaction(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource WalletTransaction Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("WalletTransaction");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing WalletTransaction call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending WalletTransaction reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/merchantbillertransaction")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity MerchantBillerTransaction(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource MerchantBillerTransaction Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("MerchantBillerTransaction");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing MerchantBillerTransaction call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending MerchantBillerTransaction reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/merchantbillercoretransaction")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity MerchantBillerCoreTransaction(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource MerchantBillerCoreTransaction Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("MerchantBillerCoreTransaction");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing MerchantBillerCoreTransaction call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending MerchantBillerCoreTransaction reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/merchantretailtransaction")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity MerchantRetailTransaction(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource MerchantRetailTransaction Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("MerchantRetailTransaction");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing MerchantRetailTransaction call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending MerchantRetailTransaction reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/merchantretailcoretransaction")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity MerchantRetailCoreTransaction(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource MerchantRetailCoreTransaction Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("MerchantRetailCoreTransaction");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing MerchantRetailCoreTransaction call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending MerchantRetailCoreTransaction reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/confirmfraudotp")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity ConfirmFraudOtp(WalletCMSWsEntity wsobj) throws Exception {

		try {
			logger.info("WebResource ConfirmFraudOtp Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("ConfirmFraudOtp");
			//WebServiceUtil.PrintNayaPayMsg(wsobj,true);
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());

			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing ConfirmFraudOtp call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending ConfirmFraudOtp reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/confirmbankotp")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity ConfirmBankOtp(WalletCMSWsEntity wsobj) throws Exception {

		try {
			logger.info("WebResource ConfirmOtp Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("ConfirmOtp");
			//WebServiceUtil.PrintNayaPayMsg(wsobj,true);
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());

			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing confirmotp call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending confirmotp reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/fetchprovisionalwallet")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity FetchProvisionalWallet(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource FetchProvisionalWallet Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("FetchProvisionalWallet");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing FetchProvisionalWallet call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending FetchProvisionalWallet reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/fetchprovisionalwalletlist")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity FetchProvisionalWalletList(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource FetchProvisionalWalletList Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("FetchProvisionalWalletList");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing FetchProvisionalWalletList call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending FetchProvisionalWalletList reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/updateprovisionalwalletaddress")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity UpdateProvisionalWalletAddress(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource UpdateProvisionalWalletAddress Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("UpdateProvisionalWalletAddress");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing UpdateProvisionalWalletAddress call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending UpdateProvisionalWalletAddress reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/supportportalgetbasicinfo")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity SupportPortalGetBasicInfo(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource SupportPortalGetBasicInfo Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("SupportPortalGetBasicInfo");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing SupportPortalGetBasicInfo call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending SupportPortalGetBasicInfo reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/getconsumertransactions")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity GetConsumerTransactions(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource GetConsumerTransactions Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("GetConsumerTransactions");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing GetConsumerTransactions call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending GetConsumerTransactions reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/gettransactiondetails")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity GetTransactionDetails(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource GetTransactionDetails Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("GetTransactionDetails");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing GetTransactionDetails call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending GetTransactionDetails reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/updateuserkycaddress")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity UpdateUserKYCAddress(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource UpdateUserKYCAddress Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("UpdateUserKYCAddress");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing UpdateUserKYCAddress call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending UpdateUserKYCAddress reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/updatewalletsecondaryphonenumber")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity UpdateWalletSecondaryPhoneNumber(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource UpdateWalletSecondaryPhoneNumber Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("UpdateWalletSecondaryPhoneNumber");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing UpdateWalletSecondaryPhoneNumber call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending UpdateWalletSecondaryPhoneNumber reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/resetwalletpin")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity ResetWalletPin(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource ResetWalletPin Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("ResetWalletPin");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing ResetWalletPin call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending ResetWalletPin reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/adminblockwalletaccount") //enable-disbale wallet account
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity AdminBlockWalletAccount(WalletCMSWsEntity wsobj) throws Exception {
		try {
			logger.info("WebResource AdminBlockWalletAccount Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("AdminBlockWalletAccount");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing AdminBlockWalletAccount call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending AdminBlockWalletAccount reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/activatedebitcard")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity ActivateDebitCard(WalletCMSWsEntity wsobj) throws Exception {
		try {
			logger.info("WebResource ActivateDebitCard Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("ActivateDebitCard");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing ActivateDebitCard call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending ActivateDebitCard reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/supportportalactivatedebitcard")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity SupportPortalActivateDebitCard(WalletCMSWsEntity wsobj) throws Exception {
		try {
			logger.info("WebResource SupportPortalActivateDebitCard Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("SupportPortalActivateDebitCard");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing SupportPortalActivateDebitCard call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending SupportPortalActivateDebitCard reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/blockdebitcard")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity BlockDebitCard(WalletCMSWsEntity wsobj) throws Exception {
		try {
			logger.info("WebResource BlockDebitCard Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("BlockDebitCard");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing BlockDebitCard call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending BlockDebitCard reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/blockchannel")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity BlockChannel(WalletCMSWsEntity wsobj) throws Exception {
		try {
			logger.info("WebResource BlockChannel Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("BlockChannel");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing BlockChannel call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending BlockChannel reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	//m.rehman: for NayaPay, adding calls for Askari Bank Services <start>
	@Path("/customerinquiry")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	//add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity CustomerInquiry(WalletCMSWsEntity wsobj) {

		try {
			logger.info("WebResource CustomerInquiry Request Received");
			if (wsobj == null) {
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("CustomerInquiry");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//Execute Thread Here...!
			//ExecutorService executor = MessageManager.threadPool;
			////MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++, wsobj);
			////return ((Future<WalletCMSWsEntity>) executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();


		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception caught while executing CustomerInquiry call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending CustomerInquiry reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/cashdeposit")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	//add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity CashDeposit(WalletCMSWsEntity wsobj) throws Exception {

		try {
			logger.info("WebResource CashDeposit Request Received");
			if (wsobj == null) {
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}

			wsobj.setServicename("CashDeposit");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++, wsobj);
			//return ((Future<WalletCMSWsEntity>) executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception caught while executing CashDeposit call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending CashDeposit reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/chequeclearing")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	//add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity ChequeClearing(WalletCMSWsEntity wsobj) throws Exception {

		try {
			logger.info("WebResource ChequeClearing Request Received");
			if (wsobj == null) {
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}

			wsobj.setServicename("ChequeClearing");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++, wsobj);
			//return ((Future<WalletCMSWsEntity>) executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception caught while executing ChequeClearing call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending ChequeClearing reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/chequeft")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	//add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity ChequeFT(WalletCMSWsEntity wsobj) throws Exception {

		try {
			logger.info("WebResource ChequeFT Request Received");
			if (wsobj == null) {
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}

			wsobj.setServicename("ChequeFT");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++, wsobj);
			//return ((Future<WalletCMSWsEntity>) executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception caught while executing ChequeFT call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending ChequeFT reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/chequebounce")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	//add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity ChequeBounce(WalletCMSWsEntity wsobj) throws Exception {

		try {
			logger.info("WebResource ChequeBounce Request Received");
			if (wsobj == null) {
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("ChequeBounce");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//create thread here
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++, wsobj);
			//return ((Future<WalletCMSWsEntity>) executor.submit(process)).get();
			//MessageManager.threadPool.execute(process);
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception caught while executing ChequeBounce call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending ChequeBounce reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}
	//m.rehman: for NayaPay, adding calls for Askari Bank Services <end>

	//m.rehman: for NayaPay, adding calls for Onelink UBPS Bill Payment Topup Services <start>
	@Path("/onelinkbillinquiry")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	//add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity OneLinkBillInquiry(WalletCMSWsEntity wsobj) throws Exception {

		try {
			logger.info("WebResource OneLinkBillInquiry Request Received");
			if (wsobj == null) {
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("OneLinkBillInquiry");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//create thread here
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++, wsobj);
			//return ((Future<WalletCMSWsEntity>) executor.submit(process)).get();
			//MessageManager.threadPool.execute(process);
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception caught while executing OneLinkBillInquiry call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending OneLinkBillInquiry reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}


	@Path("/onelinkbillpayment")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	//add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity OneLinkBillPayment(WalletCMSWsEntity wsobj) throws Exception {

		try {
			logger.info("WebResource OneLinkBillPayment Request Received");
			if (wsobj == null) {
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("OneLinkBillPayment");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//create thread here
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++, wsobj);
			//return ((Future<WalletCMSWsEntity>) executor.submit(process)).get();
			//MessageManager.threadPool.execute(process);
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception caught while executing OneLinkBillPayment call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending OneLinkBillPayment reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}
	//m.rehman: for NayaPay, adding calls for Onelink UBPS Bill Payment Topup Services <end>

	//Raza adding for 1Link Socket Issuing start
	@Path("/balanceinquiry")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	//add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity BalanceInquiry(WalletCMSWsEntity wsobj) throws Exception {

		try {
			logger.info("WebResource BalanceInquiry Request Received");
			if (wsobj == null) {
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}

			wsobj.setServicename("BalanceInquiry");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++, wsobj);
			//return ((Future<WalletCMSWsEntity>) executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception caught while executing BalanceInquiry call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending BalanceInquiry reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/cashwithdrawal")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	//add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity CashWithdrawal(WalletCMSWsEntity wsobj) throws Exception {

		try {
			logger.info("WebResource CashWithDrawal Request Received");
			if (wsobj == null) {
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}

			wsobj.setServicename("CashWithDrawal");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++, wsobj);
			//return ((Future<WalletCMSWsEntity>) executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception caught while executing CashWithDrawal call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending CashWithDrawal reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/cashwithdrawalreversal")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	//add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity CashWithdrawalReversal(WalletCMSWsEntity wsobj) throws Exception {

		try {
			logger.info("WebResource CashWithDrawalReversal Request Received");
			if (wsobj == null) {
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}

			wsobj.setServicename("CashWithDrawalReversal");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++, wsobj);
			//return ((Future<WalletCMSWsEntity>) executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception caught while executing CashWithDrawalReversal call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending CashWithDrawalReversal reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/purchase")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	//add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity Purchase(WalletCMSWsEntity wsobj) throws Exception {

		try {
			logger.info("WebResource Purchase Request Received");
			if (wsobj == null) {
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}

			wsobj.setServicename("Purchase");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++, wsobj);
			//return ((Future<WalletCMSWsEntity>) executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception caught while executing Purchase call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending Purchase reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/purchasereversal")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	//add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity PurchaseReversal(WalletCMSWsEntity wsobj) throws Exception {

		try {
			logger.info("WebResource PurchaseReversal Request Received");
			if (wsobj == null) {
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}

			wsobj.setServicename("PurchaseReversal");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++, wsobj);
			//return ((Future<WalletCMSWsEntity>) executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception caught while executing PurchaseReversal call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending PurchaseReversal reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}
	//Raza adding for 1Link Socket Issuing end

	//m.rehman: for NayaPay, adding new call for document 2.0 <start>
	@Path("/merchantreversaltransaction")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	//add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity MerchantReversalTransaction(WalletCMSWsEntity wsobj) throws Exception {

		try {
			logger.info("WebResource MerchantReversalTransaction Request Received");
			if (wsobj == null) {
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}

			wsobj.setServicename("MerchantReversalTransaction");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++, wsobj);
			//return ((Future<WalletCMSWsEntity>) executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception caught while executing MerchantReversalTransaction call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending MerchantReversalTransaction reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/onelinkbillertransaction")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	//add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity OnelinkBillerTransaction(WalletCMSWsEntity wsobj) throws Exception {

		try {
			logger.info("WebResource OnelinkBillerTransaction Request Received");
			if (wsobj == null) {
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}

			wsobj.setServicename("OnelinkBillerTransaction");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++, wsobj);
			//return ((Future<WalletCMSWsEntity>) executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception caught while executing OnelinkBillerTransaction call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending OnelinkBillerTransaction reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/onelinkbillercoretransaction")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	//add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity OnelinkBillerCoreTransaction(WalletCMSWsEntity wsobj) throws Exception {

		try {
			logger.info("WebResource OnelinkBillerCoreTransaction Request Received");
			if (wsobj == null) {
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}

			wsobj.setServicename("OnelinkBillerCoreTransaction");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++, wsobj);
			//return ((Future<WalletCMSWsEntity>) executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception caught while executing OnelinkBillerCoreTransaction call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending OnelinkBillerCoreTransaction reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/envelopload")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity EnvelopLoad(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource EnvelopLoadWallet Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("EnvelopLoad");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing EnvelopLoad call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending EnvelopLoad reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/envelopunload")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity EnvelopUnload(WalletCMSWsEntity wsobj) throws Exception {

		try {
			logger.info("WebResource EnvelopUnload Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("EnvelopUnload");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing EnvelopUnload call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending EnvelopUnload reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/reverseenvelop")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity ReverseEnvelop(WalletCMSWsEntity wsobj) throws Exception {

		try {
			logger.info("WebResource ReverseEnvelop Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("ReverseEnvelop");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing ReverseEnvelop call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending ReverseEnvelop reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}


	@Path("/updateusersecretquestions")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity UpdateUserSecretQuestions(WalletCMSWsEntity wsobj) throws Exception {

		try {
			logger.info("WebResource UpdateUserSecretQuestions Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("UpdateUserSecretQuestions");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing UpdateUserSecretQuestions call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending UpdateUserSecretQuestions reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/adminportalgetuserwallet")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity AdminPortalGetUserWallet(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource AdminPortalGetUserWallet Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}

			wsobj.setServicename("AdminPortalGetUserWallet");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing AdminPortalGetUserWallet call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending AdminPortalGetUserWallet reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/adminportalgetuserdebitcard")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity AdminPortalGetUserdebitCard(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource AdminPortalGetUserdebitCard Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}

			wsobj.setServicename("AdminPortalGetUserdebitCard");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing AdminPortalGetUserdebitCard call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending AdminPortalGetUserdebitCard reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/adminportalgetuserlinkedaccountlist")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity AdminPortalGetUserLinkedAccountList(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource AdminPortalGetUserLinkedAccountList Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}

			wsobj.setServicename("AdminPortalGetUserLinkedAccountList");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing AdminPortalGetUserLinkedAccountList call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending AdminPortalGetUserLinkedAccountList reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/adminportalgetusertransactionlist")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity AdminPortalGetUserTransactionList(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource AdminPortalGetUserTransactionList Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}

			wsobj.setServicename("AdminPortalGetUserTransactionList");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing AdminPortalGetUserTransactionList call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending AdminPortalGetUserTransactionList reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/adminportalgettransactiondetail")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity AdminPortalGetTransactionDetails(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource AdminPortalGetTransactionDetail Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("AdminPortalGetTransactionDetail");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing AdminPortalGetTransactionDetail call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending AdminPortalGetTransactionDetail reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/adminportalblockwalletaccount") //enable-disbale wallet account
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity AdminPortalBlockWalletAccount(WalletCMSWsEntity wsobj) throws Exception {
		try {
			logger.info("WebResource AdminPortalBlockWalletAccount Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("AdminPortalBlockWalletAccount");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing AdminPortalBlockWalletAccount call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending AdminPortalBlockWalletAccount reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}
	//m.rehman: for NayaPay, adding new call for document 2.0 <end>

	@Path("/tempblockdebitcard")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity TempBlockDebitCard(WalletCMSWsEntity wsobj) throws Exception {
		try {
			logger.info("WebResource TempBlockDebitCard Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("TempBlockDebitCard");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing TempBlockDebitCard call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending TempBlockDebitCard reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/gettransactioncharge")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement) //Raza adding to get Transaction Fee/Charge
	public WalletCMSWsEntity GetTransactionCharge(WalletCMSWsEntity wsobj) throws Exception {
		try {
			logger.info("WebResource GetTransactionCharge Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("GetTransactionCharge");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing GetTransactionCharge call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending GetTransactionCharge reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/verifywalletbycnic")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement) //Raza adding to get Transaction Fee/Charge
	public WalletCMSWsEntity VerifyWalletByCNIC(WalletCMSWsEntity wsobj) throws Exception {
		try {
			logger.info("WebResource VerifyWalletByCNIC Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("VerifyWalletByCNIC");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing VerifyWalletByCNIC call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending VerifyWalletByCNIC reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/bioopsupgradewalletaccount")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement) //Raza adding to get Transaction Fee/Charge
	public WalletCMSWsEntity BioOpsUpgradeWalletAccount(WalletCMSWsEntity wsobj) throws Exception {
		try {
			logger.info("WebResource BioOpsUpgradeWalletAccount Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("BioOpsUpgradeWalletAccount");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing BioOpsUpgradeWalletAccount call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending BioOpsUpgradeWalletAccount reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/bioopsenablewalletaccount")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement) //Raza adding to get Transaction Fee/Charge
	public WalletCMSWsEntity BioOpsEnableWalletAccount(WalletCMSWsEntity wsobj) throws Exception {
		try {
			logger.info("WebResource BioOpsEnableWalletAccount Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("BioOpsEnableWalletAccount");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing BioOpsEnableWalletAccount call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending BioOpsUpgradeWalletAccount reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/verifywalletbycnicforcash")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement) //Raza adding to get Transaction Fee/Charge
	public WalletCMSWsEntity VerifyWalletByCNICforCash(WalletCMSWsEntity wsobj) throws Exception {
		try {
			logger.info("WebResource VerifyWalletByCNICforCash Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("VerifyWalletByCNICforCash");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing VerifyWalletByCNICforCash call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending VerifyWalletByCNICforCash reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/cnicbasedcashwithdrawal")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement) //Raza adding to get Transaction Fee/Charge
	public WalletCMSWsEntity CNICBasedCashWithdrawal(WalletCMSWsEntity wsobj) throws Exception {
		try {
			logger.info("WebResource CNICBasedCashWithdrawal Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("CNICBasedCashWithdrawal");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing CNICBasedCashWithdrawal call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending CNICBasedCashWithdrawal reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/cnicbasedcashwithdrawalreversal")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement) //Raza adding to get Transaction Fee/Charge
	public WalletCMSWsEntity CNICBasedCashWithdrawalReversal(WalletCMSWsEntity wsobj) throws Exception {
		try {
			logger.info("WebResource CNICBasedCashWithdrawalReversal Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("CNICBasedCashWithdrawalReversal");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing CNICBasedCashWithdrawalReversal call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending CNICBasedCashWithdrawalReversal reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/walletinquiryforreversal")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement) //Raza adding to get Transaction Fee/Charge
	public WalletCMSWsEntity WalletInquiryForReversal(WalletCMSWsEntity wsobj) throws Exception {
		try {
			logger.info("WebResource WalletInquiryForReversal Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("WalletInquiryForReversal");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing WalletInquiryForReversal call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending WalletInquiryForReversal reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/merchantcredittransaction")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement) //Raza adding to get Transaction Fee/Charge
	public WalletCMSWsEntity MerchantCreditTransaction(WalletCMSWsEntity wsobj) throws Exception {
		try {
			logger.info("WebResource MerchantCreditTransaction Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("MerchantCreditTransaction");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing MerchantCreditTransaction call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending MerchantCreditTransaction reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/merchantdebittransaction")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement) //Raza adding to get Transaction Fee/Charge
	public WalletCMSWsEntity MerchantDebitTransaction(WalletCMSWsEntity wsobj) throws Exception {
		try {
			logger.info("WebResource MerchantDebitTransaction Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("MerchantDebitTransaction");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing MerchantDebitTransaction call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending MerchantDebitTransaction reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/generatepinblock")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement) //Raza adding to get Transaction Fee/Charge
	public WalletCMSWsEntity GeneratePINBlock(WalletCMSWsEntity wsobj) throws Exception {
		try {
			logger.info("WebResource GeneratePINBlock Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("GeneratePINBlock");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing GeneratePINBlock call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending GeneratePINBlock reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}



	@Path("/reversaltransaction")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement) //Raza adding to get Transaction Fee/Charge
	public WalletCMSWsEntity ReverseTransaction(WalletCMSWsEntity wsobj) throws Exception {
		try {
			logger.info("WebResource ReverseTransaction Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("ReversalTransaction");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing ReverseTransaction call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending ReverseTransaction reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}


	@Path("/supportportalgetusertransaction")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity SupportPortalGetUserTransaction(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource SupportPortalGetUserTransaction Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("SupportPortalGetUserTransaction");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing SupportPortalGetUserTransaction call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending SupportPortalGetUserTransaction reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/getuserkycaddress")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity GetUserKYCAddress(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource GetUserKYCAddress Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("GetUserKYCAddress");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing GetUserKYCAddress call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending GetUserKYCAddress reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/getuserkycquestionlist")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity GetUserKYCQuestionList(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource GetUserKYCQuestionList Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("GetUserKYCQuestionList");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing GetUserKYCQuestionList call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending GetUserKYCQuestionList reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/verifyusersecretquestion")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity VerifyUserSecretQuestion(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource VerifyUserSecretQuestion Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("VerifyUserSecretQuestion");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing VerifyUserSecretQuestion call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending VerifyUserSecretQuestion reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/getusercnicname")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity GetUserCNICName(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource GetUserCNICName Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("GetUserCNICName");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing GetUserCNICName call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending GetUserCNICName reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/getwalletstate")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity GetWalletState(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource GetWalletState Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("GetWalletState");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing GetWalletState call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending GetWalletState reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/disabledebitcard")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity DisableDebitCard(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource DisableDebitCard Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("DisableDebitCard");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing DisableDebitCard call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending DisableDebitCard reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/getdebitcardpan")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity GetDebitCardPAN(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource GetDebitCardPAN Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("GetDebitCardPAN");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing GetDebitCardPAN call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending GetDebitCardPAN reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/createmerchantwallet")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity CreateMerchantWallet(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource CreateMerchantWallet Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("CreateMerchantWallet");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing CreateMerchantWallet call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending CreateMerchantWallet reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/markdisputedtransaction")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity MarkDisputedTransaction(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource MarkDisputedTransaction Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("MarkDisputedTransaction");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing MarkDisputedTransaction call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending MarkDisputedTransaction reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/adminportallockmerchantwallet")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity AdminPortalLockMerchantWallet(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource AdminPortalLockMerchantWallet Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("AdminPortalLockMerchantWallet");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing AdminPortalLockMerchantWallet call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending AdminPortalLockMerchantWallet reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/supportportalgetadvanceinfo")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity SupportPortalGetAdvanceInfo(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource SupportPortalGetAdvanceInfo Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("SupportPortalGetAdvanceInfo");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing SupportPortalGetAdvanceInfo call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending SupportPortalGetAdvanceInfo reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/supportportallockwallet")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity SupportPortalLockWallet(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource SupportPortalLockWallet Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("SupportPortalLockWallet");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing SupportPortalLockWallet call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending SupportPortalLockWallet reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/supportportalupdatelockstate")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity SupportPortalUpdateLockState(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource SupportPortalUpdateLockState Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("SupportPortalUpdateLockState");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing SupportPortalUpdateLockState call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending SupportPortalUpdateLockState reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/supportportalgetwallet")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity SupportPortalGetWallet(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource SupportPortalGetWallet Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("SupportPortalGetWallet");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing SupportPortalGetWallet call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending SupportPortalGetWallet reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/supportportalgetlinkedaccountlist")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity SupportPortalGetLinkedAccountList(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource SupportPortalGetLinkedAccountList Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("SupportPortalGetLinkedAccountList");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing SupportPortalGetLinkedAccountList call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending SupportPortalGetLinkedAccountList reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/supportportalmarkdisputedtransaction")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity SupportPortalMarkDisputedTransaction(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource SupportPortalMarkDisputedTransaction Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("SupportPortalMarkDisputedTransaction");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing SupportPortalMarkDisputedTransaction call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending SupportPortalMarkDisputedTransaction reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/createprepaidcardwallet")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity CreatePrepaidCardWallet(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource CreatePrepaidCardWallet Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("CreatePrepaidCardWallet");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing CreatePrepaidCardWallet call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending CreatePrepaidCardWallet reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/getmerchantwallet")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity GetMerchantWallet(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource GetMerchantWallet Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("GetMerchantWallet");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing GetMerchantWallet call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending GetMerchantWallet reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/getmerchantdebitcardwallet")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity GetMerchantDebitCardWallet(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource GetMerchantDebitCardWallet Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("GetMerchantDebitCardWallet");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing GetMerchantDebitCardWallet call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending GetMerchantDebitCardWallet reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/getmerchantdebitcard")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity GetMerchantDebitCard(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource GetMerchantDebitCard Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("GetMerchantDebitCard");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing GetMerchantDebitCard call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending GetMerchantDebitCard reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/requestmerchantdebitcard")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity RequestMerchantDebitCard(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource RequestMerchantDebitCard Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("RequestMerchantDebitCard");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing RequestMerchantDebitCard call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending RequestMerchantDebitCard reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/activatemerchantdebitcard")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity ActivateMerchantDebitCard(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource ActivateMerchantDebitCard Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("ActivateMerchantDebitCard");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing ActivateMerchantDebitCard call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending ActivateMerchantDebitCard reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/updatemerchantdebitcardpin")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity UpdateMerchantDebitCardPIN(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource UpdateMerchantDebitCardPIN Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("UpdateMerchantDebitCardPIN");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing UpdateMerchantDebitCardPIN call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending UpdateMerchantDebitCardPIN reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/enablemerchantdebitcard")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity EnableMerchantDebitCard(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource EnableMerchantDebitCard Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("EnableMerchantDebitCard");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing EnableMerchantDebitCard call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending EnableMerchantDebitCard reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/supportportalenablemerchantdebitcard")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity SupportPortalEnableMerchantDebitCard(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource SupportPortalEnableMerchantDebitCard Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("SupportPortalEnableMerchantDebitCard");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing SupportPortalEnableMerchantDebitCard call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending SupportPortalEnableMerchantDebitCard reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/supportportalblockmerchantdebitcard")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity SupportPortalBlockMerchantDebitCard(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource SupportPortalBlockMerchantDebitCard Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("SupportPortalBlockMerchantDebitCard");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing SupportPortalBlockMerchantDebitCard call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending SupportPortalBlockMerchantDebitCard reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/supportportalgetuserdebitcard")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity SupportPortalGetUserDebitCard(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource SupportPortalGetUserDebitCard Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("SupportPortalGetUserDebitCard");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing SupportPortalGetUserDebitCard call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending SupportPortalGetUserDebitCard reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/merchantrefundtransaction")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity MerchantRefundTransaction(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource MerchantRefundTransaction Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("MerchantRefundTransaction");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing MerchantRefundTransaction call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending MerchantRefundTransaction reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/merchantloaddebitcard")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity MerchantLoadDebitCard(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource MerchantLoadDebitCard Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("MerchantLoadDebitCard");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing MerchantLoadDebitCard call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending MerchantLoadDebitCard reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/merchantunloaddebitcard")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity MerchantUnloadDebitCard(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource MerchantUnloadDebitCard Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("MerchantUnloadDebitCard");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing MerchantUnloadDebitCard call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending MerchantUnloadDebitCard reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/merchantunloadwallet")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity MerchantUnloadWallet(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource MerchantUnloadWallet Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("MerchantUnloadWallet");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing MerchantUnloadWallet call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending MerchantUnloadWallet reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/getmerchantwallettransactioncharge")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity GetMerchantWalletTransactionCharge(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource GetMerchantWalletTransactionCharge Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("GetMerchantWalletTransactionCharge");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing GetMerchantWalletTransactionCharge call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending GetMerchantWalletTransactionCharge reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/getmerchantwallettransactionlist")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity GetMerchantWalletTransactionList(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource GetMerchantWalletTransactionList Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("GetMerchantWalletTransactionList");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing GetMerchantWalletTransactionList call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending GetMerchantWalletTransactionList reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/getmerchantwallettransactiondetail")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity GetMerchantWalletTransactionDetail(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource GetMerchantWalletTransactionDetail Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("GetMerchantWalletTransactionDetail");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing GetMerchantWalletTransactionDetail call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending GetMerchantWalletTransactionDetail reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/getmerchantdebitcardtransactionlist")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity GetMerchantDebitCardTransactionList(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource GetMerchantDebitCardTransactionList Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("GetMerchantDebitCardTransactionList");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing GetMerchantDebitCardTransactionList call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending GetMerchantDebitCardTransactionList reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/getmerchantdebitcardtransactiondetails")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity GetMerchantDebitCardTransactionDetails(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource GetMerchantDebitCardTransactionDetails Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("GetMerchantDebitCardTransactionDetails");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing GetMerchantDebitCardTransactionDetails call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending GetMerchantDebitCardTransactionDetails reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/getuserid")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity GetUserID(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource GetUserID Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("GetUserID");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing GetUserID call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending GetUserID reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/settlementloadmerchantwallet")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity SettlementLoadMerchantWallet(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource SettlementLoadMerchantWallet Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("SettlementLoadMerchantWallet");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing SettlementLoadMerchantWallet call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending SettlementLoadMerchantWallet reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/onelinktopupbillpayment")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity OnelinkTopupBillPayment(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource OnelinkTopupBillPayment Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("OnelinkTopupBillPayment");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing OnelinkTopupBillPayment call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending OnelinkTopupBillPayment reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/titlefetch")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity TitleFetch(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource TitleFetch Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("TitleFetch");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing TitleFetch call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending TitleFetch reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/ibftin")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity IBFTIn(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource IBFTIn Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("IBFTIn");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing IBFTIn call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending IBFTIn reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/fundmanagement")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity FundManagement(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource FundManagement Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("FundManagement");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing FundManagement call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending FundManagement reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}


	// Author: Asim Shahzad, Date : 25th Feb 2020, Desc : For getting Nayapay mobile application download counts from middleware

	@Path("/getappdownloadcount")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity GetAppDownloadCount(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource GetAppDownloadCount Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("GetAppDownloadCount");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//create thread here
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			//MessageManager.threadPool.execute(process);
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing GetAppDownloadCount call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending GetAppDownloadCount reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/getusercardtransactionlist")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity GetUserCardTransactionList(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource GetUserCardTransactionList Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("GetUserCardTransactionList");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//create thread here
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			//MessageManager.threadPool.execute(process);
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing GetUserCardTransactionList call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending GetUserCardTransactionList reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/reorderdebitcard")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity ReorderDebitCard(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource ReorderDebitCard Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("ReorderDebitCard");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//create thread here
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			//MessageManager.threadPool.execute(process);
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing ReorderDebitCard call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending ReorderDebitCard reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/merchantsettlementlogging")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity MerchantSettlementLogging(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource MerchantSettlementLogging Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("MerchantSettlementLogging");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//create thread here
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			//MessageManager.threadPool.execute(process);
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing MerchantSettlementLogging call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending MerchantSettlementLogging reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	//m.rehman: Euronet Integration
	@Path("/ecommerce")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity ECommerce(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource ECommerce Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("ECommerce");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//create thread here
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			//MessageManager.threadPool.execute(process);
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing ECommerce call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending ECommerce reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	//m.rehman: Euronet Integration
	@Path("/ecommercereversal")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity ECommerceReversal(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource ECommerceReversal Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("ECommerceReversal");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//create thread here
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			//MessageManager.threadPool.execute(process);
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing ECommerceReversal call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending ECommerceReversal reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	//m.rehman: Euronet Integration
	@Path("/vcasstepup")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity VCASStepup(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource VCASStepup Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("VCASStepup");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//create thread here
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			//MessageManager.threadPool.execute(process);
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing VCASStepup call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending VCASStepup reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	//m.rehman: Euronet Integration
	@Path("/qrmerchantpayment")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity QRMerchantPayment(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource QRMerchantPayment Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("QRMerchantPayment");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//create thread here
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			//MessageManager.threadPool.execute(process);
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing QRMerchantPayment call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending QRMerchantPayment reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	//m.rehman: Euronet Integration
	@Path("/qrmerchantpaymentreversal")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity QRMerchantPaymentReversal(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource QRMerchantPaymentReversal Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("QRMerchantPaymentReversal");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//create thread here
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			//MessageManager.threadPool.execute(process);
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing QRMerchantPaymentReversal call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending QRMerchantPaymentReversal reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	//m.rehman: Euronet Integration
	@Path("/qrmerchantpaymentrefund")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity QRMerchantPaymentRefund(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource QRMerchantPaymentRefund Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("QRMerchantPaymentRefund");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//create thread here
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			//MessageManager.threadPool.execute(process);
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing QRMerchantPaymentRefund call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending QRMerchantPaymentRefund reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/ibft")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity IBFT(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource IBFT Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("IBFT");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//create thread here
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			//MessageManager.threadPool.execute(process);
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing IBFT call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending IBFT reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/titlefetchinquiry")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity TitleFetchInquiry(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource TitleFetchInquiry Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("TitleFetchInquiry");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//create thread here
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			//MessageManager.threadPool.execute(process);
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing TitleFetchInquiry call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending TitleFetchInquiry reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	// Asim Shahzad, Date : 17th Sep 2020, Tracking ID : VC-NAP-202009101 / VP-NAP-202009104
	@Path("/loadwalletinquirywithoutmpin")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity LoadWalletInquiryWithoutMPIN(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource LoadWalletInquiryWithoutMPIN Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("LoadWalletInquiryWithoutMPIN");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing LoadWalletInquiryWithoutMPIN call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending LoadWalletInquiryWithoutMPIN reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}
	// =======================================================================================

	//m.rehman: 07-08-2020, Euronet Integration verification of MPIN for VCAS challenge
	@Path("/challengempin")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity ChallengeMPIN(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource ChallengeMPIN Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("ChallengeMPIN");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//create thread here
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			//MessageManager.threadPool.execute(process);
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing ChallengeMPIN call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending ChallengeMPIN reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	//m.rehman: Euronet Integration
	@Path("/moto")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity MOTO(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource MOTO Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("MOTO");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//create thread here
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			//MessageManager.threadPool.execute(process);
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing MOTO call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending MOTO reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	//m.rehman: Euronet Integration
	@Path("/motoreversal")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity MOTOReversal(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource MOTOReversal Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("MOTOReversal");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//create thread here
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			//MessageManager.threadPool.execute(process);
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing MOTOReversal call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending MOTOReversal reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	//m.rehman: Euronet Integration
	@Path("/accountverification")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity AccountVerification(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource AccountVerification Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("AccountVerification");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//create thread here
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			//MessageManager.threadPool.execute(process);
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing AccountVerification call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending AccountVerification reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	//m.rehman: Euronet Integration
	@Path("/preauthorization")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity PreAuthorization(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource PreAuthorization Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("PreAuthorization");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//create thread here
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			//MessageManager.threadPool.execute(process);
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			logger.error(e);
			logger.error("Exception caught while executing PreAuthorization call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending PreAuthorization reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	//m.rehman: Euronet Integration
	@Path("/preauthorizationreversal")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity PreAuthorizationReversal(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource PreAuthorizationReversal Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("PreAuthorizationReversal");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//create thread here
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			//MessageManager.threadPool.execute(process);
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			logger.error(e);
			logger.error("Exception caught while executing PreAuthorizationReversal call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending PreAuthorizationReversal reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	//m.rehman: Euronet Integration
	@Path("/preauthcompletion")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity PreAuthCompletion(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource PreAuthCompletion Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("PreAuthCompletion");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//create thread here
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			//MessageManager.threadPool.execute(process);
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			logger.error(e);
			logger.error("Exception caught while executing PreAuthCompletion call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending PreAuthCompletion reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	//m.rehman: Euronet Integration
	@Path("/refund")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity Refund(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource Refund Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("Refund");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//create thread here
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			//MessageManager.threadPool.execute(process);
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing Refund call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending Refund reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	//m.rehman: Euronet Integration
	@Path("/stip")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity STIP(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource STIP Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("STIP");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//create thread here
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			//MessageManager.threadPool.execute(process);
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing STIP call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending STIP reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	//m.rehman: Euronet Integration
	@Path("/getuseridfrompan")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity GetUserIDFromPAN(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource GetUserIDFromPAN Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("GetUserIDFromPAN");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//create thread here
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			//MessageManager.threadPool.execute(process);
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			logger.error(e);
			logger.error("Exception caught while executing GetUserIDFromPAN call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending GetUserIDFromPAN reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	//m.rehman: VP-NAP-202008211 / VC-NAP-202008211 - 17-09-2020 - Creation of Dispute transactions settlement feature (Phase I)
	@Path("/disputerefundtransaction")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity DisputeRefundTransaction(WalletCMSWsEntity wsobj) throws Exception {

		try {
			logger.info("WebResource DisputeRefundTransaction Request Received");
			if (wsobj == null) {
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("DisputeRefundTransaction");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//create thread here
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++, wsobj);
			//return ((Future<WalletCMSWsEntity>) executor.submit(process)).get();
			//MessageManager.threadPool.execute(process);
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception caught while executing DisputeRefundTransaction call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending DisputeRefundTransaction reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	// Asim Shahzad, Date : 13th Jan 2021, Tracking ID : VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 (Release # 1)
	@Path("/updatecardcontrols")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity UpdateCardControls(WalletCMSWsEntity wsobj) throws Exception {

		try {
			logger.info("WebResource UpdateCardControls Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("UpdateCardControls");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing UpdateCardControls call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending UpdateCardControls reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/supportportalupdatecardcontrols")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity SupportPortalUpdateCardControls(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource SupportPortalUpdateCardControls Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("SupportPortalUpdateCardControls");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing SupportPortalUpdateCardControls call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending SupportPortalUpdateCardControls reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/adminportalupdatecardcontrols")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity AdminPortalUpdateCardControls(WalletCMSWsEntity wsobj) throws Exception{

		try {
			logger.info("WebResource AdminPortalUpdateCardControls Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("AdminPortalUpdateCardControls");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing AdminPortalUpdateCardControls call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending AdminPortalUpdateCardControls reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}
	// ============================================================================================================================

	// Asim Shahzad, Date : 20th Jan 2021, Tracking ID : VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 (Release # 2)

	@Path("/getuservirtualcardcvvtwo")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity GetUserVirtualCardCvvTwo(WalletCMSWsEntity wsobj) throws Exception {

		try {
			logger.info("WebResource GetUserVirtualCardCvvTwo Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("GetUserVirtualCardCvvTwo");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing GetUserVirtualCardCvvTwo call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending GetUserVirtualCardCvvTwo reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	// ============================================================================================================================

	//m.rehman: 21-01-2020, VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 - VISA (Switch-Middleware Integration document v 4.7.6) - Release 4
	@Path("/updatecardlimits")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity UpdateCardLimits(WalletCMSWsEntity wsobj) throws Exception {
		try {
			logger.info("WebResource UpdateCardLimits Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("UpdateCardLimits");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++, wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Exception caught while executing UpdateCardLimits call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending UpdateCardLimits reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	//m.rehman: 05-03-2021, VP-NAP-202103041/ VC-NAP-202103041 - Merchant Transaction Listing Issue
	@Path("/getmerchanttransactionlist")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity GetMerchantTransactionList(WalletCMSWsEntity wsobj) throws Exception {
		try {
			logger.info("WebResource GetMerchantTransactionList Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("GetMerchantTransactionList");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++, wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			logger.error("Exception caught while executing GetMerchantTransactionList call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending GetMerchantTransactionList reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}

	@Path("/getmerchanttransaction")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity GetMerchantTransaction(WalletCMSWsEntity wsobj) throws Exception {
		try {
			logger.info("WebResource GetMerchantTransaction Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("GetMerchantTransaction");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++, wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			logger.error("Exception caught while executing GetMerchantTransaction call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending GetMerchantTransaction reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// Asim Shahzad, Date : 11th March 2021, Tracking ID : VP-NAP-202103111 / VC-NAP-202103111
	@Path("/closewallet")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public WalletCMSWsEntity CloseWallet(WalletCMSWsEntity wsobj) throws Exception {
		try {
			logger.info("WebResource CloseWallet Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("CloseWallet");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++, wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			logger.error("Exception caught while executing CloseWallet call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending CloseWallet reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}
	//m.rehman: 12-04-2021, VP-NAP-202103292 / VC-NAP-202103293 - Refund Module Part 2
	@Path("/opendebitcredit")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity OpenDebitCredit(WalletCMSWsEntity wsobj) throws Exception {
		try {
			logger.info("WebResource OpenDebitCredit Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("OpenDebitCredit");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++, wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			logger.error("Exception caught while executing OpenDebitCredit call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending OpenDebitCredit reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//m.rehman: 29-04-2021, VG-NAP-202104271 / VP-NAP-202104261 / VC-NAP-202104261 - VISA transaction charging update
	@Path("/getdebitcardcharge")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity GetDebitCardCharge(WalletCMSWsEntity nayapaymodel) throws Exception {
		try {
			logger.info("WebResource GetDebitCardCharge Request Received");
			if(nayapaymodel == null)
			{
				logger.error("No Data Received from request! replying..");
				nayapaymodel = new WalletCMSWsEntity();
				nayapaymodel.setRespcode(ISOResponseCodes.NP_PERMISSION_DENIED);
				return nayapaymodel;
			}
			nayapaymodel.setServicename("GetDebitCardCharge");
			//m.rehman: for validating incoming ip address
			nayapaymodel.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,nayapaymodel);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,nayapaymodel);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			logger.error("Exception caught while executing GetDebitCardCharge call");
			logger.error(WebServiceUtil.getStrException(e));
			nayapaymodel.setRespcode(ISOResponseCodes.NP_PERMISSION_DENIED);
			logger.error("Sending GetDebitCardCharge reply [" + nayapaymodel.getRespcode() + "]");
			return nayapaymodel;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// Asim Shahzad, Date : 10th Aug 2021, Tracking ID : VP-NAP-202108091 / VC-NAP-202108093/ VG-NAP-202108091
	@Path("/getuserwalletstatement")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public WalletCMSWsEntity GetUserWalletStatement(WalletCMSWsEntity nayapaymodel) throws Exception {
		try {
			logger.info("WebResource GetUserWalletStatement Request Received");
			if(nayapaymodel == null)
			{
				logger.error("No Data Received from request! replying..");
				nayapaymodel = new WalletCMSWsEntity();
				nayapaymodel.setRespcode(ISOResponseCodes.NP_PERMISSION_DENIED);
				return nayapaymodel;
			}
			nayapaymodel.setServicename("GetUserWalletStatement");
			//m.rehman: for validating incoming ip address
			nayapaymodel.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++,nayapaymodel);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,nayapaymodel);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			logger.error("Exception caught while executing GetUserWalletStatement call");
			logger.error(WebServiceUtil.getStrException(e));
			nayapaymodel.setRespcode(ISOResponseCodes.NP_PERMISSION_DENIED);
			logger.error("Sending GetUserWalletStatement reply [" + nayapaymodel.getRespcode() + "]");
			return nayapaymodel;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}
	// =======================================================================================================

	//Arsalan Akhter, Date: 23rd-Aug-2021, Tracking ID: VP-NAP-202108091 / VC-NAP-202108093/ VG-NAP-202108091
	@Path("/setwalletstatuslock")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public WalletCMSWsEntity SetWalletStatusLock(WalletCMSWsEntity wsobj) throws Exception {
		try {
			logger.info("WebResource SetWalletStatusLock Request Received");
			if(wsobj == null)
			{
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setServicename("SetWalletStatusLock");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			//ExecutorService executor = MessageManager.threadPool;
			//MainWSProcess process = new MainWSProcess(WalletCMSWSServer.id++, wsobj);
			//return ((Future<WalletCMSWsEntity>)executor.submit(process)).get();
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		}
		catch (Exception e)
		{
			logger.error("Exception caught while executing SetWalletStatusLock call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending SetWalletStatusLock reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		//m.rehman: 15-11-2021, Nayapay Optimization
		finally {
			HttpSession session = request.getSession(false); //Raza adding to Destroy servlet session immediately after reply
			if (session != null) {
				session.invalidate();
			}
			//executor.shutdownNow();
		}
	}
	//=======================================================================================================


	// Asim Shahzad, Date : 6th June 2023, Tracking ID : VP-NAP-202303091
	@Path("/onuscashwithdrawalinquiry")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	//add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity OnUsCashWithDrawalInquiry(WalletCMSWsEntity wsobj) throws Exception {

		try {
			logger.info("WebResource OnUsCashWithDrawalInquiry Request Received");
			if (wsobj == null) {
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}

			wsobj.setServicename("OnUsCashWithDrawalInquiry");
			//m.rehman: for validating incoming ip address
			wsobj.setIncomingip(request.getRemoteAddr());
			SimpleMainWSProcess process = new SimpleMainWSProcess(WalletCMSWSServer.id++,wsobj);
			return process.processTransaction();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception caught while executing OnUsCashWithDrawalInquiry call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending OnUsCashWithDrawalInquiry reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		finally {
			HttpSession session = request.getSession(false);
			if (session != null) {
				session.invalidate();
			}
		}
	}

	// ================================================================================

	//Added by Huzaifa VP-NAP-202405061 => link CW (OffUs) channel Limit Authentication Change
	@Path("/updateapistatus")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	//add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
	public WalletCMSWsEntity UpdateApiStatus(WalletCMSWsEntity wsobj) throws Exception {

		try {
			logger.info("WebResource UpdateApiStatus Request Received");
			if (wsobj == null) {
				logger.error("No Data Received from request! replying..");
				wsobj = new WalletCMSWsEntity();
				wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
				return wsobj;
			}
			wsobj.setIncomingip(request.getRemoteAddr());
			return WSOperation.ExecuteUpdateApiStatus(wsobj);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception caught while executing UpdateApiStatus call");
			logger.error(WebServiceUtil.getStrException(e));
			wsobj.setRespcode(ISOResponseCodes.NP_AUTHENTICATION_FAILED);
			logger.error("Sending UpdateApiStatus reply [" + wsobj.getRespcode() + "]");
			return wsobj;
		}
		finally {
			HttpSession session = request.getSession(false);
			if (session != null) {
				session.invalidate();
			}
		}
	}

	// ================================================================================
}