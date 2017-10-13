package extractor;


public class Main {
	/** Configuration area */
	/** The resource folder should contains two children folders,
	 *  which are: 1. image, 2.label
	 *  You should store images under folder image, and tag info into folder label*/
	public static final String RESOURCE_FOLDER_PATH = "";
	public static final String EXTRACT_TAG_LIST_FILE_PATH = "";
	public static final String EXPORT_FOLDER_PATH = "";
	
	public static void main(String[] args) {
		System.out.println(String.format("Start extracting from [%s] ...", RESOURCE_FOLDER_PATH));
		try {
			boolean result = TagImageExtractor.doExtract(RESOURCE_FOLDER_PATH, 
					EXPORT_FOLDER_PATH, EXTRACT_TAG_LIST_FILE_PATH);
			System.out.println(String.format("Done export to [%s], result %s", EXPORT_FOLDER_PATH, String.valueOf(result)));
		} catch (Exception e) {
			System.err.println("Failed extracting images");
			e.printStackTrace(System.err);
		}
	}
}
