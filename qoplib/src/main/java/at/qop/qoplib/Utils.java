package at.qop.qoplib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Utils {
	
	@SuppressWarnings("unchecked")
	public static <T> T deepClone(T obj) throws IOException, ClassNotFoundException
	{
		ByteArrayOutputStream baOut = new ByteArrayOutputStream();
		ObjectOutputStream objOut = new ObjectOutputStream(baOut);
		objOut.writeObject(obj);
		objOut.flush();
		objOut.close();
		baOut.close();
		byte[] bytes = baOut.toByteArray();

		ByteArrayInputStream baIn = new ByteArrayInputStream(bytes);
		return (T) new ObjectInputStream(baIn).readObject();		
	}

}
