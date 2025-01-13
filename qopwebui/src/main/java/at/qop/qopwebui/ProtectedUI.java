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

package at.qop.qopwebui;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.login.AbstractLogin.LoginEvent;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.WrappedSession;

import at.qop.qoplib.Config;
import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.QopLibManifest;
import at.qop.qoplib.entities.Profile;

public abstract class ProtectedUI extends VerticalLayout {

	private static final String AUTHENTICATED_AS = "authenticated_as";

	private static final long serialVersionUID = 1L;

	protected abstract void ainit(VaadinRequest vaadinRequest);

	protected abstract boolean requiresAdminRole();
	
	protected String[] userProfiles() {
		String u = currentUserName();
		if (u != null)
		{
			Config cfg = Config.read();
			String[] up = cfg.getUserProfiles(u);
			return up;
		}
		else
		{
			return null;
		}
	}
	
	//@Override
	protected void init(VaadinRequest vaadinRequest) {

		Config cfg = Config.read();

		String currUsername = currentUserName();

		boolean authenticated = cfg.hasUser(currUsername);

		if (!authenticated)
		{
			showLogin(vaadinRequest, cfg);
		} else {
			start(vaadinRequest, cfg, currUsername);
		}
	}

	private void start(VaadinRequest vaadinRequest, Config cfg, String currUsername) {
		if (requiresAdminRole())
		{
			if (cfg.isAdmin(currUsername))
			{
				ainit(vaadinRequest);
			}
			else
			{
				add(new VerticalLayout(new Label("keine Admin Rechte!"), logoutButton()));
			}
		}
		else
		{
			ainit(vaadinRequest);
		}
	}


	private String currentUserName() {
		return (String)httpSess().getAttribute(AUTHENTICATED_AS);
	}


	private WrappedSession httpSess() {
		return VaadinService.getCurrentRequest().getWrappedSession();
	}

	private void showLogin(VaadinRequest vaadinRequest, Config cfg)
	{
		Label version = new Label();
		QopLibManifest manifest = new QopLibManifest();
		version.add(manifest.getShortInfo());
		
		LoginForm loginForm = new LoginForm();
		Label message = new Label();		
		
		loginForm.addLoginListener(new ComponentEventListener<LoginEvent>() {

			private static final long serialVersionUID = 1L;

			@Override
			public void onComponentEvent(LoginEvent event) {
				String userName = event.getUsername();
				String passwd = cfg.getUserPassword(userName);
				if (passwd != null && passwd.equals(event.getPassword()))
				{
					message.setText("");
					httpSess().setAttribute(AUTHENTICATED_AS, userName);
					start(vaadinRequest, cfg, currentUserName());
				}
				else
				{
					message.setText("Falscher Benutzername/Passwort");
				}
			}

		});
		
		VerticalLayout vl = new VerticalLayout(loginForm, message);
		vl.setSizeFull();
		VerticalLayout vl2 = new VerticalLayout(vl, version);
		vl2.setHorizontalComponentAlignment(Alignment.CENTER, vl);
		vl2.setHorizontalComponentAlignment(Alignment.CENTER, version);
		vl2.setSizeFull();
		add(vl2);
		//setSizeFull();
	}
	
	protected Button logoutButton()
	{
		Button logout = new Button("> " + currentUserName() + " abmelden");
		//logout.addStyleName(ValoTheme.BUTTON_LINK);
		logout.addClickListener(e -> {
			httpSess().removeAttribute(AUTHENTICATED_AS);
			// TODO Page.reload();
		});
		return logout;
	}
	
	protected List<Profile> profilesForUser() {
		List<Profile> allProfiles = LookupSessionBeans.profileDomain().listProfiles();
		
		String[] userProfiles = userProfiles();
		if (userProfiles != null)
		{
			List<Profile> profilesForUser = new ArrayList<>();
			for (String name : userProfiles)
			{
				for (Profile p : allProfiles)
				{
					if (p.name.equals(name))
					{
						profilesForUser.add(p);
					}
				}
			}
			return profilesForUser;
		}
		else
		{
			return allProfiles;
		}
	}

}
