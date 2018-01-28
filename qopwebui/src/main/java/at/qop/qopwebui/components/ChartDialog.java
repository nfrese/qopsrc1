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

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.VerticalLayout;

public class ChartDialog extends AbstractDialog {
	
	private static final long serialVersionUID = 1L;
	
	private String text;
	private JFreeChart chart;

	public ChartDialog(String title, String message, JFreeChart chart)
	{
		super(title);
		this.setModal(true);
		this.text = message;
		VerticalLayout subContent = new VerticalLayout();
		this.setContent(subContent);

		BufferedImage image = chart.createBufferedImage(500, 500);
		ByteArrayOutputStream imagebuffer = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, "png", imagebuffer);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		ByteArrayInputStream bain = new ByteArrayInputStream(
				imagebuffer.toByteArray());

		StreamSource imagesource = new StreamSource() {

			private static final long serialVersionUID = 1L;

			@Override
			public InputStream getStream() {
				return bain;
			}

		};

		StreamResource resource =
				new StreamResource(imagesource, "chart.png");

		Image chartImage = new Image("Chart", resource);
		chartImage.setWidth(480, Unit.PIXELS);
		chartImage.setHeight(480, Unit.PIXELS);
		subContent.addComponent(chartImage);
		subContent.setExpandRatio(chartImage, 10.0f);
		Button cancelButton = new Button("OK", VaadinIcons.CHECK);
		cancelButton.addClickListener(e2 -> {
			this.close(); 
		});
		subContent.addComponent(new HorizontalLayout(cancelButton));

	}

	public static String exception2string(Throwable t) {
		t.printStackTrace();
		StringWriter pw = new StringWriter();
		t.printStackTrace(new PrintWriter(pw));
		return pw.toString();
	}

}
