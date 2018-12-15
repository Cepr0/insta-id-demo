package io.github.cepr0.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.github.cepr0.demo.IdGeneratorIT.SHARD_ID_PROP;
import static io.github.cepr0.demo.InstagramStyleIdGenerator.*;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(properties = SHARD_ID_PROP)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class IdGeneratorIT {

	private static final int SHARD_ID = 2;
	static final String SHARD_ID_PROP = "insta.shard-id=" + SHARD_ID;

	@Autowired
	private UserRepo userRepo;

	@Test
	public void generate_ids__ID_PER_MILLISECOND_minus_1() {
		testGeneratedId(ID_PER_MILLISECOND - 1);
	}

	@Test
	public void generate_ids__ID_PER_MILLISECOND() {
		testGeneratedId(ID_PER_MILLISECOND);
	}

	@Test
	public void generate_ids__ID_PER_MILLISECOND_plus_1() {
		testGeneratedId(ID_PER_MILLISECOND + 1);
	}

	private void testGeneratedId(int count) {
		List<User> users = IntStream.rangeClosed(1, count)
				.mapToObj(i -> new User("user" + i))
				.map(userRepo::save)
				.collect(Collectors.toList());

		Long id = users.get(users.size() - 1).getId();

		assertThat(toShardId(id)).isEqualTo(SHARD_ID);
		assertThat(toSequencedId(id)).isEqualTo((count < ID_PER_MILLISECOND) ? count : count - ID_PER_MILLISECOND);
	}
}