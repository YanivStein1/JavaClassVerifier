package helloWorld;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * implementation of a custom ClassLoader. When this ClassLoader is used, any
 * request to load a class will pass trough it. if it can load the class, it
 * will, otherwise it will use the super.loadClass() method from ClassLoader,
 * which will eventually pass the request to the classLoader parent. in
 * addition, for each class it is requested to load, this ClassLoader will write
 * the class name to a local file (in JSON format).
 */

public class CustomClassLoader extends ClassLoader {

	public CustomClassLoader(ClassLoader parent) {
		super(parent);
	}

	/**
	 * Loads a given class from .class file just like the default ClassLoader.
	 */
	private Class<?> getClass(String name) throws ClassNotFoundException {
		// We are getting a name that looks like
		// something.package.ClassToLoad
		// and we have to convert it into the .class file name
		// like something/package/ClassToLoad.class
		String file = name.replace('.', File.separatorChar) + ".class";
		byte[] b = null;
		try {
			// This loads the byte code data from the file
			b = loadClassData(file);
			// defineClass is inherited from the ClassLoader class
			// and converts the byte array into a Class
			Class<?> c = defineClass(name, b, 0, b.length);
			resolveClass(c);
			return c;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Every request for a class passes through this method. If the requested class
	 * is in "helloWorld" package, it will load it using the getClass() method. If
	 * not, it will use the super.loadClass() method which in turn will pass the
	 * request to the parent. in addition, for each class it is requested to load,
	 * the class name is written to a local file (JSON format).
	 */
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {

		System.out.println("Custom Class Loader 1 : loading class '" + name + "'");
		JSONParser parser = new JSONParser();
		JSONObject jsonObject = new JSONObject();

		// create a file where allowed classes are saved.
		// this is done to create a profile of allowed classes
		// if the file exists, read the JSON of allowed-classes from it
		try {
			// TODO: create the file in a generic path
			Object obj = parser.parse(new FileReader("/Users/yanivstein/Desktop/aaa/allowedClasses.txt"));
			jsonObject = (JSONObject) obj;
		} catch (IOException | ParseException e1) {
			// e1.printStackTrace();
		}

		// add the requested class to the JSON object and write it to the
		// allowed-classes file
		jsonObject.put(name, "sha1");
		try {
			// TODO: create the file in a generic path
			FileWriter file = new FileWriter("/Users/yanivstein/Desktop/aaa/allowedClasses.txt");
			file.write(jsonObject.toJSONString());
			file.flush();
		} catch (IOException e) {
			// e.printStackTrace();
		}

		// Load The Class (this is a behavior of a regular class loader)
		// if the class is inside SimpleApp package, use getClass() function to load it
		// TODO: make 'simpleApp' a dynamic variable
		if (name.startsWith("SimpleApp.")) {
			return getClass(name);
		}
		// if the class is not in the SimpleApp package, call the next classLoader in
		// the class loader chain
		return super.loadClass(name);
	}

	/**
	 * Loads a given file (.class) into a byte array.
	 */
	private byte[] loadClassData(String name) throws IOException {
		// Opening the file
		InputStream stream = getClass().getClassLoader().getResourceAsStream(name);
		int size = stream.available();
		byte buff[] = new byte[size];
		DataInputStream in = new DataInputStream(stream);
		// Reading the binary data
		in.readFully(buff);
		in.close();
		return buff;
	}

	/**
	 * this function is used to sign class files with SHA-1 signature. this function
	 * is not in use.
	 */
	public byte[] createSha1(File file) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("SHA-1");
		InputStream fis = new FileInputStream(file);
		int n = 0;
		byte[] buffer = new byte[8192];
		while (n != -1) {
			n = fis.read(buffer);
			if (n > 0) {
				digest.update(buffer, 0, n);
			}
		}
		return digest.digest();
	}

}