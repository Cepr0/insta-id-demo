package io.github.cepr0.demo;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Serializable;

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
@Component
@Slf4j
public class IdGenerator extends SequenceStyleGenerator {

	@Value("${shard-id:0}")
	private int shardId;

	private int shiftedShardId;

	private static final long START_EPOCH = 1541030400000L; // 2018-11-1
	private static final int TIMESTAMP_SHIFT = 23; // (64 - 41)
	private static final int SHARD_SHIFT = 16; // (64 - 41 - 16)
	private static final int ID_PER_MILLISECOND = 65536;

	public IdGenerator() {
		shiftedShardId = shardId << SHARD_SHIFT;
	}

	@Override
	public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
		long generated = (long) super.generate(session, object);
		return (System.currentTimeMillis() - START_EPOCH) << TIMESTAMP_SHIFT
				| shiftedShardId
				| generated % ID_PER_MILLISECOND;
	}
}