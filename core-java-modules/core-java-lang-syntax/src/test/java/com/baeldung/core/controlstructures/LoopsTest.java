package com.baeldung.core.controlstructures;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;

public class LoopsTest {

    // 测试重复执行50次的方法
    @Test
    public void testRepetitionTo50Examples() {
        // 捕获控制台输出
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        Loops.repetitionTo50Examples();

        // 重置控制台输出
        System.setOut(originalOut);

        // 验证输出行数（for循环50次，while循环50次，do-while循环49次）
        String output = outContent.toString();
        long lineCount = output.split("\n").length;
        assertEquals(149, lineCount);
    }

    // 测试按单词打印句子
    @Test
    public void testPrintWordByWord() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        Loops.printWordByWord("Hello world Java");

        System.setOut(originalOut);

        String output = outContent.toString();
        assertEquals("Hello\nworld\nJava\n", output);
    }

    // 测试查找数组中名字的索引
    @Test
    public void testFindFirstInstanceOfName() {
        String[] names = { "Alice", "Bob", "Charlie", "David" };

        // 测试找到名字
        int index1 = Loops.findFirstInstanceOfName("Charlie", names);
        assertEquals(2, index1);

        // 测试找不到名字
        int index2 = Loops.findFirstInstanceOfName("Zoe", names);
        assertEquals(-1, index2);
    }

    // 测试跳过指定名字生成列表
    @Test
    public void testMakeListSkippingName() {
        String[] names = { "Alice", "Bob", "Charlie" };

        // 测试跳过中间的名字
        String result1 = Loops.makeListSkippingName("Bob", names);
        assertEquals("AliceCharlie", result1);

        // 测试跳过不存在的名字
        String result2 = Loops.makeListSkippingName("Zoe", names);
        assertEquals("AliceBobCharlie", result2);
    }

    // 测试打印N个偶数
    @Test
    public void testPrintEvenNumbers() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        Loops.printEvenNumbers(3);

        System.setOut(originalOut);

        String output = outContent.toString();
        assertEquals("0\n2\n4\n", output);
    }

    // 测试打印文本N次（有bug的版本）
    @Test
    public void testPrintTextNTimes() {
        // 验证当 times = 1 时，只打印一次
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        Loops.printTextNTimes("Test", 1);

        System.setOut(originalOut);

        String output = outContent.toString();
        assertEquals("Test\n", output);

        // 验证当 times > 1 时，会发生无限循环
        // 此测试无法直接验证无限循环，但可以通过分析代码确认
        // 代码中 counter 没有在循环体内递增，所以当 times > 1 时会无限循环
    }

    // 测试打印偶数到最大100
    @Test
    public void testPrintEvenNumbersToAMaxOf100() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        Loops.printEvenNumbersToAMaxOf100(3);

        System.setOut(originalOut);

        String output = outContent.toString();
        assertEquals("0\n2\n4\n", output);
    }
}
