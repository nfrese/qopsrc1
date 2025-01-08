/* 
 * Copyright (C) 2018 Norbert Frese
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
*/

package at.qop.ws;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import at.qop.qoplib.Config;
import at.qop.qoplib.Constants;
import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.batch.BatchCalculationInMemory;
import at.qop.qoplib.batch.BatchCalculationInMemoryImpl;
import at.qop.qoplib.entities.Address;
import at.qop.qoplib.entities.Analysis;
import at.qop.qoplib.entities.Profile;
import at.qop.qoplib.extinterfaces.batch.BatchHandler;

@RestController
public class QOPBatchCalculationServlet {
       
    public QOPBatchCalculationServlet() {
        super();
    }

    @GetMapping("/qop/rest/api/batchcalculation")
	protected String doGet() {
    	StringBuilder html = new StringBuilder();
    	html.append("<h1>HTTP-GET not supported!</h1>");
    	html.append("<p>sample JSON for HTTP-POST:</p>");
    	html.append("<pre>");
    	html.append(Constants.BATCH_CALCULATION_SAMPLE_JSON);
    	html.append("</pre>");
    	html.append("<p>required URL parameters for POST: user, password</p>");
    	html.append("<p>required encoding: UTF-8</p>");
    	return html.toString();
	}

    @PostMapping("/qop/rest/api/batchcalculation")
	protected ResponseEntity<?> doPost(@RequestParam(name = "username") String username, @RequestParam(name="password") String password, @RequestBody String jsonIn) throws ServletException, IOException {
		
		if (username == null) throw new RuntimeException("URL parameter username required");
		if (password == null) throw new RuntimeException("URL parameter password required");
		
		Config cfg = Config.read();
		if (!password.equals(cfg.getUserPassword(username)))
		{
			throw new RuntimeException("Invalid username/password!");
		}
		
		BatchHandler bh = new BatchHandler() {

			@Override
			protected BatchCalculationInMemory createBC(Profile profile, List<Address> addresses) {
				return new BatchCalculationInMemoryImpl(profile, addresses);
			}

			@Override
			protected Profile lookupProfile(String profileName) {
				
				
				List<Profile> profiles = LookupSessionBeans.profileDomain().listProfiles();
				for (Profile profile : profiles)
				{
					if (profile.name.equals(profileName))
					{
						cfg.checkUserProfile(username, profile.name);
						return profile;
					}
				}
				return null;
			}

			@Override
			protected Analysis lookupAnalysis(String analysisName) {
				List<Analysis> profiles = LookupSessionBeans.profileDomain().listAnalyses();
				for (Analysis analysis : profiles)
				{
					if (analysis.name.equals(analysisName))
					{
						return analysis;
					}
				}
				return null;
			}
			
		};
		
		String jsonOut = bh.jsonCall(jsonIn);

		return ResponseEntity.ok().header("Content-Type", "application/json;charset=UTF-8").body(jsonOut);
	}

}
