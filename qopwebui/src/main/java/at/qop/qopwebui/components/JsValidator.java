package at.qop.qopwebui.components;

import com.vaadin.data.ValidationResult;
import com.vaadin.data.Validator;
import com.vaadin.data.ValueContext;

import at.qop.qoplib.JSValidation;

public class JsValidator implements Validator<String> {

	private static final long serialVersionUID = 1L;

	@Override
	public ValidationResult apply(String js, ValueContext context) {
		 if(JSValidation.check(js)) {
	            return ValidationResult.ok();
	        } else {
	            return ValidationResult.error(
	            		"Ungültiges JavaScript: " + JSValidation.getMessage(js));
	     }
	}

}
