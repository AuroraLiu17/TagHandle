package extractor;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import utils.FileUtils;
import utils.RowDataReader;
import utils.RowDataReader.ReaderCallback;
import utils.TextUtils;

public class TagHandleHelper {
	
	private static final String RESOURCE_IMAGE_FOLDER_NAME = "image";
	private static final String RESOURCE_TAG_FOLDER_NAME = "label";
	private static final String RESOURCE_TAG_FILE_EXTENTION = ".tag";
	private static final String EXPORT_EXCEL_FILE_NAME = "test_user.xls";
	
	public static boolean doExtract(String resourceFolderPath, 
			String exportFolderPath, String tagListFilePath) throws Exception {
		Map<String, Integer> tags = readTags(FileUtils.getFile(tagListFilePath, true));
		if (tags == null || tags.isEmpty()) {
			System.out.println("Nothing to extract, empty tag list");
			return false;
		}
		System.out.println("On extract tags " + tags.toString());
		if (!resourceFolderPath.endsWith("/")) {
			resourceFolderPath += "/";
		}
		if (!exportFolderPath.endsWith("/")) {
			exportFolderPath += "/";
		}
		String resourceImageFolderPath = resourceFolderPath + RESOURCE_IMAGE_FOLDER_NAME;
		String resourceTagFolderPath = resourceFolderPath + RESOURCE_TAG_FOLDER_NAME;
		File resourceImageFolder = FileUtils.getFolder(resourceImageFolderPath, true);
		File resourceTagFolder = FileUtils.getFolder(resourceTagFolderPath, true);
		extractFromFolder(tags, resourceImageFolder, resourceTagFolder, exportFolderPath);
		System.out.println("Done extracting, result:" + tags.toString());
		return true;
	}
	
	private static final int COLUMN_INDEX_IMAGE_FILE_NAME = 0;
	private static final int COLUMN_INDEX_CREATE_TIME = 1;
	private static final int COLUMN_INDEX_GPS = 2;
	private static final int COLUMN_INDEX_LOCATION = 3;
	private static final int COLUMN_INDEX_TAG_INFO = 4;
	
	public static boolean generateExcel(String resourceFolderPath) 
			throws Exception {
		String resourceImageFolderPath = resourceFolderPath + "/" + RESOURCE_IMAGE_FOLDER_NAME;
		String resourceTagFolderPath = resourceFolderPath + "/" + RESOURCE_TAG_FOLDER_NAME;
		File resourceImageFolder = FileUtils.getFolder(resourceImageFolderPath, true);
		File resourceTagFolder = FileUtils.getFolder(resourceTagFolderPath, true);
		
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("sheet1");
		
		int rowNum = 0;
		for (File imageFile : resourceImageFolder.listFiles()) {
			if (imageFile.isDirectory()) {
				continue;
			}
			File tagFile = getTagFileByImageFile(imageFile, resourceTagFolder);
			if (tagFile == null) {
				System.out.println("Invalid tag file " + imageFile.getAbsolutePath());
				continue;
			}
			HSSFRow row = sheet.createRow(rowNum);
			row.createCell(COLUMN_INDEX_IMAGE_FILE_NAME, CellType.STRING).setCellValue(imageFile.getName());
			String tagInfo = formatTagFile(FileUtils.readFileToString(tagFile));
			row.createCell(COLUMN_INDEX_TAG_INFO, CellType.STRING).setCellValue(tagInfo);;
			rowNum++;
		}
		
		File exportFile = new File(resourceFolderPath, EXPORT_EXCEL_FILE_NAME);
		workbook.write(exportFile);
		workbook.close();
		return true;
	}
	
