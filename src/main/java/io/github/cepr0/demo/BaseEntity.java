package io.github.cepr0.demo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.util.Objects;

import static javax.persistence.GenerationType.SEQUENCE;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Setter
@NoArgsConstructor(access = PROTECTED)
@MappedSuperclass
public class BaseEntity {

	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = "idGen")
	@GenericGenerator(name = "idGen", strategy = "io.github.cepr0.demo.IdGenerator", parameters = {
			@Parameter(name = "sequence_name", value = "global_seq"),
//			@Parameter(name = "optimizer", value = "pooled-lo"), // https://vladmihalcea.com/hibernate-hidden-gem-the-pooled-lo-optimizer/
			@Parameter(name = "increment_size", value = "5")
	})
	private Long id;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		BaseEntity that = (BaseEntity) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{id=" + id + '}';
	}
}
