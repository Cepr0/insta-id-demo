package io.github.cepr0.demo;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

import java.io.Serializable;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static java.lang.String.format;

/**
 *
 * 0         10          20           30          40           50          60
 * 1234 5678 9012 3456 7890 1234 5678 9012 3456 7890 1234 5678 9012 3456 7890 1234
 * ------------------------------------------------------------------------------
 * 0000 0000 0011 0100 1111 0001 1011 1000 1011 0111 0000 0001 0000 0000 1000 1101
 * |-------------------------------------------------||--------------||----------|
 *                                          ‭          111 1111 1111 1111 1111 1111
 * |-------------------------------------------------||--------------||----------|
 *                       41 bits                       13 bits         10 bits
 *                      timestamp                      shardId          id
 *                     1 per mills                     0 - 8191   0 - 1023‬ per mills
 */
@Slf4j
public class InstagramStyleIdGenerator {

	static final long START_EPOCH = 1541030400000L; // 2018-11-1
	static final int TIMESTAMP_BIT_SIZE = 41; // bits
	static final int SHARD_BIT_SIZE = 13; // bits (max 8192 shards, 0 - 8191)
	static final int MAX_SHARD_SIZE = 8191; // ((int) Math.pow(2, SHARD_BIT_SIZE)) - 1;
	static final int TIMESTAMP_SHIFT = 64 - TIMESTAMP_BIT_SIZE; // 64 - 41 = 23
	static final int SHARD_SHIFT = 64 - TIMESTAMP_BIT_SIZE - SHARD_BIT_SIZE; // 64 - 41 - 13 = 10
	static final int ID_PER_MILLISECOND = 1 << SHARD_SHIFT; // max 1024‬ ids per second, 0 - 1023

	private static final InstagramStyleIdGenerator INSTANCE = new InstagramStyleIdGenerator();

	@Getter private int shardId;

	@Setter
	BiFunction<SharedSessionContractImplementor, Object, Long> generate;

	@Setter
	Supplier<Long> currentTime = System::currentTimeMillis;

	private InstagramStyleIdGenerator() {
		shardId = 0;
	}

	public static InstagramStyleIdGenerator getInstance() {
		return INSTANCE;
	}

	public Serializable generate(SharedSessionContractImplementor session, Object object) {
		Long sequencedId = generate.apply(session, object);
		long calcId = sequencedId & (ID_PER_MILLISECOND - 1); // http://mziccard.me/2015/05/08/modulo-and-division-vs-bitwise-operations/
		return ((currentTime.get() - START_EPOCH) << TIMESTAMP_SHIFT) | (shardId << SHARD_SHIFT) | calcId;
	}

	synchronized public void setShardId(int shardId) {
		if (shardId > MAX_SHARD_SIZE || shardId < 0) {
			throw new IllegalArgumentException(format("Incorrect shardId value %s. Must be more than 0 and less than or equal %s",
					shardId, MAX_SHARD_SIZE)
			);
		}
		this.shardId = shardId;
		log.debug("[d] Set shardId = {}", shardId);
	}

	/**
	 * https://dba.stackexchange.com/a/193320
	 */
	public static int toShardId(long id) {
		return (int) ((id << TIMESTAMP_BIT_SIZE) >>> (64 - SHARD_BIT_SIZE));
	}

	/**
	 * https://dba.stackexchange.com/a/193320
	 */
	public static int toSequencedId(long id) {
		int sequencedIdStartBit = TIMESTAMP_BIT_SIZE + SHARD_BIT_SIZE;
		return (int) ((id << sequencedIdStartBit) >>> sequencedIdStartBit);
	}

	public static long toTimestamp(long id) {
		return (id >>> TIMESTAMP_SHIFT) + START_EPOCH;
	}
}
