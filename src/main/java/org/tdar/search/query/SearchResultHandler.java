package org.tdar.search.query;

import java.util.List;

import org.hibernate.search.FullTextQuery;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.entity.Person;

/* further abstracting some of the functions of the search result handler 
 * so it can be pushed into the service layer. HibernateSearch handles the request by pulling field info
 * from the SearchResultHandler, and then sets results back on when done along with additional info if needed.
 */
/**
 * The SearchResultHandler interface is used by the SearchService to return search results, sorted and organised into pages.
 * An instance of this interface is passed to the SearchService, along with a query, and in response the SearchService
 * queries this interface for sorting, paging, faceting, etc. options, and then returns a page of results.
 * 
 * @see org.tdar.core.service.SearchService#handleSearch(org.tdar.search.query.QueryBuilder, SearchResultHandler)
 * 
 */
public interface SearchResultHandler<I extends Indexable> {

    public static final int DEFAULT_START = 0;
    public static final int DEFAULT_RESULT_SIZE = 25;

    SortOption getSortField();

    SortOption getSecondarySortField();

    /**
     * Sets the total number of records found by the SearchService.
     * When resultSize is less than startRecord + recordsPerPage, then there are more pages of results available.
     * 
     * @param resultSize
     *            the total number of records matching the search
     */
    void setTotalRecords(int resultSize);

    int getTotalRecords();

    /**
     * Gets the index of the first record which the SearchService should return in this page of results.
     * 
     * @return the index of the first record which the SearchService should return
     */
    int getStartRecord();

    void setStartRecord(int startRecord);

    /**
     * Retrieve the number of records which the SearchService should return in this page of results.
     * 
     * @return the number of records to return.
     */
    int getRecordsPerPage();

    void setRecordsPerPage(int recordsPerPage);

    void addFacets(FullTextQuery ftq);

    boolean isDebug();

    boolean isShowAll();

    /**
     * Return a page of results from the SearchService.
     * 
     * @param toReturn
     */
    void setResults(List<I> toReturn);

    List<I> getResults();

    void setMode(String mode);

    /*
     * Used for debug statements to print the mode
     */
    String getMode();

    public Person getAuthenticatedUser();

    public abstract String getSearchTitle();

    public String getSearchDescription();

    public int getNextPageStartRecord();

    public int getPrevPageStartRecord();

    List<String> getProjections();

}
