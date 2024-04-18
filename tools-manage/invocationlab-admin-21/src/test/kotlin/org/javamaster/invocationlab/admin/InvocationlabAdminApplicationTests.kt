package org.javamaster.invocationlab.admin

import com.google.gson.Gson
import org.javamaster.invocationlab.admin.InvocationlabAdminApplicationTests.IntPredicate
import org.javamaster.invocationlab.admin.model.erd.CheckboxesVo
import org.javamaster.invocationlab.admin.util.JsonUtils
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.security.SecurityProperties.User
import java.awt.Rectangle
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import javax.inject.Inject

val PI = 3.14

//@SpringBootTest
class InvocationlabAdminApplicationTests {

    @Test
    fun contextLoads() {
        testInterface()
        testProperty()
        testClass()
        testTry("9")
        testLabel()
        testWhen(2)
        testIf(2, 43)
        testDetectType("world")
        testVarParams()
        testArray()
        testExchange()
        testNeedGeneralTypeInfo()
        testCloseResource()
        testApply()
        testWith()
        testNullCall()
        testInstanceAbstract()
        testSingle()
        testExtend()
        lazyProp()
        testIn()
        testFuncDefine()
        testFor()
    }

    private fun testInterface() {
        Child()
        C()
        D()
        val isEven = IntPredicate { it % 2 == 0 }
        println("Is 7 even? - ${isEven.accept(7)}")
    }

    /**
     * 函数式接口
     */
    fun interface IntPredicate {
        fun accept(i: Int): Boolean
    }

    interface A {
        fun foo() {
            print("A")
        }

        fun bar()
    }

    interface B {
        fun foo() {
            print("B")
        }

        fun bar() {
            print("bar")
        }
    }

    class C : A {
        override fun bar() {
            print("bar")
        }
    }

    class D : A, B {
        override fun foo() {
            super<A>.foo()
            super<B>.foo()
        }

        override fun bar() {
            super<B>.bar()
        }
    }

    class Child : MyInterface {
        override fun bar() {
            println("hello")
        }

        override val prop: Int = 29
    }

    interface MyInterface {
        fun bar()

        val prop: Int // 抽象的
        val propertyWithImplementation: String
            get() = "foo"

        fun foo() {
            print(prop)
        }
    }

    private fun testProperty() {
        val result = Address() // Kotlin 中没有“new”关键字
        result.name = result.name // 将调用访问器
    }

    class Address {
        var name: String = "Holmes, Sherlock"
        var state: String? = null
        val zip: String = "123456"
        val addr: String
            get() = this.state + " " + this.zip
        val addr1 get() = this.state + " " + this.zip
        var counter = 0 // 这个初始器直接为幕后字段赋值
            set(value) {
                if (value >= 0)
                    field = value
                // counter = value // ERROR StackOverflow: Using actual name 'counter' would make setter recursive
            }
    }

    private fun testClass() {
        Person()
        Empty()
        Person1("o")
        Person2("p")
        InitOrderDemo("so")
        Customer("sp")
        Customer1("PO")
        Person3("a", "b")
        Person4("dto")
        Rectangle()
        Polygon1()
        object : WildShape() {
            override fun draw() {
            }
        }
        Square()
        User()
    }

    class Person { /*……*/ }
    class Empty
    class Person1 constructor(firstName: String) { /*……*/ }
    class Person2(firstName: String) { /*……*/ }
    class InitOrderDemo(name: String) {
        val firstProperty = "First property: $name".also(::println)

        init {
            println("First initializer block that prints $name")
        }

        val secondProperty = "Second property: ${name.length}".also(::println)

        init {
            println("Second initializer block that prints ${name.length}")
        }
    }

    class Customer(name: String) {
        val customerKey = name.uppercase()
    }

    class Person3(
        val firstName: String,
        val lastName: String,
        var age: Int = 32, // 尾部逗号
    ) { /*……*/ }

    class Customer1 @Inject constructor(name: String) { /*……*/ }
    class Person4(val name: String) {
        val children: MutableList<Person4> = mutableListOf()

        constructor(name: String, parent: Person4) : this(name) {
            parent.children.add(this)
        }
    }

    abstract class Polygon {
        abstract fun draw()
    }

    class Rectangle1 : Polygon() {
        override fun draw() {
// draw the rectangle
        }
    }

    open class Polygon1 {
        open fun draw() {
// some default polygon drawing method
        }
    }

    abstract class WildShape : Polygon1() {
        // Classes that inherit WildShape need to provide their own
// draw method instead of using the default on Polygon
        abstract override fun draw()
    }

    open class Rectangle3 {
        open fun draw() { /* …… */
        }
    }

    interface Polygon3 {
        fun draw() { /* …… */
        } // 接口成员默认就是“open”的
    }

