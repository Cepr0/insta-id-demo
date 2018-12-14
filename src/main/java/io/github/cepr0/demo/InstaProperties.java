package io.github.cepr0.demo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Getter
@Setter
@Validated
@ConfigurationProperties("insta")
public class InstaProperties {
	@Min(0) @Max(255) private int shardId = 0;
}
