package rw2018.statistics;

import java.io.File;

/**
 * This interface describes the access to the statistics database. The
 * statistics database stores how frequently a resource occurs for each
 * {@link TriplePosition} in each chunk.
 * <table style="border: 1px solid black;border-collapse: collapse;">
 * <caption>Example for 3 resources and 2 chunks.</caption> <thead>
 * <tr style="border: 1px solid black;">
 * <th style="border: 1px solid black;" rowspan="2">Resource Id</th>
 * <th style="border: 1px solid black;" colspan="2">Subject Position</th>
 * <th style="border: 1px solid black;" colspan="2">Property Position</th>
 * <th style="border: 1px solid black;" colspan="2">Object Position</th>
 * </tr>
 * <tr style="border: 1px solid black;">
 * <th style="border: 1px solid black;">Chunk 0</th>
 * <th style="border: 1px solid black;">Chunk 1</th>
 * <th style="border: 1px solid black;">Chunk 0</th>
 * <th style="border: 1px solid black;">Chunk 1</th>
 * <th style="border: 1px solid black;">Chunk 0</th>
 * <th style="border: 1px solid black;">Chunk 1</th>
 * </tr>
 * </thead> <tbody>
 * <tr style="border: 1px solid black;">
 * <td style="border: 1px solid black;"><b>1</b></td>
 * <td style="border: 1px solid black;">2</td>
 * <td style="border: 1px solid black;">1</td>
 * <td style="border: 1px solid black;">0</td>
 * <td style="border: 1px solid black;">0</td>
 * <td style="border: 1px solid black;">1</td>
 * <td style="border: 1px solid black;">0</td>
 * </tr>
 * <tr style="border: 1px solid black;">
 * <td style="border: 1px solid black;"><b>2</b></td>
 * <td style="border: 1px solid black;">0</td>
 * <td style="border: 1px solid black;">0</td>
 * <td style="border: 1px solid black;">2</td>
 * <td style="border: 1px solid black;">0</td>
 * <td style="border: 1px solid black;">0</td>
 * <td style="border: 1px solid black;">0</td>
 * </tr>
 * </tbody>
 * </table>
 * 
 * This means that, e.g., resource 2 occurs twice as a property in chunk 0. This
 * table is just the logical view and does not represent the physical
 * implementation.
 * 
 * @author Daniel Janke &lt;danijankATuni-koblenz.de&gt;
 *
 */
public interface StatisticsDB extends AutoCloseable {

  /**
   * By default the {@link StatisticsDB} stores the triple positions
   * {@link TriplePosition#SUBJECT}, {@link TriplePosition#PROPERTY} and
   * {@link TriplePosition#OBJECT}.
   */
  public static final TriplePosition[] DEFAULT_TRIPLE_POSITIONS = new TriplePosition[] {
          TriplePosition.SUBJECT, TriplePosition.PROPERTY, TriplePosition.OBJECT };

  /**
   * This method is called once during the initialization of the
   * {@link StatisticsDB} instance. It is the first method to be called. It
   * passes arguments that are required for the execution.
   * 
   * @param statisticsDir
   *          {@link File} that represents the directory in which the
   *          statistical information should be persisted.
   * @param numberOfChunks
   *          <code>int</code> the total number of chunks
   */
  public void setUp(File statisticsDir, int numberOfChunks);

  /**
   * @return <code>int</code> the number of chunks.
   */
  public int getNumberOfChunks();

  /**
   * @return {@link TriplePosition}[] that stores the triple positions that are
   *         stored by this {@link StatisticsDB} as well as there ordering as
   *         used by {@link #getFrequencies(long)}.
   */
  public default TriplePosition[] getTriplePositions() {
    return StatisticsDB.DEFAULT_TRIPLE_POSITIONS;
  }

