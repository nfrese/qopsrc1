package at.qop.qopwebui.components;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.IntConsumer;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public class ExecDialog extends AbstractDialog {
	
	private abstract static class StreamGobbler implements Runnable {
		  private final BufferedReader inputStream;
		 
		  public StreamGobbler(InputStream inputStream) {
		    this.inputStream = new BufferedReader(new InputStreamReader(inputStream));
		  }
		 
		  public void run() {
		    try {
		      String line;
		      while ((line = inputStream.readLine()) != null)
		      {
		          print(line);
		      }
		    } catch (IOException e) {
		      throw new RuntimeException(e);
		    }
		  }

		  protected abstract void print(String line);
		}
	
	/////
	
	public IntConsumer onDone = (exit) -> {};
	public IntConsumer onOK = (exit) -> {};
	public Runnable onExit = () -> {};
	
	private static final long serialVersionUID = 1L;
	
	protected VerticalLayout console;
	private Process p;
	
	HorizontalLayout hlButtons;
	Button okButton;
	Button cancelButton;
	private int exit;
	private Button closeButton;
	
	public ExecDialog(String title)
	{
		super(title);
		this.setModal(true);
		this.setWidth(640, Unit.PIXELS);
		this.setHeight(480, Unit.PIXELS);
		VerticalLayout subContent = new VerticalLayout();
		subContent.setSizeFull();
		this.setContent(subContent);
		
		Panel panel = new Panel();
		console = new VerticalLayout();
		panel.setSizeFull();
		
		panel.setContent(console);
		
		subContent.addComponent(panel);
		subContent.setExpandRatio(panel, 10.0f);
		closeButton = new Button("Schließen", VaadinIcons.CLOSE);
		closeButton.setEnabled(false);
		closeButton.addClickListener(e2 -> {
			onExit.run();
			close();
		});
		
		cancelButton = new Button("Abbruch", VaadinIcons.STOP);
		cancelButton.addClickListener(e2 -> {
			cancel();
		});
		okButton = new Button("OK", VaadinIcons.CHECK);
		okButton.setEnabled(false);
		okButton.addClickListener(e2 -> {
			onOK.accept(exit);
			close(); 
		});

		this.setClosable(false);
		UI.getCurrent().setPollInterval(1000);
		hlButtons = new HorizontalLayout(cancelButton, closeButton, okButton);
		subContent.addComponent(hlButtons);

	}

	public void executeCommand(String command, String[] envp, File dir) {

		console.addComponent(new Label("<b>" + command + "</b>", ContentMode.HTML));
		
		try {
			String[] carr = new String[] {"bash", "-c", command};
			p = Runtime.getRuntime().exec(carr, envp, dir);
			
			Thread ot = new Thread(new StreamGobbler(p.getInputStream()) {

				@Override
				protected void print(String line) {
					getUI().access(() -> {
						console.addComponent(new Label(line));
					});
				}
				
			});
			ot.start();
			
			Thread et = new Thread(new StreamGobbler(p.getErrorStream()) {

				@Override
				protected void print(String line) {
					getUI().access(() -> {
						Label label = new Label("<div style=\"color:red;\">" + line + "</div>", ContentMode.HTML);
						console.addComponent(label);
					});
				}
				
			});
			
			et.start();
			
			Runnable finishCmd = () -> { 
				try {
					int exit = p.waitFor();
					done(exit);
				} catch (InterruptedException e) {
					done(Integer.MIN_VALUE);
					e.printStackTrace();
				} 
			};

			new Thread(finishCmd).start();

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	protected void done(int exit) {
		getUI().access(() -> {
			okButton.setEnabled(exit == 0);
			cancelButton.setEnabled(false);
			closeButton.setEnabled(true);
			getUI().setPollInterval(-1);
			this.exit = exit;
			onDone.accept(exit);
		});
	}
	
	public void cancel()
	{
		p.destroy();
	}
	
}
