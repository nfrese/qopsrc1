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
import java.util.Map;
import java.util.function.IntConsumer;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

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
		this.add(subContent);
		
		Dialog panel = new Dialog();
		console = new VerticalLayout();
		panel.setSizeFull();
		
		panel.add(console);
		
		subContent.add(panel);
		//subContent.setExpandRatio(panel, 10.0f); TODO
		closeButton = new Button("Schließen", VaadinIcon.CLOSE.create());
		closeButton.setEnabled(false);
		closeButton.addClickListener(e2 -> {
			onExit.run();
			close();
		});
		
		cancelButton = new Button("Abbruch", VaadinIcon.STOP.create());
		cancelButton.addClickListener(e2 -> {
			cancel();
		});
		okButton = new Button(okButtonText(), okButtonSymbol().create());
		okButton.setEnabled(false);
		okButton.addClickListener(e2 -> {
			onOK.accept(exit);
			close(); 
		});

		this.setCloseOnEsc(false);
		this.setCloseOnOutsideClick(false);
		UI.getCurrent().setPollInterval(1000);
		hlButtons = new HorizontalLayout(cancelButton, closeButton, okButton);
		subContent.add(hlButtons);

	}

	protected VaadinIcon okButtonSymbol() {
		return VaadinIcon.CHECK;
	}

	protected String okButtonText() {
		return "OK";
	}
	
	public void executeCommand(String command, Map<String, String> addEnv, File dir) {
		executeCommands(Arrays.asList(command).iterator(), addEnv, dir, false);
	}
	
	public void executeCommands(Iterator<String> cmdIt, Map<String, String> addEnv, File dir) {
		executeCommands(cmdIt, addEnv, dir, false);
	}
	
	public void executeCommands(Iterator<String> cmdIt, Map<String, String> addEnv, File dir, boolean keepOn) {
		_executeCommands(cmdIt, addEnv, dir, keepOn, new ExitCode());
	}
	
	private static class ExitCode { int value = 0; }
	
	private void _executeCommands(Iterator<String> cmdIt, Map<String, String> addEnv, File dir, boolean keepOn, ExitCode lastFailedExitCode) {
		
		if (cmdIt.hasNext())
		{
			executeCommand(cmdIt.next(), addEnv, dir, 
					(exit) -> {
						if (exit != 0)
						{
							lastFailedExitCode.value = exit;
						}
						
						if (exit == 0 || keepOn)
						{
							getUI().get().access(() -> {
								_executeCommands(cmdIt, addEnv, dir, keepOn, lastFailedExitCode);
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
			done(lastFailedExitCode.value);
		}
	}
	
	String lastLine = null;
	int duplicateCount = 0;
	
	
    public Process exec(String[] cmdarray, Map<String, String> addEnv, File dir)
            throws IOException {
    		ProcessBuilder pb = new ProcessBuilder(cmdarray)
                .directory(dir)
                ;
    		if (addEnv != null)
    		{
    			pb.environment().putAll(addEnv);
    		}
            return pb.start();
        }
	
	public void executeCommand(String command, Map<String, String> addEnv, File dir, IntConsumer singleDone) {

		console.add(new Span("<b>" + command + "</b>"));
		
		try {
			String[] carr = new String[] {"bash", "-c", command};
			p = exec(carr, addEnv, dir);
			
			Thread ot = new Thread(new StreamGobbler(p.getInputStream()) {

				@Override
				protected void print(String line) {
					if (!line.equals(lastLine))
					{
						if (duplicateCount > 1)
						{
							getUI().get().access(() -> {
								console.add(new Span(lastLine + " repeated " + duplicateCount + " times"));
							});
						}
						
						lastLine = line;
						duplicateCount = 1;
						getUI().get().access(() -> {
							console.add(new Span(line));
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
					getUI().get().access(() -> {
						Span label = new Span("<div style=\"color:red;\">" + line + "</div>");
						console.add(label);
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
		getUI().get().access(() -> {
			okButton.setEnabled(exit == 0);
			cancelButton.setEnabled(false);
			closeButton.setEnabled(true);
			getUI().get().setPollInterval(-1);
			this.exit = exit;
			onDone.accept(exit);
		});
	}
	
	public void cancel()
	{
		p.destroy();
	}
	
}
