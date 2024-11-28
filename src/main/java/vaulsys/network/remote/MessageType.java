package vaulsys.network.remote;

public enum MessageType {
	AddChannel, StopChannel, StartChannel, RestartChannel, ChannelList, UpdateChannel,
	IssueShetabDocument, TerminalIssueDocument,
	UpdateCache,
	Response, Exception, ATMMessage, ChannelMessage, WalletTopup
	// Asim Shahzad, Date : 18th Jan 2021, Tracking ID : VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 (Release # 2)
	, HSMCommand
	// ==========================================================================================================
}
