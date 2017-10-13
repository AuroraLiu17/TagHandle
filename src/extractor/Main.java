package extractor;


public class Main {
	/** Configuration area */
	/** The resource folder should contains two children folders,
	 *  which are: 1. image, 2.label
	 *  You should store images under folder image, and tag info into folder label*/
	public static final String RESOURCE_FOLDER_PATH = "D:/Top1000/image_with_label";
	public static final String EXTRACT_TAG_LIST_FILE_PATH = "D:/Top1000/ExtractTags.txt";
	public static final String EXPORT_FOLDER_PATH = "D:/Top1000/top100";
	
	public static void main(String[] args) {
		System.out.println(String.format("Start extracting from [%s] ...", RESOURCE_FOLDER_PATH));
		try {
			long startTime = System.currentTimeMillis();
			boolean result = TagImageExtractor.doExtract(RESOURCE_FOLDER_PATH, 
					EXPORT_FOLDER_PATH, EXTRACT_TAG_LIST_FILE_PATH);
			System.out.println(String.format("Done export to [%s], result %s, cost %ds", EXPORT_FOLDER_PATH, String.valueOf(result), (System.currentTimeMillis() - startTime) / 1000));
		} catch (Exception e) {
			System.err.println("Failed extracting images");
			e.printStackTrace(System.err);
		}
	}
}
