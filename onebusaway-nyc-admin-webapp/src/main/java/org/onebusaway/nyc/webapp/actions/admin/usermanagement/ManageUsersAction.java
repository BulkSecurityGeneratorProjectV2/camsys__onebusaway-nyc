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

package org.onebusaway.nyc.webapp.actions.admin.usermanagement;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.nyc.admin.model.ui.UserDetail;
import org.onebusaway.nyc.admin.service.UserManagementService;
import org.onebusaway.nyc.webapp.actions.OneBusAwayNYCAdminActionSupport;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;

/**
 * Action class for user management operations
 * @author abelsare
 *
 */
@Results({
	@Result(name="updateUser", type="json", params= {"root","updateUserMessage"})
})
public class ManageUsersAction extends OneBusAwayNYCAdminActionSupport {

	private static final long serialVersionUID = 1L;
	
	private UserManagementService userManagementService;
	private Gson gsonTool;
	
	private String userData;
	private String updateUserMessage;
	
	/**
	 * Edits a user in the system
	 * @return
	 */
	public String editUser() {
		UserDetail userDetail = gsonTool.fromJson(userData, UserDetail.class);
		boolean success = userManagementService.updateUser(userDetail);
		if(success) {
			updateUserMessage =  "User '" +userDetail.getUserName() + "' edited successfully";
		} else {
			updateUserMessage = "Error editing user : '" +userDetail.getUserName() +"'";
		}
		
		return "updateUser";
		
	}
	
	public String deactivateUser() {
		UserDetail userDetail = gsonTool.fromJson(userData, UserDetail.class);
		boolean success = userManagementService.deactivateUser(userDetail);
		if(success) {
			updateUserMessage =  "User '" +userDetail.getUserName() + "' deactivated successfully";
		} else {
			updateUserMessage = "Error deactivating user : '" +userDetail.getUserName() +"'";
		}
		return "updateUser";
	}

	/**
	 * @param userManagementService the userManagementService to set
	 */
	@Autowired
	public void setUserManagementService(UserManagementService userManagementService) {
		this.userManagementService = userManagementService;
	}

	/**
	 * @return the userData
	 */
	public String getUserData() {
		return userData;
	}

	/**
	 * @param userData the userData to set
	 */
	public void setUserData(String userData) {
		this.userData = userData;
	}

	/**
	 * @return the updateUserMessage
	 */
	public String getUpdateUserMessage() {
		return updateUserMessage;
	}

	/**
	 * @param editUserMessage the updateUserMessage to set
	 */
	public void setUpdateUserMessage(String updateUserMessage) {
		this.updateUserMessage = updateUserMessage;
	}

	/**
	 * @param gsonTool the gsonTool to set
	 */
	@Autowired
	public void setGsonTool(Gson gsonTool) {
		this.gsonTool = gsonTool;
	}

}
