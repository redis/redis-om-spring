package com.redis.om.spring.id;

import java.util.Arrays;
import java.util.List;

public class OsTools {
  private static final String OPERATING_SYSTEM_NAME = System.getProperty("os.name").toLowerCase();

  private static final List<String> SECURE_RANDOM_ALGORITHMS_LINUX_OSX_SOLARIS = Arrays.asList("NativePRNGBlocking",
      "NativePRNGNonBlocking", "NativePRNG", "SHA1PRNG");
  private static final List<String> SECURE_RANDOM_ALGORITHMS_WINDOWS = Arrays.asList("SHA1PRNG", "Windows-PRNG");

  static List<String> secureRandomAlgorithmNames() {
    return OPERATING_SYSTEM_NAME.contains("win") ?
        SECURE_RANDOM_ALGORITHMS_WINDOWS :
        SECURE_RANDOM_ALGORITHMS_LINUX_OSX_SOLARIS;
  }
}
