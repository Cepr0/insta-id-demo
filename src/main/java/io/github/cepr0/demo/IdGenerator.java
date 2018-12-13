package io.github.cepr0.demo;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;

/**
 *
 * 0         10          20           30          40           50          60
 * 1234 5678 9012 3456 7890 1234 5678 9012 3456 7890 1234 5678 9012 3456 7890 1234
 * ------------------------------------------------------------------------------
 * 0000 0000 0011 0100 1111 0001 1011 1000 1011 0111 0000 0001 0000 0000 1000 1101
 * |-------------------------------------------------||------| |-----------------|
 *                                          ‭          111 1111 1111‬ 1111 1111 1111
 * |-------------------------------------------------||------| |-----------------|
 *                       41 bits                       7 bits         16 bits
 *                      timestamp                      shardId          id
 *                     1 per mills                     0 - 63   0 - ‭65535 per mills
 */
@Slf4j
public class IdGenerator extends SequenceStyleGenerator {

	private int shiftedShardId;

	private static final long START_EPOCH = 1541030400000L; // 2018-11-1
	private static final int TIMESTAMP_SHIFT = 23; // (64 - 41)
	private static final int SHARD_SHIFT = 16; // (64 - 41 - 7)
	private static final int ID_PER_MILLISECOND = 1 << SHARD_SHIFT; // ‭65535

	public IdGenerator() {
		int shardId = loadShardId();
		shiftedShardId = shardId << SHARD_SHIFT;
	}

	@Override
	public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
		long generated = (long) super.generate(session, object);
		return (System.currentTimeMillis() - START_EPOCH) << TIMESTAMP_SHIFT
				| shiftedShardId
				| generated % ID_PER_MILLISECOND;
	}

	private int loadShardId() {
		ClassLoader loader = ClassUtils.getDefaultClassLoader();

		if (loader == null) {
			throw new IllegalStateException("Could not obtain ClassLoader!");
		}

		InputStream resStream = loader.getResourceAsStream("application.yml");
		if (resStream == null) {
			throw new IllegalStateException("Could not load application.yml!");
		}
		Properties props = new Properties();

		try {
			props.load(resStream);
		} catch (IOException e) {
			throw new IllegalStateException("Could not read from application.yml!", e);
		}

		String shardIdValue = (String) props.get("shard-id");
		int shardId;

		if (shardIdValue == null) {
			shardId = 0;
			log.warn("[w] 'shard-id' property has not been found. Used default 0 value.");
		} else {
			try {
				shardId = Integer.valueOf(shardIdValue);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Could not convert 'shard-id' property to numeric.");
			}
		}
		return shardId;
	}
}
