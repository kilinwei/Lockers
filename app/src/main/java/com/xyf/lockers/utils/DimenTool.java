package com.xyf.lockers.utils;

/**
 * @项目名： RobotControllerApp
 * @包名： com.szyh.ewaacontroller.utils
 * @文件名: DimenTool
 * @创建者: kilin
 * @创建时间: 2018/11/30 10:06
 * @描述： TODO
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by cdy on 2016/2/3.
 * 快速生成适配工具类
 */
public class DimenTool {

    public static void gen() {
        //以此文件夹下的dimens.xml文件内容为初始值参照
        File file = new File("./app/src/main/res/values/dimens.xml");

        BufferedReader reader = null;
        StringBuilder sw160 = new StringBuilder();
        StringBuilder sw240 = new StringBuilder();
        StringBuilder sw320 = new StringBuilder();
        StringBuilder sw480 = new StringBuilder();
        StringBuilder sw600 = new StringBuilder();

        StringBuilder sw720 = new StringBuilder();

        StringBuilder sw800 = new StringBuilder();

        StringBuilder w820 = new StringBuilder();

        try {

            System.out.println("生成不同分辨率：");

            reader = new BufferedReader(new FileReader(file));

            String tempString;

            int line = 1;

            // 一次读入一行，直到读入null为文件结束

            while ((tempString = reader.readLine()) != null) {


                if (tempString.contains("</dimen>")) {

                    //tempString = tempString.replaceAll(" ", "");

                    String start = tempString.substring(0, tempString.indexOf(">") + 1);

                    String end = tempString.substring(tempString.lastIndexOf("<") - 2);
                    //截取<dimen></dimen>标签内的内容，从>右括号开始，到左括号减2，取得配置的数字
                    Double num = Double.parseDouble
                            (tempString.substring(tempString.indexOf(">") + 1,
                                    tempString.indexOf("</dimen>") - 2));

                    //根据不同的尺寸，计算新的值，拼接新的字符串，并且结尾处换行。

                    sw160.append(start).append(num).append(end).append("\r\n");

                    sw240.append(start).append(num * 0.75 * 0.5).append(end).append("\r\n");

                    sw320.append(start).append(num * 1 * 0.5).append(end).append("\r\n");

                    sw480.append(start).append(num * 1.5 * 0.5).append(end).append("\r\n");

                    sw600.append(start).append(num * 1.87 * 0.5).append(end).append("\r\n");

                    sw720.append(start).append(num * 2.25 * 0.5).append(end).append("\r\n");

                    sw800.append(start).append(num * 2.5 * 0.5).append(end).append("\r\n");

                    w820.append(start).append(num * 2.56 * 0.5).append(end).append("\r\n");


                } else {
                    sw240.append(tempString).append("");

                    sw480.append(tempString).append("");

                    sw600.append(tempString).append("");

                    sw720.append(tempString).append("");

                    sw800.append(tempString).append("");

                    w820.append(tempString).append("");

                }

                line++;

            }

            reader.close();
            System.out.println("<!--  sw240 -->");

            System.out.println(sw240);

            System.out.println("<!--  sw480 -->");

            System.out.println(sw480);

            System.out.println("<!--  sw600 -->");

            System.out.println(sw600);

            System.out.println("<!--  sw720 -->");

            System.out.println(sw720);

            System.out.println("<!--  sw800 -->");

            System.out.println(sw800);

            String sw160file = "./app/src/main/res/values-sw160dp/dimens.xml";

            String sw240file = "./app/src/main/res/values-sw240dp/dimens.xml";

            String sw320file = "./app/src/main/res/values-sw320dp/dimens.xml";

            String sw480file = "./app/src/main/res/values-sw480dp/dimens.xml";

            String sw600file = "./app/src/main/res/values-sw600dp/dimens.xml";

            String sw720file = "./app/src/main/res/values-sw720dp/dimens.xml";

            String sw800file = "./app/src/main/res/values-sw800dp/dimens.xml";

            String w820file = "./app/src/main/res/values-sw820dp/dimens.xml";
            //将新的内容，写入到指定的文件中去
            writeFile(sw160file, sw160.toString());

            writeFile(sw240file, sw240.toString());

            writeFile(sw320file, sw320.toString());

            writeFile(sw480file, sw480.toString());

            writeFile(sw600file, sw600.toString());

            writeFile(sw720file, sw720.toString());

            writeFile(sw800file, sw800.toString());

            writeFile(w820file, w820.toString());

        } catch (IOException e) {

            e.printStackTrace();

        } finally {

            if (reader != null) {

                try {

                    reader.close();

                } catch (IOException e1) {

                    e1.printStackTrace();

                }

            }

        }

    }


    /**
     * 写入方法
     */

    public static void writeFile(String file, String text) {

        PrintWriter out = null;

        try {

            out = new PrintWriter(new BufferedWriter(new FileWriter(file)));

            out.println(text);

        } catch (IOException e) {

            e.printStackTrace();

        }


        out.close();

    }

    public static void main(String[] args) {
        gen();
    }

    public static void test() {
        String a = "123";
        String b = a;
        b = "456";
        System.out.println("<!--  sw240 -->" + a);
    }

}
