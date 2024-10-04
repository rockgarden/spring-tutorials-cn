# [在 Spring AOP 中获取Advised方法信息](https://www.baeldung.com/spring-aop-get-advised-method-info)

展示如何使用 Spring AOP 方面获取有关方法签名、参数和注释的所有信息。
我们通过定义切入点、将信息打印到控制台并检查运行测试的结果来做到这一点。

## 创建切入点注释

让我们创建一个 AccountOperation 注释。为了澄清，我们将使用它作为我们方面的切入点：

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AccountOperation {
    String operation();
}
```

请注意，创建注释对于定义切入点不是必需的。也就是说，我们可以使用Spring AOP提供的切入点定义语言来定义其他切入点类型，比如类中的某些方法、以某个前缀开头的方法等。

## 创建示例服务

账户类

让我们创建一个具有 accountNumber 和 balance 属性的 methodinfo.Account POJO。我们将在我们的服务方法中使用它作为方法参数。

服务类

现在让我们创建带有两个方法的 methodinfo.BankAccountService 类，我们用 @AccountOperation 注释进行注释，这样我们就可以在我们的切面中获取方法的信息。请注意，withdraw 方法抛出一个检查异常 WithdrawLimitException 来演示我们如何获取有关方法抛出的异常的信息。

另外，请注意 getBalance 方法没有 AccountOperation 注释，因此它不会被切面拦截。

## 定义方面

让我们创建一个 methodinfo.BankAccountAspect 从我们的 BankAccountService 中调用的相关方法中获取所有必要的信息。

请注意，我们将切入点定义为注解，因此由于 BankAccountService 中的 getBalance 方法没有使用 AccountOperation 进行注解，切面不会拦截它。

> 注意 BankAccountAspect 编辑过都要重新加包，不然测试时会报错： `java.lang.NoSuchMethodError: com.baeldung.methodinfo.BankAccountAspect.aspectOf()Lcom/baeldung/methodinfo/BankAccountAspect`
  
### 获取有关方法签名的信息

为了能够获取我们的方法签名信息，我们需要从 JoinPoint 对象中检索 MethodSignature：

```java
MethodSignature signature = (MethodSignature) joinPoint.getSignature();

System.out.println("full method description: " + signature.getMethod());
System.out.println("method name: " + signature.getMethod().getName());
System.out.println("declaring type: " + signature.getDeclaringType());
```

现在让我们调用服务的 withdraw() 方法：

```java
@Test
void withdraw() {
    bankAccountService.withdraw(account, 500.0);
    assertTrue(account.getBalance() == 1500.0);
}
```

运行withdraw() 测试后，我们现在可以在控制台上看到以下结果：

```log
full method description: public void com.baeldung.method.info.BankAccountService.withdraw(com.baeldung.methodinfo.Account,java.lang.Double) throws com.baeldung.methodinfo.WithdrawLimitException
method name: withdraw
declaring type: class com.baeldung.methodinfo.BankAccountService
```

### 获取有关参数的信息

要检索有关方法参数的信息，我们可以使用 MethodSignature 对象：

```java
System.out.println("Method args names:");
Arrays.stream(signature.getParameterNames()).forEach(s -> System.out.println("arg name: " + s));

System.out.println("Method args types:");
Arrays.stream(signature.getParameterTypes()).forEach(s -> System.out.println("arg type: " + s));

System.out.println("Method args values:");
Arrays.stream(joinPoint.getArgs()).forEach(o -> System.out.println("arg value: " + o.toString()));
```

让我们通过调用 BankAccountService 中的 deposit 方法来试试这个：

```java
@Test
void deposit() {
    bankAccountService.deposit(account, 500.0);
    assertTrue(account.getBalance() == 2500.0);
}
```

这是我们在控制台上看到的：

```log
Method args names:
arg name: account
arg name: amount
Method args types:
arg type: class com.baeldung.methodinfo.Account
arg type: class java.lang.Double
Method args values:
arg value: Account{accountNumber='12345', balance=2000.0}
arg value: 500.0
```

### 获取有关方法注释的信息

我们可以通过 Method 类的 getAnnotation() 方法获取注解的信息：

```java
Method method = signature.getMethod();
AccountOperation accountOperation = method.getAnnotation(AccountOperation.class);
System.out.println("Account operation annotation: " + accountOperation);
System.out.println("Account operation value: " + accountOperation.operation());
```

现在让我们重新运行我们的 withdraw() 测试并检查我们得到了什么：

```log
Account operation annotation: @com.baeldung.methodinfo.AccountOperation(operation=withdraw)
Account operation value: withdraw
```

### 获取附加信息

可以获得一些关于方法的额外信息，比如它们的返回类型、它们的修饰符以及它们抛出的异常（如果有的话）：

```java
System.out.println("returning type: " + signature.getReturnType());
System.out.println("method modifier: " + Modifier.toString(signature.getModifiers()));
Arrays.stream(signature.getExceptionTypes())
  .forEach(aClass -> System.out.println("exception type: " + aClass));
```

现在让我们创建一个新的测试 withdrawWhenLimitReached ，使 withdraw() 方法超过其定义的取款限制：

```java
@Test 
void withdrawWhenLimitReached() { 
    Assertions.assertThatExceptionOfType(WithdrawLimitException.class)
      .isThrownBy(() -> bankAccountService.withdraw(account, 600.0)); 
    assertTrue(account.getBalance() == 2000.0); 
}
```

现在让我们检查控制台输出：

```log
....
returning type: void
method modifier: public
exception type: class com.baeldung.methodinfo.WithdrawLimitException
```

我们的最后一个测试将有助于演示 getBalance() 方法。 正如我们之前所说，它不会被切面拦截，因为方法声明中没有 AccountOperation 注解：

```java
@Test
void getBalance() {
    bankAccountService.getBalance();
}
```

运行此测试时，控制台中没有输出，正如我们预期的那样。
