package edu.txst.midterm;

/**
 * Strategy interface for loading a {@link Board} from an external resource
 * such as a CSV file.
 */
public interface BoardLoader {
	/**
	 * Loads a board from the given file name.
	 * 
	 * @param filename the path to the level file
	 * @return a populated {@link Board} instance, or {@code null} if loading
	 *         fails
	 */
	Board load(String filename);
}
