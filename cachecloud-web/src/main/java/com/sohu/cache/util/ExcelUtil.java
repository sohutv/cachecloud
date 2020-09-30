package com.sohu.cache.util;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by rucao on 2018/7/10
 */
public class ExcelUtil {
    private static HSSFWorkbook workbook = null;

    public static List<List<String>> readXlsx(String path) throws Exception {
        InputStream is = new FileInputStream(path);
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook(is);

        List<List<String>> result = new ArrayList<List<String>>();
        // 循环当前页，并处理当前循环页
        XSSFSheet xssfSheet = xssfWorkbook.getSheetAt(0);
        // 循环每一页，并处理当前循环页
//        for (XSSFSheet xssfSheet : xssfWorkbook) {
//            String a = xssfSheet.getSheetName();
//            if (xssfSheet.gets(0) != "月度汇总") {
//                continue;
//            }
        // 处理当前页，循环读取每一行 rowNum:从第几行开始读取（第2行，第1行是表头）

        for (int rowNum = 1; rowNum <= xssfSheet.getLastRowNum(); rowNum++) {
            XSSFRow xssfRow = xssfSheet.getRow(rowNum);
            int minColIx = xssfRow.getFirstCellNum();
            int maxColIx = xssfRow.getLastCellNum();
            List<String> rowList = new ArrayList<String>();
            for (int colIx = minColIx; colIx < maxColIx; colIx++) {
                XSSFCell cell = xssfRow.getCell(colIx);
                if (cell == null) {
                    continue;
                }
                rowList.add(cell.toString());
            }
            result.add(rowList);
        }
//        }
        return result;
    }

    public static void createExcel(String fileDir,String sheetName,String titleRow[]) throws Exception{
        //创建workbook
        workbook = new HSSFWorkbook();
        //新建文件
        FileOutputStream out = null;
        try {
            //添加表头
            HSSFRow row = workbook.getSheet(sheetName).createRow(0);    //创建第一行
            for(short i = 0;i < titleRow.length;i++){
                HSSFCell cell = row.createCell(i);
                cell.setCellValue(titleRow[i]);
            }
            out = new FileOutputStream(fileDir);
            workbook.write(out);
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if(out != null){
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeXls(String path,String sheetName,List<Map> mapList) throws Exception{
        if(mapList==null){
            return;
        }
        //创建workbook
        File file = new File(path);
        try {
            workbook = new HSSFWorkbook(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //流
        FileOutputStream out = null;
        HSSFSheet sheet = workbook.getSheet(sheetName);
        // 获取表格的总行数
        // int rowCount = sheet.getLastRowNum() + 1; // 需要加一
        // 获取表头的列数
        int columnCount = sheet.getRow(0).getLastCellNum();
        try {
            // 获得表头行对象
            HSSFRow titleRow = sheet.getRow(0);
            if(titleRow!=null){
                for(int rowId=0;rowId<mapList.size();rowId++){
                    Map map = mapList.get(rowId);
                    HSSFRow newRow=sheet.createRow(rowId+1);
                    for (short columnIndex = 0; columnIndex < columnCount; columnIndex++) {  //遍历表头
                        String mapKey = titleRow.getCell(columnIndex).toString().trim().toString().trim();
                        HSSFCell cell = newRow.createCell(columnIndex);
                        cell.setCellValue(map.get(mapKey)==null ? null : map.get(mapKey).toString());
                    }
                }
            }

            out = new FileOutputStream(path);
            workbook.write(out);
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if(out != null){
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
