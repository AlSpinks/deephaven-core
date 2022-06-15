/**
 * Copyright (c) 2016-2022 Deephaven Data Labs and Patent Pending
 */
package io.deephaven.parquet.compress.codec;

import org.apache.parquet.hadoop.codec.ZstandardCodec;

/**
 * Provides an alternative codec name of "ZSTD" instead of the superclass's "ZSTANDARD".
 */
public class ZstdCodec extends ZstandardCodec {

}