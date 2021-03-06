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

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.WrappedSession;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.LoginForm;
import com.vaadin.ui.LoginForm.LoginEvent;
import com.vaadin.ui.LoginForm.LoginListener;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import at.qop.qoplib.Config;
import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.QopLibManifest;
import at.qop.qoplib.entities.Profile;

public abstract class ProtectedUI extends UI {

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
	
	@Override
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
				setContent(new VerticalLayout(new Label("keine Admin Rechte!"), logoutButton()));
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
		version.setValue(manifest.getShortInfo());
		
		LoginForm loginForm = new LoginForm();
		Label message = new Label();		
		
		loginForm.addLoginListener(new LoginListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void onLogin(LoginEvent event) {
				String userName = event.getLoginParameter("username");
				String passwd = cfg.getUserPassword(userName);
				if (passwd != null && passwd.equals(event.getLoginParameter("password")))
				{
					message.setValue("");
					httpSess().setAttribute(AUTHENTICATED_AS, userName);
					start(vaadinRequest, cfg, currentUserName());
				}
				else
				{
					message.setValue("Falscher Benutzername/Passwort");
				}
			}
		});
		
		VerticalLayout vl = new VerticalLayout(loginForm, message);
		vl.setSizeFull();
		VerticalLayout vl2 = new VerticalLayout(vl, version);
		vl2.setComponentAlignment(vl, Alignment.TOP_CENTER);
		vl2.setComponentAlignment(version, Alignment.TOP_CENTER);
		vl2.setSizeFull();
		setContent(vl2);
		setSizeFull();
	}
	
	protected Button logoutButton()
	{
		Button logout = new Button("> " + currentUserName() + " abmelden");
		logout.addStyleName(ValoTheme.BUTTON_LINK);
		logout.addClickListener(e -> {
			httpSess().removeAttribute(AUTHENTICATED_AS);
			Page.getCurrent().reload();
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