    class Square() : Rectangle3(), Polygon3 {
        // 编译器要求覆盖 draw()：
        override fun draw() {
            super<Rectangle3>.draw() // 调用 Rectangle.draw()
            super<Polygon3>.draw() // 调用 Polygon.draw()
        }
    }

    private fun testTry(input: String) {
        val a: Int? = try {
            input.toInt()
        } catch (e: NumberFormatException) {
            null
        }
        println(a)
    }

    private fun testLabel() {
        loop@ for (i in 1..100) {
            for (j in 1..100) {
                if (j % 2 == 0) break@loop
            }
        }

        listOf(1, 2, 3, 4, 5).forEach {
            if (it == 3) return // 非局部直接返回到 foo() 的调用者
            print(it)
        }
        println("this point is unreachable")

        listOf(1, 2, 3, 4, 5).forEach lit@{
            if (it == 3) return@lit // 局部返回到该 lambda 表达式的调用者——forEach 循环
            print(it)
        }
        print(" done with explicit label")

        listOf(1, 2, 3, 4, 5).forEach {
            if (it == 3) return@forEach // 局部返回到该 lambda 表达式的调用者——forEach 循环
            print(it)
        }
        print(" done with implicit label")

        listOf(1, 2, 3, 4, 5).forEach(fun(value: Int) {
            if (value == 3) return // 局部返回到匿名函数的调用者——forEach 循环
            print(value)
        })
        print(" done with anonymous function")

        run loop@{
            listOf(1, 2, 3, 4, 5).forEach {
                if (it == 3) return@loop // 从传入 run 的 lambda 表达式非局部返回
                print(it)
            }
        }
        print(" done with nested loop")
    }

    private fun testWhen(x: Int) {
        val s = "3"
        when (x) {
            1 -> print("x == 1")
            2 -> print("x == 2")
            s.toInt() -> print("s encodes x")
            in 1..10 -> print("x is in the range")
            !in 10..20 -> print("x is outside the range")
            is Int -> x.toString().startsWith("prefix")
            else -> {
                print("x is neither 1 nor 2")
            }
        }

        val bit = Bit.ONE
        val numericValue = when (bit) {
            Bit.ZERO -> 0
            Bit.ONE -> 1
            // 'else' is not required because all cases are covered
        }
        println(numericValue)
    }

    enum class Bit {
        ZERO, ONE
    }

    private fun testIf(a: Int, b: Int) {
        var max = if (a > b) a else b
        println(max)

        max = if (a > b) {
            print("Choose a")
            a
        } else {
            print("Choose b")
            b
        }
        println(max)
    }

    private fun testDetectType(obj: Any) {
        if (obj is String) {
            print(obj.length)
        }
        if (obj !is String) { // 与 !(obj is String) 相同
            print("Not a String")
        } else {
            print(obj.length)
        }
    }

    private fun testVarParams() {
        val lettersArray = arrayOf("c", "d")
        printAllStrings("a", "b", *lettersArray)
    }

    private fun printAllStrings(vararg strings: String) {
        for (string in strings) {
            print(string)
        }
    }

    private fun testArray() {
        var riversArray = arrayOf("Nile", "Amazon", "Yangtze")
        // 使用 += 赋值操作创建了一个新的 riversArray，
        // 复制了原始元素并添加了“Mississippi”
        riversArray += "Mississippi"
        println(riversArray.joinToString())

        var nullArray: Array<Int?> = arrayOfNulls(3)
        println(nullArray.joinToString())

        nullArray = emptyArray()
        println(nullArray.joinToString())

        val initArray = Array(3) { 0 }
        println(initArray.joinToString())

        val asc = Array(5) { i -> (i * i).toString() }
        asc.forEach { print(it) }

        val simpleArray = arrayOf(1, 2, 3)
        val twoDArray = Array(2) { Array(2) { 0 } }
        // Accesses the element and modifies it
        simpleArray[0] = 10
        twoDArray[0][0] = 2
        // Prints the modified element
        println(simpleArray[0].toString()) // 10
        println(twoDArray[0][0].toString()) // 2
        println(simpleArray.toSet())
        println(simpleArray.toList())

        val anotherArray = arrayOf(1, 2, 3)
        // Compares contents of arrays
        println(simpleArray.contentEquals(anotherArray))

        val exampleArray = IntArray(5)
        println(exampleArray.joinToString())

        val pairArray = arrayOf("apple" to 120, "banana" to 150, "cherry" to 90, "apple" to 140)
        println(pairArray.toMap())
    }

    private fun testExchange() {
        var a = 1
        var b = 2
        a = b.also { b = a }
        println("$a $b")
    }

