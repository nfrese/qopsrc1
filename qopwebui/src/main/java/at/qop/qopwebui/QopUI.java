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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Stream;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.vaadin.stefan.table.Table;

import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.RangeInput;
import com.vaadin.flow.component.html.RangeInput.Orientation;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.theme.Theme;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

import at.qop.qoplib.Config;
import at.qop.qoplib.Constants;
import at.qop.qoplib.Utils;
import at.qop.qoplib.addresses.AddressLookup;
import at.qop.qoplib.addresses.HTTPAddressClient;
import at.qop.qoplib.calculation.CRSTransform;
import at.qop.qoplib.calculation.Calculation;
import at.qop.qoplib.calculation.CalculationSection;
import at.qop.qoplib.calculation.DbLayerSource;
import at.qop.qoplib.calculation.ILayerCalculation;
import at.qop.qoplib.calculation.IRouter;
import at.qop.qoplib.calculation.LayerCalculation;
import at.qop.qoplib.calculation.LayerSource;
import at.qop.qoplib.calculation.LayerTarget;
import at.qop.qoplib.calculation.LayerTargetDissolved;
import at.qop.qoplib.calculation.charts.QopChart;
import at.qop.qoplib.calculation.charts.QopPieChart;
import at.qop.qoplib.entities.Address;
import at.qop.qoplib.entities.Profile;
import at.qop.qoplib.extinterfaces.batch.QEXBatchInput;
import at.qop.qoplib.extinterfaces.batch.QEXBatchSourceLocation;
import at.qop.qoplib.extinterfaces.json.ExportProfile;
import at.qop.qoplib.osrmclient.OSRMClient;
import at.qop.qopwebui.components.ChartDialog;
import at.qop.qopwebui.components.DownloadDialog;
import at.qop.qopwebui.components.ExceptionDialog;
import at.qop.qopwebui.components.InfoDialog;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;
import software.xdev.vaadin.maps.leaflet.MapContainer;
import software.xdev.vaadin.maps.leaflet.basictypes.LLatLng;
import software.xdev.vaadin.maps.leaflet.layer.LLayer;
import software.xdev.vaadin.maps.leaflet.layer.other.LFeatureGroup;
import software.xdev.vaadin.maps.leaflet.layer.raster.LTileLayer;
import software.xdev.vaadin.maps.leaflet.layer.ui.LMarker;
import software.xdev.vaadin.maps.leaflet.layer.vector.LCircle;
import software.xdev.vaadin.maps.leaflet.layer.vector.LPathOptions;
import software.xdev.vaadin.maps.leaflet.layer.vector.LPolyline;
import software.xdev.vaadin.maps.leaflet.map.LMap;
import software.xdev.vaadin.maps.leaflet.registry.LComponentManagementRegistry;
import software.xdev.vaadin.maps.leaflet.registry.LDefaultComponentManagementRegistry;

//@Theme("valo")
@Route("/qop/ui")
public class QopUI extends ProtectedUI {

	private static final long serialVersionUID = 1L;

	private Profile currentProfile;
	private Address currentAddress;
	private TextField lonLatTf;
	private TabSheet locTabs;
	private ProgressBar progress;
	private LMap leafletMap;
	private LFeatureGroup lfgResults;
	private Table grid;
	private Span overallRatingLabel;

	public QopUI() {
		ainit(null);
	}
	
	private Stream<String> simulateBackendQuery(Optional<String> filter, long limit, long offset) {
		AddressLookup addressService = new HTTPAddressClient(Config.read().getAddressLookupURL());

		List<String> r = new ArrayList<String>();
		
		if (!filter.isPresent())
		{
			List<Address> addresses = addressService.fetchAddresses(
					(int)offset,
					(int)limit,
					filter.get());
			for (Address addr : addresses) {
				r.add(addr.name);
			}
		}
		
		return r.stream();
		
//		return  countries.stream()
//	            .filter(item -> !filter.isPresent() || item.contains(filter.get())).skip(offset).limit(limit);
	}
	
