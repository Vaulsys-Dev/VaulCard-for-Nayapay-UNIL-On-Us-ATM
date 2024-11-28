package vaulsys.scheduler.job;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;

import com.ghasemkiani.util.icu.PersianDateFormat;

import vaulsys.calendar.DateTime;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.scheduler.MCIVirtualVosoliJobInfo;
//import vaulsys.webservices.mcivirtualvosoli.common.VirtualVosoliRqParameters;

public class MCIVirtualVosoliThread  implements Runnable {
	private static final Logger logger = Logger.getLogger(MCIVirtualVosoliThread.class);
	
	List<MCIVirtualVosoliJobInfo> jobInfos;
	String branch;
	String bankId;
	Socket socket;
    Semaphore semaphore;

	public MCIVirtualVosoliThread(List<MCIVirtualVosoliJobInfo> jobInfos, String branch, String bankId, Semaphore semaphore){
		
		this.jobInfos = jobInfos;
		this.branch = branch;
		this.bankId = bankId;
        this.semaphore = semaphore;
	}
	
	@Override
	public void run() {
		ObjectOutputStream oos;
		
        ObjectInputStream ois;
        

        
		try {
			for(MCIVirtualVosoliJobInfo jobInfo : jobInfos){
				



				//VirtualVosoliRqParameters params = jobInfo.getVirtualVosoliRqParameters();

				socket = MCIVirtualVosoliJob.getSocket();
				
				oos = new ObjectOutputStream(socket.getOutputStream());
				
	            //oos.writeObject(params);
	            
	            //logger.debug("sent to mciVirtualVosoli: " + MCIVirtualVosoliJob.getRqString(params));
	            
	            
	            //response
	            ois = new ObjectInputStream(socket.getInputStream());
	            
	            String[] result = (String[]) ois.readObject();

                GeneralDao.Instance.beginTransaction();

	            MCIVirtualVosoliJob.parseResponse(result, jobInfo);
	            
	            GeneralDao.Instance.endTransaction();

			}
	        
		} catch (IOException e) {
            logger.error(e, e);
		} catch (ClassNotFoundException e) {
            logger.error(e, e);
		} catch(Exception e){
            logger.error(e, e);
		} finally {
            logger.info("mciVirtualVosoli release semaphore");

            semaphore.release();
        }

	}

}
