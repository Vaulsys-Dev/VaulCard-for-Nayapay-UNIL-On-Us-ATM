package vaulsys.security.hsm.eracom;

import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;

public class ESMIoFilter implements IoFilter {

	@Override
	public void destroy() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void exceptionCaught(NextFilter arg0, IoSession arg1, Throwable arg2)
			throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void filterClose(NextFilter arg0, IoSession arg1) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void filterWrite(NextFilter arg0, IoSession arg1, WriteRequest arg2)
			throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void init() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void messageReceived(NextFilter arg0, IoSession arg1, Object arg2)
			throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void messageSent(NextFilter arg0, IoSession arg1, WriteRequest arg2)
			throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPostAdd(IoFilterChain arg0, String arg1, NextFilter arg2)
			throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPostRemove(IoFilterChain arg0, String arg1, NextFilter arg2)
			throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPreAdd(IoFilterChain arg0, String arg1, NextFilter arg2)
			throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPreRemove(IoFilterChain arg0, String arg1, NextFilter arg2)
			throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void sessionClosed(NextFilter arg0, IoSession arg1) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void sessionCreated(NextFilter arg0, IoSession arg1)
			throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void sessionIdle(NextFilter arg0, IoSession arg1, IdleStatus arg2)
			throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void sessionOpened(NextFilter arg0, IoSession arg1) throws Exception {
		// TODO Auto-generated method stub

	}

}
