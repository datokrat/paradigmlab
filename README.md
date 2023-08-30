# Welcome, watch your step

This is an experimental repository I use to play with new ideas. I try to integrate them into my life as much as possible so that I have incentives to develop them further.

## de.paulr.amalgam: A dataflow programming and code synthesis tool

### Rationale

I think dataflow programming is an underestimated paradigm for programming and domain modeling.

In object-oriented programming, you try to use classes to model concepts in the problem domain. These classes can have instances, each of them representing a *context* in which we can ask questions. For example, we may have a class `User`, and each instance is a context in which we can ask questions such as "What is their name?" or "Do they have permission to do X?", and we can answer these questions by providing things that are called properties and methods.

While I enjoy translating a problem into code using objects, object-oriented projects tend to have more than one class representing the same context, but with differing degrees of knowledge. For example, many backend applications know an "internal user" and an "API user". While both represent the users of the application, the "API user" is a stripped-down variant that lacks sensitive information so that it can safely be returned by a public endpoint. While it is wise to separate these two classes, they create redundancy, complexity and bugs.

If we have multiple "user" classes, we need *converters*, and these converters are often hand-written. When the user domain changes somehow, we need to keep all "user" classes and their converters in sync and we need to keep in mind which classes represent the same kind of context.

Moreover, when classes that represent the same context reference each other to avoid duplication of logic and the innermost class needs an additional piece of information, this information must be added to every single referencing class, similarly to the annoyances of manual constructor injection when trying to manage dependencies between classes.

### Example

See the package `de.paulr.amalgam.example`.

`UserAmalgam` contains the whole domain model of a user in the form of a dataflow graph: A user has several features, namely `firstName`, `lastName`, `fullName`, `birthdayMessage` and `invoiceMessage`. The features are connected via derivation rules that are represented as public static methods. For example, take the method `fullName`.
```java
@Feat("fullName")
public static String fullName(@Feat("firstName") String firstName,
    @Feat("lastName") String lastName) {
    return (firstName + " " + lastName).trim();
}
```
The method and parameter annotations `@Feat("fullName")`, `@Feat("firstName")` and `@Feat("firstName")` tell the amalgam compiler that this method computes `fullName` in terms of `firstName` and `lastName`.

The XML files `User.mlgm` and `UserWithMessages.mlgm` in the resources folder tell the amalgam compiler how to generate classes `User` and `UserWithMessages` from the `UserAmalgam`. The executable class `GenerateClasses` calls the amalgam compiler to generate them. You can see the generated classes in action in the class `Main`.

For example, `User.mlgm` declares that `User` knows about the first and last name of a user. Therefore, the amalgam compiler generates a constructor that takes the first and last name. Because of the `UserAmalgam.fullName` method shown above, the amalgam compiler generates a method `User::getFullName` that determines the full name of a user. Conversely, the factory method `User.ofFullName`, also generated, allows to create a `User` from a full name by determining first and last name from it and calling the constructor.

`UserWithMessages` demonstrates an advanced feature. The method `UserAmalgam.birthdayMessage` is tagged with `"messages"`. Tagged methods are usually ignored by the amalgam compiler, except the tag is specified in the `*.mlgm` file. This is why `User` does not have the `getInvoiceMessage` method while `UserWithMessages` has. When it is called, `User` first determines the full name of the user and then generates an invoice text from it. The constructor of `UserWithMessages` takes a `User`, thereby allowing to convert a `User` to a `UserWithMessages`.

### Ideas and inspiration

- While I'm not sure whether I had it in mind when designing amalgams, they remind me of dependency injection á la Java CDI. While Java CDI helps managing dependencies between *classes*, amalgams help managing dependencies between *functions*.
- One way to look at amalgams is that they are objects that work with partial knowledge. If I only know the first name of a user, I can still determine the birthday message, although I cannot generate the invoice message since it depends on the full name.
- The name "amalgam" *could* be explained this way: An amalgam consists of many parts, but these parts already form a whole. No manual gluing is necessary.
- Integration tests are often slow. If I find the time, I want to write a test framework for amalgams. First, one writes "unit tests" that assert that the individual methods of an amalgam, such as `UserAmalgam.fullName`, return certain values for certain inputs. Second, these assertions are automatically collected to inform mocks of classes derived from amalgams. Third, one writes "integration tests" for the derived classes, only relying on the values provided during the unit-testing phase. It suffices to run these integration tests against the auto-generated mocks because the unit tests have already verified that the individual methods behave correctly for the test cases. Especially when a task is computationally expensive, this way of running integration tests is faster than conventional unit tests.
- Amalgams can be overspecified: It is possible that a feature can be derived in various ways. Given a collection of *examples* (example instances of the amalgam or its classes), one verify the consistency of the amalgam by checking that the different derivations of a feature lead to the same result for all valid examples. For example, sometimes one can either write a program that is easily seen to be correct or a program that is performant, but not both at once. Amalgams allow to build a "scaffolding" of easily verifiable but inefficient implementations that help verifying on example data that the efficient but hard-to-verify implementation is correct. (Inspired by GlamorousToolkit (examples) and property-based testing.)
- An overspecified amalgam gives the amalgam compiler leeway in which methods it calls, and in which order it does, to determine a derived value. There are only finitely many ways to do this. If one annotates features with *memory consumption scores*  and derivation methods with *memory* and *time consumption scores*, it is possible to implement an amalgam compiler that tries to optimize performance to meet certain specified non-functional requirements.
- I am using amalgams in this repository at many places. One limitation is that different amalgam contexts do not play well together. As an example, take the `KnowledgeBaseAmalgam` representing my whole Obsidian personal knowledge base and the `MarkdownPageAmalgam` representing one of its pages. The problem is that these amalgams have circular dependencies and the knowledge base needs to aggregate the pages. A prototypical example is that `KnowledgeBaseAmalgam` has the feature `incomingReferences` but it needs knowledge about the whole knowledge base to determine them. I suspect that this demonstrates the limits of my simple finite computational model called amalgams. I'm curious whether one could somehow usefully extend amalgams with a *relational* model.

