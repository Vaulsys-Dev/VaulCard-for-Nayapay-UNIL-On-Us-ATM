
package vaulsys.webservice.nayapaybankservice;

import vaulsys.webservice.walletcardmgmtwebservice.entity.WalletCMSWsEntity;
import vaulsys.webservice.walletcardmgmtwebservice.model.Transaction;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the vaulsys.webservice.nayapaybankservice package.
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _LoadwalletResponse_QNAME = new QName("http://nayapaybankservice/", "loadwalletResponse");
    private final static QName _CreatewalletlevelzeroResponse_QNAME = new QName("http://nayapaybankservice/", "createwalletlevelzeroResponse");
    private final static QName _Debitcardrequest_QNAME = new QName("http://nayapaybankservice/", "debitcardrequest");
    private final static QName _Loadmerchant_QNAME = new QName("http://nayapaybankservice/", "loadmerchant");
    private final static QName _BalanceinquiryotpResponse_QNAME = new QName("http://nayapaybankservice/", "balanceinquiryotpResponse");
    private final static QName _Demographicsupdate_QNAME = new QName("http://nayapaybankservice/", "demographicsupdate");
    private final static QName _Exception_QNAME = new QName("http://nayapaybankservice/", "Exception");
    private final static QName _BalanceinquiryResponse_QNAME = new QName("http://nayapaybankservice/", "balanceinquiryResponse");
    private final static QName _SayhelloResponse_QNAME = new QName("http://nayapaybankservice/", "sayhelloResponse");
    private final static QName _Createwalletlevelone_QNAME = new QName("http://nayapaybankservice/", "createwalletlevelone");
    private final static QName _CreatewalletleveloneResponse_QNAME = new QName("http://nayapaybankservice/", "createwalletleveloneResponse");
    private final static QName _DebitcardrequestResponse_QNAME = new QName("http://nayapaybankservice/", "debitcardrequestResponse");
    private final static QName _Unloadmerchant_QNAME = new QName("http://nayapaybankservice/", "unloadmerchant");
    private final static QName _PingResponse_QNAME = new QName("http://nayapaybankservice/", "pingResponse");
    private final static QName _DemographicsupdateResponse_QNAME = new QName("http://nayapaybankservice/", "demographicsupdateResponse");
    private final static QName _Balanceinquiry_QNAME = new QName("http://nayapaybankservice/", "balanceinquiry");
    private final static QName _PeertopeerResponse_QNAME = new QName("http://nayapaybankservice/", "peertopeerResponse");
    private final static QName _Peertopeer_QNAME = new QName("http://nayapaybankservice/", "peertopeer");
    private final static QName _Ministatement_QNAME = new QName("http://nayapaybankservice/", "ministatement");
    private final static QName _Unloadenvelope_QNAME = new QName("http://nayapaybankservice/", "unloadenvelope");
    private final static QName _Loadenvelope_QNAME = new QName("http://nayapaybankservice/", "loadenvelope");
    private final static QName _Accountlinkotp_QNAME = new QName("http://nayapaybankservice/", "accountlinkotp");
    private final static QName _NayaPayWsEntity_QNAME = new QName("http://nayapaybankservice/", "nayaPayWsEntity");
    private final static QName _MinistatementotpResponse_QNAME = new QName("http://nayapaybankservice/", "ministatementotpResponse");
    private final static QName _Ping_QNAME = new QName("http://nayapaybankservice/", "ping");
    private final static QName _LoadenvelopeResponse_QNAME = new QName("http://nayapaybankservice/", "loadenvelopeResponse");
    private final static QName _Sayhello_QNAME = new QName("http://nayapaybankservice/", "sayhello");
    private final static QName _Unloadwallet_QNAME = new QName("http://nayapaybankservice/", "unloadwallet");
    private final static QName _AtmtransactionlogResponse_QNAME = new QName("http://nayapaybankservice/", "atmtransactionlogResponse");
    private final static QName _Createwalletlevelzero_QNAME = new QName("http://nayapaybankservice/", "createwalletlevelzero");
    private final static QName _UnloadwalletResponse_QNAME = new QName("http://nayapaybankservice/", "unloadwalletResponse");
    private final static QName _Ministatementotp_QNAME = new QName("http://nayapaybankservice/", "ministatementotp");
    private final static QName _AccountlinkotpResponse_QNAME = new QName("http://nayapaybankservice/", "accountlinkotpResponse");
    private final static QName _LoadmerchantResponse_QNAME = new QName("http://nayapaybankservice/", "loadmerchantResponse");
    private final static QName _Balanceinquiryotp_QNAME = new QName("http://nayapaybankservice/", "balanceinquiryotp");
    private final static QName _ChangepinResponse_QNAME = new QName("http://nayapaybankservice/", "changepinResponse");
    private final static QName _MinistatementResponse_QNAME = new QName("http://nayapaybankservice/", "ministatementResponse");
    private final static QName _PaymentResponse_QNAME = new QName("http://nayapaybankservice/", "paymentResponse");
    private final static QName _Confirmotp_QNAME = new QName("http://nayapaybankservice/", "confirmotp");
    private final static QName _Loadwallet_QNAME = new QName("http://nayapaybankservice/", "loadwallet");
    private final static QName _Atmtransactionlog_QNAME = new QName("http://nayapaybankservice/", "atmtransactionlog");
    private final static QName _ConfirmotpResponse_QNAME = new QName("http://nayapaybankservice/", "confirmotpResponse");
    private final static QName _UnloadenvelopeResponse_QNAME = new QName("http://nayapaybankservice/", "unloadenvelopeResponse");
    private final static QName _Changepin_QNAME = new QName("http://nayapaybankservice/", "changepin");
    private final static QName _UnloadmerchantResponse_QNAME = new QName("http://nayapaybankservice/", "unloadmerchantResponse");
    private final static QName _Payment_QNAME = new QName("http://nayapaybankservice/", "payment");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: vaulsys.webservice.nayapaybankservice
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Sayhello }
     * 
     */
    public Sayhello createSayhello() {
        return new Sayhello();
    }

    /**
     * Create an instance of {@link Unloadwallet }
     * 
     */
    public Unloadwallet createUnloadwallet() {
        return new Unloadwallet();
    }

    /**
     * Create an instance of {@link LoadenvelopeResponse }
     * 
     */
    public LoadenvelopeResponse createLoadenvelopeResponse() {
        return new LoadenvelopeResponse();
    }

    /**
     * Create an instance of {@link AtmtransactionlogResponse }
     * 
     */
    public AtmtransactionlogResponse createAtmtransactionlogResponse() {
        return new AtmtransactionlogResponse();
    }

    /**
     * Create an instance of {@link Accountlinkotp }
     * 
     */
    public Accountlinkotp createAccountlinkotp() {
        return new Accountlinkotp();
    }

    /**
     * Create an instance of {@link WalletCMSWsEntity }
     * 
     */
    public WalletCMSWsEntity createNayaPayWsEntity() {
        return new WalletCMSWsEntity();
    }

    /**
     * Create an instance of {@link Ping }
     * 
     */
    public Ping createPing() {
        return new Ping();
    }

    /**
     * Create an instance of {@link MinistatementotpResponse }
     * 
     */
    public MinistatementotpResponse createMinistatementotpResponse() {
        return new MinistatementotpResponse();
    }

    /**
     * Create an instance of {@link UnloadwalletResponse }
     * 
     */
    public UnloadwalletResponse createUnloadwalletResponse() {
        return new UnloadwalletResponse();
    }

    /**
     * Create an instance of {@link Createwalletlevelzero }
     * 
     */
    public Createwalletlevelzero createCreatewalletlevelzero() {
        return new Createwalletlevelzero();
    }

    /**
     * Create an instance of {@link Balanceinquiryotp }
     * 
     */
    public Balanceinquiryotp createBalanceinquiryotp() {
        return new Balanceinquiryotp();
    }

    /**
     * Create an instance of {@link ChangepinResponse }
     * 
     */
    public ChangepinResponse createChangepinResponse() {
        return new ChangepinResponse();
    }

    /**
     * Create an instance of {@link MinistatementResponse }
     * 
     */
    public MinistatementResponse createMinistatementResponse() {
        return new MinistatementResponse();
    }

    /**
     * Create an instance of {@link PaymentResponse }
     * 
     */
    public PaymentResponse createPaymentResponse() {
        return new PaymentResponse();
    }

    /**
     * Create an instance of {@link AccountlinkotpResponse }
     * 
     */
    public AccountlinkotpResponse createAccountlinkotpResponse() {
        return new AccountlinkotpResponse();
    }

    /**
     * Create an instance of {@link LoadmerchantResponse }
     * 
     */
    public LoadmerchantResponse createLoadmerchantResponse() {
        return new LoadmerchantResponse();
    }

    /**
     * Create an instance of {@link Ministatementotp }
     * 
     */
    public Ministatementotp createMinistatementotp() {
        return new Ministatementotp();
    }

    /**
     * Create an instance of {@link Changepin }
     * 
     */
    public Changepin createChangepin() {
        return new Changepin();
    }

    /**
     * Create an instance of {@link UnloadmerchantResponse }
     * 
     */
    public UnloadmerchantResponse createUnloadmerchantResponse() {
        return new UnloadmerchantResponse();
    }

    /**
     * Create an instance of {@link Payment }
     * 
     */
    public Payment createPayment() {
        return new Payment();
    }

    /**
     * Create an instance of {@link Loadwallet }
     * 
     */
    public Loadwallet createLoadwallet() {
        return new Loadwallet();
    }

    /**
     * Create an instance of {@link Confirmotp }
     * 
     */
    public Confirmotp createConfirmotp() {
        return new Confirmotp();
    }

    /**
     * Create an instance of {@link Atmtransactionlog }
     * 
     */
    public Atmtransactionlog createAtmtransactionlog() {
        return new Atmtransactionlog();
    }

    /**
     * Create an instance of {@link ConfirmotpResponse }
     * 
     */
    public ConfirmotpResponse createConfirmotpResponse() {
        return new ConfirmotpResponse();
    }

    /**
     * Create an instance of {@link UnloadenvelopeResponse }
     * 
     */
    public UnloadenvelopeResponse createUnloadenvelopeResponse() {
        return new UnloadenvelopeResponse();
    }

    /**
     * Create an instance of {@link Debitcardrequest }
     * 
     */
    public Debitcardrequest createDebitcardrequest() {
        return new Debitcardrequest();
    }

    /**
     * Create an instance of {@link Loadmerchant }
     * 
     */
    public Loadmerchant createLoadmerchant() {
        return new Loadmerchant();
    }

    /**
     * Create an instance of {@link LoadwalletResponse }
     * 
     */
    public LoadwalletResponse createLoadwalletResponse() {
        return new LoadwalletResponse();
    }

    /**
     * Create an instance of {@link CreatewalletlevelzeroResponse }
     * 
     */
    public CreatewalletlevelzeroResponse createCreatewalletlevelzeroResponse() {
        return new CreatewalletlevelzeroResponse();
    }

    /**
     * Create an instance of {@link BalanceinquiryResponse }
     * 
     */
    public BalanceinquiryResponse createBalanceinquiryResponse() {
        return new BalanceinquiryResponse();
    }

    /**
     * Create an instance of {@link SayhelloResponse }
     * 
     */
    public SayhelloResponse createSayhelloResponse() {
        return new SayhelloResponse();
    }

    /**
     * Create an instance of {@link BalanceinquiryotpResponse }
     * 
     */
    public BalanceinquiryotpResponse createBalanceinquiryotpResponse() {
        return new BalanceinquiryotpResponse();
    }

    /**
     * Create an instance of {@link Demographicsupdate }
     * 
     */
    public Demographicsupdate createDemographicsupdate() {
        return new Demographicsupdate();
    }

    /**
     * Create an instance of {@link Exception }
     * 
     */
    public Exception createException() {
        return new Exception();
    }

    /**
     * Create an instance of {@link Unloadmerchant }
     * 
     */
    public Unloadmerchant createUnloadmerchant() {
        return new Unloadmerchant();
    }

    /**
     * Create an instance of {@link Createwalletlevelone }
     * 
     */
    public Createwalletlevelone createCreatewalletlevelone() {
        return new Createwalletlevelone();
    }

    /**
     * Create an instance of {@link DebitcardrequestResponse }
     * 
     */
    public DebitcardrequestResponse createDebitcardrequestResponse() {
        return new DebitcardrequestResponse();
    }

    /**
     * Create an instance of {@link CreatewalletleveloneResponse }
     * 
     */
    public CreatewalletleveloneResponse createCreatewalletleveloneResponse() {
        return new CreatewalletleveloneResponse();
    }

    /**
     * Create an instance of {@link Ministatement }
     * 
     */
    public Ministatement createMinistatement() {
        return new Ministatement();
    }

    /**
     * Create an instance of {@link Unloadenvelope }
     * 
     */
    public Unloadenvelope createUnloadenvelope() {
        return new Unloadenvelope();
    }

    /**
     * Create an instance of {@link Loadenvelope }
     * 
     */
    public Loadenvelope createLoadenvelope() {
        return new Loadenvelope();
    }

    /**
     * Create an instance of {@link DemographicsupdateResponse }
     * 
     */
    public DemographicsupdateResponse createDemographicsupdateResponse() {
        return new DemographicsupdateResponse();
    }

    /**
     * Create an instance of {@link PingResponse }
     * 
     */
    public PingResponse createPingResponse() {
        return new PingResponse();
    }

    /**
     * Create an instance of {@link Peertopeer }
     * 
     */
    public Peertopeer createPeertopeer() {
        return new Peertopeer();
    }

    /**
     * Create an instance of {@link Balanceinquiry }
     * 
     */
    public Balanceinquiry createBalanceinquiry() {
        return new Balanceinquiry();
    }

    /**
     * Create an instance of {@link PeertopeerResponse }
     * 
     */
    public PeertopeerResponse createPeertopeerResponse() {
        return new PeertopeerResponse();
    }

    /**
     * Create an instance of {@link Transaction }
     * 
     */
    public Transaction createTransaction() {
        return new Transaction();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LoadwalletResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "loadwalletResponse")
    public JAXBElement<LoadwalletResponse> createLoadwalletResponse(LoadwalletResponse value) {
        return new JAXBElement<LoadwalletResponse>(_LoadwalletResponse_QNAME, LoadwalletResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreatewalletlevelzeroResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "createwalletlevelzeroResponse")
    public JAXBElement<CreatewalletlevelzeroResponse> createCreatewalletlevelzeroResponse(CreatewalletlevelzeroResponse value) {
        return new JAXBElement<CreatewalletlevelzeroResponse>(_CreatewalletlevelzeroResponse_QNAME, CreatewalletlevelzeroResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Debitcardrequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "debitcardrequest")
    public JAXBElement<Debitcardrequest> createDebitcardrequest(Debitcardrequest value) {
        return new JAXBElement<Debitcardrequest>(_Debitcardrequest_QNAME, Debitcardrequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Loadmerchant }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "loadmerchant")
    public JAXBElement<Loadmerchant> createLoadmerchant(Loadmerchant value) {
        return new JAXBElement<Loadmerchant>(_Loadmerchant_QNAME, Loadmerchant.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BalanceinquiryotpResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "balanceinquiryotpResponse")
    public JAXBElement<BalanceinquiryotpResponse> createBalanceinquiryotpResponse(BalanceinquiryotpResponse value) {
        return new JAXBElement<BalanceinquiryotpResponse>(_BalanceinquiryotpResponse_QNAME, BalanceinquiryotpResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Demographicsupdate }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "demographicsupdate")
    public JAXBElement<Demographicsupdate> createDemographicsupdate(Demographicsupdate value) {
        return new JAXBElement<Demographicsupdate>(_Demographicsupdate_QNAME, Demographicsupdate.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Exception }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "Exception")
    public JAXBElement<Exception> createException(Exception value) {
        return new JAXBElement<Exception>(_Exception_QNAME, Exception.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BalanceinquiryResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "balanceinquiryResponse")
    public JAXBElement<BalanceinquiryResponse> createBalanceinquiryResponse(BalanceinquiryResponse value) {
        return new JAXBElement<BalanceinquiryResponse>(_BalanceinquiryResponse_QNAME, BalanceinquiryResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SayhelloResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "sayhelloResponse")
    public JAXBElement<SayhelloResponse> createSayhelloResponse(SayhelloResponse value) {
        return new JAXBElement<SayhelloResponse>(_SayhelloResponse_QNAME, SayhelloResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Createwalletlevelone }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "createwalletlevelone")
    public JAXBElement<Createwalletlevelone> createCreatewalletlevelone(Createwalletlevelone value) {
        return new JAXBElement<Createwalletlevelone>(_Createwalletlevelone_QNAME, Createwalletlevelone.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreatewalletleveloneResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "createwalletleveloneResponse")
    public JAXBElement<CreatewalletleveloneResponse> createCreatewalletleveloneResponse(CreatewalletleveloneResponse value) {
        return new JAXBElement<CreatewalletleveloneResponse>(_CreatewalletleveloneResponse_QNAME, CreatewalletleveloneResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DebitcardrequestResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "debitcardrequestResponse")
    public JAXBElement<DebitcardrequestResponse> createDebitcardrequestResponse(DebitcardrequestResponse value) {
        return new JAXBElement<DebitcardrequestResponse>(_DebitcardrequestResponse_QNAME, DebitcardrequestResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Unloadmerchant }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "unloadmerchant")
    public JAXBElement<Unloadmerchant> createUnloadmerchant(Unloadmerchant value) {
        return new JAXBElement<Unloadmerchant>(_Unloadmerchant_QNAME, Unloadmerchant.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PingResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "pingResponse")
    public JAXBElement<PingResponse> createPingResponse(PingResponse value) {
        return new JAXBElement<PingResponse>(_PingResponse_QNAME, PingResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DemographicsupdateResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "demographicsupdateResponse")
    public JAXBElement<DemographicsupdateResponse> createDemographicsupdateResponse(DemographicsupdateResponse value) {
        return new JAXBElement<DemographicsupdateResponse>(_DemographicsupdateResponse_QNAME, DemographicsupdateResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Balanceinquiry }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "balanceinquiry")
    public JAXBElement<Balanceinquiry> createBalanceinquiry(Balanceinquiry value) {
        return new JAXBElement<Balanceinquiry>(_Balanceinquiry_QNAME, Balanceinquiry.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PeertopeerResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "peertopeerResponse")
    public JAXBElement<PeertopeerResponse> createPeertopeerResponse(PeertopeerResponse value) {
        return new JAXBElement<PeertopeerResponse>(_PeertopeerResponse_QNAME, PeertopeerResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Peertopeer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "peertopeer")
    public JAXBElement<Peertopeer> createPeertopeer(Peertopeer value) {
        return new JAXBElement<Peertopeer>(_Peertopeer_QNAME, Peertopeer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Ministatement }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "ministatement")
    public JAXBElement<Ministatement> createMinistatement(Ministatement value) {
        return new JAXBElement<Ministatement>(_Ministatement_QNAME, Ministatement.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Unloadenvelope }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "unloadenvelope")
    public JAXBElement<Unloadenvelope> createUnloadenvelope(Unloadenvelope value) {
        return new JAXBElement<Unloadenvelope>(_Unloadenvelope_QNAME, Unloadenvelope.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Loadenvelope }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "loadenvelope")
    public JAXBElement<Loadenvelope> createLoadenvelope(Loadenvelope value) {
        return new JAXBElement<Loadenvelope>(_Loadenvelope_QNAME, Loadenvelope.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Accountlinkotp }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "accountlinkotp")
    public JAXBElement<Accountlinkotp> createAccountlinkotp(Accountlinkotp value) {
        return new JAXBElement<Accountlinkotp>(_Accountlinkotp_QNAME, Accountlinkotp.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link WalletCMSWsEntity }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "nayaPayWsEntity")
    public JAXBElement<WalletCMSWsEntity> createNayaPayWsEntity(WalletCMSWsEntity value) {
        return new JAXBElement<WalletCMSWsEntity>(_NayaPayWsEntity_QNAME, WalletCMSWsEntity.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MinistatementotpResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "ministatementotpResponse")
    public JAXBElement<MinistatementotpResponse> createMinistatementotpResponse(MinistatementotpResponse value) {
        return new JAXBElement<MinistatementotpResponse>(_MinistatementotpResponse_QNAME, MinistatementotpResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Ping }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "ping")
    public JAXBElement<Ping> createPing(Ping value) {
        return new JAXBElement<Ping>(_Ping_QNAME, Ping.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LoadenvelopeResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "loadenvelopeResponse")
    public JAXBElement<LoadenvelopeResponse> createLoadenvelopeResponse(LoadenvelopeResponse value) {
        return new JAXBElement<LoadenvelopeResponse>(_LoadenvelopeResponse_QNAME, LoadenvelopeResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Sayhello }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "sayhello")
    public JAXBElement<Sayhello> createSayhello(Sayhello value) {
        return new JAXBElement<Sayhello>(_Sayhello_QNAME, Sayhello.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Unloadwallet }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "unloadwallet")
    public JAXBElement<Unloadwallet> createUnloadwallet(Unloadwallet value) {
        return new JAXBElement<Unloadwallet>(_Unloadwallet_QNAME, Unloadwallet.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AtmtransactionlogResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "atmtransactionlogResponse")
    public JAXBElement<AtmtransactionlogResponse> createAtmtransactionlogResponse(AtmtransactionlogResponse value) {
        return new JAXBElement<AtmtransactionlogResponse>(_AtmtransactionlogResponse_QNAME, AtmtransactionlogResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Createwalletlevelzero }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "createwalletlevelzero")
    public JAXBElement<Createwalletlevelzero> createCreatewalletlevelzero(Createwalletlevelzero value) {
        return new JAXBElement<Createwalletlevelzero>(_Createwalletlevelzero_QNAME, Createwalletlevelzero.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UnloadwalletResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "unloadwalletResponse")
    public JAXBElement<UnloadwalletResponse> createUnloadwalletResponse(UnloadwalletResponse value) {
        return new JAXBElement<UnloadwalletResponse>(_UnloadwalletResponse_QNAME, UnloadwalletResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Ministatementotp }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "ministatementotp")
    public JAXBElement<Ministatementotp> createMinistatementotp(Ministatementotp value) {
        return new JAXBElement<Ministatementotp>(_Ministatementotp_QNAME, Ministatementotp.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AccountlinkotpResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "accountlinkotpResponse")
    public JAXBElement<AccountlinkotpResponse> createAccountlinkotpResponse(AccountlinkotpResponse value) {
        return new JAXBElement<AccountlinkotpResponse>(_AccountlinkotpResponse_QNAME, AccountlinkotpResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LoadmerchantResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "loadmerchantResponse")
    public JAXBElement<LoadmerchantResponse> createLoadmerchantResponse(LoadmerchantResponse value) {
        return new JAXBElement<LoadmerchantResponse>(_LoadmerchantResponse_QNAME, LoadmerchantResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Balanceinquiryotp }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "balanceinquiryotp")
    public JAXBElement<Balanceinquiryotp> createBalanceinquiryotp(Balanceinquiryotp value) {
        return new JAXBElement<Balanceinquiryotp>(_Balanceinquiryotp_QNAME, Balanceinquiryotp.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ChangepinResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "changepinResponse")
    public JAXBElement<ChangepinResponse> createChangepinResponse(ChangepinResponse value) {
        return new JAXBElement<ChangepinResponse>(_ChangepinResponse_QNAME, ChangepinResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MinistatementResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "ministatementResponse")
    public JAXBElement<MinistatementResponse> createMinistatementResponse(MinistatementResponse value) {
        return new JAXBElement<MinistatementResponse>(_MinistatementResponse_QNAME, MinistatementResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PaymentResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "paymentResponse")
    public JAXBElement<PaymentResponse> createPaymentResponse(PaymentResponse value) {
        return new JAXBElement<PaymentResponse>(_PaymentResponse_QNAME, PaymentResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Confirmotp }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "confirmotp")
    public JAXBElement<Confirmotp> createConfirmotp(Confirmotp value) {
        return new JAXBElement<Confirmotp>(_Confirmotp_QNAME, Confirmotp.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Loadwallet }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "loadwallet")
    public JAXBElement<Loadwallet> createLoadwallet(Loadwallet value) {
        return new JAXBElement<Loadwallet>(_Loadwallet_QNAME, Loadwallet.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Atmtransactionlog }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "atmtransactionlog")
    public JAXBElement<Atmtransactionlog> createAtmtransactionlog(Atmtransactionlog value) {
        return new JAXBElement<Atmtransactionlog>(_Atmtransactionlog_QNAME, Atmtransactionlog.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ConfirmotpResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "confirmotpResponse")
    public JAXBElement<ConfirmotpResponse> createConfirmotpResponse(ConfirmotpResponse value) {
        return new JAXBElement<ConfirmotpResponse>(_ConfirmotpResponse_QNAME, ConfirmotpResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UnloadenvelopeResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "unloadenvelopeResponse")
    public JAXBElement<UnloadenvelopeResponse> createUnloadenvelopeResponse(UnloadenvelopeResponse value) {
        return new JAXBElement<UnloadenvelopeResponse>(_UnloadenvelopeResponse_QNAME, UnloadenvelopeResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Changepin }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "changepin")
    public JAXBElement<Changepin> createChangepin(Changepin value) {
        return new JAXBElement<Changepin>(_Changepin_QNAME, Changepin.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UnloadmerchantResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "unloadmerchantResponse")
    public JAXBElement<UnloadmerchantResponse> createUnloadmerchantResponse(UnloadmerchantResponse value) {
        return new JAXBElement<UnloadmerchantResponse>(_UnloadmerchantResponse_QNAME, UnloadmerchantResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Payment }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nayapaybankservice/", name = "payment")
    public JAXBElement<Payment> createPayment(Payment value) {
        return new JAXBElement<Payment>(_Payment_QNAME, Payment.class, null, value);
    }

}
