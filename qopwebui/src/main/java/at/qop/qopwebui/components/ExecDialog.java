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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Iterator;
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
		okButton = new Button(okButtonText(), okButtonSymbol());
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

	protected VaadinIcons okButtonSymbol() {
		return VaadinIcons.CHECK;
	}

	protected String okButtonText() {
		return "OK";
	}
	
	public void executeCommand(String command, String[] envp, File dir) {
		executeCommands(Arrays.asList(command).iterator(), envp, dir);
	}
	
	public void executeCommands(Iterator<String> cmdIt, String[] envp, File dir) {
		
		if (cmdIt.hasNext())
		{
			executeCommand(cmdIt.next(), envp, dir, 
					(exit) -> {
						if (exit == 0)
						{
							getUI().access(() -> {
								executeCommands(cmdIt, envp, dir);
							});
						}
						else
						{
							done(exit);
						}
					}
					);
		}
		else
		{
			done(0);
		}
	}
	
	String lastLine = null;
	int duplicateCount = 0;
	
	public void executeCommand(String command, String[] envp, File dir, IntConsumer singleDone) {

		console.addComponent(new Label("<b>" + command + "</b>", ContentMode.HTML));
		
		try {
			String[] carr = new String[] {"bash", "-c", command};
			p = Runtime.getRuntime().exec(carr, envp, dir);
			
			Thread ot = new Thread(new StreamGobbler(p.getInputStream()) {

				@Override
				protected void print(String line) {
					if (!line.equals(lastLine))
					{
						if (duplicateCount > 1)
						{
							getUI().access(() -> {
								console.addComponent(new Label(lastLine + " repeated " + duplicateCount + " times"));
							});
						}
						
						lastLine = line;
						duplicateCount = 1;
						getUI().access(() -> {
							console.addComponent(new Label(line));
						});
					}
					else
					{
						duplicateCount++;
					}
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
					singleDone.accept(exit);
				} catch (InterruptedException e) {
					singleDone.accept(Integer.MIN_VALUE);
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
