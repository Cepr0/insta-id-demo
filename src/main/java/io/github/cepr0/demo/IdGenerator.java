package io.github.cepr0.demo;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import java.io.Serializable;
import java.util.function.BiFunction;
import java.util.function.Supplier;

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

	static final long START_EPOCH = 1541030400000L; // 2018-11-1
	static final int TIMESTAMP_SHIFT = 23; // (64 - 41)
	static final int SHARD_SHIFT = 16; // (64 - 41 - 7)
	static final int ID_PER_MILLISECOND = 1 << SHARD_SHIFT; // ‭65535

	private int shiftedShardId;

	/**
	 * For testing purpose only
	 */
	@Setter
	BiFunction<SharedSessionContractImplementor, Object, Long> generate = (s, o) -> (long) super.generate(s, o);

	/**
	 * For testing purpose only
	 */
	@Setter
	Supplier<Long> currentTime = System::currentTimeMillis;

	public IdGenerator() {
		int shardId = InstaConfig.getShardId();
		log.debug("[d] shardId = {}", shardId);
		shiftedShardId = shardId << SHARD_SHIFT;
	}

	@Override
	public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
		return (currentTime.get() - START_EPOCH) << TIMESTAMP_SHIFT
				| shiftedShardId
				| generate.apply(session, object) % ID_PER_MILLISECOND;
	}
}
