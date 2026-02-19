package com.redis.om.spring.ops.search;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.redis.om.spring.autocomplete.Suggestion;
import com.redis.om.spring.repository.query.autocomplete.AutoCompleteOptions;

import redis.clients.jedis.search.*;
import redis.clients.jedis.search.aggr.AggregationBuilder;
import redis.clients.jedis.search.aggr.AggregationResult;
import redis.clients.jedis.search.hybrid.FTHybridParams;
import redis.clients.jedis.search.hybrid.HybridResult;
import redis.clients.jedis.search.schemafields.SchemaField;

/**
 * Operations interface for RediSearch functionality in Redis OM Spring.
 * 
 * <p>This interface provides a high-level abstraction over RediSearch operations,
 * enabling full-text search, aggregation, auto-completion, and index management
 * capabilities for Redis data structures. It serves as the primary API for
 * interacting with RediSearch from within the Redis OM Spring framework.</p>
 * 
 * <p>The interface supports:</p>
 * <ul>
 * <li>Index creation and management with schema definitions</li>
 * <li>Full-text search with advanced query capabilities</li>
 * <li>Aggregation operations for data analysis</li>
 * <li>Auto-completion functionality for search suggestions</li>
 * <li>Index configuration and optimization</li>
 * <li>Alias management for index names</li>
 * <li>Synonym groups for enhanced search</li>
 * </ul>
 * 
 * <p>This interface is typically implemented by classes that wrap Redis clients
 * and provide Redis OM Spring-specific functionality on top of the underlying
 * RediSearch module.</p>
 * 
 * @param <K> the type of keys used in Redis operations
 * 
 * @see com.redis.om.spring.ops.RedisModulesOperations
 * @see redis.clients.jedis.search.SearchProtocol
 * 
 * @since 1.0.0
 * @author Redis OM Spring Team
 */
public interface SearchOperations<K> {

  /**
   * Creates a new search index with the specified schema and options.
   * 
   * @param schema  the index schema definition containing field specifications
   * @param options additional options for index creation (language, stopwords, etc.)
   * @return status message indicating success or failure of index creation
   * @throws RuntimeException if index creation fails
   */
  String createIndex(Schema schema, IndexOptions options);

  /**
   * Creates a new search index using FT.CREATE parameters and schema fields.
   * 
   * @param params the FT.CREATE parameters including index name and options
   * @param fields list of schema fields defining the index structure
   * @return status message indicating success or failure of index creation
   * @throws RuntimeException if index creation fails
   */
  String createIndex(FTCreateParams params, List<SchemaField> fields);

  /**
   * Executes a search query against the index.
   * 
   * @param q the search query containing search terms and parameters
   * @return search results containing matching documents and metadata
   * @throws RuntimeException if search execution fails
   */
  SearchResult search(Query q);

  /**
   * Executes a search query with additional search parameters.
   * 
   * @param q      the search query containing search terms and parameters
   * @param params additional FT.SEARCH parameters for query customization
   * @return search results containing matching documents and metadata
   * @throws RuntimeException if search execution fails
   */
  SearchResult search(Query q, FTSearchParams params);

  /**
   * Executes an aggregation query for data analysis and grouping.
   * 
   * @param q the aggregation builder containing grouping, reducing, and sorting operations
   * @return aggregation results with computed statistics and grouped data
   * @throws RuntimeException if aggregation execution fails
   */
  AggregationResult aggregate(AggregationBuilder q);

  /**
   * Deletes a cursor used for paginated aggregation results.
   * 
   * @param cursorId the unique identifier of the cursor to delete
   * @return status message indicating success or failure of cursor deletion
   * @throws RuntimeException if cursor deletion fails
   */
  String cursorDelete(long cursorId);

  /**
   * Reads the next batch of results from a paginated aggregation cursor.
   * 
   * @param cursorId the unique identifier of the cursor to read from
   * @param count    the maximum number of results to retrieve in this batch
   * @return aggregation results containing the next batch of data
   * @throws RuntimeException if cursor read operation fails
   */
  AggregationResult cursorRead(long cursorId, int count);

  /**
   * Explains the execution plan for a search query without executing it.
   * 
   * @param q the search query to analyze
   * @return detailed explanation of how the query would be executed
   * @throws RuntimeException if query explanation fails
   */
  String explain(Query q);

  /**
   * Retrieves information and statistics about the search index.
   * 
   * @return map containing index information including document count, field statistics, and configuration
   * @throws RuntimeException if info retrieval fails
   */
  Map<String, Object> getInfo();

  /**
   * Drops the search index while preserving the underlying documents.
   * 
   * @return status message indicating success or failure of index deletion
   * @throws RuntimeException if index drop operation fails
   */
  String dropIndex();

  /**
   * Drops the search index and deletes all associated documents.
   * 
   * @return status message indicating success or failure of index and document deletion
   * @throws RuntimeException if drop operation fails
   */
  String dropIndexAndDocuments();

  /**
   * Adds a suggestion to an auto-completion dictionary with default score.
   * 
   * @param key        the dictionary key for the auto-completion index
   * @param suggestion the suggestion string to add
   * @return the new size of the suggestion dictionary
   * @throws RuntimeException if suggestion addition fails
   */
  Long addSuggestion(String key, String suggestion);

