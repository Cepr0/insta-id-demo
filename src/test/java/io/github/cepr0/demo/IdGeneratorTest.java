package io.github.cepr0.demo;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

public class IdGeneratorTest {

	@Test
	public void loadShardId() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		IdGenerator generator = new IdGenerator();
		Method loadShardId = IdGenerator.class.getDeclaredMethod("loadShardId");
		loadShardId.setAccessible(true);
		int shardId = (int) loadShardId.invoke(generator);
		assertThat(shardId).isEqualTo(3);
	}
}