package vaulsys.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;
import jcifs.util.transport.TransportException;


public class SMBFileTransferUtil {
	
	public enum ResultStatus {
		Ok, Failed, Timeout, NotFound, Security
	}

	private static final Logger logger = Logger.getLogger(SMBFileTransferUtil.class);
	private static final String username , password ;

	static {
		username = ConfigUtil.getProperty(ConfigUtil.SMB_AUTH_USERNAME);
		password = ConfigUtil.getDecProperty(ConfigUtil.SMB_AUTH_PASSWORD);
	}
	
	
	public static ResultStatus upload(String srcPath, String ip, String destPath) {
		ResultStatus status = ResultStatus.Failed;
		FileInputStream in = null;
		SmbFileOutputStream out = null;
		try {
			jcifs.Config.setProperty("jcifs.netbios.wins", ip);
			if (Util.hasText(username) && Util.hasText(password)) {
				jcifs.Config.setProperty("jcifs.smb.client.username", username);
				jcifs.Config.setProperty("jcifs.smb.client.password", password);
			}
			String url = "smb://" + ip + "/" + destPath;
			in = new FileInputStream(srcPath);
			out = new SmbFileOutputStream(new SmbFile(url));
			byte[] b = new byte[8192];
			int n;
			while ((n = in.read(b)) > 0)
				out.write(b, 0, n);
			status = ResultStatus.Ok;

		} catch (TransportException e) {
			logger.error("Error in Uploading File from " + srcPath + " to " + ip, e);
			if (e.getMessage().contains("Connection timeout"))
				status = ResultStatus.Timeout;
		} catch (SmbAuthException e) {
			logger.error("Error in Uploading File from " + srcPath + " to " + ip, e);
			status = ResultStatus.Security;
		} catch (SmbException e) {
			logger.error("Error in Uploading File from " + srcPath + " to " + ip, e);
			if (e.getMessage().contains("network name cannot be found"))
				status = ResultStatus.NotFound;
		} catch (Exception e) {
			logger.error("Error in Uploading File from " + srcPath + " to " + ip, e);
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
				logger.error("Closing input stream", e);
			}
			try {
				if (out != null)
					out.close();
			} catch (IOException e) {
				logger.error("Closing smb output stream", e);
			}
		}
		return status;
	}
	
	public static ResultStatus upload(byte[] content, String ip, String destPath) throws IOException {
		ResultStatus status = ResultStatus.Failed;
		SmbFileOutputStream out = null;
		try {
			jcifs.Config.setProperty("jcifs.netbios.wins", ip);
			if (Util.hasText(username) && Util.hasText(password)) {
				jcifs.Config.setProperty("jcifs.smb.client.username", username);
				jcifs.Config.setProperty("jcifs.smb.client.password", password);
			}
			String url = "smb://" + ip + "/" + destPath;
			out = new SmbFileOutputStream(new SmbFile(url));
			out.write(content);
			status = ResultStatus.Ok;

		} catch (MalformedURLException e) {
			logger.error("Error in Uploading Content from to " + ip, e);
			if (e.getMessage().contains("Connection timeout"))
				status = ResultStatus.Timeout;
		} catch (UnknownHostException e) {
			logger.error("Error in Uploading File from to " + ip, e);
			status = ResultStatus.Security;
		} catch (SmbException e) {
			logger.error(e);
			if (e.getMessage().contains("network name cannot be found"))
				status = ResultStatus.NotFound;
		}
		catch (Exception e) {
			logger.error("Error in Uploading File from to " + ip, e);
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (IOException e) {
				logger.error("Closing smb output stream", e);
			}
		}
		return status;
	}

}
