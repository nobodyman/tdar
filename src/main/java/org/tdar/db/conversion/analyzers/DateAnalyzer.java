package org.tdar.db.conversion.analyzers;

import java.util.Date;
import java.util.List;

import org.antlr.runtime.tree.Tree;
import org.tdar.core.bean.resource.datatable.DataTableColumnType;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;

/**
 * Tries to find out of the a string value passed to it contains a date or not.
 * 
 * @author Martin Paulo
 */
public class DateAnalyzer implements ColumnAnalyzer {

    /**
     * <p>
     * This method is surfaced as it is important that the same date is found by both the analyzer and the method that converts the value into a timestamp in
     * preparation for the database to convert the column into a time column. Not only does that avoid problems with different interpretations, but it also
     * allows the current implementation to be swapped out or refined in this one location if it isn't good enough.
     * <p>
     * As a static method it offends my sensibilities, but for the time being it allows us to explore the options and trade offs.
     * <p>
     * Uses <a href="http://natty.joestelmach.com/doc.jsp">natty</a>.
     * <p>
     * The original code in the PostgressDatabase.java file that did the conversion read:
     * 
     * <pre>
     * DateFormat dateFormat = new SimpleDateFormat();
     * DateFormat accessDateFormat = new SimpleDateFormat(&quot;EEE MMM dd hh:mm:ss z yyyy&quot;);
     * Date date = null;
     * try {
     *     java.sql.Date.valueOf(colValue);
     *     date = dateFormat.parse(colValue);
     * } catch (Exception e) {
     *     logger.trace(&quot;couldn't parse &quot; + colValue, e);
     * }
     * try {
     *     date = accessDateFormat.parse(colValue);
     * } catch (Exception e) {
     *     logger.trace(&quot;couldn't parse &quot; + colValue, e);
     * }
     * </pre>
     * 
     * @param value
     *            A string that is to be inspected to see if it contains a date
     * @return Either null if no date was found in the String, or the date expressed as a java.util.Date
     */
    public static Date convertValue(final String value) {
        Date result = null;
        List<DateGroup> candidateDates = new Parser().parse(value);
        if (isOnlyOneDateFound(candidateDates)) {
            Tree syntaxTree = candidateDates.get(0).getSyntaxTree();
            // At the top of the syntax tree we want a single date_time alternative (more than one means alternate dates were be found)
            if (syntaxTree.getChildCount() == 1) {
                Tree datetime = syntaxTree.getChild(0);
                // The date_time instance will have a date plus a time, or a date, or a time
                // For the possible tree see: http://natty.joestelmach.com/doc.jsp
                Tree firstChild = datetime.getChild(0);
                // we are only interested in the date component if it is an explicit date.
                if ("EXPLICIT_DATE".equals(firstChild.toString())) {
                    // could further demand a day, a month and a year
                    result = candidateDates.get(0).getDates().get(0);
                }
            }
        }
        return result;
    }

    private static boolean isOnlyOneDateFound(List<DateGroup> candidateDates) {
        return candidateDates.size() == 1
                && candidateDates.get(0).getDates().size() == 1
                && !candidateDates.get(0).isRecurring();
    }

    @Override
    public boolean analyze(final String value) {
        if (null == value) {
            return false;
        }
        return null != convertValue(value);
    }

    @Override
    public DataTableColumnType getType() {
        return DataTableColumnType.DATE;
    }

    @Override
    public int getLength() {
        return 0;
    }

}