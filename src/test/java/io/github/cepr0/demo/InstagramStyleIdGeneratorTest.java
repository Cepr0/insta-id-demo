package io.github.cepr0.demo;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static io.github.cepr0.demo.InstagramStyleIdGenerator.*;
import static io.github.cepr0.demo.InstagramStyleIdGeneratorTest.SHARD_ID_PROP;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = SHARD_ID_PROP)
@ContextConfiguration(classes = InstaConfig.class)
public class InstagramStyleIdGeneratorTest {

	private static final int SHARD_ID = 33;
	static final String SHARD_ID_PROP = "insta.shard-id=" + SHARD_ID;

	@MockBean private SharedSessionContractImplementor session;

	@Test
	public void should_assign_shardId() {
		assertThat(InstagramStyleIdGenerator.getInstance().getShardId()).isEqualTo(SHARD_ID);
	}

	@Test
	public void should_generate_id_correctly() {
		InstagramStyleIdGenerator generator = InstagramStyleIdGenerator.getInstance();
		int shardId = generator.getShardId();
		User user = new User();

		long currentTime = System.currentTimeMillis();
		generator.setCurrentTime(() -> currentTime);

		generator.setGenerate((s, o) -> 1L);
		long generatedId = (long) generator.generate(session, user);
		testId(currentTime, shardId, 1L, generatedId);

		generator.setGenerate((s, o) -> ID_PER_MILLISECOND - 1L);
		generatedId = (long) generator.generate(session, user);
		testId(currentTime, shardId, ID_PER_MILLISECOND - 1L, generatedId);

		generator.setGenerate((s, o) -> (long) ID_PER_MILLISECOND);
		generatedId = (long) generator.generate(session, user);
		testId(currentTime, shardId, 0, generatedId);

		generator.setGenerate((s, o) -> ID_PER_MILLISECOND + 1L);
		generatedId = (long) generator.generate(session, user);
		testId(currentTime, shardId, 1L, generatedId);
	}

	private void testId(long currentMillis, int shardId, long sequencedId, long generatedId) {
		assertThat(toTimestamp(generatedId)).isEqualTo(currentMillis);
		assertThat(toShardId(generatedId)).isEqualTo(shardId);
		assertThat(toSequencedId(generatedId)).isEqualTo(sequencedId);
	}
}
