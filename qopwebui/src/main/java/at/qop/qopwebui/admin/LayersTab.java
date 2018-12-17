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
