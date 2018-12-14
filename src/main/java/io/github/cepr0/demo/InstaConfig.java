package io.github.cepr0.demo;

import lombok.Getter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(InstaProperties.class)
public class InstaConfig {
	@Getter private static int shardId;

	public InstaConfig(InstaProperties properties) {
		shardId = properties.getShardId();
	}
}
