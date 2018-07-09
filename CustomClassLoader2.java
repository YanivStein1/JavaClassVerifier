package helloWorld;

import java.io.DataInputStream;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import javax.management.RuntimeErrorException;

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
 * addition, for each class it is requested to load, this ClassLoader check that
 * this class is a part of the allowed-classes profile created by the second
 * class loader.
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
	 * the class name is being validated against the allowed-classes profile.
	 */
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		System.out.println("Custom Class Loader 2 : validate class '" + name + "'");
		JSONParser parser = new JSONParser();
		JSONObject jsonObject = new JSONObject();

		// read allowed classes JSON from file
		try {
			Object obj = parser.parse(new FileReader("/Users/yanivstein/Desktop/aaa/allowedClasses.txt"));
			jsonObject = (JSONObject) obj;
		} catch (IOException | ParseException e1) {
			e1.printStackTrace();
		}

		// validate if the called class is found in allowedClasses.txt
		String classSignature = (String) jsonObject.get(name);
		if (classSignature != null) {
			System.out.println("Custom Class Loader 2 : class '" + name + " is approved!'");
			// check the signature of this class
		} else {
			throw new RuntimeErrorException(null, "The class " + name + " is not approved by the profile!");
		}

		// TODO: make 'simpleApp' a dynamic variable
		if (name.startsWith("SimpleApp.")) {
			return getClass(name);
		}
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
}