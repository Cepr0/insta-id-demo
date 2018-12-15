package io.github.cepr0.demo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import static io.github.cepr0.demo.InstagramStyleIdGenerator.MAX_SHARD_SIZE;

@Getter
@Setter
@Validated
@ConfigurationProperties("insta")
public class InstaProperties {
	@Min(0) @Max(MAX_SHARD_SIZE) private int shardId = 0;
}
