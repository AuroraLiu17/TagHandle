package utils;

import java.io.Closeable;

public class MiscUtils {
	public static void closeSilently(Closeable closeable) {
		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (Exception e) {
			System.err.println("Error occurred when closing closable");
			e.printStackTrace();
		}
	}
}
