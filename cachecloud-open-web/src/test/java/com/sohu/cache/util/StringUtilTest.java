package com.sohu.cache.util;

import com.sohu.cache.util.StringUtil;
import org.junit.Assert;
import org.junit.Test;

public class StringUtilTest {

  @Test
  public void convertToCamelCaseStringInputNotNullFalseOutputNotNull() {
    // Arrange
    final String inputString = "a_b_c-1_2_3";
    final boolean firstCharacterUppercase = false;
    // Act
    final String retval = StringUtil.convertToCamelCaseString(inputString, firstCharacterUppercase);
    // Assert result
    Assert.assertEquals("aBC123", retval);
  }

  @Test
  public void convertToCamelCaseStringInputNotNullTrueOutputNotNull() {
    // Arrange
    final String inputString = "a_b_c-1_2_3";
    final boolean firstCharacterUppercase = true;
    // Act
    final String retval = StringUtil.convertToCamelCaseString(inputString, firstCharacterUppercase);
    // Assert result
    Assert.assertEquals("ABC123", retval);
  }

  @Test
  public void convertToCamelCaseStringInputNullFalseOutputNull() {
    // Arrange
    final String inputString = null;
    final boolean firstCharacterUppercase = false;
    // Act
    final String retval = StringUtil.convertToCamelCaseString(inputString, firstCharacterUppercase);
    // Assert result
    Assert.assertNull(retval);
  }

  @Test
  public void equalsIgnoreCaseOneInputNotNull0OutputFalse() {
    // Arrange
    final String targetStr = "\"";
    final String[] compareStrArray = {};
    // Act
    final boolean retval = StringUtil.equalsIgnoreCaseOne(targetStr, compareStrArray);
    // Assert result
    Assert.assertEquals(false, retval);
  }

  @Test
  public void equalsIgnoreCaseOneInputNotNull0OutputFalse2() {
    // Arrange
    final String targetStr = "\u001e\u0002\u0002 \u9963\u8020 ";
    final String[] compareStrArray = {};
    // Act
    final boolean retval = StringUtil.equalsIgnoreCaseOne(targetStr, compareStrArray);
    // Assert result
    Assert.assertEquals(false, retval);
  }

  @Test
  public void equalsIgnoreCaseOneInputNotNull1OutputFalse() {
    // Arrange
    final String targetStr = "\u0002\u0002";
    final String[] compareStrArray = {"\u0002\u0002\u000f"};
    // Act
    final boolean retval = StringUtil.equalsIgnoreCaseOne(targetStr, compareStrArray);
    // Assert result
    Assert.assertEquals(false, retval);
  }

  @Test
  public void equalsIgnoreCaseOneInputNotNull1OutputTrue() {
    // Arrange
    final String targetStr = "\u0002\u0002\u0802";
    final String[] compareStrArray = {"\u0002\u0002\u0802"};
    // Act
    final boolean retval = StringUtil.equalsIgnoreCaseOne(targetStr, compareStrArray);
    // Assert result
    Assert.assertEquals(true, retval);
  }

  @Test
  public void equalsIgnoreCaseOneInputNotNullNullOutputFalse() {
    // Arrange
    final String targetStr = "    !A@AA@@*";
    final String[] compareStrArray = null;
    // Act
    final boolean retval = StringUtil.equalsIgnoreCaseOne(targetStr, compareStrArray);
    // Assert result
    Assert.assertEquals(false, retval);
  }

  @Test
  public void equalsIgnoreCaseOneInputNull0OutputFalse() {
    // Arrange
    final String targetStr = null;
    final String[] compareStrArray = {};
    // Act
    final boolean retval = StringUtil.equalsIgnoreCaseOne(targetStr, compareStrArray);
    // Assert result
    Assert.assertEquals(false, retval);
  }

  @Test
  public void subStringIfTooLongInputNotNullNegativeNotNullOutputNotNull() {
    // Arrange
    final String originalStr = "\u001c";
    final int maxLength = -2_147_483_648;
    final String suffix = "\uffdd\uffdd\uffdd\uffdd\uffdd\uffdd\uffdd\uffdd\uffdd";
    // Act
    final String retval = StringUtil.subStringIfTooLong(originalStr, maxLength, suffix);
    // Assert result
    Assert.assertEquals("", retval);
  }

  @Test
  public void subStringIfTooLongInputNotNullPositiveNullOutputNotNull() {
    // Arrange
    final String originalStr = "";
    final int maxLength = 1;
    final String suffix = null;
    // Act
    final String retval = StringUtil.subStringIfTooLong(originalStr, maxLength, suffix);
    // Assert result
    Assert.assertEquals("", retval);
  }

  @Test
  public void subStringIfTooLongInputNullPositiveNotNullOutputNotNull() {
    // Arrange
    final String originalStr = null;
    final int maxLength = 11;
    final String suffix = "                      ]]";
    // Act
    final String retval = StringUtil.subStringIfTooLong(originalStr, maxLength, suffix);
    // Assert result
    Assert.assertEquals("", retval);
  }

  @Test
  public void subStringIfTooLongInputNullPositiveNullOutputNotNull() {
    // Arrange
    final String originalStr = null;
    final int maxLength = 1_073_741_825;
    final String suffix = null;
    // Act
    final String retval = StringUtil.subStringIfTooLong(originalStr, maxLength, suffix);
    // Assert result
    Assert.assertEquals("", retval);
  }
}
