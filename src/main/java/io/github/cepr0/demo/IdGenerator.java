package io.github.cepr0.demo;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import java.io.Serializable;

@Slf4j
public class IdGenerator extends SequenceStyleGenerator {

	private static final InstagramStyleIdGenerator GENERATOR = InstagramStyleIdGenerator.getInstance();

	public IdGenerator() {
		GENERATOR.setGenerate((s, o) -> (long) super.generate(s, o));
	}

	@Override
	public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
		return GENERATOR.generate(session, object);
	}
}
