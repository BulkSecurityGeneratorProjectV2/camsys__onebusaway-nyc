/**
 * Copyright (c) 2011 Metropolitan Transportation Authority
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.nyc.webapp.actions.m;

import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.onebusaway.nyc.presentation.model.EnumDisplayMedia;
import org.onebusaway.nyc.presentation.model.realtime_data.DistanceAway;
import org.onebusaway.nyc.presentation.model.realtime_data.RouteItem;
import org.onebusaway.nyc.presentation.model.realtime_data.StopItem;
import org.onebusaway.nyc.presentation.model.search.RouteDestinationItem;
import org.onebusaway.nyc.presentation.model.search.RouteSearchResult;
import org.onebusaway.nyc.presentation.model.search.StopSearchResult;
import org.onebusaway.nyc.presentation.service.NycSearchService;
import org.onebusaway.nyc.presentation.service.search.SearchResult;
import org.onebusaway.nyc.webapp.actions.OneBusAwayNYCActionSupport;
import org.onebusaway.utility.DateLibrary;

import org.springframework.beans.factory.annotation.Autowired;

public class IndexAction extends OneBusAwayNYCActionSupport {
  private static final long serialVersionUID = 1L;
  
  private static final String GA_ACCOUNT = "UA-XXXXXXXX-X";

  private String q;
  
  private Date time = null;

  private List<SearchResult> searchResults = new ArrayList<SearchResult>();
  
  @Autowired
  private NycSearchService searchService;

  public String getQ() {
    return q;
  }

  public void setQ(String q) {
    this.q = q;
  }

  public void setTime(String time) throws ParseException {
    if(time != null && !time.isEmpty())
      this.time = DateLibrary.getIso8601StringAsTime(time);
  }
  
  public Date getTime() {
    if(time == null)
      return new Date();
    else
      return time;
  }
  
  public List<SearchResult> getSearchResults() {
    return searchResults;
  }
  
  public String execute() throws Exception {
    if (q != null) {
      searchService.setTime(getTime());        
      searchResults = searchService.search(q, EnumDisplayMedia.MOBILE_WEB);
    }
    return SUCCESS;
  }

  // Adapted from http://code.google.com/mobile/analytics/docs/web/#jsp
  public String getGoogleAnalyticsTrackingUrl() {
	  try {
	      StringBuilder url = new StringBuilder();
	      url.append("/ga?");
	      url.append("utmac=").append(GA_ACCOUNT);
	      url.append("&utmn=").append(Integer.toString((int) (Math.random() * 0x7fffffff)));

	      // referer
	      HttpServletRequest request = ServletActionContext.getRequest();      
	      String referer = request.getHeader("referer");
	
	      if (referer == null || "".equals(referer)) {
	        referer = "-";
	      }
	      url.append("&utmr=").append(URLEncoder.encode(referer, "UTF-8"));

	      // event tracking
	      String label = getQ();	      
	      if(label == null) {
	    	  label = "";
	      }
	      
	      String action = new String("Unknown");
	      if(searchResults != null && !searchResults.isEmpty()) {
	    	  SearchResult firstResult = searchResults.get(0);	    	  
	    	  if(firstResult.getType().equals("route")) {
	    		  action = "Route Search";
	    	  } else if(firstResult.getType().equals("stop")) {
	    		  if(searchResults.size() > 1) {
	    			  action = "Intersection Search";
	    		  } else {
	    			  action = "Stop Search";
	    		  }
	    	  }	    	  
	      }	else {
	    	  if(getQueryIsEmpty()) {
	    		  action = "Home";
	    	  } else {
	    		  action = "No Search Results";	    		  
	    	  }
	      }
	      
	      // page view on homepage hit, "event" for everything else.
	      if(action.equals("Home")) {
    	      url.append("&utmp=/m/index");
	      } else {
    	      url.append("&utmt=event&utme=5(Mobile Web*" + action + "*" + label + ")");	    	  
	      }
	      
	      // misc.
	      url.append("&guid=ON");
	      
	      return url.toString().replace("&", "&amp;"); 
	  } catch(Exception e) {
		  return null;
	  }
  }

  public boolean getQueryIsEmpty() {
	  if(q == null) {
		  return true;
	  } else {
		  return q.isEmpty();
	  }
  }
    
  public String getCacheBreaker() {
	  return (int)Math.ceil((Math.random() * 100000)) + "";
  }
  
  // find latest time across all DistanceAways
  public String getLastUpdateTime() {
    Date lastUpdated = null;
    for(SearchResult _result : searchResults) {
      if(_result.getType().equals("route")) {
        RouteSearchResult result = (RouteSearchResult)_result;
        for(RouteDestinationItem destination : result.getDestinations()) {
          for(StopItem stop : destination.getStopItems()) {
            for(DistanceAway distanceAway : stop.getDistanceAways()) {
              if(lastUpdated == null || distanceAway.getUpdateTimestamp().getTime() < lastUpdated.getTime()) {
                lastUpdated = distanceAway.getUpdateTimestamp();
              }
            }
          }
        }
      } else if(_result.getType().equals("stop")) {
        StopSearchResult result = (StopSearchResult)_result;
        for(RouteItem route : result.getRoutesAvailable()) {
          for(DistanceAway distanceAway : route.getDistanceAways()) {
            if(lastUpdated == null || distanceAway.getUpdateTimestamp().getTime() < lastUpdated.getTime()) {
              lastUpdated = distanceAway.getUpdateTimestamp();
            }
          }
        }
      }
    }

    if(lastUpdated != null) {
      return DateFormat.getTimeInstance().format(lastUpdated);
    } else {
      // no realtime data
      return DateFormat.getTimeInstance().format(new Date());
    }
  }  
  
  public String getTitle() {
    String title = this.q;
	
    if(searchResults.size() == 1) {
      SearchResult result = searchResults.get(0);
	  
      if(result != null) {
        title = result.getName() + " (" + title + ")";
      }
    }
	
    return title;
  }
}
