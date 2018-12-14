package io.github.cepr0.demo;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static io.github.cepr0.demo.IdGenerator.*;
import static io.github.cepr0.demo.IdGeneratorTest.SHARD_ID_PROP;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = SHARD_ID_PROP)
@ContextConfiguration(classes = InstaConfig.class)
public class IdGeneratorTest {

	private static final int SHARD_ID = 33;
	static final String SHARD_ID_PROP = "insta.shard-id=" + SHARD_ID;

	@MockBean private SharedSessionContractImplementor session;

	@Test
	public void shardId() {
		int shardId = InstaConfig.getShardId();
		assertThat(shardId).isEqualTo(SHARD_ID);
	}

	@Test
	public void generate() {
		int shardId = InstaConfig.getShardId();
		IdGenerator generator = new IdGenerator();
		User user = new User();

		long currentTime = System.currentTimeMillis();
		generator.setCurrentTime(() -> currentTime);

		long sequencedId = 1L;
		generator.setGenerate((s, o) -> 1L);
		long generatedId = (long) generator.generate(session, user);
		testId(currentTime, shardId, 1L, generatedId);

		generator.setGenerate((s, o) -> 32768L);
		generatedId = (long) generator.generate(session, user);
		testId(currentTime, shardId, 32768L, generatedId);

		generator.setGenerate((s, o) -> 32769L);
		generatedId = (long) generator.generate(session, user);
		testId(currentTime, shardId, 32769L, generatedId);
	}

	private void testId(long currentMillis, int shardId, long sequencedId, Long generatedId) {
		String binaryId = Long.toBinaryString(generatedId);

		long baseMils = currentMillis - START_EPOCH;

		System.out.println(Long.toBinaryString(baseMils));
		System.out.println(binaryId);
		assertThat(binaryId).startsWith(Long.toBinaryString(baseMils));

		long shiftedBaseMils = baseMils << TIMESTAMP_SHIFT;
		System.out.println(Long.toBinaryString(shiftedBaseMils));

		long shardAndId = generatedId ^ shiftedBaseMils;
		int shiftedShardId = shardId << SHARD_SHIFT;

		String binaryShardAndGeneratedId = Long.toBinaryString(shardAndId);
		String binaryShiftedShardId = Long.toBinaryString(shiftedShardId);

		System.out.println(binaryShardAndGeneratedId);
		System.out.println(binaryShiftedShardId);

		assertThat(binaryShiftedShardId).startsWith(Long.toBinaryString(shardId));

		long restId = shiftedShardId ^ shardAndId;
		System.out.println(Long.toBinaryString(restId));

		assertThat(restId).isEqualTo(sequencedId % ID_PER_MILLISECOND);
	}
}
