package at.qop.qopwebui;

import java.util.Collection;

import com.vaadin.data.provider.DataCommunicator;
import com.vaadin.ui.ComboBox;

public class SearchComboBox<T> extends ComboBox<T> {

	public SearchComboBox() {
		super();
	}

	public SearchComboBox(DataCommunicator<T> dataCommunicator) {
		super(dataCommunicator);
	}

	public SearchComboBox(String caption, Collection<T> options) {
		super(caption, options);
		
	}

	public SearchComboBox(String caption) {
		super(caption);
	}
	
	public String currentFilterText()
	{
		return this.getState().currentFilterText;
	}
	
	public void setDatasourceUpdate()
	{
		getState().forceDataSourceUpdate = true;
	}
	
	

}
