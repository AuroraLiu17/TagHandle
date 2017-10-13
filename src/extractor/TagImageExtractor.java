package extractor;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import utils.FileUtils;
import utils.RowDataReader;
import utils.RowDataReader.ReaderCallback;
import utils.TextUtils;

public class TagImageExtractor {
	
	private static final String RESOURCE_IMAGE_FOLDER_NAME = "image";
	private static final String RESOURCE_TAG_FOLDER_NAME = "label";
	private static final String RESOURCE_TAG_FILE_EXTENTION = ".tag";
	
	public static boolean doExtract(String resourceFolderPath, 
			String exportFolderPath, String tagListFilePath) throws Exception {
		List<String> tags = readTags(FileUtils.getFile(tagListFilePath, true));
		if (tags == null || tags.isEmpty()) {
			System.out.println("Nothing to extract, empty tag list");
			return false;
		}
		
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
		return true;
	}
	
	private static void extractFromFolder(List<String> extractTags,
			File resourceImageFolder, File resourceTagFolder, String exportFolderPath) {
		System.out.println(String.format("Extract from [%s], [%s] to [%s]", resourceImageFolder.getAbsolutePath(), resourceTagFolder.getAbsolutePath(), exportFolderPath));
		for (File imageFile : resourceImageFolder.listFiles()) {
			if (imageFile.isDirectory()) {
				File subTagFolder = new File(resourceTagFolder, imageFile.getName());
				extractFromFolder(extractTags, imageFile, subTagFolder, exportFolderPath);
				continue;
			}
			String tagFileName = getTagFileNameByImageName(imageFile.getName());
			if (TextUtils.isEmpty(tagFileName)) {
				System.out.println("Invalid tag file name");
				continue;
			}
			File tagFile = new File(resourceTagFolder, tagFileName);
			List<String> imageTags = readImageTagFile(tagFile);
			if (imageTags == null || imageTags.size() <= 0) {
				continue;
			}
			
			for (String imageTag : imageTags) {
				if (extractTags.contains(imageTag)) {
					System.out.println(String.format("Hit image [%s]", imageFile.getAbsolutePath()));
					FileUtils.copyFile(imageFile, new File(exportFolderPath + RESOURCE_IMAGE_FOLDER_NAME, imageFile.getName()));
					FileUtils.copyFile(tagFile, new File(exportFolderPath + RESOURCE_TAG_FOLDER_NAME, tagFile.getName()));
					break;
				}
			}
		}
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
	
	private static final Gson S_GSON = new Gson();
	private static List<String> readImageTagFile(File tagFile) {
		try {
			String jsonData = FileUtils.readFileToString(tagFile);
			Type type = new TypeToken<Map<String, String>>() {}.getType();  
		    Map<String, String> tagMap = S_GSON.fromJson(jsonData, type);
		    if (tagMap == null || tagMap.isEmpty()) {
				return null;
			}

			final List<String> tags = new ArrayList<>();
			for (String tagInfo : tagMap.values()) {
				if (!TextUtils.isEmpty(tagInfo)) {
					if (tagInfo.endsWith("#") || tagInfo.endsWith("@")) {
						tagInfo = tagInfo.substring(0, tagInfo.length() - 1);
					}
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
	
	private static List<String> readTags(File tagListFile) {
		if (tagListFile == null) {
			return null;
		}
		
		final List<String> tags = new ArrayList<>();
		ReaderCallback callback = new ReaderCallback() {
			@Override
			public boolean onReadRow(int rowIndex, String rowData) {
				if (!TextUtils.isEmpty(rowData)) {
					if (rowIndex <= 1 && rowData.startsWith("\uFEFF")) {
						rowData = rowData.substring(1);
					}
					tags.add(rowData.trim());
				}
				return true;
			}
		};
		RowDataReader.read(tagListFile, callback);
		return tags;
	}
}
