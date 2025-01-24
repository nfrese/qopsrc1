package at.qop.ws;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.fasterxml.jackson.databind.node.ObjectNode;

@ControllerAdvice
public class QOPRestApiExceptions extends QOPRestApiBase {
	
	@ExceptionHandler(Throwable.class)
	public ResponseEntity<String> handleError(HttpServletRequest req, Throwable ex) throws IOException {
		String request = req.getRequestURL().toString();
		String stacktraceToStr = stacktraceToStr(ex);
		System.err.println("request: " + request + " failed with: "  + stacktraceToStr);

		ObjectNode on = om().createObjectNode();
		on.put("success", false);
		on.put("message", ex.getMessage());
		on.put("stackTrace", stacktraceToStr);

		return ResponseEntity.internalServerError()
				.contentType(MediaType.APPLICATION_JSON)
				.body(on+"");
	}

	public static String stacktraceToStr(Throwable e) {
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		pw.close();
		final String stt = sw.toString();
		return stt;
	}

}
