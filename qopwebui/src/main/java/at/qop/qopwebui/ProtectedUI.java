package at.qop.qopwebui;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.WrappedSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import at.qop.qoplib.ConfigFile;
import at.qop.qoplib.LookupSessionBeans;
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
			ConfigFile cfg = ConfigFile.read();
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

		ConfigFile cfg = ConfigFile.read();

		String currUsername = currentUserName();

		boolean authenticated = cfg.hasUser(currUsername);

		if (!authenticated)
		{
			showLogin(vaadinRequest, cfg);
		} else {
			start(vaadinRequest, cfg, currUsername);
		}
	}

	private void start(VaadinRequest vaadinRequest, ConfigFile cfg, String currUsername) {
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

	private void showLogin(VaadinRequest vaadinRequest, ConfigFile cfg)
	{
		TextField login = new TextField("Benutzername");

		PasswordField password = new PasswordField("Passwort");
		Button ok = new Button("Anmelden");
		Label message = new Label();
		ok.addClickListener(e -> {

			String passwd = cfg.getUserPassword(login.getValue());
			if (passwd != null && passwd.equals(password.getValue()))
			{
				message.setValue("");
				httpSess().setAttribute(AUTHENTICATED_AS, login.getValue());
				start(vaadinRequest, cfg, currentUserName());
			}
			else
			{
				message.setValue("Falscher Benutzername/Passwort");
			}
		});

		HorizontalLayout buttonsHl = new HorizontalLayout(ok);

		VerticalLayout vl = new VerticalLayout(login, password, buttonsHl, message);		
		setContent(vl);
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