	private static void extractFromFolder(Map<String, Integer> extractTags,
			File resourceImageFolder, File resourceTagFolder, String exportFolderPath) {
		System.out.println(String.format("Extract from [%s], [%s] to [%s]", resourceImageFolder.getAbsolutePath(), resourceTagFolder.getAbsolutePath(), exportFolderPath));
		for (File imageFile : resourceImageFolder.listFiles()) {
			if (imageFile.isDirectory()) {
				File subTagFolder = new File(resourceTagFolder, imageFile.getName());
				extractFromFolder(extractTags, imageFile, subTagFolder, exportFolderPath);
				continue;
			}
			File tagFile = getTagFileByImageFile(imageFile, resourceTagFolder);
			if (tagFile == null) {
				System.out.println("Invalid tag file " + imageFile.getAbsolutePath());
				continue;
			}
			List<String> imageTags = readImageTagFile(tagFile);
			if (imageTags == null || imageTags.size() <= 0) {
				continue;
			}
			
			for (String imageTag : imageTags) {
				boolean copied = false;
				Integer count = extractTags.get(imageTag);
				if (count == null) {
					continue;
				}
				if (!copied) {
					System.out.println(String.format("Hit image [%s]", imageFile.getAbsolutePath()));
					FileUtils.copyFile(imageFile, new File(exportFolderPath + RESOURCE_IMAGE_FOLDER_NAME, imageFile.getName()));
					FileUtils.copyFile(tagFile, new File(exportFolderPath + RESOURCE_TAG_FOLDER_NAME, tagFile.getName()));
					copied = true;
				}
				extractTags.put(imageTag, count + 1);
			}
		}
	}
	
	private static File getTagFileByImageFile(File imageFile, File tagParentFolder) {
		String tagFileName = getTagFileNameByImageName(imageFile.getName());
		if (TextUtils.isEmpty(tagFileName)) {
			System.out.println("Invalid tag file name");
			return null;
		}
		File tagFile = new File(tagParentFolder, tagFileName);
		return tagFile.exists() ? tagFile : null;
	}
	
	private static String getTagFileNameByImageName(String imageName) {
		if (TextUtils.isEmpty(imageName)) {
			return null;
		}
		String fileName = FileUtils.getFileNameWithoutExtension(imageName);
		String extension = FileUtils.getExtension(imageName);
		if (!TextUtils.isEmpty(extension)) {
			return fileName + "_" + extension + RESOURCE_TAG_FILE_EXTENTION;
		} else {
			return fileName + RESOURCE_TAG_FILE_EXTENTION;
		}
	}
	
	private static String formatTagFile(String data) {
		if (!TextUtils.isEmpty(data)) {
			data = data.replace("#", "");
			data = data.replace("@", "");
		}
		return data;
	}
	
	private static final Gson S_GSON = new Gson();
	private static List<String> readImageTagFile(File tagFile) {
		try {
			String jsonData = formatTagFile(FileUtils.readFileToString(tagFile));
			Type type = new TypeToken<Map<String, String>>() {}.getType();  
		    Map<String, String> tagMap = S_GSON.fromJson(jsonData, type);
		    if (tagMap == null || tagMap.isEmpty()) {
				return null;
			}

			final List<String> tags = new ArrayList<>();
			for (String tagInfo : tagMap.values()) {
				if (!TextUtils.isEmpty(tagInfo)) {
					String[] subTagInfo = tagInfo.split(",");
					if (subTagInfo != null) {
						for (String subTag : subTagInfo) {
							tags.add(subTag);
						}
					}
				}
			}
			return tags;
		} catch (Exception e) {
			System.err.println("Failed read image tags");
			e.printStackTrace();
			return null;
		}
	}
	
	private static Map<String, Integer> readTags(File tagListFile) {
		if (tagListFile == null) {
			return null;
		}
		
		final Map<String, Integer> tags = new HashMap<>();
		ReaderCallback callback = new ReaderCallback() {
			@Override
			public boolean onReadRow(int rowIndex, String rowData) {
				if (!TextUtils.isEmpty(rowData)) {
					if (rowIndex <= 1 && rowData.startsWith("\uFEFF")) {
						rowData = rowData.substring(1);
					}
					tags.put(rowData.trim(), 0);
				}
				return true;
			}
		};
		RowDataReader.read(tagListFile, callback);
		return tags;
	}
}
