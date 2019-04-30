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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import at.qop.qoplib.Config;
import at.qop.qoplib.Constants;
import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.batch.BatchCalculationInMemory;
import at.qop.qoplib.batch.BatchCalculationInMemoryImpl;
import at.qop.qoplib.entities.Address;
import at.qop.qoplib.entities.Profile;
import at.qop.qoplib.extinterfaces.batch.BatchHandler;

@WebServlet("/batchcalculation_servlet")
public class QOPBatchCalculationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public QOPBatchCalculationServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().append("<h1>HTTP-GET not supported!</h1>");
		response.getWriter().append("<p>sample JSON for HTTP-POST:</p>");
		response.getWriter().append("<pre>");
		response.getWriter().append(Constants.BATCH_CALCULATION_SAMPLE_JSON);
		response.getWriter().append("</pre>");
		response.getWriter().append("<p>required URL parameters for POST: user, password</p>");
		response.getWriter().append("<p>required encoding: UTF-8</p>");
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String username = request.getParameter("username");
		if (username == null) throw new RuntimeException("URL parameter username required");
		
		String password = request.getParameter("password");
		if (password == null) throw new RuntimeException("URL parameter password required");
		
		Config cfg = Config.read();
		if (!password.equals(cfg.getUserPassword(username)))
		{
			throw new RuntimeException("Invalid username/password!");
		}
		
		ServletInputStream inputStream = request.getInputStream();
 
		String jsonIn = readToString(inputStream);
		
		response.setContentType("application/json;charset=UTF-8");
		
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
			
		};
		
		String jsonOut = bh.jsonCall(jsonIn);
		
		response.getWriter().append(jsonOut);
	}

	private String readToString(ServletInputStream inputStream) throws IOException, UnsupportedEncodingException {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		while ((length = inputStream.read(buffer)) != -1) {
		    result.write(buffer, 0, length);
		}

		String in = result.toString("UTF-8");
		return in;
	}

}