    private fun testNeedGeneralTypeInfo() {
        val bean = CheckboxesVo()
        bean.menuId = "helloId"
        val res = JsonUtils.jacksonObjectMapper.writeValueAsString(bean)

        val obj = Gson().fromJson<CheckboxesVo>(res)
        println(obj)

        println(Box(3))

        fun demo(strs: Source<String>) {
            val objects: Source<Any> = strs // 这个没问题，因为 T 是一个 out-参数
        }

        fun demo1(x: Comparable<Number>) {
            x.compareTo(1.0) // 1.0 拥有类型 Double，它是 Number 的子类型
            // 因此，可以将 x 赋给类型为 Comparable <Double> 的变量
            val y: Comparable<Double> = x // OK！
        }

        demo(object : Source<String> {
            override fun nextT(): String {
                return "hello"
            }
        })
        demo1(object : Comparable<Number> {
            override fun compareTo(other: Number): Int {
                return this.compareTo(other)
            }
        })
    }

    interface Source<out T> {
        fun nextT(): T
    }

    interface Comparable<in T> {
        operator fun compareTo(other: T): Int
    }

    class Box<T>(t: T) {
        var value = t
    }

    private inline fun <reified T : Any> Gson.fromJson(json: String): T = this.fromJson(
        json,
        T::class.java
    )

    private fun testCloseResource() {
        val stream = Files.newInputStream(Paths.get("settings.gradle.kts"))
        stream.buffered()
            .reader()
            .use { reader ->
                println(reader.readText())
            }
    }

    private fun testApply() {
        val myRectangle = Rectangle().apply {
            width = 2
            height = 23
        }
        println(myRectangle)
    }

    private fun testWith() {
        val myTurtle = Turtle()
        with(myTurtle) { // 画一个 100 像素的正方形
            penDown()
            for (i in 1..4) {
                forward(100.0)
                turn(90.0)
            }
            penUp()
        }
    }

    class Turtle {
        fun penDown() {}
        fun penUp() {}
        fun turn(degrees: Double) {}
        fun forward(pixels: Double) {}
    }

    private fun testNullCall() {
        val files = File("Test").listFiles()
        println(files?.size)
        println(files?.size ?: "empty")
        val filesSize = files?.size ?: run {
            val someSize = 23
            someSize * 2
        }
        println(filesSize)

        val values = mapOf("email" to 23)
        val email = values["email"] ?: throw IllegalStateException("Email is missing!")
        println(email)

        val emails = listOf(1)
        val mainEmail = emails.firstOrNull() ?: ""
        println(mainEmail)

        files?.let {
            println(it)
        }
    }

    private fun testInstanceAbstract() {
        val myObject = object : MyAbstractClass() {
            override fun doSomething() {
                println("hello")
            }

            override fun sleep() {
                println("world")
            }

        }
    }

    abstract class MyAbstractClass {
        abstract fun doSomething()
        abstract fun sleep()
    }

    private fun testSingle() {
        println(Resource.name)
    }

    object Resource {
        val name = "Name"
    }

    private fun testExtend() {
        println("Convert this to camelcase".trimWhitespace())
        val list = listOf(1, 2)
        println(list.lastIndex)
    }

    private val <T> List<T>.lastIndex: Int
        get() = size - 1

    private fun String.trimWhitespace(): String {
        return this.replace(" ", "")

    }

    private fun lazyProp() {
        // 该值仅在首次访问时计算 计算该字符串
        val p: String by lazy {
            "world"
        }
        println(p)
    }

    private fun testIn() {
        val items = setOf("apple", "banana", "kiwifruit")
        when {
            "orange" in items -> println("juicy")
            "apple" in items -> println("apple is fine too")
        }
        if ("apple" in items) {
            println("apple")
        }
    }

    private fun testFuncDefine() {
        fun sum(a: Int, b: Int): Int {
            return a + b
        }
        println(sum(1, 2))
        println(sum1(1, 2))
        println(printSum(1, 2))
        println(printSum1(1, 2))
    }

    private fun sum1(a: Int, b: Int) = a + b

    private fun printSum(a: Int, b: Int): Unit {
        println("sum of $a and $b is ${a + b}")
    }

    private fun printSum1(a: Int, b: Int) {
        println("sum of $a and $b is ${a + b}")
    }

    private fun testFor() {
        val bean = CheckboxesVo()
        bean.defaultValue = listOf(12L, 33L)
        val res = JsonUtils.jacksonObjectMapper.writeValueAsString(bean)
        println(res)

        for (item in bean.defaultValue!!) {
            print("$item ")
        }
        println()

        for (index in bean.defaultValue!!.indices) {
            println("item at $index is ${bean.defaultValue!![index]}")
        }

        for (x in 1..5) {
            print(x)
        }
        println()

        for (x in 1..10 step 2) {
            print(x)
        }
        println()

        for (x in 9 downTo 0 step 3) {
            print(x)
        }
        println()
    }
}
