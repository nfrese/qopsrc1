package at.qop.qopwebui;

import java.util.List;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import at.qop.qoplib.Configuration;
import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.domains.IAddressDomain;
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
        
	
        
        ComboBox<Address> filtercombo = new ComboBox<>("Adressen nachschlagen");
        filtercombo.setWidth(100, Unit.PERCENTAGE);
        filtercombo.setEmptySelectionAllowed(true);
        AddressService addressService = new AddressService() {

			@Override
			public List<Address> fetchAddresses(int offset, int limit, String namePrefix) {
				IAddressDomain ad = LookupSessionBeans.addressDomain();
				return ad.findAddresses(offset, limit, namePrefix);
			}

			@Override
			public int getAddressCount(String namePrefix) {
				IAddressDomain ad = LookupSessionBeans.addressDomain();
				return ad.countAddresses(namePrefix);
			}
        	
        };
        DataProvider<Address, String> dataProvider =
        		DataProvider.fromFilteringCallbacks(
        				query -> {
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
        
		filtercombo.addSelectionListener(event ->
	    layout.addComponent(new Label("Selected " +
	        event.getSelectedItem().get().geom)));
		
        layout.addComponents(title, filtercombo);
        
        setContent(layout);
    }

    @WebServlet(urlPatterns = "/*", name = "QopUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = QopUI.class, productionMode = false)
    public static class QopUIServlet extends VaadinServlet {
		private static final long serialVersionUID = 1L;
    }
}
