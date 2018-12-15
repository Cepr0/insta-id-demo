package io.github.cepr0.demo;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(InstaProperties.class)
@ConditionalOnClass(IdGenerator.class)
public class InstaConfig {

	public InstaConfig(InstaProperties properties) {
		InstagramStyleIdGenerator.getInstance().setShardId(properties.getShardId());
	}
}
