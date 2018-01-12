package at.qop.qopwebui;

import java.io.PrintWriter;
import java.io.StringWriter;
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

import at.qop.qoplib.ConfigFile;
import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.calculation.Calculation;
import at.qop.qoplib.calculation.DbLayerSource;
import at.qop.qoplib.calculation.IRouter;
import at.qop.qoplib.calculation.LayerSource;
import at.qop.qoplib.domains.IAddressDomain;
import at.qop.qoplib.entities.Address;
import at.qop.qoplib.entities.Profile;
import at.qop.qoplib.osrmclient.OSRMClient;
import at.qop.qopwebui.components.ExceptionDialog;

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


		final Label title  = new Label("<big><b>QOP Standortbewertung</b></big>", ContentMode.HTML);

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
			startCalculationWCatch();
		});

		leafletMap = new LMap();
		leafletMap.setWidth(100, Unit.PERCENTAGE);
		leafletMap.setHeight(100, Unit.PERCENTAGE);
		LTileLayer baseLayerOsm = new LTileLayer();
		baseLayerOsm.setUrl("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png");
		baseLayerOsm.setOpacity(0.9);
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
				startCalculationWCatch();
				leafletMap.zoomToContent();
			}

		});

		lfgResults = new LFeatureGroup();
		leafletMap.addLayer(lfgResults);

		grid = new GridLayout(5, 1);
		grid.setSpacing(true);

		HorizontalLayout hl = new HorizontalLayout(new VerticalLayout(title, filtercombo, profileCombo, new Label(""), grid), leafletMap);
		hl.setSizeFull();
		setContent(hl);
		
	}

	private void startCalculationWCatch() {
		try {
			startCalculation();
		} catch (Exception ex)
		{
			ex.printStackTrace();
			StringWriter pw = new StringWriter();
			ex.printStackTrace(new PrintWriter(pw));
			
			new ExceptionDialog("Fehler bei Auswertung:" , pw.toString()).show();;
		}
	}

	private void startCalculation() {
		if (currentProfile != null && currentAddress != null)
		{
			LayerSource source = new DbLayerSource();
			ConfigFile cf = ConfigFile.read();
			IRouter router = new OSRMClient(cf.getOSRMHost(), cf.getOSRMPort());
			Calculation calculation = new Calculation(currentProfile, currentAddress, source, router);
			calculation.run();

			grid.removeAllComponents();
			grid.addComponent(new Label("<u>Berechnung</u>",  ContentMode.HTML));
			grid.addComponent(new Label("<u>Resultat</u>",  ContentMode.HTML));
			grid.addComponent(new Label("<u>Bewertung</u>",  ContentMode.HTML));
			grid.addComponent(new Label("<u>Gewicht</u>",  ContentMode.HTML));
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
				button.addClickListener(e -> {

					if (lfgResults != null)
					{
						lfgResults.removeAllComponents();

						lc.keptTargets.stream().forEach(lt -> {
							if (lt.route != null)
							{
								LPolyline lp = new LPolyline(lt.route);
								lp.setColor("#ff6020");
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
			
			grid.addComponent(new Label("<b>Summe</b>",  ContentMode.HTML));
			grid.addComponent(new Label("",  ContentMode.HTML));
			
			overallRatingLabel = new Label("",  ContentMode.HTML);
			
			grid.addComponent(overallRatingLabel);
			grid.addComponent(new Label("",  ContentMode.HTML));
			grid.addComponent(new Label("",  ContentMode.HTML));
			
			refreshOverallRating(calculation);
		}
	}

	private void refreshOverallRating(Calculation calculation) {
		overallRatingLabel.setValue("<big><b>" + formatDouble2Decimal(calculation.overallRating()) + "</b></big>");
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
