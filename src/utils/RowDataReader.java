package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import utils.TextUtils;

/**
 * Given a file path and read data by line and return it throw callback
 * @author liuxiaohui
 */
public class RowDataReader {
	
	public interface ReaderCallback {
		/**
		 * Callback method when read to row
		 * @param rowIndex
		 * @param rowData
		 * @return true if reader should continue to read next line
		 * 		   false if reader should stop reading
		 */
		boolean onReadRow(int rowIndex, String rowData);
	}
	
	public static void read(String filePath, ReaderCallback callback) {
		if (TextUtils.isEmpty(filePath) || callback == null) {
			System.err.println("Empty file path to read or empty callback!");
			return;
		}
		read(new File(filePath), callback);
	}

	public static void read(File file, ReaderCallback callback) {
		if (file == null || !file.exists() || callback == null) {
			System.err.println("Invalid faile to read or empty callback!");
			return;
		}
		
		FileInputStream fileInputStream = null;
		InputStreamReader inputStreamReader = null;
		BufferedReader bufferedReader = null;
		try {
			fileInputStream = new FileInputStream(file);
			inputStreamReader = new InputStreamReader(fileInputStream, "utf-8");
			bufferedReader = new BufferedReader(inputStreamReader);

			String line = null;
			int rowIndex = 0;
			while ((line = bufferedReader.readLine()) != null) {
				if (!callback.onReadRow(rowIndex, line)) {
					break;
				}
				rowIndex++;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.err.println("Something wrong happened while reading file " + file.getPath());
			e.printStackTrace();
		} finally {
			try {
				bufferedReader.close();
				inputStreamReader.close();
				fileInputStream.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
}