  /**
   * Adds a suggestion to an auto-completion dictionary with a specific score.
   * 
   * @param key        the dictionary key for the auto-completion index
   * @param suggestion the suggestion string to add
   * @param score      the score/weight for ranking this suggestion (higher scores rank higher)
   * @return the new size of the suggestion dictionary
   * @throws RuntimeException if suggestion addition fails
   */
  Long addSuggestion(String key, String suggestion, double score);

  /**
   * Retrieves auto-completion suggestions for a given prefix.
   * 
   * @param key    the dictionary key for the auto-completion index
   * @param prefix the prefix to search for suggestions
   * @return list of matching suggestions sorted by score
   * @throws RuntimeException if suggestion retrieval fails
   */
  List<Suggestion> getSuggestion(String key, String prefix);

  /**
   * Retrieves auto-completion suggestions with additional options.
   * 
   * @param key     the dictionary key for the auto-completion index
   * @param prefix  the prefix to search for suggestions
   * @param options additional options for auto-completion (max results, fuzzy matching, etc.)
   * @return list of matching suggestions sorted by score
   * @throws RuntimeException if suggestion retrieval fails
   */
  List<Suggestion> getSuggestion(String key, String prefix, AutoCompleteOptions options);

  /**
   * Deletes a specific suggestion from an auto-completion dictionary.
   * 
   * @param key   the dictionary key for the auto-completion index
   * @param entry the exact suggestion string to delete
   * @return true if the suggestion was successfully deleted, false otherwise
   * @throws RuntimeException if suggestion deletion fails
   */
  Boolean deleteSuggestion(String key, String entry);

  /**
   * Gets the number of suggestions in an auto-completion dictionary.
   * 
   * @param key the dictionary key for the auto-completion index
   * @return the total number of suggestions in the dictionary
   * @throws RuntimeException if length retrieval fails
   */
  Long getSuggestionLength(String key);

  /**
   * Modifies an existing search index by adding new fields to the schema.
   * 
   * @param fields variable number of schema fields to add to the existing index
   * @return status message indicating success or failure of index alteration
   * @throws RuntimeException if index alteration fails
   */
  String alterIndex(SchemaField... fields);

  /**
   * Sets a global RediSearch configuration option.
   * 
   * @param option the configuration option name to set
   * @param value  the new value for the configuration option
   * @return status message indicating success or failure of configuration update
   * @throws RuntimeException if configuration setting fails
   */
  String setConfig(String option, String value);

  /**
   * Retrieves the value of a global RediSearch configuration option.
   * 
   * @param option the configuration option name to retrieve
   * @return map containing the configuration option and its current value
   * @throws RuntimeException if configuration retrieval fails
   */
  Map<String, Object> getConfig(String option);

  /**
   * Retrieves index-specific configuration information.
   * 
   * @param option the index configuration option to retrieve
   * @return map containing the index configuration option and its value
   * @throws RuntimeException if index configuration retrieval fails
   */
  Map<String, Object> getIndexConfig(String option);

  /**
   * Creates an alias for the current search index.
   * 
   * @param name the alias name to assign to the index
   * @return status message indicating success or failure of alias creation
   * @throws RuntimeException if alias addition fails or alias already exists
   */
  String addAlias(String name);

  /**
   * Updates an existing alias to point to the current search index.
   * 
   * @param name the alias name to update
   * @return status message indicating success or failure of alias update
   * @throws RuntimeException if alias update fails or alias doesn't exist
   */
  String updateAlias(String name);

  /**
   * Deletes an alias from the search index.
   * 
   * @param name the alias name to delete
   * @return status message indicating success or failure of alias deletion
   * @throws RuntimeException if alias deletion fails or alias doesn't exist
   */
  String deleteAlias(String name);

  /**
   * Updates or creates a synonym group with the specified terms.
   * 
   * @param synonymGroupId the unique identifier for the synonym group
   * @param terms          variable number of terms that should be treated as synonyms
   * @return status message indicating success or failure of synonym update
   * @throws RuntimeException if synonym update fails
   */
  String updateSynonym(String synonymGroupId, String... terms);

  /**
   * Retrieves all synonym groups and their associated terms.
   * 
   * @return map where keys are synonym group IDs and values are lists of synonym terms
   * @throws RuntimeException if synonym dump operation fails
   */
  Map<String, List<String>> dumpSynonym();

  /**
   * Retrieves all distinct values for a tag field in the index.
   * 
   * @param value the tag field name to retrieve values for
   * @return set of all distinct tag values found in the index
   * @throws RuntimeException if tag values retrieval fails
   */
  Set<String> tagVals(String value);

  /**
   * Executes a native FT.HYBRID command combining text and vector search.
   * <p>
   * This command requires Redis 8.4+ and provides built-in score fusion
   * via RRF (Reciprocal Rank Fusion) or LINEAR combination methods.
   * </p>
   *
   * @param params the FT.HYBRID parameters including search, vector search,
   *               combination, and post-processing configuration
   * @return hybrid search results containing matching documents with combined scores
   * @throws RuntimeException if the command fails (e.g., Redis version does not support FT.HYBRID)
   * @since 2.1.0
   */
  HybridResult ftHybrid(FTHybridParams params);

}
