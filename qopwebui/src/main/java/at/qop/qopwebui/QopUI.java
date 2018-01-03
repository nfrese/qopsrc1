package at.qop.qopwebui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.ComboBox.CaptionFilter;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import at.qop.qoplib.Configuration;
import at.qop.qoplib.LookupDomains;
import at.qop.qoplib.domains.IConfigDomain;
import at.qop.qoplib.entities.Address;

@Theme("mytheme")
public class QopUI extends UI {

	private static final long serialVersionUID = 1L;

	public interface AddressService {
		  List<Address> fetchAddresses(
		    int offset,
		    int limit,
		    String namePrefix);
		  int getAddressCount(String namePrefix);
		}
	
	@Override
    protected void init(VaadinRequest vaadinRequest) {
        final VerticalLayout layout = new VerticalLayout();
        
        final Label title  = new Label(Configuration.TITLE());
        
//        final TextField name = new TextField();
//        name.setCaption("Type your name here:");
//
//        Button button = new Button("Click Me please!");
//        button.setClickShortcut(KeyCode.ENTER);
//        button.addClickListener(e -> {
//            layout.addComponent(new Label("Thanks " + name.getValue() 
//                    + ", it works!"));
//        });
        
        ComboBox<Address> filtercombo = new ComboBox<>("Adressen nachschlagen");
        filtercombo.setWidth(100, Unit.PERCENTAGE);
        filtercombo.setEmptySelectionAllowed(true);
        AddressService addressService = new AddressService() {

			@Override
			public List<Address> fetchAddresses(int offset, int limit, String namePrefix) {
				IConfigDomain cd;
				cd = LookupDomains.configDomain();
				return cd.findAddresses(offset, limit, namePrefix);
			}

			@Override
			public int getAddressCount(String namePrefix) {
				IConfigDomain cd;
				cd = LookupDomains.configDomain();
				return cd.countAddresses(namePrefix);
			}
        	
        };
        DataProvider<Address, String> dataProvider =
        		DataProvider.fromFilteringCallbacks(
        				query -> {
        					// getFilter returns Optional<String>
        					String filter = query.getFilter().orElse(null);
        					return addressService.fetchAddresses(
        							query.getOffset(),
        							query.getLimit(),
        							filter
        							).stream();
        				},
        				query -> {
        					String filter = query.getFilter().orElse(null);
        					return addressService.getAddressCount(filter);
        				}
        				);
		filtercombo.setDataProvider(dataProvider);
        
        layout.addComponents(title, filtercombo);
        
        setContent(layout);
    }

    @WebServlet(urlPatterns = "/*", name = "QopUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = QopUI.class, productionMode = false)
    public static class QopUIServlet extends VaadinServlet {
		private static final long serialVersionUID = 1L;
    }
}
