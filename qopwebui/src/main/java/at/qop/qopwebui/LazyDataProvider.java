package at.qop.qopwebui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Stream;

import com.vaadin.data.provider.DataChangeEvent;
import com.vaadin.data.provider.DataProviderListener;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.UI;

import at.qop.qoplib.addresses.AddressLookup;
import at.qop.qoplib.entities.Address;

public class LazyDataProvider extends ListDataProvider<Address> {

	private static Collection<Address> addresses = new ArrayList<>();
	final SearchComboBox<Address> box;
	final AddressLookup addressService;
	final ThreadFactory threadFactory;
	private UI currentUI = UI.getCurrent();
	
	private String lastFilter = null;
	
	public LazyDataProvider(SearchComboBox<Address> box, ThreadFactory threadFactory, AddressLookup addressService) {
		super(addresses);
		this.box = box;
		this.addressService = addressService;
		this.threadFactory = threadFactory;
		addDataProviderListener(new DataProviderListener<Address>() {
			
			@Override
			public void onDataChange(DataChangeEvent<Address> event) {
				box.setDatasourceUpdate();
				System.err.println("listener!");
				
			}
		});
	}

	private static final long serialVersionUID = 1L;

	@Override
	public Stream<Address> fetch(Query<Address, SerializablePredicate<Address>> query) {
		lazyLoad();
		return super.getItems().stream();
	}

	private void lazyLoad() {
		
		final String filter = box.currentFilterText();
		if (filter == lastFilter) return;
		lastFilter = filter;
		
		Thread t = threadFactory.newThread(() -> {
			try {
			System.out.println("start *async* " + filter);
			List<Address> addresses = addressService.fetchAddresses(0, 100, filter);
			
			System.out.println("found * async* " + addresses.size());
			
			currentUI .access(() -> {
					super.getItems().clear();
					super.getItems().addAll(addresses);
					refreshAll();
				}
				
			);}
			
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		});
		
		t.start();
		
		
	}
	@Override
	public void setFilter(SerializablePredicate<Address> filter) {
		super.setFilter(filter);
	}
	
	
	
	
}
