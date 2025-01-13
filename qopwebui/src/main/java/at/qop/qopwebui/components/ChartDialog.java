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

package at.qop.qopwebui.components;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.imageio.ImageIO;

import org.jfree.chart.JFreeChart;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;


public class ChartDialog extends AbstractDialog {
	
	private static final long serialVersionUID = 1L;
	
	private String text;
	private JFreeChart chart;

	public ChartDialog(String title, String message, JFreeChart chart)
	{
		super(title);
		this.setModal(true);
		this.text = message;
		VerticalLayout subContent = new com.vaadin.flow.component.orderedlayout.VerticalLayout();
		this.add(subContent);

		BufferedImage image = chart.createBufferedImage(500, 500);
		ByteArrayOutputStream imagebuffer = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, "png", imagebuffer);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		ByteArrayInputStream bain = new ByteArrayInputStream(
				imagebuffer.toByteArray());

		InputStreamFactory imagesource = new InputStreamFactory() {

			private static final long serialVersionUID = 1L;

			@Override
			public InputStream createInputStream() {
				return bain;
			}

		};

		StreamResource resource =
				new StreamResource( "chart.png", imagesource);

		Image chartImage = new Image(resource, "Chart");
		chartImage.setWidth(480, Unit.PIXELS);
		chartImage.setHeight(480, Unit.PIXELS);
		subContent.add(chartImage);
		//subContent.setExpandRatio(chartImage, 10.0f);
		Button cancelButton = new Button("OK", VaadinIcon.CHECK.create());
		cancelButton.addClickListener(e2 -> {
			this.close(); 
		});
		subContent.add(new HorizontalLayout(cancelButton));

	}

	public static String exception2string(Throwable t) {
		t.printStackTrace();
		StringWriter pw = new StringWriter();
		t.printStackTrace(new PrintWriter(pw));
		return pw.toString();
	}

}
