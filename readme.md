Entity ID generation example in the [style of Instagram](https://instagram-engineering.com/sharding-ids-at-instagram-1cf5a71e5a5c).

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