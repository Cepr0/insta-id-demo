Entity ID generation example in the [style of Instagram](https://instagram-engineering.com/sharding-ids-at-instagram-1cf5a71e5a5c).

### Id Generator

```java
/**
 *
 * 0         10          20           30          40           50          60
 * 1234 5678 9012 3456 7890 1234 5678 9012 3456 7890 1234 5678 9012 3456 7890 1234
 * ------------------------------------------------------------------------------
 * 0000 0000 0011 0100 1111 0001 1011 1000 1011 0111 0000 0001 0000 0000 1000 1101
 * |-------------------------------------------------||------| |-----------------|
 *                                          ‭          111 1111 1111‬ 1111 1111 1111
 * |-------------------------------------------------||------| |-----------------|
 *                       41 bits                       7 bits         16 bits
 *                      timestamp                      shardId          id
 *                     1 per mills                     0 - 63   0 - ‭65535 per mills
 */
@Component
@Slf4j
public class IdGenerator extends SequenceStyleGenerator {

	@Value("${shard-id:0}")
	private int shardId;

	private int shiftedShardId;

	private static final long START_EPOCH = 1541030400000L; // 2018-11-1
	private static final int TIMESTAMP_SHIFT = 23; // (64 - 41)
	private static final int SHARD_SHIFT = 16; // (64 - 41 - 7)
	private static final int ID_PER_MILLISECOND = 1 << SHARD_SHIFT; // ‭65535

	public IdGenerator() {
		shiftedShardId = shardId << SHARD_SHIFT;
	}

	@Override
	public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
		long generated = (long) super.generate(session, object);
		return (System.currentTimeMillis() - START_EPOCH) << TIMESTAMP_SHIFT
				| shiftedShardId
				| generated % ID_PER_MILLISECOND;
	}
}
```

### Usage example

```java
@Id
@GeneratedValue(strategy = SEQUENCE, generator = "idGen")
@GenericGenerator(name = "idGen", strategy = "io.github.cepr0.demo.IdGenerator", parameters = {
        @Parameter(name = "sequence_name", value = "global_seq"),
        @Parameter(name = "increment_size", value = "5")
})
private Long id;
```