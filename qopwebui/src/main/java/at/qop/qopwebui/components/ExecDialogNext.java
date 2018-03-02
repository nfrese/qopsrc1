package at.qop.qopwebui.components;

import com.vaadin.icons.VaadinIcons;

public class ExecDialogNext extends ExecDialog {

	private static final long serialVersionUID = 1L;
	
	public ExecDialogNext(String title) {
		super(title);
	}

	@Override
	protected VaadinIcons okButtonSymbol() {
		return VaadinIcons.ARROW_RIGHT;
	}

	@Override
	protected String okButtonText() {
		return "Weiter";
	}

}