  /**
   * After invoking this method, the frequency of resource
   * <code>resourceId</code> at the triple position <code>triplePosition</code>
   * in chunk <code>chunkNumber</code> is increased by 1.
   * 
   * @param resourceId
   *          <code>long</code> the id of the resource whose occurrence should
   *          be increased. The first resource has id 0.
   * @param chunkNumber
   *          <code>int</code> the number of the chunk in which resource
   *          <code>resourceId</code> occurs. The first chunk has id 0.
   * @param triplePosition
   *          {@link TriplePosition} the position in the triple at which
   *          resource <code>resourceId</code> occurs
   */
  public void incrementFrequency(long resourceId, int chunkNumber, TriplePosition triplePosition);

  /**
   * Returns how frequently resource <code>resourceId</code> occurs at the
   * triple position <code>triplePosition</code> in chunk
   * <code>chunkNumber</code>.
   * 
   * @param resourceId
   *          <code>long</code> the id of the resource whose occurrence
   *          frequency is requested. The first resource has id 0.
   * @param chunkNumber
   *          <code>int</code> the number of the chunk for which the occurrence
   *          of resource <code>resourceId</code> is requested. The first chunk
   *          has id 0.
   * @param triplePosition
   *          {@link TriplePosition} the position in the triple for which the
   *          occurrence of resource <code>resourceId</code> is requested
   * @return <code>long</code> how often resource <code>resourceId</code> occurs
   *         at the triple position <code>triplePosition</code> in chunk
   *         <code>chunkNumber</code>. If the resource is not found,
   *         <code>-1</code> is returned.
   */
  public long getFrequency(long resourceId, int chunkNumber, TriplePosition triplePosition);

  /**
   * Returns how frequently the resource <code>resourceId</code> occurs at the
   * different triple positions in all chunks. The triple positions and there
   * order are defined by {@link #getTriplePositions()}. For instance, in the
   * table listed in the JavaDoc of {@link StatisticsDB}, the method call
   * <code>getFrequencies(1)</code> would return the array
   * <code>[2, 1, 0, 0, 1, 0]</code>. The resource Id is not returned. The first
   * two elements represents the occurrences as subject in chunks 0 and 1,
   * respectively. The second two elements represents the occurrences as
   * property in both chunks and the third two elements represent the
   * occurrences as object in both chunks.
   * 
   * @param resourceId
   *          <code>long</code> the id of the requested resource. The first
   *          resource has id 0.
   * @return <code>long[]</code> how often the resource <code>resourceId</code>
   *         occurs at the different triple positions in all chunks. If the
   *         resource is not found, <code>null</code> is returned.
   */
  public default long[] getFrequencies(long resourceId) {
    int numberOfChunks = getNumberOfChunks();
    TriplePosition[] positions = getTriplePositions();
    long[] resourceRow = new long[positions.length * numberOfChunks];
    for (int posI = 0; posI < positions.length; posI++) {
      for (int chunkI = 0; chunkI < numberOfChunks; chunkI++) {
        resourceRow[(posI * numberOfChunks) + chunkI] = getFrequency(resourceId, chunkI,
                positions[posI]);
      }
    }
    return resourceRow;
  }

  public default String prettyPrint() {
    StringBuilder sb = new StringBuilder();
    sb.append("RessourceId");
    for (TriplePosition pos : getTriplePositions()) {
      for (int chunkI = 0; chunkI < getNumberOfChunks(); chunkI++) {
        sb.append(",").append(pos).append("-chunk").append(chunkI);
      }
    }
    for (int resourceId = 1; true; resourceId++) {
      long[] frequencies = getFrequencies(resourceId);
      if (frequencies == null) {
        break;
      }
      boolean isEmpty = true;
      for (long frequency : frequencies) {
        if (frequency > 0) {
          isEmpty = false;
          break;
        }
      }
      if (isEmpty) {
        continue;
      }
      sb.append("\nresource:").append(resourceId);
      for (long frequency : frequencies) {
        sb.append(",").append(frequency);
      }
    }
    return sb.toString();
  }

  @Override
  public void close();

}
