package vaulsys.protocols.ndc.base.NetworkToTerminal;

public abstract class NDCResponseMsg extends NDCNetworkToTerminalMsg {
      public String MAC;

	@Override
	public Boolean isRequest(){
		return false;
	}
	
    @Override
    public String toString() {
        return super.toString();
    }
}
