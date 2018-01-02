package at.qop.qopwebui;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import at.qop.qoplib.Configuration;

@Theme("mytheme")
public class QopUI extends UI {

	private static final long serialVersionUID = 1L;

	@Override
    protected void init(VaadinRequest vaadinRequest) {
        final VerticalLayout layout = new VerticalLayout();
        
        final Label title  = new Label(Configuration.TITLE());
        
        final TextField name = new TextField();
        name.setCaption("Type your name here:");

        Button button = new Button("Click Me please!");
        button.setClickShortcut(KeyCode.ENTER);
        button.addClickListener(e -> {
            layout.addComponent(new Label("Thanks " + name.getValue() 
                    + ", it works!"));
        });
        
        layout.addComponents(title, name, button);
        
        setContent(layout);
    }

    @WebServlet(urlPatterns = "/*", name = "QopUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = QopUI.class, productionMode = false)
    public static class QopUIServlet extends VaadinServlet {
		private static final long serialVersionUID = 1L;
    }
}
