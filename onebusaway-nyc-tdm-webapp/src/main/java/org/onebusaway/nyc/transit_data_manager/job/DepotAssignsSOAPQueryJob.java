/**
 * Copyright (C) 2011 Metropolitan Transportation Authority
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onebusaway.nyc.transit_data_manager.job;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.soap.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;

import org.onebusaway.nyc.util.configuration.ConfigurationService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.remoting.RemoteConnectFailureException;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * 	Quartz job to invoke Depot Assign web service every hour.
 * 
 */
public class DepotAssignsSOAPQueryJob extends QuartzJobBean {

	
	private ConfigurationService _configurationService;
	private String _depotFileDir;
	private int DEFAULT_MINIMUM_LINES = 5000;
	
	@Autowired
	public void setConfigurationService(ConfigurationService configurationService) {
		_configurationService = configurationService;
	}
	
	@Autowired
	public void setDepotFileDir(String depotFileDir) {
		_depotFileDir = depotFileDir;
	}

	private static Logger _log = LoggerFactory.getLogger(DepotAssignsSOAPQueryJob.class);

	@Override
	protected void executeInternal(JobExecutionContext executionContext)
			throws JobExecutionException {
		try {
            // Create SOAP Connection
            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            SOAPConnection soapConnection = soapConnectionFactory.createConnection();

            // Send SOAP Message to SOAP Server
            String url = "http://dobmobile.nyct.com/prwebservices/cis.asmx?WSDL";
            SOAPMessage soapResponse = soapConnection.call(createSOAPRequest(), url);
            
            // Process the SOAP Response
            printSOAPResponse(soapResponse);
            soapConnection.close();
        } catch (Exception e) {
            _log.error("Error occurred while sending SOAP Request to Server");
            e.printStackTrace();
        }
	}
	
	private SOAPMessage createSOAPRequest() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        String serverURI = "http://mtlivbus/";
        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", serverURI  + "BusAssignment");
        soapMessage.saveChanges();
        return soapMessage;
    }

    /**
     * Method used to print the SOAP Response
     */
    private void printSOAPResponse(SOAPMessage soapResponse) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.METHOD, "text");
        Source sourceContent = soapResponse.getSOAPPart().getContent();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmm");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String date = dateFormat.format(new Date());
        String path = System.getProperty(_depotFileDir)+"/"+"depot_assignments_"+date+".xml";
        File file = new File(path);
        StreamResult result = new StreamResult(file);
        transformer.transform(sourceContent, result);
        LineNumberReader  lnr = new LineNumberReader(new FileReader(file));
        lnr.skip(Long.MAX_VALUE);
        int lines = lnr.getLineNumber(); 
        lnr.close();
        int minimumLines = getMinimumLines();
        if (lines < minimumLines){
        	_log.error("Insufficient lines. (Lines:" + lines + ", Minimum: " + minimumLines + ")");
        	file.delete();
        } else {
        	_log.info(file.getAbsoluteFile()+"(Lines:" + lines + ", Minimum: " + minimumLines + ")");
        }
    }
    
    private int getMinimumLines() {
    	if (_configurationService != null) {
    		try {
    			return _configurationService.getConfigurationValueAsInteger("tdm.minimumLines", DEFAULT_MINIMUM_LINES);
    		} catch (RemoteConnectFailureException e){
    			_log.error("default minimum lines lookup failed:", e);
    			return DEFAULT_MINIMUM_LINES;
    		}
    	}
    	return DEFAULT_MINIMUM_LINES;
    }
}
