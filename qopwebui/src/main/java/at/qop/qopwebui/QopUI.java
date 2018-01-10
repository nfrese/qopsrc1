package at.qop.qopwebui;

import java.text.DecimalFormat;
import java.util.List;

import javax.servlet.annotation.WebServlet;

import org.vaadin.addon.leaflet.LCircle;
import org.vaadin.addon.leaflet.LFeatureGroup;
import org.vaadin.addon.leaflet.LMap;
import org.vaadin.addon.leaflet.LMarker;
import org.vaadin.addon.leaflet.LPolyline;
import org.vaadin.addon.leaflet.LTileLayer;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.shared.ui.slider.SliderOrientation;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Slider;
import com.vaadin.ui.Slider.ValueOutOfBoundsException;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import com.vividsolutions.jts.geom.Point;

import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.calculation.Calculation;
import at.qop.qoplib.calculation.DbLayerSource;
import at.qop.qoplib.calculation.IRouter;
import at.qop.qoplib.calculation.LayerSource;
import at.qop.qoplib.domains.IAddressDomain;
import at.qop.qoplib.entities.Address;
import at.qop.qoplib.entities.Profile;
import at.qop.qoplib.osrmclient.OSRMClient;

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

	private Profile currentProfile;
	private Address currentAddress;
	private LMap leafletMap;
	private LFeatureGroup lfgResults;
	private GridLayout grid;
	private Label overallRatingLabel;

	@Override
	protected void init(VaadinRequest vaadinRequest) {


		final Label title  = new Label("QOP Standortbewertung");

		ComboBox<Address> filtercombo = new ComboBox<>("Adresse nachschlagen");
		filtercombo.setWidth(400, Unit.PIXELS);
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

		List<Profile> profiles = LookupSessionBeans.profileDomain().listProfiles();
		ComboBox<Profile> profileCombo = new ComboBox<>("Profilauswahl", profiles);
		if (profiles.size() > 0)
		{
			profileCombo.setSelectedItem(profiles.get(0));
			currentProfile = profiles.get(0);
		}
		profileCombo.setEmptySelectionAllowed(false);
		profileCombo.setTextInputAllowed(false);

		profileCombo.addSelectionListener(event -> {
			currentProfile = event.getSelectedItem().isPresent() ? event.getSelectedItem().get() : null;
			startCalculation();
		});

		leafletMap = new LMap();
		leafletMap.setWidth("600px");
		leafletMap.setHeight("400px");
		LTileLayer baseLayerOsm = new LTileLayer();
		baseLayerOsm.setUrl("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png");
		leafletMap.addBaseLayer(baseLayerOsm, "OSM");


		LFeatureGroup lfg = new LFeatureGroup();
		leafletMap.addLayer(lfg);

		filtercombo.addSelectionListener(event -> {

			lfg.removeAllComponents();
			lfgResults.removeAllComponents();

			currentAddress = event.getSelectedItem().isPresent() ? event.getSelectedItem().get() : null;
			if (currentAddress != null) {
				LMarker lm = new LMarker(currentAddress.geom);
				lm.setCaption("<b>Aktuelle Adresse:</b><br>" + currentAddress.name);
				lfg.addComponent(lm);
				startCalculation();
				leafletMap.zoomToContent();
			}

		});

		lfgResults = new LFeatureGroup();
		leafletMap.addLayer(lfgResults);

		grid = new GridLayout(5, 1);
		grid.setSpacing(true);

		final VerticalLayout layout = new VerticalLayout(title, new HorizontalLayout(new VerticalLayout(filtercombo, profileCombo, grid), leafletMap));
		layout.setWidth(100, Unit.PERCENTAGE);

		setContent(layout);
	}

	private void startCalculation() {
		if (currentProfile != null && currentAddress != null)
		{
			LayerSource source = new DbLayerSource();
			IRouter router = new OSRMClient("http://10.0.0.17:5000");
			Calculation calculation = new Calculation(currentProfile, currentAddress, source, router);
			calculation.run();

			grid.removeAllComponents();
			grid.addComponent(new Label("<b>Berechnung</b>",  ContentMode.HTML));
			grid.addComponent(new Label("<b>Resultat</b>",  ContentMode.HTML));
			grid.addComponent(new Label("<b>Bewertung</b>",  ContentMode.HTML));
			grid.addComponent(new Label("<b>Gewicht</b>",  ContentMode.HTML));
			grid.addComponent(new Label("",  ContentMode.HTML));

			calculation.layerCalculations.forEach(lc -> {

				grid.addComponent(new Label(lc.params.description,  ContentMode.HTML));
				grid.addComponent(new Label(formatDouble2Decimal(lc.result) +"",  ContentMode.HTML));
				grid.addComponent(new Label(formatDouble2Decimal(lc.rating) +"",  ContentMode.HTML));
				Slider slider = new Slider(0, 2);
				slider.setResolution(1);
				slider.setWidth(150, Unit.PIXELS);
				slider.setOrientation(SliderOrientation.HORIZONTAL);
				try {
					slider.setValue(lc.weight);
					slider.addValueChangeListener(l -> {
						lc.weight = slider.getValue();
						refreshOverallRating(calculation);
					});
					grid.addComponent(slider);
				} catch (ValueOutOfBoundsException e) {
					grid.addComponent(new Label("Bad Value " + lc.weight,  ContentMode.HTML));
				}				

				Button button = new Button("Karte >");
				button.setStyleName(ValoTheme.BUTTON_LINK);
				//button.setIcon(new ClassResource("/images/button-img.jpg"));
				button.addClickListener(e -> {

					if (lfgResults != null)
					{
						lfgResults.removeAllComponents();

						lc.keptTargets.stream().forEach(lt -> {
							if (lt.route != null)
							{
								LPolyline lp = new LPolyline(lt.route);
								lp.setColor("#ff7070");
								lfgResults.addComponent(lp);
							}
						});
						
						lc.keptTargets.stream().forEach(lt -> {

							if (lt.geom instanceof Point)
							{
								LMarker lm = new LMarker((Point)lt.geom);
								lm.addStyleName("specialstyle");
								lm.setIcon(new ExternalResource("https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-green.png"));
								lm.setIconAnchor(new org.vaadin.addon.leaflet.shared.Point(12, 41));
								if (lt.caption != null)
								{
									lm.setPopup(lt.caption);
								}

								lfgResults.addComponent(lm);
							}
						});


						if (lc.params.hasRadius()) {
							LCircle circle = new LCircle(lc.start, lc.params.radius);
							lfgResults.addComponent(circle);
						}
						leafletMap.zoomToContent();
					}
				});
				grid.addComponent(button);

			});
			
			grid.addComponent(new Label("<b><i>Summe</i></b>",  ContentMode.HTML));
			grid.addComponent(new Label("",  ContentMode.HTML));
			
			overallRatingLabel = new Label("",  ContentMode.HTML);
			
			grid.addComponent(overallRatingLabel);
			grid.addComponent(new Label("",  ContentMode.HTML));
			grid.addComponent(new Label("",  ContentMode.HTML));
			
			refreshOverallRating(calculation);
		}
	}

	private void refreshOverallRating(Calculation calculation) {
		overallRatingLabel.setValue("<b><i>" + formatDouble2Decimal(calculation.overallRating()) + "</i></b>");
	}

	private String formatDouble2Decimal(double d) {
		return new DecimalFormat("#.##").format(d);
	}

	@WebServlet(urlPatterns = "/*", name = "QopUIServlet", asyncSupported = true)
	@VaadinServletConfiguration(ui = QopUI.class, productionMode = false)
	public static class QopUIServlet extends VaadinServlet {
		private static final long serialVersionUID = 1L;
	}
}
