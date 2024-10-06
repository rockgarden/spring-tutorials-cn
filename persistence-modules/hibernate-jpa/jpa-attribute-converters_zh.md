# [JPA属性转换器](https://www.baeldung.com/jpa-attribute-converters)

1. 介绍

    在这篇简短的文章中，我们将介绍JPA 3.0中可用的属性转换器的使用情况——简单地说，它允许我们将JDBC类型映射到Java类。

    我们将在这里使用Hibernate 6作为我们的JPA实现。

2. 创建转换器

    我们将展示如何为自定义Java类实现属性转换器。

    首先，让我们创建一个PersonName类——稍后将进行转换：

    ```java
    public class PersonName implements Serializable {

        private String name;
        private String surname;

        // getters and setters
    }
    ```

    然后，我们将在@Entity类中添加PersonName类型的属性：

    ```java
    @Entity(name = "PersonTable")
    public class Person {

        private PersonName personName;

        //...
    }
    ```

    现在我们需要创建一个转换器，将PersonName属性转换为数据库列，反之亦然。在我们的案例中，我们将属性转换为包含姓名字段的字符串值。

    要做到这一点，我们必须用@Converter注释我们的转换器类，并实现AttributeConverterinterface。我们将按照以下顺序将接口与类和数据库列的类型进行参数化：

    ```java
    @Converter
    public class PersonNameConverter implements
    AttributeConverter<PersonName, String> {

        private static final String SEPARATOR = ", ";

        @Override
        public String convertToDatabaseColumn(PersonName personName) {
            if (personName == null) {
                return null;
            }

            StringBuilder sb = new StringBuilder();
            if (personName.getSurname() != null && !personName.getSurname()
                .isEmpty()) {
                sb.append(personName.getSurname());
                sb.append(SEPARATOR);
            }

            if (personName.getName() != null 
            && !personName.getName().isEmpty()) {
                sb.append(personName.getName());
            }

            return sb.toString();
        }

        @Override
        public PersonName convertToEntityAttribute(String dbPersonName) {
            if (dbPersonName == null || dbPersonName.isEmpty()) {
                return null;
            }

            String[] pieces = dbPersonName.split(SEPARATOR);

            if (pieces == null || pieces.length == 0) {
                return null;
            }

            PersonName personName = new PersonName();        
            String firstPiece = !pieces[0].isEmpty() ? pieces[0] : null;
            if (dbPersonName.contains(SEPARATOR)) {
                personName.setSurname(firstPiece);

                if (pieces.length >= 2 && pieces[1] != null 
                && !pieces[1].isEmpty()) {
                    personName.setName(pieces[1]);
                }
            } else {
                personName.setName(firstPiece);
            }

            return personName;
        }
    }
    ```

    请注意，我们必须实现2种方法：convertToDatabaseColumn()和convertToEntityAttribute()。

    这两种方法用于从属性转换为数据库列，反之亦然。

3. 使用转换器

    要使用我们的转换器，我们只需要将@Convert注释添加到属性中，并指定我们要使用的转换器类：

    ```java
    @Entity(name = "PersonTable")
    public class Person {

        @Convert(converter = PersonNameConverter.class)
        private PersonName personName;
        
        // ...
    }
    ```

    最后，让我们创建一个单元测试，看看它是否真的有效。

    为此，我们将首先将Person对象存储在我们的数据库中：

    ```java
    @Test
    public void givenPersonName_whenSaving_thenNameAndSurnameConcat() {
        String name = "name";
        String surname = "surname";

        PersonName personName = new PersonName();
        personName.setName(name);
        personName.setSurname(surname);

        Person person = new Person();
        person.setPersonName(personName);

        Long id = (Long) session.save(person);

        session.flush();
        session.clear();
    }
    ```

    接下来，我们将通过从数据库表中检索该字段来测试PersonName是否按照我们在转换器中定义的存储：

    ```java
    @Test
    public void givenPersonName_whenSaving_thenNameAndSurnameConcat() {
        // ...

        String dbPersonName = (String) session.createNativeQuery(
        "select p.personName from PersonTable p where p.id = :id")
        .setParameter("id", id)
        .getSingleResult();

        assertEquals(surname + ", " + name, dbPersonName);
    }
    ```

    我们还通过编写检索整个Person类的查询来测试从数据库中存储的值到PersonName类的转换是否按照转换器中定义的方式工作：

    ```java
    @Test
    public void givenPersonName_whenSaving_thenNameAndSurnameConcat() {
        // ...

        Person dbPerson = session.createNativeQuery(
        "select * from PersonTable p where p.id = :id", Person.class)
            .setParameter("id", id)
            .getSingleResult();

        assertEquals(dbPerson.getPersonName()
        .getName(), name);
        assertEquals(dbPerson.getPersonName()
        .getSurname(), surname);
    }
    ```

4. 结论

    在这个简短的教程中，我们展示了如何在JPA 3.0中使用新引入的属性转换器。
