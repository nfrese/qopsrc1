package at.qop.qopwebui.admin;

import com.vaadin.server.Page;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import at.qop.qoplib.LookupSessionBeans;
import at.qop.qoplib.dbconnector.metadata.QopDBColumn;
import at.qop.qoplib.dbconnector.metadata.QopDBMetadata;
import at.qop.qoplib.dbconnector.metadata.QopDBTable;
import at.qop.qoplib.domains.IGenericDomain;

public class LayersTab extends AbstractTab {

	@Override
	public Component initialize(Page page) {
		StringBuilder html = new StringBuilder();

		IGenericDomain gd = LookupSessionBeans.genericDomain();

		QopDBMetadata meta = gd.getMetadata();
		for (QopDBTable table : meta.tables)
		{
			if (table.isGeometric())
			{
				html.append("<p>" + table.name + "</p>");
				for (QopDBColumn column : table.columns)
				{
					html.append("<ul>" + column.name + "(" + column.typename + ")</ul>");
				}
			}
		}
    	
		final Label label = new Label(html.toString(), ContentMode.HTML);
        label.setWidth(100.0f, Unit.PERCENTAGE);

        final VerticalLayout vl = new VerticalLayout(label);
        vl.setMargin(true);
        return vl;
	}

}