	@Override
	protected void ainit(VaadinRequest vaadinRequest) {


		final Span title  = new Span("<big><b>QOP Standortbewertung</b></big>");

		List<Profile> profiles = profilesForUser();
		ComboBox<Profile> profileCombo = new ComboBox<>("Profilauswahl", profiles);
		if (profiles.size() > 0)
		{
			profileCombo.setValue(profiles.get(0));
			currentProfile = profiles.get(0);
		}
		profileCombo.setRequired(true);
		
		profileCombo.addValueChangeListener(event -> {
			currentProfile = event.getValue();
			startCalculationWCatch();
		});
		
//		TextField addressSearchField = new TextField();
//		addressSearchField.setWidth(100, Unit.PERCENTAGE);
//		addressSearchField.focus();
		
		ComboBox<String> addressSearchField = new ComboBox<>("Select a country");
		addressSearchField.focus();
		
		addressSearchField.setItems(query->{
            Optional<String> filter = query.getFilter();
            int limit = query.getLimit();
            int offset = query.getOffset();
            return simulateBackendQuery(filter,limit,offset);
        });
		
//		SuggestionGenerator<Address> generator = new SuggestionGenerator<Address>() {
//
//			@Override
//			public List<Address> apply(String query, Integer limit) {
//				List<Address> addresses = addressService.fetchAddresses(
//						0,
//						limit,
//						query);
//				System.out.println(addresses);
//				return addresses;
//			}};
//			
//			
//		SuggestionValueConverter<Address> valueConverter = new SuggestionValueConverter<Address>() {
//
//			@Override
//			public String apply(Address suggestion) {
//				return suggestion.name;
//			}
//			
//		};
//		SuggestionCaptionConverter<Address> captionConverter = new SuggestionCaptionConverter<Address>() {
//
//			@Override
//			public String apply(Address suggestion, String query) {
//			    return "<div class='suggestion-container'>"
//			            //+ "<img src='" + user.getPicture() + "' class='userimage'>"
//			            + "<span class='username'>"
//			            + suggestion.name.replaceAll("(?i)(" + query + ")", "<b>$1</b>")
//			            + "</span>"
//			            + "</div>";
//			}
//			
//		};
//		AutocompleteExtension<Address> ace = new AutocompleteExtension<Address>(addressSearchField);
//		ace.setSuggestionGenerator(generator, valueConverter, captionConverter);
//		ace.showSuggestions();
//		ace.setSuggestionDelay(350);

		lonLatTf = new TextField();
		lonLatTf.setWidth(300, Unit.PIXELS);
		Button toLonLatButton = new Button("Los");
		
		final LDefaultComponentManagementRegistry reg = new LDefaultComponentManagementRegistry(this);
		
		// Create and add the MapContainer (which contains the map) to the UI
		final MapContainer mapContainer = new MapContainer(reg);
		//mapContainer.setSizeFull();
		mapContainer.setWidth("600px");
		mapContainer.setHeight("400px");
		
		LMap leafletMap = mapContainer.getlMap();
		LTileLayer baseLayerOsm = LTileLayer.createDefaultForOpenStreetMapTileServer(reg);
//		baseLayerOsm.setUrl("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png");
		leafletMap.addLayer(baseLayerOsm);
        //leafletMap = new LMap();
		//leafletMap.setWidth(100, Unit.PERCENTAGE);
		//leafletMap.setHeight(100, Unit.PERCENTAGE);
		//LTileLayer baseLayerOsm = new LTileLayer();
		//baseLayerOsm.setUrl("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png");
		baseLayerOsm.setOpacity(0.9);
		//leafletMap.addBaseLayer(baseLayerOsm, "OSM");

		LFeatureGroup lfg = new LFeatureGroup(reg);
		leafletMap.addLayer(lfg);

		toLonLatButton.addClickListener(l -> {
			String lonLatStr = lonLatTf.getValue();
			try {
				Point start = Utils.parseLonLatStr(lonLatStr);
				
				resetResults(lfg);
				resetAddressSearch(addressSearchField);
				
				LLatLng latLon = latLng(reg, start);
				LMarker lm = new LMarker(reg, latLon);
				lm.bindPopup("<b>Lon, Lat Eingabe:</b><br>" +lonLatStr);
				lfg.addLayer(lm);
				
				startCalculation(start);
				leafletMap.flyTo(latLon);
				
			} catch (IllegalArgumentException ex)
			{
				new InfoDialog("Fehler", "lon, lat ungültig!").show();
			}
		});
		
//		ace.addSuggestionSelectListener(new SuggestionSelectListener<Address>() {
//			
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			public void suggestionSelect(SuggestionSelectEvent<Address> event) {
//				resetResults(lfg);
//
//				currentAddress = event.getSelectedItem().isPresent() ? event.getSelectedItem().get() : null;
//				if (currentAddress != null) {
//					var latLon = latLng(reg, currentAddress.geom);
//					LMarker lm = new LMarker(reg, latLon);
//					lm.bindTooltip("<b>Aktuelle Adresse:</b><br>" + currentAddress.name);
//					lfg.addLayer(lm);
//					startCalculationWCatch();
//					leafletMap.flyTo(latLon);
//				}
//				
//			}
//		});
		
		lfgResults = new LFeatureGroup(reg);
		leafletMap.addLayer(lfgResults);

//		leafletMap. .addClickListener(l -> {
//			org.vaadin.addon.leaflet.shared.Point start = l.getPoint();
//			Point startJts = CRSTransform.gfWGS84.createPoint(new Coordinate(start.getLon(), start.getLat()));
//			setLonLat(startJts, true);
//			
//			resetResults(lfg);
//			resetAddressSearch(addressSearchField);
//
//			LMarker lm = new LMarker(startJts);
//			lm.setCaption("<b>Aktuelle Position:</b><br>" +startJts);
//			lfg.addComponent(lm);
//			startCalculation(startJts);
//			leafletMap.zoomToContent();
//			
//		});
		
		grid = new Table(); //new Grid(6, 1);
		// grid.setSpacing(true); TODO

		overallRatingLabel = new Span("");
		
		locTabs = new TabSheet();
		{
			HorizontalLayout t = new HorizontalLayout(addressSearchField);
			t.setHeight(50, Unit.PIXELS);
			t.setWidth(100, Unit.PERCENTAGE);
			locTabs.add("Adresse", t);
			Alignment alignment = Alignment.START;//     TOP_LEFT;
			t.setVerticalComponentAlignment(alignment, addressSearchField);
		}
		locTabs.add("Lon, Lat", new HorizontalLayout(lonLatTf, toLonLatButton));
		
		
		progress = new ProgressBar();
		progress.setVisible(false);
		progress.setIndeterminate(true);
		
		HorizontalLayout topHl = new HorizontalLayout(title, logoutButton(),  progress);
		
		boolean isAdmin = true;
		if (isAdmin)
		{
			Button exportJSONButton = new Button("JSON");
			//exportJSONButton.addStyleName(ValoTheme.BUTTON_LINK); // TODO
			exportJSONButton.addClickListener(l -> {
				exportCurrentAsJSON();
			});
			
			topHl.add(exportJSONButton);
		}
		
		VerticalLayout vl = new VerticalLayout(topHl, profileCombo, locTabs, new Label(""), grid, overallRatingLabel);
		vl.setSizeUndefined();
//		Panel panel = new Panel();
//		panel.setContent(vl);
//		panel.setSizeFull();
		SplitLayout hl = new SplitLayout(vl, mapContainer);
		//hl.setSplitPosition(60, Unit.PERCENTAGE);
		hl.setSplitterPosition(60);
		hl.setSizeFull();
		add(hl);
//		Styles styles = Page.getCurrent().getStyles(); TODO
//		styles.add(".autocomplete-suggestion-list-wrapper{\n" + 
//				"position: fixed;\n" + 
//				"}");

	}

	private LLatLng latLng(LComponentManagementRegistry reg, Point start) {
		return new LLatLng(reg, start.getY(), start.getX());
	}

	private void exportCurrentAsJSON() {
		if (this.currentProfile==null || currentAddress==null) {
			new InfoDialog("Fehler", "Für diese Funktion muss eine Adresse und ein Profil ausgewählt sein").show();
			return;
		}
		try {
			QEXBatchInput input = new QEXBatchInput();
			input.profile = null;
			input.customProfile = new ExportProfile().map(this.currentProfile);

			{
				QEXBatchSourceLocation source = new QEXBatchSourceLocation();
				source.id = 1;
				source.name = currentAddress.name;
				source.lon = currentAddress.geom.getX(); 
				source.lat = currentAddress.geom.getY();

				input.sources.add(source);
			}

			PrettyPrinter pp = new DefaultPrettyPrinter();
			String json = new ObjectMapper().setDefaultPrettyPrinter(pp).enable(SerializationFeature.INDENT_OUTPUT).writeValueAsString(input);
			
			StringBuilder bashScript = new StringBuilder();
			
			bashScript.append("#!/bin/bash\n");
			bashScript.append("cat > topost.json <<- EOM\n");
			bashScript.append(json);
			bashScript.append("\n");
			bashScript.append("EOM\n");
			bashScript.append("\n");
			bashScript.append("curl -X POST -H \"Content-Type: application/json\" -d @topost.json \"http://SERVER:PORT/qopwebui/batchcalculation_servlet?username=USERNAME&password=PASSWORD\"\n");
			
			
			InputStreamFactory isf = new InputStreamFactory() {
				private static final long serialVersionUID = 1L;

				@Override
				public InputStream createInputStream() {
					try {
						return new ByteArrayInputStream(bashScript.toString().getBytes("UTF-8"));
					} catch (UnsupportedEncodingException e) {
						throw new RuntimeException(e);
					}
				}
			};
			
			StreamResource resource = new StreamResource("export_" + this.currentProfile + "_at_" + this.currentAddress + ".sh", isf);
			DownloadDialog dd = new DownloadDialog("Downlaod JSON", "CurrentRequestAsScript", resource);
			dd.show();
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}

	protected void resetAddressSearch(ComboBox<String> addressSearchField) {
		addressSearchField.clear();
	}

	protected void resetResults(LFeatureGroup lfg) {
//		lfg.removeAllComponents(); TODO
//		lfgResults.removeAllComponents();
	}

	private void setLonLat(Point point, boolean switchTab)
	{
		lonLatTf.setValue(point.getCoordinate().x + ", " + point.getCoordinate().y);
		if (switchTab)
		{
			locTabs.setSelectedIndex(1);
		}
	}
	
	private void startCalculationWCatch() {
		try {
			startCalculation();
		} catch (Exception ex)
		{
			new ExceptionDialog("Fehler bei Auswertung:" ,ex).show();;
		}
	}


	//EventRouter evr = new EventRouter();

	public static class SliderChangedEvent extends EventObject {

		private static final long serialVersionUID = 1L;

		public SliderChangedEvent(Object source) {
			super(source);
		}
	}
	
	@FunctionalInterface
	public static interface SliderChangedListener extends Serializable {
	  void sliderChanged(SliderChangedEvent event);
	}

	public void addSliderChangedListener(SliderChangedListener l)
	{
		// TODO
//		try {
//			evr.addListener(SliderChangedEvent.class, l, SliderChangedListener.class.getMethod("sliderChanged", SliderChangedEvent.class));
//		} catch (NoSuchMethodException | SecurityException e) {
//			throw new RuntimeException(e);
//		}
	}
	
	private void startCalculation() {
		if (currentAddress != null)
		{
			Point start = currentAddress.geom;
			setLonLat(start, false);
			startCalculation(start);
		}
	}

	private void startCalculation(Point start) {
		
		progress.setVisible(true);
		clearResults();
		
		final UI currentUI = UI.getCurrent();
		
		if (currentProfile != null)
		{
			ThreadFactory threadFactory = getThreadFactory();	

			Thread t = threadFactory.newThread(() -> {
				try {

					LayerSource source = new DbLayerSource();
					Config cf = Config.read();
					IRouter router = new OSRMClient(cf.getOSRMConf(), Constants.SPLIT_DESTINATIONS_AT);
					Calculation calculation = new Calculation(currentProfile, start, source, router);
					calculation.run();
					currentUI.access(() -> {
						progress.setVisible(false);
						calculationFinished(calculation);
					});
				}
				catch (Throwable ex)
				{
					ex.printStackTrace();
					progress.setVisible(false);
				}
			});
			t.start();
			currentUI.setPollInterval(500);
		}
	}

	protected ThreadFactory getThreadFactory() {
		ThreadFactory threadFactory = Executors.defaultThreadFactory();
		return threadFactory;
	}
	private void calculationFinished(Calculation calculation) {


			clearResults();

			for (CalculationSection<ILayerCalculation> section : calculation.getSections())
			{
				String title = section.getTitle();
				if (title != null && !title.isEmpty())
				{
					var row = grid.addRow();
					row.addCells(new Span("<b><u>" + title + "</u></b>"),
					new Span(""),
					new Span(""),
					new Span(""),
					new Span(""),
					new Span(""));
				}
				gridAddHeaders();

				for (ILayerCalculation ilc :section.lcs)
				{
					LayerCalculation lc = (LayerCalculation)ilc; 
					
					var row = grid.addRow();
					
					row.addCells(new Span(lc.analysis().description));

					if (lc.charts != null && lc.charts.size() > 0)
					{
						HorizontalLayout hl = new HorizontalLayout();

						for (QopChart chart : lc.charts) {
							VaadinIcon icon = VaadinIcon.CHART;

							if (chart instanceof QopPieChart)
							{
								icon = VaadinIcon.PIE_CHART;
							}
							else if (chart instanceof QopPieChart)
							{
								icon = VaadinIcon.BAR_CHART;
							}

							Button chartButton = new Button("", icon.create());
							chartButton.addClickListener(e -> {
								new ChartDialog(lc.analysis().description, "", chart.createChart()).show();
							});
							hl.add(chartButton);
						}

						row.addCells(hl);
					}
					else
					{
						row.addCells(new Span(""));
					}

					if (lc.params.ratingvisible)
					{
						row.addCells(new Span(formatDouble2Decimal(lc.result)));
						row.addCells(new Span(formatDouble2Decimal(lc.rating)));
					}
					else
					{
						row.addCells(new Span(""));
						row.addCells(new Span(""));
					}
					RangeInput slider = new RangeInput();
					slider.setMin(0.);
					slider.setMax(2.);
					slider.setStep(1.);
					slider.setWidth(150, Unit.PIXELS);
					slider.setOrientation(Orientation.HORIZONTAL);
					try {
						slider.setValue(lc.weight);
						slider.addValueChangeListener(l -> {
							lc.weight = slider.getValue();
							refreshOverallRating(calculation);
						});
						row.addCells(slider);
					} catch (Exception e) {
						row.addCells(new Span("Bad Value " + lc.weight));
					}				

					Button button = new Button("Karte >");
					//button.setStyleName(ValoTheme.BUTTON_LINK); // TODO
					button.addClickListener(e -> {

						if (lfgResults != null)
						{
							removeAllComponents(lfgResults);

							lc.keptTargets.stream().filter(lt -> lt instanceof LayerTargetDissolved).map(lt -> {

								LayerTargetDissolved ltd = (LayerTargetDissolved)lt;
								return (LayerTarget) ltd.parent;
							}).distinct().forEach( parent -> {
								if (parent != null)
								{
									LLayer layer = layer(parent.geom);
									lfgResults.addLayer(layer);
								}
							});

							
							lc.keptTargets.stream().forEach(lt -> {
								if (lt.route != null)
								{
									LPolyline lp = layerPoly(lt.route);
									//lp.setStyle(LPathOptions<LPathOptions<S>>)
									//lp.setColor("#ff6020");
									lfgResults.addLayer(lp);
								}
							});

							lc.keptTargets.stream().forEach(lt -> {

								if (lt.geom instanceof Point)
								{
									LMarker lm = layerMarker((Point)lt.geom);
//									lm.addStyleName("specialstyle");
//									lm.setIcon(new ExternalResource("https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-green.png"));
//									lm.setIconAnchor(new org.vaadin.addon.leaflet.shared.Point(12, 41));
									if (lt.caption != null)
									{
										lm.bindPopup(lt.caption);
									}

									lfgResults.addLayer(lm);
								}
								
							});

// TODO:
//							if (lc.analysis().hasRadius()) {
//								LCircle circle = new LCircle(lc.start, lc.analysis().radius);
//								lfgResults.addComponent(circle);
//							}
//							leafletMap.zoomToContent();
						}
					});
					row.addCells(button);

				};
				{
					var row = grid.addRow();
					row.addCells(new Span("<b>Summe</b>"));
					row.addCells(new Span(""));
					row.addCells(new Span(""));
				
				Span sectionRatingLabel = new Span("");
				
				row.addCells(sectionRatingLabel);
				row.addCells(new Span(""));
				row.addCells(new Span(""));
				addSliderChangedListener(e -> {
					sectionRatingLabel.setText("<big><b>" + formatDouble2Decimal(section.rating) + "</b></big>");
				});
				}
			}
			
			addSliderChangedListener(e -> {
				overallRatingLabel.setText("<big><big><b>Gesamtindex: " + formatDouble2Decimal(calculation.getOverallRating()) + "</b></big></big>");
			});
			refreshOverallRating(calculation);
		}

	private LMarker layerMarker(Point geom) {
		// TODO Auto-generated method stub
		return null;
	}

	private LPolyline layerPoly(LineString route) {
		// TODO Auto-generated method stub
		return null;
	}

	private LLayer layer(Geometry geom) {
		// TODO Auto-generated method stub
		return null;
	}

	private void removeAllComponents(LFeatureGroup lfgResults2) {
		// TODO Auto-generated method stub
		
	}

	protected void clearResults() {
		grid.removeAllRows();
		overallRatingLabel.setText("");
	}
	

	public void gridAddHeaders() {
		var head = grid.getHead().addRow();
		head.addCells(new Span("<u>Berechnung</u>")
				,new Span("")
				,new Span("<u>Resultat</u>")
				,new Span("<u>Bewertung</u>")
				,new Span("<u>Gewicht</u>")
				,new Span(""));
	}

	private void refreshOverallRating(Calculation calculation) {
		calculation.runRating();
		// TODO evr.fireEvent(new SliderChangedEvent(this));
	}

	private String formatDouble2Decimal(double d) {
		if (Double.isNaN(d)) return "-";

		return new DecimalFormat("#.##").format(d);
	}

//	@WebServlet(urlPatterns = "/qop/ui/*", name = "QopUIServlet", asyncSupported = true)
//	//@VaadinServletConfiguration(ui = QopUI.class, productionMode = false)
//	@WebInitParam(name = "UI", value = "at.qop.qopwebui.QopUI")
//	public static class QopUIServlet extends VaadinServlet {
//		private static final long serialVersionUID = 1L;
//		
//	}
	
	@Override
	protected boolean requiresAdminRole() {
		return false;
	}
}
