# Hibernate Internationalized JSON Column
This Java library allows Hibernate ORM entities to contain internationalized attribute values, which are persisted in a single JSON column.

## Requirements
* Hibernate 6
* [Jackson](https://github.com/FasterXML/jackson-databind) or [Jakarta JSON Binding](https://github.com/jakartaee/jsonb-api) or a custom `org.hibernate.type.FormatMapper` implementation

You need to provide both of these requirements for the library to work.

For example, using Maven:
```xml
<dependencies>
    <dependency>
        <groupId>org.hibernate.orm</groupId>
        <artifactId>hibernate-core</artifactId>
        <version>6.1.7.Final</version>
    </dependency>
    
    <!-- Either: -->
    <dependency>
        <groupId>jakarta.json.bind</groupId>
        <artifactId>jakarta.json.bind-api</artifactId>
        <version>2.0.0</version>
    </dependency>
    <!-- or: -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.14.2</version>
    </dependency>
</dependencies>
```

## Usage
In your entity, define an internationalized attribute:

```java
@Entity
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private String author;

    @AttributeOverride(name = Internationalized.I18N_ATTRIBUTE_NAME, column = @Column(name = "titles"))
    private Internationalized<String> titles = new Internationalized<>();
}
```

Note that the `id` and `author` attributes are not internationalized. This library allows internationalized and non-internationalized attributes to live side by side without the need for separate entities and separate tables. 

You will want to use the annotation `jakarta.persistence.AttributeOverride` to set the column name that Hibernate uses to persist the localized values.
