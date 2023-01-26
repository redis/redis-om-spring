package com.redis.om.spring.annotations;

public enum ReducerFunction {
  // REDUCE COUNT 0
  // Count the number of records in each group
  COUNT,

  // REDUCE COUNT_DISTINCT 1 {property}
  // Count the number of distinct values for property.
  COUNT_DISTINCT,

  // REDUCE COUNT_DISTINCTISH 1 {property}
  // Same as COUNT_DISTINCT - but provide an approximation instead of an exact count
  COUNT_DISTINCTISH,

  // REDUCE SUM 1 {property}
  // Return the sum of all numeric values of a given property in a group.
  // Non-numeric values if the group are counted as 0.
  SUM,

  // REDUCE MIN 1 {property}
  // Return the minimal value of a property, whether it is a string, number or NULL.
  MIN,

  // REDUCE MAX 1 {property}
  // Return the maximal value of a property, whether it is a string, number or NULL.
  MAX,

  // REDUCE AVG 1 {property}
  // Return the average value of a numeric property.
  AVG,

  // REDUCE STDDEV 1 {property}
  // Return the standard deviation of a numeric property in the group.
  STDDEV,

  // REDUCE QUANTILE 2 {property} {quantile}
  // Return the value of a numeric property at a given quantile of the results.
  // Quantile is expressed as a number between 0 and 1.
  QUANTILE,

  // REDUCE TOLIST 1 {property}
  // Merge all distinct values of a given property into a single array.
  TOLIST,

  // REDUCE FIRST_VALUE {nargs} {property} [BY {property} [ASC|DESC]]
  // Return the first or top value of a given property in the group,
  // optionally by comparing that or another property.
  FIRST_VALUE,

  // REDUCE RANDOM_SAMPLE {nargs} {property} {sample_size}
  // Perform a reservoir sampling of the group elements with a given size,
  // and return an array of the sampled items with an even distribution.
  RANDOM_SAMPLE
}
